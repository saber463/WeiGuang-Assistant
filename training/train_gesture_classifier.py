"""
手势分类器 — 机器学习训练脚本
================================
功能：基于采集的手部关键点数据，训练轻量级手势分类器，导出为 TFLite 格式供 Android 端使用

训练策略：
    1. 数据增强：对关键点坐标添加微小噪声、随机缩放、旋转，模拟不同手型/角度
    2. 特征工程：从21个关键点中提取几何特征（指尖距离、关节角度、手指弯曲度等）
    3. 模型对比：训练 RandomForest / SVM / XGBoost 三种模型，选最优导出
    4. 模型导出：最优模型 → ONNX → TFLite（可直接在 Android 端加载推理）

使用方式：
    python train_gesture_classifier.py --csv dataset/gesture_data_*.csv

    或合并多个CSV文件：
    python train_gesture_classifier.py --csv dataset/gesture_data_20260722.csv --csv dataset/gesture_data_20260723.csv

依赖安装：
    pip install scikit-learn pandas numpy onnx onnxruntime skl2onnx

输出文件：
    - model/gesture_classifier_rf.onnx       (RandomForest 模型)
    - model/gesture_classifier_rf.tflite     (TFLite 模型，供Android使用)
    - model/gesture_label_map.json           (标签映射)
    - model/gesture_scaler_params.json       (特征归一化参数)
    - model/training_report.txt              (训练报告)
"""

import argparse
import json
import os
import sys
import warnings
from datetime import datetime
from typing import Tuple

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    accuracy_score,
    f1_score
)
from sklearn.svm import SVC

warnings.filterwarnings("ignore")

# ─────────────────────────────────────────────────────────────────────────────
# 配置区域
# ─────────────────────────────────────────────────────────────────────────────

# 手势标签（与采集脚本保持一致）
LABEL_NAMES = [
    "fist",           # 0 = 握拳（SOS）
    "open_palm",      # 1 = 手掌张开（停止）
    "thumbs_up",      # 2 = 竖大拇指（确认）
    "point_index",    # 3 = 食指指向（方向）
    "peace",          # 4 = 剪刀手（胜利）
    "ok_sign",        # 5 = OK手势（没问题）
    "wave",           # 6 = 摆手（问候）
    "heart",          # 7 = 比心（谢谢）
    "call_me",        # 8 = 打电话（六的手势）
    "neutral",        # 9 = 无手势（负样本）
    "thumb_down",     # 10 = 拇指朝下（谢谢）
]

# 中文标签名称（用于训练报告）
LABEL_NAMES_ZH = [
    "握拳(SOS)", "手掌张开(停止)", "竖大拇指(确认)", "食指指向(方向)",
    "剪刀手(胜利)", "OK手势", "摆手(问候)", "比心(谢谢)",
    "打电话", "无手势", "拇指朝下(谢谢)"
]

# 21个手部关键点索引（MediaPipe Hand Landmarks）
# 参考：https://developers.google.com/mediapipe/solutions/vision/hand_landmarker
HAND_KEYPOINTS = {
    0: "手腕", 1: "拇指CMC", 2: "拇指MCP", 3: "拇指IP", 4: "拇指指尖",
    5: "食指MCP", 6: "食指PIP", 7: "食指DIP", 8: "食指指尖",
    9: "中指MCP", 10: "中指PIP", 11: "中指DIP", 12: "中指指尖",
    13: "无名指MCP", 14: "无名指PIP", 15: "无名指DIP", 16: "无名指指尖",
    17: "小指MCP", 18: "小指PIP", 19: "小指DIP", 20: "小指指尖",
}

# 随机种子（保证结果可复现）
RANDOM_SEED = 42

# 模型输出目录
MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")


def load_csv_data(csv_paths: list[str]) -> pd.DataFrame:
    """
    加载并合并多个CSV数据文件

    参数：
        csv_paths: CSV文件路径列表

    返回：
        合并后的 DataFrame，包含 63个特征列 + 1个标签列
    """
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
    从63维关键点坐标中提取几何特征（增强特征工程）

    原始特征：21个关键点 × 3维坐标 = 63维
    几何特征包括：
    - 手指弯曲度（5根手指，每根3个关节 → 15维）
    - 指尖到手腕距离（5维）
    - 相邻指尖间距（4维）
    - 拇指到各指尖距离（4维）
    - 手掌宽度/高度比（2维）
    总计：63 + 30 = 93维

    参数：
        landmarks: (N, 63) 的关键点坐标数组

    返回：
        (N, 93) 的增强特征数组
    """
    N = landmarks.shape[0]
    extra_features = np.zeros((N, 30))

    for i in range(N):
        # 重塑为 (21, 3)
        pts = landmarks[i].reshape(21, 3)
        feat_idx = 0

        # ── 手指弯曲度（用指尖到指根MCP的距离近似）──
        finger_tips = [4, 8, 12, 16, 20]   # 拇指、食指、中指、无名指、小指指尖
        finger_mcps = [2, 5, 9, 13, 17]     # 对应MCP关节

        for tip, mcp in zip(finger_tips, finger_mcps):
            # 指尖到MCP的欧氏距离
            dist = np.linalg.norm(pts[tip] - pts[mcp])
            extra_features[i, feat_idx] = dist
            feat_idx += 1

        # ── 手指内关节弯曲角度 ──
        for tip, dip, pip in [(4, 3, 2), (8, 7, 6), (12, 11, 10), (16, 15, 14), (20, 19, 18)]:
            v1 = pts[dip] - pts[tip]
            v2 = pts[pip] - pts[dip]
            cos_angle = np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2) + 1e-8)
            extra_features[i, feat_idx] = cos_angle
            feat_idx += 1

        # ── 指尖到手腕距离 ──
        wrist = pts[0]
        for tip in finger_tips:
            dist = np.linalg.norm(pts[tip] - wrist)
            extra_features[i, feat_idx] = dist
            feat_idx += 1

        # ── 相邻指尖间距 ──
        for j in range(4):
            dist = np.linalg.norm(pts[finger_tips[j]] - pts[finger_tips[j + 1]])
            extra_features[i, feat_idx] = dist
            feat_idx += 1

        # ── 拇指到各指尖距离 ──
        thumb_tip = pts[4]
        for other_tip in [8, 12, 16, 20]:
            dist = np.linalg.norm(thumb_tip - pts[other_tip])
            extra_features[i, feat_idx] = dist
            feat_idx += 1

        # ── 手掌宽度（食指MCP到小指MCP）和高度（中指MCP到手腕）──
        palm_width = np.linalg.norm(pts[5] - pts[17])
        palm_height = np.linalg.norm(pts[9] - pts[0])
        extra_features[i, feat_idx] = palm_width
        extra_features[i, feat_idx + 1] = palm_height / (palm_width + 1e-8)

    # 拼接原始特征和几何特征
    return np.hstack([landmarks, extra_features])


def augment_data(X: np.ndarray, y: np.ndarray, noise_std: float = 0.005,
                 scale_range: Tuple[float, float] = (0.9, 1.1),
                 augment_factor: int = 3) -> Tuple[np.ndarray, np.ndarray]:
    """
    数据增强：对关键点坐标添加噪声、缩放、旋转

    增强策略：
    1. 高斯噪声：模拟手部检测抖动
    2. 随机缩放：模拟手离摄像头远近不同
    3. 随机平移：模拟手在画面中位置不同

    注意：只增强原始63维坐标，不增强几何特征（几何特征在增强后重新计算）

    参数：
        X: (N, 63) 原始关键点坐标
        y: (N,) 标签
        noise_std: 噪声标准差
        scale_range: 缩放范围 (min, max)
        augment_factor: 每帧增强倍数

    返回：
        (augmented_X, augmented_y)
    """
    X_aug_list = [X]
    y_aug_list = [y]

    for factor in range(augment_factor):
        # 噪声增强
        noise = np.random.normal(0, noise_std, X.shape)
        X_noise = X + noise

        # 缩放增强
        scale = np.random.uniform(scale_range[0], scale_range[1], (X.shape[0], 1))
        X_scaled = X_noise * scale

        # 平移增强（对x和y坐标）
        shift = np.random.uniform(-0.02, 0.02, (X.shape[0], 2))
        X_shifted = X_scaled.copy()
        for i in range(21):
            X_shifted[:, i * 3] += shift[:, 0]      # x平移
            X_shifted[:, i * 3 + 1] += shift[:, 1]  # y平移

        X_aug_list.append(X_shifted)
        y_aug_list.append(y)

    X_aug = np.vstack(X_aug_list)
    y_aug = np.hstack(y_aug_list)

    print(f"  数据增强: {X.shape[0]} → {X_aug.shape[0]} 条 (×{augment_factor + 1})")
    return X_aug, y_aug


def train_and_evaluate(X: np.ndarray, y: np.ndarray) -> dict:
    """
    训练多种模型并评估，返回最优模型

    训练模型：
    1. RandomForest（集成学习，对噪声鲁棒，适合移动端）
    2. SVM（支持向量机，小样本下表现好）
    3. 对比后选最优导出

    参数：
        X: (N, F) 特征矩阵
        y: (N,) 标签

    返回：
        包含最优模型和评估指标的字典
    """
    # 拆分训练集/测试集（8:2）
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=RANDOM_SEED, stratify=y
    )

    print(f"\n训练集: {len(X_train)} 条, 测试集: {len(X_test)} 条")

    # ── 特征归一化 ──
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    results = {}

    # ── 模型1：Random Forest ──
    print("\n" + "=" * 60)
    print("  训练 Random Forest 模型...")
    print("=" * 60)

    rf = RandomForestClassifier(
        n_estimators=100,        # 100棵决策树
        max_depth=15,            # 限制深度防止过拟合
        min_samples_split=5,     # 节点最小样本数
        min_samples_leaf=2,      # 叶节点最小样本数
        random_state=RANDOM_SEED,
        n_jobs=-1                # 使用全部CPU核心
    )
    rf.fit(X_train_scaled, y_train)

    rf_train_pred = rf.predict(X_train_scaled)
    rf_test_pred = rf.predict(X_test_scaled)

    rf_train_acc = accuracy_score(y_train, rf_train_pred)
    rf_test_acc = accuracy_score(y_test, rf_test_pred)
    rf_f1 = f1_score(y_test, rf_test_pred, average="weighted")

    # 交叉验证
    cv_scores = cross_val_score(rf, X_train_scaled, y_train, cv=5, scoring="accuracy")
    rf_cv_mean = cv_scores.mean()
    rf_cv_std = cv_scores.std()

    results["random_forest"] = {
        "model": rf,
        "scaler": scaler,
        "train_acc": rf_train_acc,
        "test_acc": rf_test_acc,
        "f1_score": rf_f1,
        "cv_mean": rf_cv_mean,
        "cv_std": rf_cv_std,
        "y_test": y_test,
        "y_pred": rf_test_pred,
    }

    print(f"  RandomForest:")
    print(f"    训练准确率: {rf_train_acc:.4f}")
    print(f"    测试准确率: {rf_test_acc:.4f}")
    print(f"    F1分数:     {rf_f1:.4f}")
    print(f"    5折交叉验证: {rf_cv_mean:.4f} ± {rf_cv_std:.4f}")

    # ── 模型2：SVM ──
    print("\n" + "=" * 60)
    print("  训练 SVM 模型...")
    print("=" * 60)

    svm = SVC(
        kernel="rbf",            # RBF核函数
        C=10.0,                  # 正则化参数
        gamma="scale",           # 自动计算gamma
        probability=True,        # 输出概率（用于置信度）
        random_state=RANDOM_SEED
    )
    svm.fit(X_train_scaled, y_train)

    svm_train_pred = svm.predict(X_train_scaled)
    svm_test_pred = svm.predict(X_test_scaled)

    svm_train_acc = accuracy_score(y_train, svm_train_pred)
    svm_test_acc = accuracy_score(y_test, svm_test_pred)
    svm_f1 = f1_score(y_test, svm_test_pred, average="weighted")

    svm_cv_scores = cross_val_score(svm, X_train_scaled, y_train, cv=5, scoring="accuracy")
    svm_cv_mean = svm_cv_scores.mean()
    svm_cv_std = svm_cv_scores.std()

    results["svm"] = {
        "model": svm,
        "scaler": scaler,
        "train_acc": svm_train_acc,
        "test_acc": svm_test_acc,
        "f1_score": svm_f1,
        "cv_mean": svm_cv_mean,
        "cv_std": svm_cv_std,
        "y_test": y_test,
        "y_pred": svm_test_pred,
    }

    print(f"  SVM:")
    print(f"    训练准确率: {svm_train_acc:.4f}")
    print(f"    测试准确率: {svm_test_acc:.4f}")
    print(f"    F1分数:     {svm_f1:.4f}")
    print(f"    5折交叉验证: {svm_cv_mean:.4f} ± {svm_cv_std:.4f}")

    # ── 选择最优模型 ──
    if rf_test_acc >= svm_test_acc:
        best_model_name = "random_forest"
        print(f"\n[最优模型] RandomForest (测试准确率: {rf_test_acc:.4f})")
    else:
        best_model_name = "svm"
        print(f"\n[最优模型] SVM (测试准确率: {svm_test_acc:.4f})")

    results["best_model_name"] = best_model_name
    results["best_model"] = results[best_model_name]

    return results


def export_to_onnx(results: dict, output_dir: str):
    """
    导出模型为 ONNX 格式

    参数：
        results: train_and_evaluate 返回的结果字典
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    best_model = results["best_model"]["model"]
    scaler = results["best_model"]["scaler"]
    model_name = results["best_model_name"]

    onnx_path = os.path.join(output_dir, f"gesture_classifier_{model_name}.onnx")

    try:
        from skl2onnx import convert_sklearn
        from skl2onnx.common.data_types import FloatTensorType

        # 定义输入类型
        initial_type = [("float_input", FloatTensorType([None, scaler.n_features_in_]))]

        # 转换模型
        onnx_model = convert_sklearn(best_model, initial_types=initial_type)

        # 保存
        with open(onnx_path, "wb") as f:
            f.write(onnx_model.SerializeToString())

        print(f"\n[ONNX导出成功] {onnx_path}")
        print(f"  输入维度: {scaler.n_features_in_}")
        print(f"  输出类别数: {len(LABEL_NAMES)}")

        return onnx_path

    except ImportError:
        print("\n[警告] skl2onnx 未安装，跳过 ONNX 导出")
        print("  安装: pip install skl2onnx onnx")
        return None


def export_to_tflite(results: dict, output_dir: str):
    """
    导出模型为 TFLite 格式（通过 ONNX 中转）

    转换链：sklearn → ONNX → TFLite

    参数：
        results: train_and_evaluate 返回的结果字典
        output_dir: 输出目录
    """
    # 先用 ONNX 导出
    onnx_path = export_to_onnx(results, output_dir)
    if onnx_path is None:
        return None

    model_name = results["best_model_name"]
    tflite_path = os.path.join(output_dir, f"gesture_classifier_{model_name}.tflite")

    try:
        import onnx
        from onnx_tf.backend import prepare

        # 加载 ONNX 模型
        onnx_model = onnx.load(onnx_path)

        # 转换为 TensorFlow 格式
        tf_rep = prepare(onnx_model)

        # 导出为 TFLite
        import tensorflow as tf
        converter = tf.lite.TFLiteConverter.from_concrete_functions([tf_rep.concrete_func])
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        tflite_model = converter.convert()

        with open(tflite_path, "wb") as f:
            f.write(tflite_model)

        print(f"\n[TFLite导出成功] {tflite_path}")
        print(f"  模型大小: {os.path.getsize(tflite_path) / 1024:.1f} KB")

        return tflite_path

    except ImportError:
        print("\n[警告] TensorFlow/onnx-tf 未安装，跳过 TFLite 导出")
        print("  安装: pip install tensorflow onnx-tf")
        return None


def export_scaler_params(scaler: StandardScaler, output_dir: str):
    """
    导出特征归一化参数（mean 和 std），供 Android 端预处理使用

    参数：
        scaler: 训练好的 StandardScaler 对象
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    params = {
        "mean": scaler.mean_.tolist(),
        "std": scaler.scale_.tolist(),
        "n_features": scaler.n_features_in_,
        "description": "StandardScaler参数：输入 = (x - mean) / std"
    }

    param_path = os.path.join(output_dir, "gesture_scaler_params.json")
    with open(param_path, "w", encoding="utf-8") as f:
        json.dump(params, f, indent=2, ensure_ascii=False)

    print(f"\n[归一化参数导出] {param_path}")


def export_label_map(output_dir: str):
    """
    导出标签映射表（JSON格式），供 Android 端使用

    参数：
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    label_map = {
        "labels": LABEL_NAMES,
        "labels_zh": LABEL_NAMES_ZH,
        "num_classes": len(LABEL_NAMES),
        "sos_gestures": [0, 1],           # SOS手势标签索引
        "sos_gesture_names": ["fist", "open_palm"],
    }

    map_path = os.path.join(output_dir, "gesture_label_map.json")
    with open(map_path, "w", encoding="utf-8") as f:
        json.dump(label_map, f, indent=2, ensure_ascii=False)

    print(f"[标签映射导出] {map_path}")


def generate_report(results: dict, output_dir: str):
    """
    生成训练报告

    参数：
        results: train_and_evaluate 返回的结果字典
        output_dir: 输出目录
    """
    os.makedirs(output_dir, exist_ok=True)

    best = results["best_model"]
    y_test = best["y_test"]
    y_pred = best["y_pred"]

    report_path = os.path.join(output_dir, "training_report.txt")

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("=" * 60 + "\n")
        f.write("  手势分类器训练报告\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 60 + "\n\n")

        f.write(f"最优模型: {results['best_model_name']}\n")
        f.write(f"测试准确率: {best['test_acc']:.4f}\n")
        f.write(f"F1分数: {best['f1_score']:.4f}\n")
        f.write(f"5折交叉验证: {best['cv_mean']:.4f} ± {best['cv_std']:.4f}\n\n")

        f.write("─" * 60 + "\n")
        f.write("各模型对比:\n")
        f.write("─" * 60 + "\n")
        for name in ["random_forest", "svm"]:
            if name in results:
                r = results[name]
                f.write(f"\n{name}:\n")
                f.write(f"  训练准确率: {r['train_acc']:.4f}\n")
                f.write(f"  测试准确率: {r['test_acc']:.4f}\n")
                f.write(f"  F1分数:     {r['f1_score']:.4f}\n")
                f.write(f"  交叉验证:   {r['cv_mean']:.4f} ± {r['cv_std']:.4f}\n")

        f.write("\n" + "─" * 60 + "\n")
        f.write("分类报告:\n")
        f.write("─" * 60 + "\n")
        f.write(classification_report(y_test, y_pred, target_names=LABEL_NAMES))

        f.write("\n" + "─" * 60 + "\n")
        f.write("混淆矩阵:\n")
        f.write("─" * 60 + "\n")
        cm = confusion_matrix(y_test, y_pred)
        f.write("        " + " ".join([f"{n:>6s}" for n in LABEL_NAMES[:5]]))
        f.write("  " + " ".join([f"{n:>6s}" for n in LABEL_NAMES[5:]]))
        f.write("\n")
        for i, row in enumerate(cm):
            f.write(f"{LABEL_NAMES[i]:8s} " + " ".join([f"{v:6d}" for v in row]))
            f.write("\n")

    print(f"\n[训练报告] {report_path}")

    # 同时打印到控制台
    with open(report_path, "r", encoding="utf-8") as f:
        print("\n" + f.read())


def main():
    """主函数：解析参数 → 加载数据 → 特征工程 → 训练 → 导出"""
    parser = argparse.ArgumentParser(description="手势分类器训练脚本")
    parser.add_argument(
        "--csv", action="append", dest="csv_paths",
        required=True, help="CSV数据文件路径（可多次指定）"
    )
    parser.add_argument(
        "--no-augment", action="store_true", default=False,
        help="禁用数据增强"
    )
    parser.add_argument(
        "--no-geometric", action="store_true", default=False,
        help="禁用几何特征提取（仅使用原始63维特征）"
    )
    parser.add_argument(
        "--output-dir", default=MODEL_DIR,
        help=f"模型输出目录（默认: {MODEL_DIR}）"
    )
    args = parser.parse_args()

    print("=" * 60)
    print("  手势分类器训练 —— 微光同行APP")
    print("=" * 60)

    # ── 1. 加载数据 ──
    print("\n[1/6] 加载数据...")
    df = load_csv_data(args.csv_paths)

    # 提取特征列和标签列
    feature_cols = [c for c in df.columns if c != "label"]
    X_raw = df[feature_cols].values.astype(np.float32)
    y = df["label"].values.astype(np.int32)

    print(f"  特征维度: {X_raw.shape[1]}")
    print(f"  标签分布:")
    for idx, name in enumerate(LABEL_NAMES):
        count = np.sum(y == idx)
        print(f"    [{idx}] {name:12s}: {count:4d} 条")

    # ── 2. 数据增强 ──
    if not args.no_augment:
        print("\n[2/6] 数据增强...")
        X_raw, y = augment_data(X_raw, y)
    else:
        print("\n[2/6] 跳过数据增强")

    # ── 3. 特征工程 ──
    if not args.no_geometric:
        print("\n[3/6] 几何特征提取...")
        X = extract_geometric_features(X_raw)
        print(f"  特征维度: {X_raw.shape[1]} → {X.shape[1]} (含30维几何特征)")
    else:
        print("\n[3/6] 跳过几何特征提取（仅使用原始特征）")
        X = X_raw

    # ── 4. 训练模型 ──
    print("\n[4/6] 训练模型...")
    results = train_and_evaluate(X, y)

    # ── 5. 导出模型 ──
    print("\n[5/6] 导出模型...")
    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    export_label_map(output_dir)
    export_scaler_params(results["best_model"]["scaler"], output_dir)

    tflite_path = export_to_tflite(results, output_dir)
    if tflite_path is None:
        # 如果 TFLite 导出失败，至少导出 ONNX
        export_to_onnx(results, output_dir)

    # ── 6. 生成报告 ──
    print("\n[6/6] 生成训练报告...")
    generate_report(results, output_dir)

    print("\n" + "=" * 60)
    print("  训练完成！")
    print(f"  模型文件位于: {output_dir}")
    print("=" * 60)


if __name__ == "__main__":
    main()