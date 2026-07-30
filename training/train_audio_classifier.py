"""
ESC-50 环境声音分类训练脚本
============================
功能：基于 ESC-50 数据集（2000个5秒环境声音片段），训练轻量级 CNN 音频分类模型，
      导出为 TFLite 格式供 Android 端使用

数据集：ESC-50（50类环境声音，每类40个样本）
  - 5折交叉验证（fold 1-5）
  - 每折：训练32类(1280样本) + 验证8类(320样本) + 测试10类(400样本)

模型架构：
  输入: 64维 MFCC 特征（128帧 × 64维 → 取均值 = 64维）
  结构: 全连接网络 (128→64→32) → Softmax 50分类
  体积: ~50KB（INT8量化后），适合移动端

特征提取：
  - MFCC（Mel频率倒谱系数）：模拟人耳听觉特性的频谱特征
  - 采样率 22050Hz，每段取128帧 MFCC，64维 → 均值池化 → 64维

使用方式：
  python train_audio_classifier.py

依赖安装：
  pip install librosa tensorflow pandas numpy scikit-learn

输出文件：
  - model/audio_dnn_float32.tflite     (FP32 全精度模型)
  - model/audio_dnn_int8.tflite        (INT8 量化模型)
  - model/audio_label_map.json         (标签映射，50类)
  - model/audio_scaler_params.json     (特征归一化参数)
  - model/audio_training_report.txt    (训练报告)
  - model/audio_training_history.png   (训练曲线图)
"""

import json
import os
import sys
import warnings
from datetime import datetime

import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, f1_score
from sklearn.utils.class_weight import compute_class_weight

warnings.filterwarnings("ignore")
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# ─────────────────────────────────────────────────────────────────────────────
# 配置区域
# ─────────────────────────────────────────────────────────────────────────────

# 路径配置
BASE_DIR = os.path.dirname(__file__)
DATA_DIR = os.path.join(BASE_DIR, "data", "ESC-50-extracted")
AUDIO_DIR = os.path.join(DATA_DIR, "audio")
META_FILE = os.path.join(DATA_DIR, "meta", "esc50.csv")
MODEL_DIR = os.path.join(BASE_DIR, "model")

# 音频参数
SAMPLE_RATE = 22050           # 采样率（Hz）
DURATION_MS = 5000            # 音频时长（ms）
N_MFCC = 64                   # MFCC 特征维度
N_FFT = 2048                  # FFT 窗口大小
HOP_LENGTH = 512              # 帧移
N_MELS = 128                  # Mel 滤波器组数量

# 训练超参数
BATCH_SIZE = 32
EPOCHS = 150
LEARNING_RATE = 0.001
EARLY_STOPPING_PATIENCE = 25
RANDOM_SEED = 42

# 设置随机种子
tf.random.set_seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)


def load_metadata() -> pd.DataFrame:
    """
    加载 ESC-50 元数据（CSV文件）

    返回：
        DataFrame，包含 filename/fold/target/category 列
    """
    if not os.path.exists(META_FILE):
        print(f"[错误] 元数据文件不存在: {META_FILE}")
        sys.exit(1)

    df = pd.read_csv(META_FILE)
    print(f"  元数据加载: {len(df)} 条记录")
    print(f"  类别数: {df['target'].nunique()}")
    print(f"  折数: {df['fold'].nunique()}")
    return df


def extract_mfcc_features(file_path: str) -> np.ndarray:
    """
    从音频文件中提取 MFCC 特征

    MFCC（Mel-Frequency Cepstral Coefficients）：
    - 模拟人耳对频率的非线性感知
    - 将频谱映射到 Mel 刻度，再取对数、做 DCT 变换
    - 是语音/音频分类中最常用的特征之一

    参数：
        file_path: 音频文件路径

    返回：
        (64,) 的 MFCC 均值特征向量
    """
    import librosa

    try:
        # 加载音频，重采样到目标采样率
        y, sr = librosa.load(file_path, sr=SAMPLE_RATE, duration=DURATION_MS / 1000.0)

        # 确保音频长度一致（不足则补零）
        target_len = SAMPLE_RATE * DURATION_MS // 1000
        if len(y) < target_len:
            y = np.pad(y, (0, target_len - len(y)))
        else:
            y = y[:target_len]

        # 提取 MFCC 特征
        # 形状：(n_mfcc, n_frames) = (64, ~128)
        mfcc = librosa.feature.mfcc(
            y=y, sr=sr, n_mfcc=N_MFCC,
            n_fft=N_FFT, hop_length=HOP_LENGTH,
            n_mels=N_MELS
        )

        # 沿时间轴取均值 → (64,) 维特征向量
        mfcc_mean = np.mean(mfcc, axis=1)

        return mfcc_mean.astype(np.float32)

    except Exception as e:
        print(f"  [警告] 特征提取失败 {file_path}: {e}")
        return np.zeros(N_MFCC, dtype=np.float32)


def prepare_dataset(df: pd.DataFrame, test_fold: int = 5) -> tuple:
    """
    准备训练/验证/测试数据集

    ESC-50 自带 5 折交叉验证划分，默认 fold 5 作为测试集，
    fold 1-4 中取 15% 作为验证集

    参数：
        df: 元数据 DataFrame
        test_fold: 用作测试集的折号（默认 5）

    返回：
        (X_train, y_train, X_val, y_val, X_test, y_test, label_encoder, category_names)
    """
    print(f"\n[准备数据集] 测试折: fold {test_fold}")

    # 按 fold 划分
    test_mask = df["fold"] == test_fold
    train_mask = ~test_mask

    df_train = df[train_mask].copy()
    df_test = df[test_mask].copy()

    print(f"  训练+验证: {len(df_train)} 条 (fold 1-4)")
    print(f"  测试: {len(df_test)} 条 (fold {test_fold})")

    # 标签编码
    le = LabelEncoder()
    y_all = le.fit_transform(df["category"].values)
    category_names = le.classes_.tolist()
    num_classes = len(category_names)

    print(f"  类别数: {num_classes}")

    # 提取特征
    print(f"\n[特征提取] 提取 MFCC 特征...")
    X_all = []
    for idx, row in df.iterrows():
        file_path = os.path.join(AUDIO_DIR, row["filename"])
        features = extract_mfcc_features(file_path)
        X_all.append(features)
        if (idx + 1) % 400 == 0:
            print(f"  进度: {idx + 1}/{len(df)}")

    X_all = np.array(X_all, dtype=np.float32)
    print(f"  特征矩阵: {X_all.shape} (样本数 × 特征维度)")

    # 划分数据
    X_train_val = X_all[train_mask.values]
    y_train_val = y_all[train_mask.values]
    X_test = X_all[test_mask.values]
    y_test = y_all[test_mask.values]

    # 从训练集中分出验证集（15%）
    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val,
        test_size=0.1765,  # 0.15 / 0.85 ≈ 0.1765
        random_state=RANDOM_SEED,
        stratify=y_train_val
    )

    print(f"\n  训练集: {len(X_train)} 条")
    print(f"  验证集: {len(X_val)} 条")
    print(f"  测试集: {len(X_test)} 条")

    return X_train, y_train, X_val, y_val, X_test, y_test, le, category_names


def augment_features(X: np.ndarray, y: np.ndarray, noise_std: float = 0.1,
                     augment_factor: int = 2) -> tuple:
    """
    MFCC 特征增强：添加高斯噪声，模拟录音环境变化

    参数：
        X: (N, F) 特征矩阵
        y: (N,) 标签
        noise_std: 噪声标准差
        augment_factor: 增强倍数

    返回：
        (augmented_X, augmented_y)
    """
    X_aug_list = [X]
    y_aug_list = [y]

    for _ in range(augment_factor):
        noise = np.random.normal(0, noise_std, X.shape)
        X_aug_list.append(X + noise)
        y_aug_list.append(y)

    X_aug = np.vstack(X_aug_list)
    y_aug = np.hstack(y_aug_list)
    print(f"  特征增强: {X.shape[0]} → {X_aug.shape[0]} 条 (×{augment_factor + 1})")
    return X_aug, y_aug


def build_model(input_dim: int, num_classes: int) -> keras.Model:
    """
    构建轻量级音频分类神经网络

    架构设计：
    - 输入：64维 MFCC 均值特征
    - 4层全连接：512→256→128→64，逐步降维提取层次化特征
    - BatchNormalization 加速收敛
    - 高 Dropout（0.5）防止过拟合（50类数据量小，需要强正则化）
    - 输出：Softmax 多分类（50类）

    参数：
        input_dim: 输入特征维度（64）
        num_classes: 分类类别数（50）

    返回：
        Keras Model 对象
    """
    l2_reg = 0.001  # L2 正则化系数（50类数据量小，需要更强正则化）

    model = keras.Sequential([
        # 输入层
        layers.Input(shape=(input_dim,), name="mfcc_input"),

        # 隐藏层1: 512 → BN → ReLU → Dropout(0.5)
        layers.Dense(512, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_512"),
        layers.BatchNormalization(name="bn_512"),
        layers.Activation("relu", name="relu_512"),
        layers.Dropout(0.5, name="dropout_512"),

        # 隐藏层2: 256 → BN → ReLU → Dropout(0.5)
        layers.Dense(256, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_256"),
        layers.BatchNormalization(name="bn_256"),
        layers.Activation("relu", name="relu_256"),
        layers.Dropout(0.5, name="dropout_256"),

        # 隐藏层3: 128 → BN → ReLU → Dropout(0.5)
        layers.Dense(128, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_128"),
        layers.BatchNormalization(name="bn_128"),
        layers.Activation("relu", name="relu_128"),
        layers.Dropout(0.5, name="dropout_128"),

        # 隐藏层4: 64 → BN → ReLU → Dropout(0.4)
        layers.Dense(64, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_64"),
        layers.BatchNormalization(name="bn_64"),
        layers.Activation("relu", name="relu_64"),
        layers.Dropout(0.4, name="dropout_64"),

        # 输出层: Softmax 多分类
        layers.Dense(num_classes, activation="softmax", name="output"),
    ], name="AudioClassifierDNN")

    return model


def train_model(X_train, y_train, X_val, y_val, class_weights, num_classes):
    """
    训练音频分类模型

    参数：
        X_train, y_train: 训练数据
        X_val, y_val: 验证数据
        class_weights: 类别权重字典
        num_classes: 类别数

    返回：
        (model, history)
    """
    input_dim = X_train.shape[1]
    model = build_model(input_dim, num_classes)

    # 学习率调度：余弦退火
    steps_per_epoch = len(X_train) // BATCH_SIZE
    total_steps = steps_per_epoch * EPOCHS

    cosine_decay = keras.optimizers.schedules.CosineDecay(
        initial_learning_rate=LEARNING_RATE,
        decay_steps=total_steps,
        alpha=0.01  # 最终学习率 = 初始 × 0.01
    )

    optimizer = keras.optimizers.Adam(learning_rate=cosine_decay)

    model.compile(
        optimizer=optimizer,
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    model.summary()

    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor="val_loss",
            patience=EARLY_STOPPING_PATIENCE,
            restore_best_weights=True,
            verbose=1
        ),
        keras.callbacks.ModelCheckpoint(
            filepath=os.path.join(MODEL_DIR, "audio_dnn_best.keras"),
            monitor="val_accuracy",
            save_best_only=True,
            verbose=1
        ),
        # 注意：ReduceLROnPlateau 与 CosineDecay 学习率调度冲突
        # 使用 CosineDecay 时，学习率不可被回调修改，因此禁用此回调
        # keras.callbacks.ReduceLROnPlateau(
        #     monitor="val_loss",
        #     factor=0.5,
        #     patience=10,
        #     min_lr=1e-6,
        #     verbose=1
        # ),
    ]

    print(f"\n开始训练...")
    print(f"  输入维度: {input_dim}")
    print(f"  输出类别: {num_classes}")
    print(f"  训练样本: {len(X_train)}")
    print(f"  验证样本: {len(X_val)}")
    print(f"  批次大小: {BATCH_SIZE}")
    print(f"  最大轮次: {EPOCHS}")

    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=EPOCHS,
        batch_size=BATCH_SIZE,
        class_weight=class_weights,
        callbacks=callbacks,
        verbose=1
    )

    return model, history


def evaluate_model(model, X_test, y_test):
    """评估模型并返回指标"""
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    y_pred_proba = model.predict(X_test, verbose=0)
    y_pred = np.argmax(y_pred_proba, axis=1)
    f1 = f1_score(y_test, y_pred, average="weighted")

    return {
        "test_loss": test_loss,
        "test_acc": test_acc,
        "f1_score": f1,
        "y_test": y_test,
        "y_pred": y_pred,
    }


def export_tflite_float32(model, output_dir):
    """导出 FP32 全精度 TFLite 模型"""
    os.makedirs(output_dir, exist_ok=True)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "audio_dnn_float32.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    print(f"\n[FP32 TFLite导出] {path}")
    print(f"  模型大小: {os.path.getsize(path) / 1024:.1f} KB")
    return path


def export_tflite_int8(model, X_calib, output_dir):
    """导出 INT8 量化 TFLite 模型"""
    os.makedirs(output_dir, exist_ok=True)

    def representative_dataset():
        for i in range(0, len(X_calib), BATCH_SIZE):
            batch = X_calib[i:i + BATCH_SIZE]
            yield [batch.astype(np.float32)]

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.representative_dataset = representative_dataset
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.inference_input_type = tf.float32
    converter.inference_output_type = tf.float32

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "audio_dnn_int8.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    print(f"\n[INT8量化TFLite导出] {path}")
    print(f"  模型大小: {os.path.getsize(path) / 1024:.1f} KB")
    return path


def export_scaler_params(scaler, output_dir):
    """导出特征归一化参数"""
    os.makedirs(output_dir, exist_ok=True)

    params = {
        "mean": scaler.mean_.tolist(),
        "std": scaler.scale_.tolist(),
        "n_features": scaler.n_features_in_,
        "description": "StandardScaler参数：输入 = (x - mean) / std"
    }

    path = os.path.join(output_dir, "audio_scaler_params.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(params, f, indent=2, ensure_ascii=False)

    print(f"\n[归一化参数导出] {path}")


def export_label_map(category_names, output_dir):
    """导出标签映射表"""
    os.makedirs(output_dir, exist_ok=True)

    label_map = {
        "labels": category_names,
        "num_classes": len(category_names),
        "description": "ESC-50 环境声音类别标签"
    }

    path = os.path.join(output_dir, "audio_label_map.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(label_map, f, indent=2, ensure_ascii=False)

    print(f"[标签映射导出] {path}")


def plot_training_history(history, output_dir):
    """绘制训练曲线图"""
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt

        os.makedirs(output_dir, exist_ok=True)

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

        ax1.plot(history.history["accuracy"], label="训练准确率", linewidth=2)
        ax1.plot(history.history["val_accuracy"], label="验证准确率", linewidth=2)
        ax1.set_title("模型准确率", fontsize=14)
        ax1.set_xlabel("Epoch")
        ax1.set_ylabel("Accuracy")
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        ax2.plot(history.history["loss"], label="训练损失", linewidth=2)
        ax2.plot(history.history["val_loss"], label="验证损失", linewidth=2)
        ax2.set_title("模型损失", fontsize=14)
        ax2.set_xlabel("Epoch")
        ax2.set_ylabel("Loss")
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        path = os.path.join(output_dir, "audio_training_history.png")
        plt.savefig(path, dpi=150, bbox_inches="tight")
        plt.close()
        print(f"\n[训练曲线图] {path}")

    except ImportError:
        print("\n[提示] matplotlib 未安装，跳过训练曲线图绘制")


def generate_report(results, history, category_names, output_dir):
    """生成训练报告"""
    os.makedirs(output_dir, exist_ok=True)

    y_test = results["y_test"]
    y_pred = results["y_pred"]

    report_path = os.path.join(output_dir, "audio_training_report.txt")

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("=" * 60 + "\n")
        f.write("  环境声音分类训练报告 —— ESC-50\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 60 + "\n\n")

        f.write(f"模型架构: 3层全连接网络 (256→128→64)\n")
        f.write(f"特征类型: MFCC (64维均值)\n")
        f.write(f"测试准确率: {results['test_acc']:.4f}\n")
        f.write(f"测试损失:   {results['test_loss']:.4f}\n")
        f.write(f"F1分数:     {results['f1_score']:.4f}\n")
        f.write(f"训练轮次:   {len(history.history['loss'])}\n\n")

        f.write("─" * 60 + "\n")
        f.write("分类报告（Top 10 类别）:\n")
        f.write("─" * 60 + "\n")
        report = classification_report(y_test, y_pred, target_names=category_names, zero_division=0)
        # 只显示前10个类别和有问题的类别
        f.write(report[:3000])
        f.write("\n... (完整报告共50类)\n")

    print(f"\n[训练报告] {report_path}")

    # 打印摘要
    with open(report_path, "r", encoding="utf-8") as f:
        content = f.read()
    print("\n" + content[:1500])


def main():
    """主函数：数据加载 → 特征提取 → 训练 → 评估 → 导出"""
    print("=" * 60)
    print("  环境声音分类训练 —— ESC-50")
    print("=" * 60)

    # ── 1. 加载数据 ──
    print("\n[1/7] 加载元数据...")
    df = load_metadata()

    # ── 2. 准备数据集 ──
    print("\n[2/7] 准备数据集 + 特征提取...")
    X_train, y_train, X_val, y_val, X_test, y_test, le, category_names = prepare_dataset(df, test_fold=5)

    # ── 3. 特征归一化 ──
    print("\n[3/7] 特征归一化...")
    scaler = StandardScaler()
    X_train = scaler.fit_transform(X_train)
    X_val = scaler.transform(X_val)
    X_test = scaler.transform(X_test)

    # 类别权重
    class_weights_array = compute_class_weight(
        class_weight="balanced",
        classes=np.unique(y_train),
        y=y_train
    )
    class_weights = {i: float(w) for i, w in enumerate(class_weights_array)}

    # ── 4. 特征增强 + 训练模型 ──
    print("\n[4/7] 特征增强 + 训练神经网络...")
    X_train, y_train = augment_features(X_train, y_train, noise_std=0.1, augment_factor=2)
    num_classes = len(category_names)
    model, history = train_model(X_train, y_train, X_val, y_val, class_weights, num_classes)

    # ── 5. 评估模型 ──
    print("\n[5/7] 评估模型...")
    results = evaluate_model(model, X_test, y_test)
    print(f"\n  测试准确率: {results['test_acc']:.4f}")
    print(f"  测试损失:   {results['test_loss']:.4f}")
    print(f"  F1分数:     {results['f1_score']:.4f}")

    # ── 6. 导出模型 ──
    print("\n[6/7] 导出模型...")
    output_dir = MODEL_DIR
    os.makedirs(output_dir, exist_ok=True)

    export_label_map(category_names, output_dir)
    export_scaler_params(scaler, output_dir)

    export_tflite_float32(model, output_dir)

    calib_size = min(200, len(X_val))
    export_tflite_int8(model, X_val[:calib_size], output_dir)

    plot_training_history(history, output_dir)

    # ── 7. 生成报告 ──
    print("\n[7/7] 生成训练报告...")
    generate_report(results, history, category_names, output_dir)

    print("\n" + "=" * 60)
    print("  训练完成！")
    print(f"  模型文件位于: {output_dir}")
    print("=" * 60)


if __name__ == "__main__":
    main()