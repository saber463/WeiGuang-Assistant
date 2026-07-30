"""
手势分类器 — 深度学习训练脚本
================================
功能：基于采集的手部关键点数据，训练轻量级神经网络分类器，直接导出为 TFLite 格式

与机器学习版本的区别：
    - ML版本（train_gesture_classifier.py）：使用 sklearn 训练 RandomForest/SVM，通过 ONNX 中转导出 TFLite
    - DL版本（本脚本）：使用 TensorFlow/Keras 训练全连接神经网络，原生导出 TFLite，无需中转

模型架构：
    输入层: 63维（21个关键点 × 3坐标）或 93维（含几何特征）
    隐藏层1: 128神经元 + BatchNorm + Dropout(0.3)
    隐藏层2: 64神经元 + BatchNorm + Dropout(0.3)
    隐藏层3: 32神经元 + BatchNorm
    输出层: 10神经元（Softmax多分类）

训练策略：
    1. 学习率预热（Warmup）+ 余弦退火衰减
    2. 早停（EarlyStopping）防止过拟合
    3. 类别权重平衡（处理样本不均衡）
    4. 模型量化（INT8）减小体积，加速移动端推理

使用方式：
    python train_gesture_deep.py --csv dataset/gesture_data_*.csv

依赖安装：
    pip install tensorflow pandas numpy scikit-learn

输出文件：
    - model/gesture_dnn_float32.tflite     (FP32 全精度模型)
    - model/gesture_dnn_int8.tflite        (INT8 量化模型，体积更小)
    - model/gesture_label_map.json         (标签映射)
    - model/gesture_scaler_params.json     (特征归一化参数)
    - model/training_report_deep.txt       (训练报告)
    - model/training_history.png           (训练曲线图)
"""

import argparse
import json
import os
import sys
import warnings
from datetime import datetime

import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, f1_score
from sklearn.utils.class_weight import compute_class_weight

warnings.filterwarnings("ignore")

# 禁用 TensorFlow 日志噪音
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# ─────────────────────────────────────────────────────────────────────────────
# 配置区域
# ─────────────────────────────────────────────────────────────────────────────

# 手势标签
LABEL_NAMES = [
    "fist", "open_palm", "thumbs_up", "point_index", "peace",
    "ok_sign", "wave", "heart", "call_me", "neutral", "thumb_down"
]

LABEL_NAMES_ZH = [
    "握拳(SOS)", "手掌张开(停止)", "竖大拇指(确认)", "食指指向(方向)",
    "剪刀手(胜利)", "OK手势", "摆手(问候)", "比心(谢谢)",
    "打电话", "无手势", "拇指朝下(谢谢)"
]

NUM_CLASSES = len(LABEL_NAMES)

# 随机种子
RANDOM_SEED = 42
tf.random.set_seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)

# 模型输出目录
MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")

# 训练超参数
BATCH_SIZE = 64
EPOCHS = 200
LEARNING_RATE = 0.001
EARLY_STOPPING_PATIENCE = 30
VALIDATION_SPLIT = 0.15


def load_csv_data(csv_paths: list[str]) -> pd.DataFrame:
    """加载并合并多个CSV数据文件"""
    dfs = []
    for path in csv_paths:
        if not os.path.exists(path):
            print(f"[警告] 文件不存在: {path}")
            continue
        df = pd.read_csv(path)
        print(f"  加载: {path} → {len(df)} 条数据")
        dfs.append(df)

    if not dfs:
        print("[错误] 没有有效的CSV文件")
        sys.exit(1)

    merged = pd.concat(dfs, ignore_index=True)
    print(f"\n合并后总数据: {len(merged)} 条")
    return merged


def extract_geometric_features(landmarks: np.ndarray) -> np.ndarray:
    """
    从63维关键点坐标中提取30维几何特征
    与 train_gesture_classifier.py 保持一致
    """
    N = landmarks.shape[0]
    extra_features = np.zeros((N, 30))

    for i in range(N):
        pts = landmarks[i].reshape(21, 3)
        feat_idx = 0

        finger_tips = [4, 8, 12, 16, 20]
        finger_mcps = [2, 5, 9, 13, 17]

        # 指尖到MCP距离
        for tip, mcp in zip(finger_tips, finger_mcps):
            extra_features[i, feat_idx] = np.linalg.norm(pts[tip] - pts[mcp])
            feat_idx += 1

        # 关节弯曲角度
        for tip, dip, pip in [(4, 3, 2), (8, 7, 6), (12, 11, 10), (16, 15, 14), (20, 19, 18)]:
            v1 = pts[dip] - pts[tip]
            v2 = pts[pip] - pts[dip]
            extra_features[i, feat_idx] = np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2) + 1e-8)
            feat_idx += 1

        # 指尖到手腕距离
        for tip in finger_tips:
            extra_features[i, feat_idx] = np.linalg.norm(pts[tip] - pts[0])
            feat_idx += 1

        # 相邻指尖间距
        for j in range(4):
            extra_features[i, feat_idx] = np.linalg.norm(pts[finger_tips[j]] - pts[finger_tips[j + 1]])
            feat_idx += 1

        # 拇指到各指尖距离
        for other_tip in [8, 12, 16, 20]:
            extra_features[i, feat_idx] = np.linalg.norm(pts[4] - pts[other_tip])
            feat_idx += 1

        # 手掌尺寸
        extra_features[i, feat_idx] = np.linalg.norm(pts[5] - pts[17])
        extra_features[i, feat_idx + 1] = np.linalg.norm(pts[9] - pts[0]) / (np.linalg.norm(pts[5] - pts[17]) + 1e-8)

    return np.hstack([landmarks, extra_features])


def augment_data(X: np.ndarray, y: np.ndarray, noise_std: float = 0.005,
                 augment_factor: int = 3) -> tuple:
    """数据增强：噪声 + 缩放 + 平移"""
    X_aug_list = [X]
    y_aug_list = [y]

    for _ in range(augment_factor):
        noise = np.random.normal(0, noise_std, X.shape)
        scale = np.random.uniform(0.9, 1.1, (X.shape[0], 1))
        X_aug = (X + noise) * scale
        shift = np.random.uniform(-0.02, 0.02, (X.shape[0], 2))
        for i in range(21):
            X_aug[:, i * 3] += shift[:, 0]
            X_aug[:, i * 3 + 1] += shift[:, 1]
        X_aug_list.append(X_aug)
        y_aug_list.append(y)

    X_aug = np.vstack(X_aug_list)
    y_aug = np.hstack(y_aug_list)
    print(f"  数据增强: {X.shape[0]} → {X_aug.shape[0]} 条 (×{augment_factor + 1})")
    return X_aug, y_aug


def build_model(input_dim: int, num_classes: int, l2_reg: float = 0.0001) -> keras.Model:
    """
    构建轻量级全连接神经网络

    架构设计思路：
    - 3层全连接，逐步降维（128→64→32），提取层次化特征
    - BatchNormalization 加速收敛，允许更高学习率
    - Dropout 防止过拟合
    - L2 正则化进一步约束权重
    - 输出层使用 Softmax 多分类

    参数：
        input_dim: 输入特征维度（63或93）
        num_classes: 分类类别数（10）
        l2_reg: L2正则化系数

    返回：
        Keras Model 对象
    """
    model = keras.Sequential([
        # 输入层
        layers.Input(shape=(input_dim,), name="landmark_input"),

        # 隐藏层1: 128 → BatchNorm → ReLU → Dropout
        layers.Dense(128, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_128"),
        layers.BatchNormalization(name="bn_128"),
        layers.Activation("relu", name="relu_128"),
        layers.Dropout(0.3, name="dropout_128"),

        # 隐藏层2: 64 → BatchNorm → ReLU → Dropout
        layers.Dense(64, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_64"),
        layers.BatchNormalization(name="bn_64"),
        layers.Activation("relu", name="relu_64"),
        layers.Dropout(0.3, name="dropout_64"),

        # 隐藏层3: 32 → BatchNorm → ReLU
        layers.Dense(32, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_32"),
        layers.BatchNormalization(name="bn_32"),
        layers.Activation("relu", name="relu_32"),

        # 输出层: Softmax多分类
        layers.Dense(num_classes, activation="softmax", name="output"),
    ], name="GestureClassifierDNN")

    return model


def cosine_decay_schedule(initial_lr: float, total_steps: int, warmup_steps: int = 100):
    """
    学习率调度器：预热 + 余弦退火

    前 warmup_steps 步线性增长，之后余弦衰减到 initial_lr * 0.01

    参数：
        initial_lr: 初始学习率
        total_steps: 总训练步数
        warmup_steps: 预热步数

    返回：
        学习率调度函数
    """
    class WarmupCosineDecay(keras.optimizers.schedules.LearningRateSchedule):
        def __init__(self, initial_lr, total_steps, warmup_steps):
            super().__init__()
            self.initial_lr = initial_lr
            self.total_steps = total_steps
            self.warmup_steps = warmup_steps

        def __call__(self, step):
            step = tf.cast(step, tf.float32)
            warmup_steps = tf.cast(self.warmup_steps, tf.float32)
            total_steps = tf.cast(self.total_steps, tf.float32)

            # 预热阶段
            warmup_lr = self.initial_lr * (step / warmup_steps)

            # 余弦衰减阶段
            progress = (step - warmup_steps) / (total_steps - warmup_steps)
            progress = tf.clip_by_value(progress, 0.0, 1.0)
            cosine_lr = self.initial_lr * 0.01 + 0.5 * (self.initial_lr - self.initial_lr * 0.01) * \
                        (1.0 + tf.cos(np.pi * progress))

            return tf.where(step < warmup_steps, warmup_lr, cosine_lr)

        def get_config(self):
            return {
                "initial_lr": self.initial_lr,
                "total_steps": self.total_steps,
                "warmup_steps": self.warmup_steps
            }

    return WarmupCosineDecay(initial_lr, total_steps, warmup_steps)


def train_model(X_train: np.ndarray, y_train: np.ndarray,
                X_val: np.ndarray, y_val: np.ndarray,
                class_weights: dict) -> tuple:
    """
    训练神经网络模型

    参数：
        X_train, y_train: 训练数据
        X_val, y_val: 验证数据
        class_weights: 类别权重字典

    返回：
        (model, history)
    """
    input_dim = X_train.shape[1]
    model = build_model(input_dim, NUM_CLASSES)

    # 计算总训练步数
    steps_per_epoch = len(X_train) // BATCH_SIZE
    total_steps = steps_per_epoch * EPOCHS

    # 学习率调度器
    lr_schedule = cosine_decay_schedule(LEARNING_RATE, total_steps, warmup_steps=100)

    # 优化器
    optimizer = keras.optimizers.Adam(learning_rate=lr_schedule)

    # 编译模型
    model.compile(
        optimizer=optimizer,
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    # 模型摘要
    model.summary()

    # 回调函数
    callbacks = [
        # 早停：验证损失不再下降时停止训练
        keras.callbacks.EarlyStopping(
            monitor="val_loss",
            patience=EARLY_STOPPING_PATIENCE,
            restore_best_weights=True,
            verbose=1
        ),
        # 模型检查点：保存最佳模型
        keras.callbacks.ModelCheckpoint(
            filepath=os.path.join(MODEL_DIR, "gesture_dnn_best.keras"),
            monitor="val_accuracy",
            save_best_only=True,
            verbose=1
        ),
        # 降低学习率：平台期自动降低学习率（与余弦退火调度冲突，已禁用）
        # keras.callbacks.ReduceLROnPlateau(
        #     monitor="val_loss",
        #     factor=0.5,
        #     patience=10,
        #     min_lr=1e-6,
        #     verbose=1
        # ),
        # TensorBoard 日志（可选，如未安装tensorboard则跳过）
        # keras.callbacks.TensorBoard(
        #     log_dir=os.path.join(MODEL_DIR, "logs"),
        #     histogram_freq=1,
        #     write_graph=False
        # ),
    ]

    print(f"\n开始训练...")
    print(f"  输入维度: {input_dim}")
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


def evaluate_model(model: keras.Model, X_test: np.ndarray, y_test: np.ndarray) -> dict:
    """
    评估模型并返回指标

    参数：
        model: 训练好的模型
        X_test, y_test: 测试数据

    返回：
        评估指标字典
    """
    # 损失和准确率
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)

    # 预测
    y_pred_proba = model.predict(X_test, verbose=0)
    y_pred = np.argmax(y_pred_proba, axis=1)

    # F1分数
    f1 = f1_score(y_test, y_pred, average="weighted")

    return {
        "test_loss": test_loss,
        "test_acc": test_acc,
        "f1_score": f1,
        "y_test": y_test,
        "y_pred": y_pred,
        "y_pred_proba": y_pred_proba,
    }


def export_tflite_float32(model: keras.Model, output_dir: str) -> str:
    """
    导出 FP32 全精度 TFLite 模型

    参数：
        model: Keras 模型
        output_dir: 输出目录

    返回：
        TFLite 文件路径
    """
    os.makedirs(output_dir, exist_ok=True)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "gesture_dnn_float32.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    print(f"\n[FP32 TFLite导出] {path}")
    print(f"  模型大小: {os.path.getsize(path) / 1024:.1f} KB")

    return path


def export_tflite_int8(model: keras.Model, X_calib: np.ndarray, output_dir: str) -> str:
    """
    导出 INT8 量化 TFLite 模型（体积更小，推理更快）

    量化原理：
    - 将 FP32 权重和激活值映射到 INT8 范围 [-128, 127]
    - 需要校准数据集（X_calib）来统计激活值范围
    - 模型体积约减少 4 倍，推理速度提升 2-3 倍

    参数：
        model: Keras 模型
        X_calib: 校准数据集（用于统计激活值分布）
        output_dir: 输出目录

    返回：
        TFLite 文件路径
    """
    os.makedirs(output_dir, exist_ok=True)

    def representative_dataset():
        """生成代表数据集用于量化校准"""
        for i in range(0, len(X_calib), BATCH_SIZE):
            batch = X_calib[i:i + BATCH_SIZE]
            yield [batch.astype(np.float32)]

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.representative_dataset = representative_dataset
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.inference_input_type = tf.float32    # 输入保持 FP32
    converter.inference_output_type = tf.float32   # 输出保持 FP32

    tflite_model = converter.convert()

    path = os.path.join(output_dir, "gesture_dnn_int8.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)

    print(f"\n[INT8量化TFLite导出] {path}")
    print(f"  模型大小: {os.path.getsize(path) / 1024:.1f} KB")

    return path


def export_scaler_params(scaler: StandardScaler, output_dir: str):
    """导出特征归一化参数"""
    os.makedirs(output_dir, exist_ok=True)

    params = {
        "mean": scaler.mean_.tolist(),
        "std": scaler.scale_.tolist(),
        "n_features": scaler.n_features_in_,
        "description": "StandardScaler参数：输入 = (x - mean) / std"
    }

    path = os.path.join(output_dir, "gesture_scaler_params.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(params, f, indent=2, ensure_ascii=False)

    print(f"\n[归一化参数导出] {path}")


def export_label_map(output_dir: str):
    """导出标签映射表"""
    os.makedirs(output_dir, exist_ok=True)

    label_map = {
        "labels": LABEL_NAMES,
        "labels_zh": LABEL_NAMES_ZH,
        "num_classes": NUM_CLASSES,
        "sos_gestures": [0, 1],
        "sos_gesture_names": ["fist", "open_palm"],
    }

    path = os.path.join(output_dir, "gesture_label_map.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(label_map, f, indent=2, ensure_ascii=False)

    print(f"[标签映射导出] {path}")


def plot_training_history(history, output_dir: str):
    """
    绘制训练曲线图（准确率 + 损失）

    参数：
        history: Keras History 对象
        output_dir: 输出目录
    """
    try:
        import matplotlib
        matplotlib.use("Agg")  # 非交互式后端
        import matplotlib.pyplot as plt

        os.makedirs(output_dir, exist_ok=True)

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

        # 准确率曲线
        ax1.plot(history.history["accuracy"], label="训练准确率", linewidth=2)
        ax1.plot(history.history["val_accuracy"], label="验证准确率", linewidth=2)
        ax1.set_title("模型准确率", fontsize=14)
        ax1.set_xlabel("Epoch")
        ax1.set_ylabel("Accuracy")
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        # 损失曲线
        ax2.plot(history.history["loss"], label="训练损失", linewidth=2)
        ax2.plot(history.history["val_loss"], label="验证损失", linewidth=2)
        ax2.set_title("模型损失", fontsize=14)
        ax2.set_xlabel("Epoch")
        ax2.set_ylabel("Loss")
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        path = os.path.join(output_dir, "training_history.png")
        plt.savefig(path, dpi=150, bbox_inches="tight")
        plt.close()
        print(f"\n[训练曲线图] {path}")

    except ImportError:
        print("\n[提示] matplotlib 未安装，跳过训练曲线图绘制")


def generate_report(results: dict, history, output_dir: str):
    """生成训练报告"""
    os.makedirs(output_dir, exist_ok=True)

    y_test = results["y_test"]
    y_pred = results["y_pred"]

    report_path = os.path.join(output_dir, "training_report_deep.txt")

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("=" * 60 + "\n")
        f.write("  手势分类器深度学习训练报告\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 60 + "\n\n")

        f.write(f"模型架构: 3层全连接网络 (128→64→32)\n")
        f.write(f"测试准确率: {results['test_acc']:.4f}\n")
        f.write(f"测试损失:   {results['test_loss']:.4f}\n")
        f.write(f"F1分数:     {results['f1_score']:.4f}\n")
        f.write(f"训练轮次:   {len(history.history['loss'])}\n\n")

        # 获取测试集中实际出现的类别
        present_classes = np.unique(np.concatenate([y_test, y_pred]))
        present_labels = [LABEL_NAMES[i] for i in present_classes]

        f.write("─" * 60 + "\n")
        f.write("分类报告:\n")
        f.write("─" * 60 + "\n")
        f.write(classification_report(y_test, y_pred, labels=present_classes, target_names=present_labels))

        f.write("\n─" * 60 + "\n")
        f.write("混淆矩阵:\n")
        f.write("─" * 60 + "\n")
        cm = confusion_matrix(y_test, y_pred, labels=present_classes)
        # 动态计算列宽和排版
        header = "        " + " ".join([f"{n:>8s}" for n in present_labels])
        f.write(header + "\n")
        for i, row in enumerate(cm):
            f.write(f"{present_labels[i]:8s} " + " ".join([f"{v:8d}" for v in row]))
            f.write("\n")

    print(f"\n[训练报告] {report_path}")

    with open(report_path, "r", encoding="utf-8") as f:
        print("\n" + f.read())


def main():
    parser = argparse.ArgumentParser(description="手势分类器深度学习训练脚本")
    parser.add_argument("--csv", action="append", dest="csv_paths", required=True,
                        help="CSV数据文件路径（可多次指定）")
    parser.add_argument("--no-augment", action="store_true", default=False,
                        help="禁用数据增强")
    parser.add_argument("--no-geometric", action="store_true", default=False,
                        help="禁用几何特征提取")
    parser.add_argument("--no-quantize", action="store_true", default=False,
                        help="禁用INT8量化（仅导出FP32模型）")
    parser.add_argument("--output-dir", default=MODEL_DIR,
                        help=f"模型输出目录（默认: {MODEL_DIR}）")
    args = parser.parse_args()

    print("=" * 60)
    print("  手势分类器深度学习训练 —— 微光同行APP")
    print("=" * 60)

    # ── 1. 加载数据 ──
    print("\n[1/7] 加载数据...")
    df = load_csv_data(args.csv_paths)

    feature_cols = [c for c in df.columns if c != "label"]
    X_raw = df[feature_cols].values.astype(np.float32)
    y = df["label"].values.astype(np.int32)

    print(f"  特征维度: {X_raw.shape[1]}")
    for idx, name in enumerate(LABEL_NAMES):
        count = np.sum(y == idx)
        print(f"    [{idx}] {name:12s}: {count:4d} 条")

    # ── 2. 数据增强 ──
    if not args.no_augment:
        print("\n[2/7] 数据增强...")
        X_raw, y = augment_data(X_raw, y)
    else:
        print("\n[2/7] 跳过数据增强")

    # ── 3. 特征工程 ──
    if not args.no_geometric:
        print("\n[3/7] 几何特征提取...")
        X = extract_geometric_features(X_raw)
        print(f"  特征维度: {X_raw.shape[1]} → {X.shape[1]}")
    else:
        print("\n[3/7] 跳过几何特征提取")
        X = X_raw

    # ── 4. 数据预处理 ──
    print("\n[4/7] 数据预处理...")

    # 拆分数据集（训练:验证:测试 = 70:15:15）
    X_temp, X_test, y_temp, y_test = train_test_split(
        X, y, test_size=0.15, random_state=RANDOM_SEED, stratify=y
    )
    X_train, X_val, y_train, y_val = train_test_split(
        X_temp, y_temp, test_size=0.1765, random_state=RANDOM_SEED, stratify=y_temp
    )  # 0.15 / 0.85 ≈ 0.1765

    # 特征归一化
    scaler = StandardScaler()
    X_train = scaler.fit_transform(X_train)
    X_val = scaler.transform(X_val)
    X_test = scaler.transform(X_test)

    print(f"  训练集: {len(X_train)} 条")
    print(f"  验证集: {len(X_val)} 条")
    print(f"  测试集: {len(X_test)} 条")

    # 计算类别权重（处理样本不均衡）
    class_weights_array = compute_class_weight(
        class_weight="balanced",
        classes=np.unique(y_train),
        y=y_train
    )
    class_weights = {i: w for i, w in enumerate(class_weights_array)}
    print(f"  类别权重: {[f'{w:.2f}' for w in class_weights_array]}")

    # ── 5. 训练模型 ──
    print("\n[5/7] 训练神经网络...")
    model, history = train_model(X_train, y_train, X_val, y_val, class_weights)

    # ── 6. 评估模型 ──
    print("\n[6/7] 评估模型...")
    results = evaluate_model(model, X_test, y_test)
    print(f"\n  测试准确率: {results['test_acc']:.4f}")
    print(f"  测试损失:   {results['test_loss']:.4f}")
    print(f"  F1分数:     {results['f1_score']:.4f}")

    # ── 7. 导出模型 ──
    print("\n[7/7] 导出模型...")
    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    export_label_map(output_dir)
    export_scaler_params(scaler, output_dir)

    # 导出 FP32 模型
    export_tflite_float32(model, output_dir)

    # 导出 INT8 量化模型
    if not args.no_quantize:
        # 用验证集的一部分作为校准数据
        calib_size = min(500, len(X_val))
        export_tflite_int8(model, X_val[:calib_size], output_dir)

    # 绘制训练曲线
    plot_training_history(history, output_dir)

    # 生成报告
    generate_report(results, history, output_dir)

    print("\n" + "=" * 60)
    print("  训练完成！")
    print(f"  模型文件位于: {output_dir}")
    print("=" * 60)


if __name__ == "__main__":
    main()