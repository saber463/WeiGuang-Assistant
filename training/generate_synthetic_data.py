"""
合成手势数据生成器
===================
功能：为每种手势定义标准关键点模板，添加随机噪声/缩放/旋转/平移，生成逼真的训练数据
用途：在没有真实采集数据时，快速验证训练流程并获取基准准确率

使用方式：
    python generate_synthetic_data.py

输出：
    dataset/synthetic_gesture_data.csv  — 合成数据（10种手势 × 200帧 = 2000条）
    dataset/synthetic_label_map.txt     — 标签映射

工作原理：
    1. 对每种手势定义21个关键点的标准坐标（归一化到[0,1]）
    2. 每帧加入随机扰动：噪声(σ=0.008)、缩放(0.85~1.15)、旋转(±15°)、平移(±0.03)
    3. 模拟真实使用场景下的手部位置变化
"""

import csv
import os
import numpy as np

# ─────────────────────────────────────────────────────────────────────────────
# 配置
# ─────────────────────────────────────────────────────────────────────────────

SAMPLES_PER_GESTURE = 200          # 每种手势生成帧数
NOISE_STD = 0.008                  # 高斯噪声标准差
SCALE_RANGE = (0.85, 1.15)         # 缩放范围
ROTATION_DEGREES = 15              # 最大旋转角度（度）
TRANSLATION_RANGE = 0.03           # 最大平移量
RANDOM_SEED = 42                   # 随机种子（保证可复现）

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "dataset")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "synthetic_gesture_data.csv")

LABEL_NAMES = [
    "fist", "open_palm", "thumbs_up", "point_index", "peace",
    "ok_sign", "wave", "heart", "call_me", "neutral", "thumb_down"
]

# ─────────────────────────────────────────────────────────────────────────────
# 手势模板：每种手势的21个关键点归一化坐标 (x, y, z)
# 坐标系：原点在手腕，x向右，y向下，z向前（出屏幕）
# 每个坐标值范围 [0, 1]，相对于手腕(0, 0.5, 0)的位置
# ─────────────────────────────────────────────────────────────────────────────

def get_canonical_pose(gesture_idx: int) -> np.ndarray:
    """
    返回某种手势的21个关键点标准坐标 (21, 3)

    关键点索引（MediaPipe标准）：
        0=手腕, 1=拇指CMC, 2=拇指MCP, 3=拇指IP, 4=拇指指尖
        5=食指MCP, 6=食指PIP, 7=食指DIP, 8=食指指尖
        9=中指MCP, 10=中指PIP, 11=中指DIP, 12=中指指尖
        13=无名指MCP, 14=无名指PIP, 15=无名指DIP, 16=无名指指尖
        17=小指MCP, 18=小指PIP, 19=小指DIP, 20=小指指尖
    """
    # 基础手掌骨架（所有手势共享）
    # 手腕在原点，手掌朝上展开
    wrist = np.array([0.50, 0.60, 0.0])

    # 拇指关键点（从手腕到指尖）
    thumb_cmc = np.array([0.42, 0.55, 0.0])   # 拇指根部
    thumb_mcp = np.array([0.38, 0.48, 0.0])   # 拇指掌指关节
    thumb_ip  = np.array([0.34, 0.40, 0.0])   # 拇指指间关节
    thumb_tip_base = np.array([0.30, 0.33, 0.0])  # 拇指指尖基础位置

    # 食指关键点
    index_mcp = np.array([0.55, 0.45, 0.0])   # 食指根部
    index_pip = np.array([0.58, 0.32, 0.0])   # 食指近端指间关节
    index_dip = np.array([0.60, 0.22, 0.0])   # 食指远端指间关节
    index_tip_base = np.array([0.61, 0.12, 0.0])  # 食指尖基础位置

    # 中指关键点
    middle_mcp = np.array([0.50, 0.42, 0.0])
    middle_pip = np.array([0.50, 0.25, 0.0])
    middle_dip = np.array([0.50, 0.14, 0.0])
    middle_tip_base = np.array([0.50, 0.04, 0.0])

    # 无名指关键点
    ring_mcp = np.array([0.45, 0.45, 0.0])
    ring_pip = np.array([0.42, 0.30, 0.0])
    ring_dip = np.array([0.40, 0.19, 0.0])
    ring_tip_base = np.array([0.39, 0.09, 0.0])

    # 小指关键点
    pinky_mcp = np.array([0.40, 0.50, 0.0])
    pinky_pip = np.array([0.36, 0.38, 0.0])
    pinky_dip = np.array([0.33, 0.28, 0.0])
    pinky_tip_base = np.array([0.31, 0.19, 0.0])

    # 构建基础关键点数组
    base_pose = np.array([
        wrist,
        thumb_cmc, thumb_mcp, thumb_ip, thumb_tip_base,
        index_mcp, index_pip, index_dip, index_tip_base,
        middle_mcp, middle_pip, middle_dip, middle_tip_base,
        ring_mcp, ring_pip, ring_dip, ring_tip_base,
        pinky_mcp, pinky_pip, pinky_dip, pinky_tip_base
    ])

    # ── 根据手势类型调整指尖位置 ──
    pose = base_pose.copy()

    if gesture_idx == 0:  # 握拳 fist
        # 所有手指弯曲，指尖靠近手掌中心
        pose[4]  = np.array([0.44, 0.50, 0.0])   # 拇指弯曲内收
        pose[8]  = np.array([0.54, 0.48, 0.0])   # 食指弯曲
        pose[12] = np.array([0.50, 0.46, 0.0])   # 中指弯曲
        pose[16] = np.array([0.46, 0.48, 0.0])   # 无名指弯曲
        pose[20] = np.array([0.42, 0.50, 0.0])   # 小指弯曲

    elif gesture_idx == 1:  # 手掌张开 open_palm
        # 所有手指完全伸展，向外张开
        pose[4]  = np.array([0.25, 0.28, 0.0])   # 拇指外展
        pose[8]  = np.array([0.63, 0.08, 0.0])   # 食指伸展
        pose[12] = np.array([0.50, 0.02, 0.0])   # 中指伸展
        pose[16] = np.array([0.37, 0.06, 0.0])   # 无名指伸展
        pose[20] = np.array([0.28, 0.15, 0.0])   # 小指伸展

    elif gesture_idx == 2:  # 竖大拇指 thumbs_up
        # 拇指向上，其他手指握拳
        pose[4]  = np.array([0.30, 0.15, 0.0])   # 拇指竖起
        pose[8]  = np.array([0.54, 0.48, 0.0])   # 其他手指弯曲
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 3:  # 食指指向 point_index
        # 食指伸直指向，其他手指握拳
        pose[4]  = np.array([0.44, 0.50, 0.0])   # 拇指弯曲
        pose[8]  = np.array([0.63, 0.06, 0.0])   # 食指伸直指向前方
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 4:  # 剪刀手 peace
        # 食指和中指伸直，其他手指弯曲
        pose[4]  = np.array([0.44, 0.50, 0.0])   # 拇指压住无名指小指
        pose[8]  = np.array([0.62, 0.06, 0.0])   # 食指V
        pose[12] = np.array([0.54, 0.04, 0.0])   # 中指V
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 5:  # OK手势 ok_sign
        # 拇指和食指指尖接触形成圆圈，其他三指伸直
        pose[4]  = np.array([0.50, 0.28, 0.0])   # 拇指尖接触食指尖
        pose[8]  = np.array([0.50, 0.28, 0.0])   # 食指尖接触拇指尖
        pose[12] = np.array([0.50, 0.04, 0.0])   # 中指伸直
        pose[16] = np.array([0.39, 0.09, 0.0])   # 无名指伸直
        pose[20] = np.array([0.31, 0.19, 0.0])   # 小指伸直

    elif gesture_idx == 6:  # 摆手 wave
        # 手掌张开但略微倾斜，模拟挥手动作
        pose[4]  = np.array([0.27, 0.30, 0.0])
        pose[8]  = np.array([0.61, 0.10, 0.0])
        pose[12] = np.array([0.50, 0.03, 0.0])
        pose[16] = np.array([0.38, 0.08, 0.0])
        pose[20] = np.array([0.29, 0.17, 0.0])

    elif gesture_idx == 7:  # 比心 heart
        # 拇指和食指交叉形成心形
        pose[4]  = np.array([0.48, 0.22, 0.0])   # 拇指弯曲
        pose[8]  = np.array([0.55, 0.20, 0.0])   # 食指弯曲
        pose[12] = np.array([0.50, 0.04, 0.0])
        pose[16] = np.array([0.39, 0.09, 0.0])
        pose[20] = np.array([0.31, 0.19, 0.0])

    elif gesture_idx == 8:  # 打电话 call_me
        # 拇指和小指伸直，中间三指弯曲
        pose[4]  = np.array([0.30, 0.20, 0.0])   # 拇指竖起
        pose[8]  = np.array([0.54, 0.48, 0.0])   # 食指弯曲
        pose[12] = np.array([0.50, 0.46, 0.0])   # 中指弯曲
        pose[16] = np.array([0.46, 0.48, 0.0])   # 无名指弯曲
        pose[20] = np.array([0.25, 0.18, 0.0])   # 小指竖起

    elif gesture_idx == 9:  # 无手势 neutral
        # 手指自然半弯曲，放松状态
        pose[4]  = np.array([0.35, 0.38, 0.0])
        pose[8]  = np.array([0.58, 0.22, 0.0])
        pose[12] = np.array([0.50, 0.14, 0.0])
        pose[16] = np.array([0.42, 0.19, 0.0])
        pose[20] = np.array([0.34, 0.28, 0.0])

    elif gesture_idx == 10:  # 拇指朝下 thumb_down
        # 拇指弯曲向下，其他四指握拳
        pose[1]  = np.array([0.42, 0.55, 0.0])  # thumb_cmc
        pose[2]  = np.array([0.38, 0.48, 0.0])  # thumb_mcp
        pose[3]  = np.array([0.36, 0.58, 0.0])  # thumb_ip（向下弯曲）
        pose[4]  = np.array([0.34, 0.68, 0.0])  # thumb_tip（朝下）
        pose[8]  = np.array([0.54, 0.48, 0.0])  # 食指弯曲
        pose[12] = np.array([0.50, 0.46, 0.0])  # 中指弯曲
        pose[16] = np.array([0.46, 0.48, 0.0])  # 无名指弯曲
        pose[20] = np.array([0.42, 0.50, 0.0])  # 小指弯曲

    return pose


def apply_augmentation(pose: np.ndarray) -> np.ndarray:
    """
    对手势关键点应用随机扰动，模拟真实采集中的变化

    扰动类型：
    1. 高斯噪声：模拟MediaPipe检测误差
    2. 随机缩放：模拟手离摄像头远近不同
    3. 随机旋转：模拟手部倾斜
    4. 随机平移：模拟手在画面中的位置变化

    参数：
        pose: (21, 3) 标准关键点坐标

    返回：
        (21, 3) 扰动后的关键点坐标
    """
    augmented = pose.copy()

    # 1. 高斯噪声
    noise = np.random.normal(0, NOISE_STD, pose.shape)
    augmented += noise

    # 2. 随机缩放（以手腕为中心）
    scale = np.random.uniform(*SCALE_RANGE)
    wrist = augmented[0].copy()
    augmented -= wrist
    augmented *= scale
    augmented += wrist

    # 3. 随机旋转（绕Z轴，即垂直于屏幕的轴）
    angle = np.random.uniform(-ROTATION_DEGREES, ROTATION_DEGREES)
    rad = np.radians(angle)
    cos_a, sin_a = np.cos(rad), np.sin(rad)
    wrist = augmented[0].copy()
    augmented -= wrist
    for i in range(21):
        x, y = augmented[i, 0], augmented[i, 1]
        augmented[i, 0] = x * cos_a - y * sin_a
        augmented[i, 1] = x * sin_a + y * cos_a
    augmented += wrist

    # 4. 随机平移
    tx = np.random.uniform(-TRANSLATION_RANGE, TRANSLATION_RANGE)
    ty = np.random.uniform(-TRANSLATION_RANGE, TRANSLATION_RANGE)
    augmented[:, 0] += tx
    augmented[:, 1] += ty

    # 裁剪到 [0, 1] 范围
    augmented[:, :2] = np.clip(augmented[:, :2], 0.0, 1.0)

    return augmented


def generate_dataset() -> tuple:
    """
    生成完整合成数据集

    返回：
        (X, y) — 特征矩阵 (N, 63) 和标签数组 (N,)
    """
    np.random.seed(RANDOM_SEED)

    X_list = []
    y_list = []

    for gesture_idx in range(len(LABEL_NAMES)):
        canonical = get_canonical_pose(gesture_idx)

        for sample_idx in range(SAMPLES_PER_GESTURE):
            if sample_idx == 0:
                # 第一帧使用标准姿态（不加噪声），作为该手势的基准
                augmented = canonical.copy()
            else:
                augmented = apply_augmentation(canonical)

            # 展平为 (63,) 的一维数组
            features = augmented.flatten()
            X_list.append(features)
            y_list.append(gesture_idx)

        print(f"  [{gesture_idx}] {LABEL_NAMES[gesture_idx]:12s}: {SAMPLES_PER_GESTURE} 帧")

    X = np.array(X_list, dtype=np.float32)
    y = np.array(y_list, dtype=np.int32)

    return X, y


def save_dataset(X: np.ndarray, y: np.ndarray):
    """保存数据集为CSV格式"""
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 构建列名
    columns = []
    for i in range(21):
        columns.extend([f"x{i}", f"y{i}", f"z{i}"])
    columns.append("label")

    # 写入CSV
    with open(OUTPUT_FILE, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(columns)
        for i in range(len(X)):
            row = list(X[i]) + [int(y[i])]
            writer.writerow(row)

    print(f"\n[保存] {OUTPUT_FILE}")
    print(f"  总数据: {len(X)} 条")
    print(f"  特征维度: {X.shape[1]}")

    # 标签映射
    label_map_path = os.path.join(OUTPUT_DIR, "synthetic_label_map.txt")
    with open(label_map_path, "w", encoding="utf-8") as f:
        for idx, name in enumerate(LABEL_NAMES):
            f.write(f"{idx} = {name}\n")

    print(f"  标签映射: {label_map_path}")


def main():
    print("=" * 60)
    print("  合成手势数据生成器")
    print("=" * 60)
    print(f"\n配置: 每种手势 {SAMPLES_PER_GESTURE} 帧")
    print(f"噪声: σ={NOISE_STD}, 缩放: {SCALE_RANGE}, 旋转: ±{ROTATION_DEGREES}°")

    print("\n生成数据...")
    X, y = generate_dataset()

    save_dataset(X, y)

    print("\n完成！现在可以运行训练:")
    print(f"  python train_gesture_classifier.py --csv {OUTPUT_FILE}")
    print(f"  python train_gesture_deep.py --csv {OUTPUT_FILE}")


if __name__ == "__main__":
    main()