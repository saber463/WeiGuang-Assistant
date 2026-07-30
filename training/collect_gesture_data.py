"""
手势数据采集脚本
=================
功能：使用摄像头实时采集手部21个关键点坐标，支持键盘打标签，数据保存为CSV格式

使用方式：
    python collect_gesture_data.py

操作说明：
    - 按数字键 0-9 切换当前标签（标签名在 LABELS 列表中定义）
    - 按 空格键 采集一帧数据（记录当前手部关键点 + 标签）
    - 按 S 键 保存数据到 CSV 文件
    - 按 Q 键 退出

标签体系（面向微光同行APP）：
    0 = 握拳（SOS求助手势）
    1 = 手掌张开（停止/求助）
    2 = 竖大拇指（好/确认）
    3 = 食指指向（那个/方向）
    4 = 剪刀手（胜利/V）
    5 = OK手势（没问题）
    6 = 摆手（你好/再见）
    7 = 比心（爱心/谢谢）
    8 = 打电话（六的手势）
    9 = 无手势（自然状态/负样本）

输出格式：每行63个特征（21个关键点 × 3维坐标 x,y,z）+ 1个标签列
    特征列：x0,y0,z0, x1,y1,z1, ..., x20,y20,z20, label
    标签列：0-9 的数字

依赖安装：
    pip install mediapipe opencv-python numpy pandas
"""

import csv
import os
import sys
from datetime import datetime

import cv2
import mediapipe as mp
import numpy as np
import pandas as pd

# ─────────────────────────────────────────────────────────────────────────────
# 配置区域（可根据需要修改）
# ─────────────────────────────────────────────────────────────────────────────

# 手势标签定义 —— 序号对应键盘数字键，名称对应APP中的手势类型
LABELS = [
    "fist",           # 0 = 握拳（SOS）
    "open_palm",      # 1 = 手掌张开（停止）
    "thumbs_up",      # 2 = 竖大拇指（确认）
    "point_index",    # 3 = 食指指向（方向）
    "peace",          # 4 = 剪刀手（胜利）
    "ok_sign",        # 5 = OK手势（没问题）
    "wave",           # 6 = 摆手（问候）
    "heart",          # 7 = 比心（谢谢）
    "call_me",        # 8 = 打电话（六的手势）
    "neutral",        # 9 = 无手势（负样本/背景）
]

# 每个手势建议采集的帧数（可根据需要调整）
SAMPLES_PER_GESTURE = 200

# 摄像头设备ID（0=默认摄像头，如有多个摄像头可改为1,2...）
CAMERA_ID = 0

# 摄像头分辨率
CAMERA_WIDTH = 640
CAMERA_HEIGHT = 480

# 输出目录
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "dataset")

# ─────────────────────────────────────────────────────────────────────────────
# MediaPipe 初始化
# ─────────────────────────────────────────────────────────────────────────────

mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

# 手部检测器配置
hands_detector = mp_hands.Hands(
    static_image_mode=False,       # 视频流模式
    max_num_hands=1,               # 只检测一只手（右手优先）
    min_detection_confidence=0.7,  # 检测置信度阈值
    min_tracking_confidence=0.5    # 追踪置信度阈值
)

# ─────────────────────────────────────────────────────────────────────────────
# 数据存储
# ─────────────────────────────────────────────────────────────────────────────

# 21个关键点 × 3维坐标 = 63个特征
FEATURE_COUNT = 21 * 3

# 存储所有采集的帧数据
collected_data: list[list[float]] = []

# 统计每个标签已采集的帧数
label_counts: dict[int, int] = {i: 0 for i in range(len(LABELS))}

# 当前选中的标签
current_label: int = 0

# 是否已修改过数据（用于提示保存）
data_modified: bool = False


def extract_landmarks(hand_landmarks) -> list[float]:
    """
    从 MediaPipe HandLandmarks 中提取21个关键点的 (x, y, z) 坐标

    参数：
        hand_landmarks: MediaPipe 检测到的手部关键点对象

    返回：
        63个浮点数的列表 [x0, y0, z0, x1, y1, z1, ..., x20, y20, z20]
    """
    features = []
    for lm in hand_landmarks.landmark:
        features.extend([lm.x, lm.y, lm.z])
    return features


def save_to_csv():
    """
    将采集的数据保存为CSV文件

    文件命名格式：gesture_data_YYYYMMDD_HHMMSS.csv
    列名：x0,y0,z0, x1,y1,z1, ..., x20,y20,z20, label
    """
    global data_modified

    if len(collected_data) == 0:
        print("[警告] 没有数据可保存")
        return

    # 确保输出目录存在
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 生成文件名
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"gesture_data_{timestamp}.csv"
    filepath = os.path.join(OUTPUT_DIR, filename)

    # 构建列名
    columns = []
    for i in range(21):
        columns.extend([f"x{i}", f"y{i}", f"z{i}"])
    columns.append("label")

    # 写入CSV
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(columns)
        writer.writerows(collected_data)

    # 同时保存标签映射表
    label_map_path = os.path.join(OUTPUT_DIR, "label_map.txt")
    with open(label_map_path, "w", encoding="utf-8") as f:
        for idx, name in enumerate(LABELS):
            f.write(f"{idx} = {name}\n")

    print(f"\n[保存成功] 共 {len(collected_data)} 条数据")
    print(f"  文件路径: {filepath}")
    print(f"  标签映射: {label_map_path}")

    # 打印各标签统计
    print("\n各标签采集统计:")
    for idx, name in enumerate(LABELS):
        count = label_counts[idx]
        bar = "█" * (count // 10) + ("░" if count % 10 >= 5 else "")
        print(f"  [{idx}] {name:12s} | {count:4d} 帧 | {bar}")

    data_modified = False


def draw_ui(frame: np.ndarray) -> np.ndarray:
    """
    在摄像头画面上叠加UI信息

    显示内容：
    - 当前标签名称和编号
    - 各标签已采集帧数
    - 操作提示
    """
    h, w = frame.shape[:2]

    # ── 顶部半透明背景条 ──
    overlay = frame.copy()
    cv2.rectangle(overlay, (0, 0), (w, 120), (0, 0, 0), -1)
    frame = cv2.addWeighted(overlay, 0.5, frame, 0.5, 0)

    # ── 当前标签 ──
    label_name = LABELS[current_label]
    cv2.putText(
        frame, f"当前标签: [{current_label}] {label_name}",
        (15, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2
    )

    # ── 该标签已采集数 ──
    count = label_counts[current_label]
    cv2.putText(
        frame, f"已采集: {count} 帧 (目标: {SAMPLES_PER_GESTURE})",
        (15, 65), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 1
    )

    # ── 进度条 ──
    bar_width = int(w * 0.9)
    bar_height = 12
    bar_x, bar_y = 15, 80
    progress = min(count / SAMPLES_PER_GESTURE, 1.0)
    cv2.rectangle(frame, (bar_x, bar_y), (bar_x + bar_width, bar_y + bar_height), (100, 100, 100), -1)
    cv2.rectangle(
        frame, (bar_x, bar_y),
        (bar_x + int(bar_width * progress), bar_y + bar_height),
        (0, 255, 0) if progress < 1.0 else (0, 200, 255), -1
    )

    # ── 底部操作提示 ──
    tips = [
        "[0-9] 切换标签  |  [空格] 采集一帧  |  [S] 保存数据  |  [Q] 退出",
        f"总采集: {len(collected_data)} 帧",
    ]
    for i, tip in enumerate(tips):
        cv2.putText(
            frame, tip, (15, h - 20 - i * 25),
            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1
        )

    return frame


def print_status():
    """在控制台打印当前采集状态"""
    os.system("cls" if os.name == "nt" else "clear")
    print("=" * 60)
    print("  手势数据采集工具 —— 微光同行APP")
    print("=" * 60)
    print(f"\n当前标签: [{current_label}] {LABELS[current_label]}")
    print(f"已采集: {label_counts[current_label]} / {SAMPLES_PER_GESTURE} 帧\n")
    print("各标签进度:")
    for idx, name in enumerate(LABELS):
        count = label_counts[idx]
        marker = " ← 当前" if idx == current_label else ""
        bar_len = 30
        filled = int(bar_len * count / SAMPLES_PER_GESTURE)
        bar = "█" * filled + "░" * (bar_len - filled)
        print(f"  [{idx}] {name:12s} | {bar} | {count:4d}/{SAMPLES_PER_GESTURE}{marker}")
    print(f"\n总采集: {len(collected_data)} 帧")
    print("-" * 60)
    print("操作: [0-9]切换标签 [空格]采集 [S]保存 [Q]退出")


def main():
    """主循环：打开摄像头 → 实时检测手部 → 键盘交互采集"""
    global current_label, data_modified

    print_status()

    # 打开摄像头
    cap = cv2.VideoCapture(CAMERA_ID)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, CAMERA_WIDTH)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, CAMERA_HEIGHT)

    if not cap.isOpened():
        print("[错误] 无法打开摄像头，请检查设备连接")
        sys.exit(1)

    print(f"\n摄像头已打开: {CAMERA_WIDTH}x{CAMERA_HEIGHT}")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("[警告] 无法读取摄像头帧")
            break

        # 镜像翻转（更自然，像照镜子）
        frame = cv2.flip(frame, 1)

        # 转换颜色空间（OpenCV BGR → MediaPipe RGB）
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # 手部检测
        results = hands_detector.process(frame_rgb)

        hand_detected = False
        if results.multi_hand_landmarks:
            hand_detected = True
            for hand_landmarks in results.multi_hand_landmarks:
                # 绘制手部骨架
                mp_drawing.draw_landmarks(
                    frame,
                    hand_landmarks,
                    mp_hands.HAND_CONNECTIONS,
                    mp_drawing_styles.get_default_hand_landmarks_style(),
                    mp_drawing_styles.get_default_hand_connections_style()
                )

                # 在关键点旁标注编号
                for idx, lm in enumerate(hand_landmarks.landmark):
                    cx = int(lm.x * frame.shape[1])
                    cy = int(lm.y * frame.shape[0])
                    cv2.putText(frame, str(idx), (cx, cy),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.3, (255, 255, 255), 1)

        # 绘制UI
        frame = draw_ui(frame)

        # 显示画面
        cv2.imshow("Gesture Data Collector - 微光同行", frame)

        # 键盘交互
        key = cv2.waitKey(1) & 0xFF

        # ── 数字键 0-9：切换标签 ──
        if ord("0") <= key <= ord("9"):
            new_label = key - ord("0")
            if new_label < len(LABELS):
                current_label = new_label
                print_status()

        # ── 空格键：采集一帧 ──
        elif key == ord(" "):
            if hand_detected and results.multi_hand_landmarks:
                landmarks = extract_landmarks(results.multi_hand_landmarks[0])
                landmarks.append(float(current_label))
                collected_data.append(landmarks)
                label_counts[current_label] += 1
                data_modified = True

                # 视觉反馈：画面闪绿
                flash = frame.copy()
                cv2.rectangle(flash, (0, 0), (frame.shape[1], frame.shape[0]), (0, 255, 0), -1)
                frame = cv2.addWeighted(flash, 0.3, frame, 0.7, 0)
                cv2.putText(frame, "已采集!", (frame.shape[1] // 2 - 50, frame.shape[0] // 2),
                            cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0, 255, 0), 3)
                cv2.imshow("Gesture Data Collector - 微光同行", frame)
                cv2.waitKey(200)

                print_status()
            else:
                print("[提示] 未检测到手部，请将手放入摄像头画面中")

        # ── S 键：保存数据 ──
        elif key == ord("s") or key == ord("S"):
            save_to_csv()

        # ── Q 键：退出 ──
        elif key == ord("q") or key == ord("Q"):
            if data_modified:
                print("\n[提示] 有未保存的数据！")
                answer = input("是否保存后退出？(y/n): ").strip().lower()
                if answer == "y":
                    save_to_csv()
            print("\n感谢使用！再见~")
            break

        # ── ESC 键：退出 ──
        elif key == 27:
            if data_modified:
                print("\n[提示] 有未保存的数据！")
            break

    # 释放资源
    cap.release()
    cv2.destroyAllWindows()
    hands_detector.close()


if __name__ == "__main__":
    main()