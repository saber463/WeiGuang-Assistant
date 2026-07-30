#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
公开数据集关键点提取流水线
===========================

功能：从公开手语/手势图片数据集中提取MediaPipe手部关键点（21点×3坐标=63维向量），
     计算每种手势的质心向量，导出为JSON供Android端GestureVectorDB使用。

支持的公开数据集：
  1. Kaggle Hand Gesture Recognition Dataset (LeapGestRecog)
     - 10种手势，10人，每人每种10张 = 1000张图片
     - 下载: kaggle datasets download -d gti-upm/leapgestrecog
  2. Kaggle ASL Alphabet Dataset
     - 29类（A-Z + del/nothing/space），每类3000张
     - 下载: kaggle datasets download -d grassknoted/asl-alphabet
  3. 自定义图片文件夹（按类别分文件夹存放）

使用方式：
  # 方式1：处理Kaggle LeapGestRecog数据集
  python extract_landmarks_from_dataset.py --dataset leapgestrecog --data_dir ./leapgestrecog

  # 方式2：处理自定义图片文件夹
  python extract_landmarks_from_dataset.py --dataset custom --data_dir ./my_gestures

  # 方式3：仅用已有合成数据生成质心（不需要图片）
  python extract_landmarks_from_dataset.py --dataset synthetic --output ./model/gesture_centroids.json

输出：JSON文件，包含每种手势的质心向量和统计信息
  {
    "fist": {"centroid": [...63 floats...], "count": 150, "std": [...]},
    "open_palm": {...},
    ...
  }

依赖：pip install mediapipe opencv-python numpy tqdm
"""

import argparse
import json
import os
import sys
import time
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import numpy as np

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent))


# ═══════════════════════════════════════════════════════════════════════════════
# 配置
# ═══════════════════════════════════════════════════════════════════════════════

# 手势标签映射：数据集类别 → 我们的标准标签
# 根据实际数据集结构调整
LEAPGESTRECOG_LABEL_MAP = {
    "01_palm": "open_palm",
    "02_l": "point_index",
    "03_fist": "fist",
    "04_fist_moved": "fist",
    "05_thumb": "thumbs_up",
    "06_index": "point_index",
    "07_ok": "ok_sign",
    "08_palm_moved": "open_palm",
    "09_c": "call_me",
    "10_down": "thumb_down",
}

ASL_LABEL_MAP = {
    "A": "fist", "B": "open_palm", "C": "call_me",
    "D": "point_index", "E": "fist", "F": "ok_sign",
    "G": "point_index", "H": "peace", "I": "point_index",
    "J": "call_me", "K": "peace", "L": "point_index",
    "M": "fist", "N": "fist", "O": "ok_sign",
    "P": "peace", "Q": "ok_sign", "R": "peace",
    "S": "fist", "T": "fist", "U": "peace",
    "V": "peace", "W": "call_me", "X": "fist",
    "Y": "thumbs_up", "Z": "point_index",
    "nothing": "neutral", "space": "open_palm",
}

# 我们支持的11种手势标签
SUPPORTED_LABELS = [
    "fist", "open_palm", "thumbs_up", "thumb_down",
    "point_index", "peace", "ok_sign", "wave",
    "heart", "call_me", "neutral"
]

# 手势中文名
LABEL_NAMES_ZH = {
    "fist": "握拳(SOS)",
    "open_palm": "手掌张开(停止)",
    "thumbs_up": "竖大拇指(确认)",
    "thumb_down": "拇指朝下(谢谢)",
    "point_index": "食指指向(方向)",
    "peace": "剪刀手(胜利)",
    "ok_sign": "OK手势",
    "wave": "摆手(问候)",
    "heart": "比心(谢谢)",
    "call_me": "打电话",
    "neutral": "无手势",
}


# ═══════════════════════════════════════════════════════════════════════════════
# MediaPipe关键点提取
# ═══════════════════════════════════════════════════════════════════════════════

def init_mediapipe():
    """初始化MediaPipe Hands（延迟导入避免启动慢）"""
    import mediapipe as mp
    mp_hands = mp.solutions.hands
    hands = mp_hands.Hands(
        static_image_mode=True,       # 图片模式（非视频流）
        max_num_hands=1,              # 只取一只手
        min_detection_confidence=0.5, # 检测置信度阈值
        min_tracking_confidence=0.5   # 追踪置信度阈值
    )
    return hands, mp_hands


def extract_landmarks_from_image(image_path: str, hands, mp_hands) -> Optional[List[float]]:
    """
    从单张图片提取21个手部关键点 → 63维向量

    Args:
        image_path: 图片路径
        hands: MediaPipe Hands实例
        mp_hands: MediaPipe Hands模块

    Returns:
        63维浮点数列表 [x0,y0,z0,...,x20,y20,z20]，或None（未检测到手）
    """
    import cv2
    img = cv2.imread(image_path)
    if img is None:
        return None

    # 转RGB（MediaPipe要求）
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    h, w, _ = img.shape

    results = hands.process(img_rgb)

    if results.multi_hand_landmarks:
        # 取第一只检测到的手
        landmarks = results.multi_hand_landmarks[0]
        vector = []
        for lm in landmarks.landmark:
            vector.extend([lm.x, lm.y, lm.z])
        return vector

    return None


# ═══════════════════════════════════════════════════════════════════════════════
# 数据集处理
# ═══════════════════════════════════════════════════════════════════════════════

def process_leapgestrecog(data_dir: str, hands, mp_hands) -> Dict[str, List[List[float]]]:
    """
    处理Kaggle LeapGestRecog数据集
    目录结构: data_dir/00/01_palm/frame_00_01_0001.png
    """
    from tqdm import tqdm

    vectors: Dict[str, List[List[float]]] = {label: [] for label in SUPPORTED_LABELS}
    total_processed = 0
    total_detected = 0

    data_path = Path(data_dir)
    # 遍历所有子目录（00, 01, ... 代表人）
    subject_dirs = sorted([d for d in data_path.iterdir() if d.is_dir()])

    for subject_dir in subject_dirs:
        gesture_dirs = sorted([d for d in subject_dir.iterdir() if d.is_dir()])
        for gesture_dir in gesture_dirs:
            raw_label = gesture_dir.name
            # 映射到标准标签
            std_label = LEAPGESTRECOG_LABEL_MAP.get(raw_label)
            if std_label is None:
                print(f"  [跳过] 未知手势: {raw_label}")
                continue

            # 处理该目录下所有图片
            image_files = list(gesture_dir.glob("*.png")) + list(gesture_dir.glob("*.jpg"))
            for img_file in tqdm(image_files, desc=f"{subject_dir.name}/{raw_label}", leave=False):
                total_processed += 1
                vec = extract_landmarks_from_image(str(img_file), hands, mp_hands)
                if vec is not None and len(vec) == 63:
                    vectors[std_label].append(vec)
                    total_detected += 1

    print(f"\n总处理: {total_processed} 张图片, 检测到手: {total_detected} 张 ({100*total_detected/max(1,total_processed):.1f}%)")
    return vectors


def process_custom_folder(data_dir: str, hands, mp_hands, label_map: Optional[Dict[str, str]] = None) -> Dict[str, List[List[float]]]:
    """
    处理自定义图片文件夹
    目录结构: data_dir/gesture_name/*.png
    每个子文件夹名=手势标签
    """
    from tqdm import tqdm

    vectors: Dict[str, List[List[float]]] = {label: [] for label in SUPPORTED_LABELS}
    total_processed = 0
    total_detected = 0

    data_path = Path(data_dir)
    gesture_dirs = sorted([d for d in data_path.iterdir() if d.is_dir()])

    for gesture_dir in gesture_dirs:
        raw_label = gesture_dir.name.lower().strip()
        # 映射到标准标签
        if label_map and raw_label in label_map:
            std_label = label_map[raw_label]
        elif raw_label in SUPPORTED_LABELS:
            std_label = raw_label
        else:
            print(f"  [跳过] 未知手势: {raw_label}")
            continue

        image_files = list(gesture_dir.glob("*.png")) + list(gesture_dir.glob("*.jpg")) + list(gesture_dir.glob("*.jpeg"))
        for img_file in tqdm(image_files, desc=raw_label, leave=False):
            total_processed += 1
            vec = extract_landmarks_from_image(str(img_file), hands, mp_hands)
            if vec is not None and len(vec) == 63:
                vectors[std_label].append(vec)
                total_detected += 1

    print(f"\n总处理: {total_processed} 张图片, 检测到手: {total_detected} 张 ({100*total_detected/max(1,total_processed):.1f}%)")
    return vectors


def process_synthetic() -> Dict[str, List[List[float]]]:
    """
    使用现有合成数据生成器生成参考向量
    不需要图片，直接从generate_realistic_data.py导入
    """
    from generate_realistic_data import generate_one_sample, get_canonical_pose

    vectors: Dict[str, List[List[float]]] = {label: [] for label in SUPPORTED_LABELS}

    print("使用合成数据生成器生成参考向量...")
    for idx, label in enumerate(SUPPORTED_LABELS):
        # 为每种手势生成100个合成样本
        print(f"  生成 {label} ({LABEL_NAMES_ZH.get(label, '')}) 样本...")
        for i in range(100):
            try:
                vec = generate_one_sample(idx, i)
                if vec is not None:
                    vectors[label].append(vec.flatten().tolist())
            except Exception as e:
                pass  # 跳过生成失败的样本

    return vectors


# ═══════════════════════════════════════════════════════════════════════════════
# 质心计算与导出
# ═══════════════════════════════════════════════════════════════════════════════

def compute_centroids(vectors: Dict[str, List[List[float]]]) -> Dict[str, dict]:
    """
    计算每种手势的质心向量和统计信息

    Returns:
        {
            "fist": {
                "centroid": [63 floats],
                "count": int,
                "std": [63 floats],
                "label_zh": "握拳(SOS)"
            },
            ...
        }
    """
    result = {}
    for label in SUPPORTED_LABELS:
        vecs = vectors.get(label, [])
        if len(vecs) == 0:
            # 没有数据 → 使用GestureReferenceVectors中的默认值
            print(f"  [警告] {label} 无数据，将使用默认参考向量")
            continue

        arr = np.array(vecs)
        centroid = np.mean(arr, axis=0).tolist()
        std = np.std(arr, axis=0).tolist()

        result[label] = {
            "centroid": [round(v, 6) for v in centroid],
            "count": len(vecs),
            "std": [round(v, 6) for v in std],
            "label_zh": LABEL_NAMES_ZH.get(label, label),
        }

        # 打印统计信息
        if len(vecs) > 0:
            print(f"  {label:15s} ({LABEL_NAMES_ZH.get(label, '')}): "
                  f"{len(vecs):5d} 样本, "
                  f"质心范数={np.linalg.norm(centroid):.4f}, "
                  f"平均标准差={np.mean(std):.4f}")

    return result


def export_to_json(centroids: dict, output_path: str):
    """导出质心向量到JSON文件"""
    output = {
        "version": "1.0",
        "created_at": time.strftime("%Y-%m-%d %H:%M:%S"),
        "num_gestures": len(centroids),
        "description": "手势质心向量库，用于GestureVectorDB余弦相似度匹配",
        "gestures": centroids,
    }

    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    # 同时生成Kotlin格式的常量文件（可选）
    kotlin_path = output_path.replace(".json", "_kotlin.txt")
    with open(kotlin_path, "w", encoding="utf-8") as f:
        f.write("// 自动生成的手势质心向量（从公开数据集提取）\n")
        f.write(f"// 生成时间: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"// 手势数量: {len(centroids)}\n\n")
        for label, info in centroids.items():
            vec_str = ", ".join([f"{v:.6f}f" for v in info["centroid"]])
            f.write(f"// {info['label_zh']} ({info['count']} 样本)\n")
            f.write(f"val {label.upper()}_CENTROID = floatArrayOf({vec_str})\n\n")

    print(f"\n质心JSON已导出: {output_path}")
    print(f"Kotlin常量已导出: {kotlin_path}")


# ═══════════════════════════════════════════════════════════════════════════════
# 主流程
# ═══════════════════════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(
        description="从公开手语/手势图片数据集中提取MediaPipe关键点并生成质心向量"
    )
    parser.add_argument(
        "--dataset", type=str, default="synthetic",
        choices=["leapgestrecog", "asl", "custom", "synthetic"],
        help="数据集类型（默认: synthetic）"
    )
    parser.add_argument(
        "--data_dir", type=str, default="./data",
        help="数据集目录路径"
    )
    parser.add_argument(
        "--output", type=str, default="./model/gesture_centroids.json",
        help="输出JSON文件路径"
    )
    parser.add_argument(
        "--label_map", type=str, default=None,
        help="自定义标签映射JSON文件（仅custom模式）"
    )
    args = parser.parse_args()

    print("=" * 60)
    print("  公开数据集关键点提取流水线")
    print("=" * 60)
    print(f"  数据集类型: {args.dataset}")
    print(f"  数据目录:   {args.data_dir}")
    print(f"  输出文件:   {args.output}")
    print("=" * 60)

    # 加载标签映射
    label_map = None
    if args.label_map and os.path.exists(args.label_map):
        with open(args.label_map, "r", encoding="utf-8") as f:
            label_map = json.load(f)

    # 初始化MediaPipe
    if args.dataset != "synthetic":
        print("\n[1/3] 初始化MediaPipe Hands...")
        hands, mp_hands = init_mediapipe()
        print("  MediaPipe Hands 初始化完成")
    else:
        hands, mp_hands = None, None

    # 处理数据集
    print(f"\n[2/3] 处理数据集: {args.dataset}...")
    if args.dataset == "leapgestrecog":
        vectors = process_leapgestrecog(args.data_dir, hands, mp_hands)
    elif args.dataset == "asl":
        vectors = process_custom_folder(args.data_dir, hands, mp_hands, ASL_LABEL_MAP)
    elif args.dataset == "custom":
        vectors = process_custom_folder(args.data_dir, hands, mp_hands, label_map)
    else:
        vectors = process_synthetic()

    # 统计
    print("\n  数据统计:")
    total = 0
    for label, vecs in vectors.items():
        if vecs:
            print(f"    {label:15s}: {len(vecs):5d} 个有效向量")
            total += len(vecs)
    print(f"    总计: {total} 个向量")

    # 计算质心并导出
    print(f"\n[3/3] 计算质心并导出...")
    centroids = compute_centroids(vectors)
    export_to_json(centroids, args.output)

    print("\n" + "=" * 60)
    print("  流水线执行完成！")
    print(f"  质心JSON: {args.output}")
    print("=" * 60)


if __name__ == "__main__":
    main()