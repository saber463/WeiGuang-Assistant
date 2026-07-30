"""
YAMNet 迁移学习 —— ESC-50 环境声音分类训练脚本
================================================
功能：基于 Google YAMNet 预训练模型（AudioSet 632类/200万+片段），
      提取深度音频嵌入特征，训练轻量分类器实现 50 类环境声音识别

技术路线：
  YAMNet（冻结）→ 1024维嵌入 → 轻量DNN分类器 → 50类输出
  迁移学习优势：YAMNet 已学习丰富的音频语义，只需少量样本即可微调分类器

YAMNet 简介：
  - Google 开源的音频事件检测模型
  - 基于 AudioSet 数据集（632类，超过200万音频片段）训练
  - 输入：16kHz 单声道音频，输出：1024维嵌入 + 521个音频事件分数
  - 论文：https://arxiv.org/abs/1904.00079

移动端部署方案：
  - YAMNet TFLite（~15MB）：运行在设备端提取嵌入特征
  - 分类器 TFLite（~300KB）：运行在设备端做 50 类分类
  - 总推理延迟：约 80-150ms/秒（移动端 CPU）

依赖安装：
  pip install tensorflow tensorflow-hub librosa pandas numpy scikit-learn

输出文件：
  - model/audio_yamnet_classifier_float32.tflite  (FP32 分类器)
  - model/audio_yamnet_classifier_int8.tflite     (INT8 量化分类器)
  - model/audio_yamnet_label_map.json             (标签映射)
  - model/audio_yamnet_scaler_params.json         (归一化参数)
  - model/audio_yamnet_training_report.txt        (训练报告)
  - model/audio_yamnet_training_history.png       (训练曲线)
  - model/audio_yamnet_embeddings.npy             (预提取嵌入缓存)
"""

import json
import os
import sys
import time
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
import tensorflow_hub as hub
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

# YAMNet 模型 URL（Google TF Hub）
YAMNET_URL = "https://tfhub.dev/google/yamnet/1"

# 音频参数（YAMNet 要求 16kHz 单声道）
SAMPLE_RATE = 16000              # YAMNet 标准采样率
DURATION_SECONDS = 5.0           # ESC-50 音频时长（秒）

# 训练超参数
BATCH_SIZE = 32
EPOCHS = 200
LEARNING_RATE = 0.001
EARLY_STOPPING_PATIENCE = 30
RANDOM_SEED = 42
EMBEDDING_DIM = 1024             # YAMNet 嵌入维度

# 设置随机种子
tf.random.set_seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)


# ─────────────────────────────────────────────────────────────────────────────
# 第1步：加载 YAMNet 模型
# ─────────────────────────────────────────────────────────────────────────────

def load_yamnet():
    """
    从 TF Hub 加载 YAMNet 预训练模型

    YAMNet 输入/输出：
      - 输入：(N,) 的 float32 音频波形，采样率 16kHz
      - 输出：scores (N, 521) 音频事件分数 + embeddings (N, 1024) 深度嵌入
      - 嵌入是倒数第二层的输出，适合作为迁移学习的特征

    返回：
        yamnet_model: TF Hub KerasLayer，可直接用于推断
    """
    print(f"[加载YAMNet] 从 TF Hub 加载预训练模型...")
    t0 = time.time()
    yamnet_model = hub.load(YAMNET_URL)
    print(f"  加载完成，耗时 {time.time() - t0:.1f}s")
    return yamnet_model


# ─────────────────────────────────────────────────────────────────────────────
# 第2步：加载元数据
# ─────────────────────────────────────────────────────────────────────────────

def load_metadata() -> pd.DataFrame:
    """
    加载 ESC-50 元数据

    返回：
        DataFrame，包含 filename/fold/target/category/esc10/src_file/take 列
    """
    if not os.path.exists(META_FILE):
        print(f"[错误] 元数据文件不存在: {META_FILE}")
        sys.exit(1)

    df = pd.read_csv(META_FILE)
    print(f"  元数据: {len(df)} 条, {df['target'].nunique()} 类, {df['fold'].nunique()} 折")
    return df


# ─────────────────────────────────────────────────────────────────────────────
# 第3步：批量提取 YAMNet 嵌入特征
# ─────────────────────────────────────────────────────────────────────────────

def extract_yamnet_embeddings(yamnet_model, df: pd.DataFrame, cache_path: str) -> np.ndarray:
    """
    使用 YAMNet 批量提取所有音频文件的嵌入特征，并缓存到磁盘

    YAMNet 处理流程：
      1. librosa 加载音频 → 16kHz 单声道 float32
      2. 送入 YAMNet → 得到 scores (521维) + embeddings (1024维)
      3. 对 embeddings 沿时间轴取均值 → 1024维固定长度特征向量

    参数：
        yamnet_model: 已加载的 YAMNet TF Hub 模型
        df: 元数据 DataFrame
        cache_path: 嵌入缓存文件路径（.npy）

    返回：
        (N, 1024) 的嵌入特征矩阵
    """
    # 如果缓存存在，直接加载
    if os.path.exists(cache_path):
        print(f"  [缓存命中] 从 {cache_path} 加载预提取嵌入...")
        embeddings = np.load(cache_path)
        print(f"  嵌入矩阵: {embeddings.shape}")
        return embeddings

    print(f"\n[提取YAMNet嵌入] 处理 {len(df)} 个音频文件...")
    print(f"  预计耗时: ~{len(df) * 0.15:.0f}s（每文件约150ms）")

    embeddings_list = []
    t0 = time.time()

    for idx, row in df.iterrows():
        file_path = os.path.join(AUDIO_DIR, row["filename"])

        try:
            # 加载音频并重采样到 16kHz
            import librosa
            y, sr = librosa.load(file_path, sr=SAMPLE_RATE, duration=DURATION_SECONDS)

            # 确保长度一致（不足则补零，超出则截断）
            target_len = int(SAMPLE_RATE * DURATION_SECONDS)
            if len(y) < target_len:
                y = np.pad(y, (0, target_len - len(y)))
            else:
                y = y[:target_len]

            # YAMNet 推理：输入 (N,) 波形，输出 3 个值
            #   [0] scores: (T, 521) 每帧的音频事件分数
            #   [1] embeddings: (T, 1024) 每帧的深度嵌入
            #   [2] spectrogram: (T, 64) log-mel 频谱图
            scores, emb, spectrogram = yamnet_model(y)

            # 沿时间轴取均值 → 固定维度 (1024,)
            emb_mean = np.mean(emb.numpy(), axis=0)
            embeddings_list.append(emb_mean)

        except Exception as e:
            print(f"  [警告] 处理失败 {row['filename']}: {e}")
            embeddings_list.append(np.zeros(EMBEDDING_DIM, dtype=np.float32))

        # 进度显示
        if (idx + 1) % 200 == 0:
            elapsed = time.time() - t0
            speed = (idx + 1) / elapsed
            remaining = (len(df) - idx - 1) / speed
            print(f"  进度: {idx + 1}/{len(df)} | 速度: {speed:.1f}文件/秒 | 剩余: {remaining:.0f}s")

    embeddings = np.array(embeddings_list, dtype=np.float32)
    print(f"  提取完成: {embeddings.shape} | 总耗时: {time.time() - t0:.1f}s")

    # 缓存到磁盘
    np.save(cache_path, embeddings)
    print(f"  嵌入已缓存到: {cache_path}")

    return embeddings


# ─────────────────────────────────────────────────────────────────────────────
# 第4步：准备数据集
# ─────────────────────────────────────────────────────────────────────────────

def prepare_dataset(df: pd.DataFrame, embeddings: np.ndarray, test_fold: int = 5) -> tuple:
    """
    准备训练/验证/测试数据集

    ESC-50 自带 5 折交叉验证划分，默认 fold 5 作为测试集

    参数：
        df: 元数据 DataFrame
        embeddings: 预提取的 YAMNet 嵌入 (N, 1024)
        test_fold: 用作测试集的折号（默认 5）

    返回：
        (X_train, y_train, X_val, y_val, X_test, y_test, label_encoder, category_names)
    """
    print(f"\n[准备数据集] 测试折: fold {test_fold}")

    # 按 fold 划分
    test_mask = df["fold"].values == test_fold
    train_mask = ~test_mask

    # 标签编码
    le = LabelEncoder()
    y_all = le.fit_transform(df["category"].values)
    category_names = le.classes_.tolist()
    num_classes = len(category_names)

    # 划分数据
    X_train_val = embeddings[train_mask]
    y_train_val = y_all[train_mask]
    X_test = embeddings[test_mask]
    y_test = y_all[test_mask]

    # 从训练集中分出验证集（~15%）
    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val,
        test_size=0.1765,  # 0.15 / 0.85
        random_state=RANDOM_SEED,
        stratify=y_train_val
    )

    print(f"  训练集: {len(X_train)} 条 | 验证集: {len(X_val)} 条 | 测试集: {len(X_test)} 条")
    print(f"  类别数: {num_classes}")

    return X_train, y_train, X_val, y_val, X_test, y_test, le, category_names


# ─────────────────────────────────────────────────────────────────────────────
# 第5步：特征增强
# ─────────────────────────────────────────────────────────────────────────────

def augment_embeddings(X: np.ndarray, y: np.ndarray, noise_std: float = 0.05,
                       augment_factor: int = 3) -> tuple:
    """
    嵌入向量增强：添加小幅度高斯噪声，模拟录音环境变化

    注意：噪声标准差较小（0.05），因为 YAMNet 嵌入已经高度抽象，
          过大的噪声会破坏语义信息

    参数：
        X: (N, 1024) 嵌入矩阵
        y: (N,) 标签
        noise_std: 噪声标准差（默认 0.05）
        augment_factor: 增强倍数（默认 3，即生成 3 倍增强数据）

    返回：
        (augmented_X, augmented_y)
    """
    X_aug_list = [X]
    y_aug_list = [y]

    for i in range(augment_factor):
        noise = np.random.normal(0, noise_std, X.shape).astype(np.float32)
        X_aug_list.append(X + noise)
        y_aug_list.append(y)

    X_aug = np.vstack(X_aug_list)
    y_aug = np.hstack(y_aug_list)
    print(f"  嵌入增强: {X.shape[0]} → {X_aug.shape[0]} 条 (×{augment_factor + 1})")
    return X_aug, y_aug


# ─────────────────────────────────────────────────────────────────────────────
# 第6步：构建分类器
# ─────────────────────────────────────────────────────────────────────────────

def build_classifier(input_dim: int, num_classes: int) -> keras.Model:
    """
    构建轻量级分类器（在 YAMNet 嵌入之上）

    架构设计理念：
      - YAMNet 嵌入已经高度抽象，2层全连接足够
      - 2层全连接逐步降维：256→128→50（~300K参数，适配1320样本）
      - BatchNormalization 稳定训练
      - 高 Dropout（0.5）防止过拟合（50类样本量小，需要强正则化）
      - L2 正则化（0.001）约束权重

    参数：
        input_dim: 输入维度（1024 = YAMNet 嵌入维度）
        num_classes: 输出类别数（50）

    返回：
        Keras Sequential 模型
    """
    l2_reg = 0.001  # 强 L2 正则化

    model = keras.Sequential([
        # 输入层
        layers.Input(shape=(input_dim,), name="yamnet_embedding"),

        # 隐藏层1: 256 → BN → ReLU → Dropout(0.5)
        layers.Dense(256, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_256"),
        layers.BatchNormalization(name="bn_256"),
        layers.Activation("relu", name="relu_256"),
        layers.Dropout(0.5, name="dropout_256"),

        # 隐藏层2: 128 → BN → ReLU → Dropout(0.5)
        layers.Dense(128, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_128"),
        layers.BatchNormalization(name="bn_128"),
        layers.Activation("relu", name="relu_128"),
        layers.Dropout(0.5, name="dropout_128"),

        # 输出层: Softmax 50类
        layers.Dense(num_classes, activation="softmax", name="output"),
    ], name="YAMNetClassifier")

    return model


# ─────────────────────────────────────────────────────────────────────────────
# 第7步：训练分类器
# ─────────────────────────────────────────────────────────────────────────────

def train_classifier(X_train, y_train, X_val, y_val, class_weights, num_classes):
    """
    训练 YAMNet 嵌入之上的分类器

    训练策略：
      - 余弦退火学习率调度：从 0.001 衰减到 0.00001
      - EarlyStopping：验证损失 30 轮不改善则停止
      - ModelCheckpoint：保存验证准确率最高的模型
      - 类别权重：平衡各类别样本不均匀的影响

    参数：
        X_train, y_train: 训练数据
        X_val, y_val: 验证数据
        class_weights: 类别权重字典
        num_classes: 类别数

    返回：
        (model, history)
    """
    input_dim = X_train.shape[1]
    model = build_classifier(input_dim, num_classes)

    # 余弦退火学习率调度
    steps_per_epoch = max(1, len(X_train) // BATCH_SIZE)
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

    # 回调函数
    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor="val_loss",
            patience=EARLY_STOPPING_PATIENCE,
            restore_best_weights=True,
            verbose=1
        ),
        keras.callbacks.ModelCheckpoint(
            filepath=os.path.join(MODEL_DIR, "audio_yamnet_classifier_best.keras"),
            monitor="val_accuracy",
            save_best_only=True,
            verbose=1
        ),
    ]

    print(f"\n[开始训练]")
    print(f"  输入维度: {input_dim}")
    print(f"  输出类别: {num_classes}")
    print(f"  训练样本: {len(X_train)}（含增强）")
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


# ─────────────────────────────────────────────────────────────────────────────
# 第8步：评估模型
# ─────────────────────────────────────────────────────────────────────────────

def evaluate_model(model, X_test, y_test, category_names, output_dir):
    """
    全面评估模型性能

    评估指标：
      - 准确率（Accuracy）
      - F1 分数（加权平均）
      - 分类报告（每类 precision/recall/f1）
      - 混淆矩阵（找出最容易混淆的类别对）

    参数：
        model: 训练好的模型
        X_test, y_test: 测试数据
        category_names: 类别名称列表
        output_dir: 输出目录

    返回：
        评估结果字典
    """
    print(f"\n[评估模型]")

    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    y_pred_proba = model.predict(X_test, verbose=0)
    y_pred = np.argmax(y_pred_proba, axis=1)
    f1 = f1_score(y_test, y_pred, average="weighted")

    print(f"  测试准确率: {test_acc:.4f} ({test_acc * 100:.1f}%)")
    print(f"  测试损失:   {test_loss:.4f}")
    print(f"  F1分数:     {f1:.4f}")

    # 每类准确率
    print(f"\n  各类别准确率:")
    cm = confusion_matrix(y_test, y_pred)
    per_class_acc = cm.diagonal() / cm.sum(axis=1)
    for i, (name, acc) in enumerate(zip(category_names, per_class_acc)):
        bar = "█" * int(acc * 20) + "░" * (20 - int(acc * 20))
        print(f"    {name:<25s} {bar} {acc:.1%}")

    return {
        "test_loss": test_loss,
        "test_acc": test_acc,
        "f1_score": f1,
        "y_test": y_test,
        "y_pred": y_pred,
        "per_class_acc": per_class_acc,
    }


# ─────────────────────────────────────────────────────────────────────────────
# 第9步：导出 TFLite 模型
# ─────────────────────────────────────────────────────────────────────────────

def export_tflite_float32(model, output_dir):
    """
    导出 FP32 全精度 TFLite 分类器

    参数：
        model: Keras 模型
        output_dir: 输出目录

    返回：
        导出文件路径
    """
    os.makedirs(output_dir, exist_ok=True)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "audio_yamnet_classifier_float32.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    size_kb = os.path.getsize(path) / 1024
    print(f"\n[FP32 TFLite] {path}")
    print(f"  模型大小: {size_kb:.1f} KB")

    # 验证 TFLite 模型
    interpreter = tf.lite.Interpreter(model_path=path)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    print(f"  输入: {input_details[0]['shape']} ({input_details[0]['dtype']})")
    print(f"  输出: {output_details[0]['shape']} ({output_details[0]['dtype']})")

    return path


def export_tflite_int8(model, X_calib, output_dir):
    """
    导出 INT8 量化 TFLite 分类器（更小体积、更快推理）

    参数：
        model: Keras 模型
        X_calib: 校准数据集（用于确定量化范围）
        output_dir: 输出目录

    返回：
        导出文件路径
    """
    os.makedirs(output_dir, exist_ok=True)

    def representative_dataset():
        for i in range(0, min(len(X_calib), 200), 8):
            batch = X_calib[i:i + 8]
            yield [batch.astype(np.float32)]

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.representative_dataset = representative_dataset
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.inference_input_type = tf.float32
    converter.inference_output_type = tf.float32

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "audio_yamnet_classifier_int8.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    size_kb = os.path.getsize(path) / 1024
    print(f"\n[INT8 TFLite] {path}")
    print(f"  模型大小: {size_kb:.1f} KB")

    return path


def export_label_map(category_names, output_dir):
    """
    导出标签映射表（JSON格式，供 Android 端读取）

    参数：
        category_names: 类别名称列表
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    label_map = {
        "labels": category_names,
        "num_classes": len(category_names),
        "model_type": "YAMNet迁移学习分类器",
        "embedding_dim": EMBEDDING_DIM,
        "description": "ESC-50 环境声音类别标签（YAMNet迁移学习版）"
    }

    path = os.path.join(output_dir, "audio_yamnet_label_map.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(label_map, f, indent=2, ensure_ascii=False)

    print(f"[标签映射] {path}")


def export_scaler_params(scaler, output_dir):
    """
    导出特征归一化参数（StandardScaler 的 mean 和 std）

    参数：
        scaler: 已拟合的 StandardScaler
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    params = {
        "mean": scaler.mean_.tolist(),
        "std": scaler.scale_.tolist(),
        "n_features": int(scaler.n_features_in_),
        "description": "StandardScaler归一化参数：input = (x - mean) / std"
    }

    path = os.path.join(output_dir, "audio_yamnet_scaler_params.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(params, f, indent=2, ensure_ascii=False)

    print(f"[归一化参数] {path}")


# ─────────────────────────────────────────────────────────────────────────────
# 第10步：生成训练报告
# ─────────────────────────────────────────────────────────────────────────────

def plot_training_history(history, output_dir):
    """
    绘制训练曲线图（准确率 + 损失）

    参数：
        history: Keras History 对象
        output_dir: 输出目录
    """
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt

        os.makedirs(output_dir, exist_ok=True)

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

        # 准确率曲线
        ax1.plot(history.history["accuracy"], label="训练准确率", linewidth=2)
        ax1.plot(history.history["val_accuracy"], label="验证准确率", linewidth=2)
        ax1.set_title("YAMNet 迁移学习 —— 模型准确率", fontsize=14)
        ax1.set_xlabel("Epoch")
        ax1.set_ylabel("Accuracy")
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        # 损失曲线
        ax2.plot(history.history["loss"], label="训练损失", linewidth=2)
        ax2.plot(history.history["val_loss"], label="验证损失", linewidth=2)
        ax2.set_title("YAMNet 迁移学习 —— 模型损失", fontsize=14)
        ax2.set_xlabel("Epoch")
        ax2.set_ylabel("Loss")
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        path = os.path.join(output_dir, "audio_yamnet_training_history.png")
        plt.savefig(path, dpi=150, bbox_inches="tight")
        plt.close()
        print(f"\n[训练曲线] {path}")

    except ImportError:
        print("\n[提示] matplotlib 未安装，跳过训练曲线图")


def generate_report(results, history, category_names, output_dir):
    """
    生成详细的训练报告（TXT格式）

    报告内容：
      - 模型架构概览
      - 测试性能指标
      - 各类别准确率明细
      - 分类报告（precision/recall/f1）

    参数：
        results: evaluate_model 返回的结果字典
        history: Keras History 对象
        category_names: 类别名称列表
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    y_test = results["y_test"]
    y_pred = results["y_pred"]
    per_class_acc = results["per_class_acc"]

    report_path = os.path.join(output_dir, "audio_yamnet_training_report.txt")

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("=" * 70 + "\n")
        f.write("  YAMNet 迁移学习 —— ESC-50 环境声音分类训练报告\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 70 + "\n\n")

        # 模型信息
        f.write("【模型架构】\n")
        f.write(f"  预训练模型: YAMNet（Google AudioSet，632类/200万+片段）\n")
        f.write(f"  特征维度:   {EMBEDDING_DIM}维（YAMNet 嵌入）\n")
        f.write(f"  分类器:     2层全连接网络 (256→128→50)\n")
        f.write(f"  正则化:     L2(0.001) + Dropout(0.5) + BN\n")
        f.write(f"  训练策略:   余弦退火学习率 + EarlyStopping + 类别权重\n\n")

        # 性能指标
        f.write("【测试性能】\n")
        f.write(f"  准确率:     {results['test_acc']:.4f} ({results['test_acc'] * 100:.1f}%)\n")
        f.write(f"  测试损失:   {results['test_loss']:.4f}\n")
        f.write(f"  F1分数:     {results['f1_score']:.4f}\n")
        f.write(f"  训练轮次:   {len(history.history['loss'])}\n\n")

        # 与 MFCC 方案对比
        f.write("【与 MFCC 方案对比】\n")
        f.write(f"  MFCC+DNN 准确率: 33.5%\n")
        f.write(f"  YAMNet 迁移学习:  {results['test_acc'] * 100:.1f}%\n")
        f.write(f"  提升幅度:         {results['test_acc'] * 100 - 33.5:.1f}个百分点\n\n")

        # 各类别准确率
        f.write("─" * 70 + "\n")
        f.write("【各类别准确率明细】\n")
        f.write("─" * 70 + "\n")
        for i, (name, acc) in enumerate(zip(category_names, per_class_acc)):
            bar = "█" * int(acc * 20) + "░" * (20 - int(acc * 20))
            f.write(f"  {i:2d}. {name:<25s} {bar} {acc:.1%}\n")

        # 分类报告
        f.write("\n" + "─" * 70 + "\n")
        f.write("【分类报告】\n")
        f.write("─" * 70 + "\n")
        report = classification_report(
            y_test, y_pred,
            target_names=category_names,
            zero_division=0
        )
        f.write(report)

        # 最易混淆的类别对
        f.write("\n" + "─" * 70 + "\n")
        f.write("【最易混淆的 Top 10 类别对】\n")
        f.write("─" * 70 + "\n")
        cm = confusion_matrix(y_test, y_pred)
        # 将对角线置零，找最大的非对角线元素
        cm_no_diag = cm.copy()
        np.fill_diagonal(cm_no_diag, 0)
        # 获取上三角部分（避免重复）
        triu_indices = np.triu_indices_from(cm_no_diag)
        confusion_pairs = []
        for i, j in zip(*triu_indices):
            if cm_no_diag[i, j] > 0:
                confusion_pairs.append((cm_no_diag[i, j], i, j))
        confusion_pairs.sort(reverse=True)
        for rank, (count, i, j) in enumerate(confusion_pairs[:10], 1):
            f.write(f"  {rank:2d}. {category_names[i]} ↔ {category_names[j]}: {count}次混淆\n")

    print(f"\n[训练报告] {report_path}")

    # 打印报告摘要
    with open(report_path, "r", encoding="utf-8") as f:
        content = f.read()
    # 打印前面部分
    print("\n" + content[:content.index("【分类报告】")] + "...\n")


# ─────────────────────────────────────────────────────────────────────────────
# 单折训练 + 评估（被 main 和 main_cv 共用）
# ─────────────────────────────────────────────────────────────────────────────

def run_single_fold(df, embeddings, test_fold, category_names, y_all, verbose=True):
    """
    执行单折训练 + 评估

    参数：
        df: 元数据 DataFrame
        embeddings: 预提取的 YAMNet 嵌入 (N, 1024)
        test_fold: 测试折号 (1-5)
        category_names: 类别名称列表
        y_all: 全部标签（已编码）
        verbose: 是否打印详细日志

    返回：
        (results, model, scaler) 或 None（失败时）
    """
    fold_name = f"Fold {test_fold}"
    if verbose:
        print(f"\n{'─' * 60}")
        print(f"  {fold_name}: 测试折 = {test_fold}")
        print(f"{'─' * 60}")

    # 划分数据
    test_mask = df["fold"].values == test_fold
    train_mask = ~test_mask

    X_train_val = embeddings[train_mask]
    y_train_val = y_all[train_mask]
    X_test = embeddings[test_mask]
    y_test = y_all[test_mask]

    # 验证集划分
    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val,
        test_size=0.1765,
        random_state=RANDOM_SEED,
        stratify=y_train_val
    )

    # 归一化
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

    # 增强
    X_train, y_train = augment_embeddings(X_train, y_train, noise_std=0.03, augment_factor=2)

    # 训练
    num_classes = len(category_names)
    model, history = train_classifier(X_train, y_train, X_val, y_val, class_weights, num_classes)

    # 评估
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    y_pred_proba = model.predict(X_test, verbose=0)
    y_pred = np.argmax(y_pred_proba, axis=1)
    f1 = f1_score(y_test, y_pred, average="weighted")

    if verbose:
        print(f"  {fold_name} 结果: acc={test_acc:.4f} ({test_acc*100:.1f}%), f1={f1:.4f}, loss={test_loss:.4f}")

    return {
        "fold": test_fold,
        "test_acc": float(test_acc),
        "test_loss": float(test_loss),
        "f1_score": float(f1),
        "y_test": y_test,
        "y_pred": y_pred,
        "history": history,
        "model": model,
        "scaler": scaler,
    }, model, scaler


# ─────────────────────────────────────────────────────────────────────────────
# 5折交叉验证
# ─────────────────────────────────────────────────────────────────────────────

def main_cv():
    """
    5折交叉验证主流程：
      - 对 fold 1-5 各执行一次训练+测试
      - 汇总平均准确率、F1、标准差
      - 生成交叉验证报告
      - 用全量数据训练最终模型并导出
    """
    print("=" * 70)
    print("  YAMNet 迁移学习 —— ESC-50 5折交叉验证")
    print("=" * 70)

    # ── 1. 加载 YAMNet ──
    print("\n[1/3] 加载 YAMNet 预训练模型...")
    yamnet_model = load_yamnet()

    # ── 2. 加载元数据 + 嵌入 ──
    print("\n[2/3] 加载数据 + 提取嵌入...")
    df = load_metadata()
    cache_path = os.path.join(MODEL_DIR, "audio_yamnet_embeddings.npy")
    embeddings = extract_yamnet_embeddings(yamnet_model, df, cache_path)

    # 标签编码
    le = LabelEncoder()
    y_all = le.fit_transform(df["category"].values)
    category_names = le.classes_.tolist()

    # ── 3. 逐折训练+评估 ──
    print(f"\n[3/3] 开始 5 折交叉验证...")
    t0 = time.time()

    fold_results = []
    for fold in range(1, 6):
        result, model, scaler = run_single_fold(
            df, embeddings, fold, category_names, y_all, verbose=True
        )
        fold_results.append(result)

    cv_time = time.time() - t0

    # ── 汇总统计 ──
    accs = [r["test_acc"] for r in fold_results]
    f1s = [r["f1_score"] for r in fold_results]
    losses = [r["test_loss"] for r in fold_results]

    cv_mean_acc = np.mean(accs)
    cv_std_acc = np.std(accs)
    cv_mean_f1 = np.mean(f1s)
    cv_std_f1 = np.std(f1s)
    cv_mean_loss = np.mean(losses)

    # ── 打印汇总 ──
    print(f"\n{'=' * 70}")
    print(f"  5折交叉验证结果汇总")
    print(f"{'=' * 70}")
    print(f"  {'折号':<8} {'准确率':<12} {'F1分数':<12} {'损失':<12}")
    print(f"  {'─' * 44}")
    for r in fold_results:
        print(f"  Fold {r['fold']:<3}  {r['test_acc']*100:.1f}%        {r['f1_score']:.4f}       {r['test_loss']:.4f}")
    print(f"  {'─' * 44}")
    print(f"  平均       {cv_mean_acc*100:.1f}% ±{cv_std_acc*100:.1f}%  {cv_mean_f1:.4f} ±{cv_std_f1:.4f}  {cv_mean_loss:.4f}")
    print(f"  总耗时: {cv_time:.0f}s")
    print(f"{'=' * 70}")

    # ── 生成 CV 报告 ──
    output_dir = MODEL_DIR
    os.makedirs(output_dir, exist_ok=True)

    cv_report_path = os.path.join(output_dir, "audio_yamnet_cv_report.txt")
    with open(cv_report_path, "w", encoding="utf-8") as f:
        f.write("=" * 70 + "\n")
        f.write("  YAMNet 迁移学习 —— ESC-50 5折交叉验证报告\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 70 + "\n\n")

        f.write("【模型架构】\n")
        f.write(f"  预训练模型: YAMNet（Google AudioSet，632类/200万+片段）\n")
        f.write(f"  特征维度:   {EMBEDDING_DIM}维（YAMNet 嵌入）\n")
        f.write(f"  分类器:     2层全连接网络 (256→128→50)\n")
        f.write(f"  正则化:     L2(0.001) + Dropout(0.5) + BN\n\n")

        f.write("【5折交叉验证结果】\n")
        f.write(f"  {'折号':<8} {'准确率':<12} {'F1分数':<12} {'损失':<12}\n")
        f.write(f"  {'─' * 44}\n")
        for r in fold_results:
            f.write(f"  Fold {r['fold']:<3}  {r['test_acc']*100:.1f}%        {r['f1_score']:.4f}       {r['test_loss']:.4f}\n")
        f.write(f"  {'─' * 44}\n")
        f.write(f"  平均        {cv_mean_acc*100:.1f}% ±{cv_std_acc*100:.1f}%  {cv_mean_f1:.4f} ±{cv_std_f1:.4f}  {cv_mean_loss:.4f}\n\n")

        f.write("【统计摘要】\n")
        f.write(f"  最高准确率: {max(accs)*100:.1f}% (Fold {accs.index(max(accs))+1})\n")
        f.write(f"  最低准确率: {min(accs)*100:.1f}% (Fold {accs.index(min(accs))+1})\n")
        f.write(f"  标准差:     {cv_std_acc*100:.1f}%\n")
        f.write(f"  总耗时:     {cv_time:.0f}s\n\n")

        # 各类别平均准确率（汇总所有 fold）
        f.write("─" * 70 + "\n")
        f.write("【各类别 5 折平均准确率】\n")
        f.write("─" * 70 + "\n")
        per_class_all = np.zeros((len(category_names), 5))
        for fi, r in enumerate(fold_results):
            cm = confusion_matrix(r["y_test"], r["y_pred"], labels=range(len(category_names)))
            per_class = cm.diagonal() / (cm.sum(axis=1) + 1e-8)
            per_class_all[:, fi] = per_class

        per_class_mean = per_class_all.mean(axis=1)
        per_class_std = per_class_all.std(axis=1)

        for i, (name, mean_acc, std_acc) in enumerate(zip(category_names, per_class_mean, per_class_std)):
            bar = "█" * int(mean_acc * 20) + "░" * (20 - int(mean_acc * 20))
            f.write(f"  {i:2d}. {name:<25s} {bar} {mean_acc:.1%} ±{std_acc:.1%}\n")

        f.write(f"\n  图例: █ = 准确率, 误差 = 标准差\n")

        f.write("\n" + "─" * 70 + "\n")
        f.write("【与 MFCC 方案对比】\n")
        f.write("─" * 70 + "\n")
        f.write(f"  MFCC+DNN 准确率:       33.5%\n")
        f.write(f"  YAMNet 单折 (fold 5):  {fold_results[4]['test_acc']*100:.1f}%\n")
        f.write(f"  YAMNet 5折平均:        {cv_mean_acc*100:.1f}% ±{cv_std_acc*100:.1f}%\n")
        f.write(f"  提升幅度:              {cv_mean_acc*100 - 33.5:.1f}个百分点\n")

    print(f"\n[CV报告] {cv_report_path}")

    # ── 用全量数据训练最终模型（用于部署）──
    print(f"\n[最终模型] 用全量数据训练部署模型...")
    # 使用 fold 5 作为验证集（与之前单折保持一致）
    final_results, final_model, final_scaler = run_single_fold(
        df, embeddings, 5, category_names, y_all, verbose=False
    )
    print(f"  最终模型准确率: {final_results['test_acc']*100:.1f}%")

    export_label_map(category_names, output_dir)
    export_scaler_params(final_scaler, output_dir)
    export_tflite_float32(final_model, output_dir)
    export_tflite_int8(final_model, embeddings[df["fold"].values != 5][:200], output_dir)

    print(f"\n{'=' * 70}")
    print(f"  5折交叉验证完成！")
    print(f"  平均准确率: {cv_mean_acc*100:.1f}% ±{cv_std_acc*100:.1f}%")
    print(f"  平均F1:     {cv_mean_f1:.4f} ±{cv_std_f1:.4f}")
    print(f"  模型文件位于: {output_dir}")
    print(f"{'=' * 70}")


# ─────────────────────────────────────────────────────────────────────────────
# 单折训练（兼容旧版）
# ─────────────────────────────────────────────────────────────────────────────

def main():
    """
    单折训练主流程（fold 5 作为测试集）：
      1. 加载 YAMNet 预训练模型
      2. 加载 ESC-50 元数据
      3. 提取 YAMNet 嵌入特征（带缓存）
      4. 准备训练/验证/测试集
      5. 特征归一化 + 增强
      6. 训练分类器
      7. 评估模型
      8. 导出 TFLite 模型
      9. 生成训练报告
    """
    print("=" * 70)
    print("  YAMNet 迁移学习 —— ESC-50 环境声音分类")
    print("=" * 70)

    # ── 1. 加载 YAMNet ──
    print("\n[1/8] 加载 YAMNet 预训练模型...")
    yamnet_model = load_yamnet()

    # ── 2. 加载元数据 ──
    print("\n[2/8] 加载 ESC-50 元数据...")
    df = load_metadata()

    # ── 3. 提取 YAMNet 嵌入 ──
    print("\n[3/8] 提取 YAMNet 嵌入特征...")
    cache_path = os.path.join(MODEL_DIR, "audio_yamnet_embeddings.npy")
    embeddings = extract_yamnet_embeddings(yamnet_model, df, cache_path)

    # 标签编码
    le = LabelEncoder()
    y_all = le.fit_transform(df["category"].values)
    category_names = le.classes_.tolist()

    # ── 4-8. 单折训练 ──
    result, model, scaler = run_single_fold(
        df, embeddings, 5, category_names, y_all, verbose=False
    )

    # 打印评估详情
    evaluate_model(result["model"], embeddings[df["fold"].values == 5],
                   y_all[df["fold"].values == 5], category_names, MODEL_DIR)

    # 导出
    output_dir = MODEL_DIR
    os.makedirs(output_dir, exist_ok=True)

    export_label_map(category_names, output_dir)
    export_scaler_params(scaler, output_dir)
    export_tflite_float32(model, output_dir)
    export_tflite_int8(model, embeddings[df["fold"].values != 5][:200], output_dir)
    plot_training_history(result["history"], output_dir)
    generate_report(result, result["history"], category_names, output_dir)

    print("\n" + "=" * 70)
    print(f"  训练完成！")
    print(f"  测试准确率: {result['test_acc'] * 100:.1f}%")
    print(f"  F1分数:     {result['f1_score']:.4f}")
    print(f"  模型文件位于: {output_dir}")
    print("=" * 70)


if __name__ == "__main__":
    # 检查命令行参数
    if "--cv" in sys.argv:
        main_cv()
    else:
        main()