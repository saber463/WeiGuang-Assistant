"""
高保真手势数据生成器
=====================
模拟真实场景下的手部变化，生成更接近真机采集效果的训练数据

与 generate_synthetic_data.py 的区别：
    1. 样本量更大：每种手势 1000 帧（vs 200帧）
    2. 手型变化：模拟不同手型大小、手指长度比例
    3. 视角变化：模拟摄像头不同角度、手部3D旋转
    4. 遮挡模拟：随机隐藏部分关键点（模拟手指被遮挡）
    5. 检测误差：模拟 MediaPipe 在不同光照下的检测抖动
    6. 背景噪声：关键点坐标叠加更真实的噪声分布

使用方式：
    python generate_realistic_data.py

输出：
    dataset/realistic_gesture_data.csv  — 10000条数据（10种手势 × 1000帧）
"""

import csv
import os
import numpy as np

# ─────────────────────────────────────────────────────────────────────────────
# 配置
# ─────────────────────────────────────────────────────────────────────────────

SAMPLES_PER_GESTURE = 1000         # 每种手势帧数（总共10,000条）
RANDOM_SEED = 42                   # 可复现

# 噪声参数（模拟真实 MediaPipe 在不同条件下的检测误差）
NOISE_BASE = 0.003                 # 基础噪声（理想光线）
NOISE_LOW_LIGHT = 0.012            # 低光照额外噪声
NOISE_OCCLUSION = 0.020            # 部分遮挡时噪声更大

# 手型变化参数
HAND_SCALE_RANGE = (0.75, 1.30)    # 手型大小范围（模拟不同人的手）
FINGER_LENGTH_VAR = 0.12           # 手指长度个体差异（±12%）
PALM_WIDTH_VAR = 0.10              # 手掌宽度个体差异

# 视角变化参数
ROTATION_X_RANGE = (-25, 25)       # 绕X轴旋转（上下倾斜，度）
ROTATION_Y_RANGE = (-30, 30)       # 绕Y轴旋转（左右倾斜，度）
ROTATION_Z_RANGE = (-20, 20)       # 绕Z轴旋转（平面内旋转，度）

# 平移变化
TRANSLATION_RANGE = 0.06           # 手在画面中的位置变化

# 遮挡概率
OCCLUSION_PROB = 0.08              # 8%的帧有部分遮挡

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "dataset")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "realistic_gesture_data.csv")

LABEL_NAMES = [
    "fist", "open_palm", "thumbs_up", "point_index", "peace",
    "ok_sign", "wave", "heart", "call_me", "neutral", "thumb_down"
]


def get_canonical_pose(gesture_idx: int) -> np.ndarray:
    """
    返回手势的标准关键点坐标 (21, 3)
    坐标已归一化到 [0, 1]，原点在手腕处
    """
    wrist = np.array([0.50, 0.60, 0.0])

    thumb_cmc = np.array([0.42, 0.55, 0.0])
    thumb_mcp = np.array([0.38, 0.48, 0.0])
    thumb_ip  = np.array([0.34, 0.40, 0.0])
    thumb_tip = np.array([0.30, 0.33, 0.0])

    index_mcp = np.array([0.55, 0.45, 0.0])
    index_pip = np.array([0.58, 0.32, 0.0])
    index_dip = np.array([0.60, 0.22, 0.0])
    index_tip = np.array([0.61, 0.12, 0.0])

    middle_mcp = np.array([0.50, 0.42, 0.0])
    middle_pip = np.array([0.50, 0.25, 0.0])
    middle_dip = np.array([0.50, 0.14, 0.0])
    middle_tip = np.array([0.50, 0.04, 0.0])

    ring_mcp = np.array([0.45, 0.45, 0.0])
    ring_pip = np.array([0.42, 0.30, 0.0])
    ring_dip = np.array([0.40, 0.19, 0.0])
    ring_tip = np.array([0.39, 0.09, 0.0])

    pinky_mcp = np.array([0.40, 0.50, 0.0])
    pinky_pip = np.array([0.36, 0.38, 0.0])
    pinky_dip = np.array([0.33, 0.28, 0.0])
    pinky_tip = np.array([0.31, 0.19, 0.0])

    pose = np.array([
        wrist,
        thumb_cmc, thumb_mcp, thumb_ip, thumb_tip,
        index_mcp, index_pip, index_dip, index_tip,
        middle_mcp, middle_pip, middle_dip, middle_tip,
        ring_mcp, ring_pip, ring_dip, ring_tip,
        pinky_mcp, pinky_pip, pinky_dip, pinky_tip
    ])

    # 应用手势特定的指尖位置
    if gesture_idx == 0:  # 握拳
        pose[4]  = np.array([0.44, 0.50, 0.0])
        pose[8]  = np.array([0.54, 0.48, 0.0])
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 1:  # 手掌张开
        pose[4]  = np.array([0.25, 0.28, 0.0])
        pose[8]  = np.array([0.63, 0.08, 0.0])
        pose[12] = np.array([0.50, 0.02, 0.0])
        pose[16] = np.array([0.37, 0.06, 0.0])
        pose[20] = np.array([0.28, 0.15, 0.0])

    elif gesture_idx == 2:  # 竖大拇指
        pose[4]  = np.array([0.30, 0.15, 0.0])
        pose[8]  = np.array([0.54, 0.48, 0.0])
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 3:  # 食指指向
        pose[4]  = np.array([0.44, 0.50, 0.0])
        pose[8]  = np.array([0.63, 0.06, 0.0])
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 4:  # 剪刀手
        pose[4]  = np.array([0.44, 0.50, 0.0])
        pose[8]  = np.array([0.62, 0.06, 0.0])
        pose[12] = np.array([0.54, 0.04, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    elif gesture_idx == 5:  # OK
        pose[4]  = np.array([0.50, 0.28, 0.0])
        pose[8]  = np.array([0.50, 0.28, 0.0])
        pose[12] = np.array([0.50, 0.04, 0.0])
        pose[16] = np.array([0.39, 0.09, 0.0])
        pose[20] = np.array([0.31, 0.19, 0.0])

    elif gesture_idx == 6:  # 摆手
        pose[4]  = np.array([0.27, 0.30, 0.0])
        pose[8]  = np.array([0.61, 0.10, 0.0])
        pose[12] = np.array([0.50, 0.03, 0.0])
        pose[16] = np.array([0.38, 0.08, 0.0])
        pose[20] = np.array([0.29, 0.17, 0.0])

    elif gesture_idx == 7:  # 比心
        pose[4]  = np.array([0.48, 0.22, 0.0])
        pose[8]  = np.array([0.55, 0.20, 0.0])
        pose[12] = np.array([0.50, 0.04, 0.0])
        pose[16] = np.array([0.39, 0.09, 0.0])
        pose[20] = np.array([0.31, 0.19, 0.0])

    elif gesture_idx == 8:  # 打电话
        pose[4]  = np.array([0.30, 0.20, 0.0])
        pose[8]  = np.array([0.54, 0.48, 0.0])
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.25, 0.18, 0.0])

    elif gesture_idx == 9:  # 无手势
        pose[4]  = np.array([0.35, 0.38, 0.0])
        pose[8]  = np.array([0.58, 0.22, 0.0])
        pose[12] = np.array([0.50, 0.14, 0.0])
        pose[16] = np.array([0.42, 0.19, 0.0])
        pose[20] = np.array([0.34, 0.28, 0.0])

    elif gesture_idx == 10:  # 拇指朝下/谢谢（拇指弯曲向下，其他四指握拳）
        # 拇指弯曲向下：CMC→MCP→IP→TIP，y递增表示朝下
        pose[1]  = np.array([0.42, 0.55, 0.0])  # thumb_cmc
        pose[2]  = np.array([0.38, 0.48, 0.0])  # thumb_mcp
        pose[3]  = np.array([0.36, 0.58, 0.0])  # thumb_ip（开始向下）
        pose[4]  = np.array([0.34, 0.68, 0.0])  # thumb_tip（朝下指向）
        # 其他四指握拳
        pose[8]  = np.array([0.54, 0.48, 0.0])
        pose[12] = np.array([0.50, 0.46, 0.0])
        pose[16] = np.array([0.46, 0.48, 0.0])
        pose[20] = np.array([0.42, 0.50, 0.0])

    return pose


def apply_hand_variation(pose: np.ndarray) -> np.ndarray:
    """
    模拟不同人的手型差异：手大小、手指长度比例、手掌宽度

    每个人的手型都不同，这个函数模拟这种个体差异
    """
    varied = pose.copy()
    wrist = varied[0].copy()
    varied -= wrist

    # 整体手型缩放
    hand_scale = np.random.uniform(*HAND_SCALE_RANGE)
    varied *= hand_scale

    # 手指长度个体差异（每根手指独立变化）
    finger_groups = [
        [1, 2, 3, 4],        # 拇指
        [5, 6, 7, 8],        # 食指
        [9, 10, 11, 12],     # 中指
        [13, 14, 15, 16],    # 无名指
        [17, 18, 19, 20],    # 小指
    ]

    for group in finger_groups:
        factor = 1.0 + np.random.uniform(-FINGER_LENGTH_VAR, FINGER_LENGTH_VAR)
        for idx in group:
            varied[idx] *= factor

    # 手掌宽度变化
    palm_factor = 1.0 + np.random.uniform(-PALM_WIDTH_VAR, PALM_WIDTH_VAR)
    for idx in [1, 5, 9, 13, 17]:  # 各手指MCP关节（手掌宽度方向）
        varied[idx, 0] *= palm_factor

    varied += wrist
    return varied


def apply_3d_rotation(pose: np.ndarray) -> np.ndarray:
    """
    模拟3D手部旋转（不同视角下的手部姿态）

    绕X轴旋转：上下倾斜（手向上/向下）
    绕Y轴旋转：左右倾斜（手向左/向右偏）
    绕Z轴旋转：平面内旋转（手顺时针/逆时针转）
    """
    rotated = pose.copy()
    wrist = rotated[0].copy()
    rotated -= wrist

    # 随机旋转角度
    rx = np.radians(np.random.uniform(*ROTATION_X_RANGE))
    ry = np.radians(np.random.uniform(*ROTATION_Y_RANGE))
    rz = np.radians(np.random.uniform(*ROTATION_Z_RANGE))

    # 绕X轴旋转矩阵
    Rx = np.array([
        [1, 0, 0],
        [0, np.cos(rx), -np.sin(rx)],
        [0, np.sin(rx), np.cos(rx)]
    ])

    # 绕Y轴旋转矩阵
    Ry = np.array([
        [np.cos(ry), 0, np.sin(ry)],
        [0, 1, 0],
        [-np.sin(ry), 0, np.cos(ry)]
    ])

    # 绕Z轴旋转矩阵
    Rz = np.array([
        [np.cos(rz), -np.sin(rz), 0],
        [np.sin(rz), np.cos(rz), 0],
        [0, 0, 1]
    ])

    # 组合旋转
    R = Rz @ Ry @ Rx

    for i in range(21):
        rotated[i] = R @ rotated[i]

    rotated += wrist
    return rotated


def apply_detection_noise(pose: np.ndarray, is_occluded: bool) -> np.ndarray:
    """
    模拟 MediaPipe 在不同条件下的检测误差

    理想光线：基础噪声（σ=0.003）
    低光照：额外噪声（σ=0.012）
    遮挡：更大噪声（σ=0.020）

    噪声分为两部分：
    1. 系统性偏移：整只手的关键点有微小漂移
    2. 随机抖动：每个关键点独立抖动
    """
    noisy = pose.copy()

    # 随机选择光线条件
    light_condition = np.random.choice(["good", "medium", "low"], p=[0.5, 0.35, 0.15])

    if light_condition == "good":
        noise_level = NOISE_BASE
    elif light_condition == "medium":
        noise_level = NOISE_BASE + 0.005
    else:
        noise_level = NOISE_LOW_LIGHT

    if is_occluded:
        noise_level = max(noise_level, NOISE_OCCLUSION)

    # 系统性偏移（所有关键点一起偏移）
    system_bias = np.random.normal(0, noise_level * 0.5, 3)
    noisy += system_bias

    # 随机抖动（每个关键点独立）
    individual_noise = np.random.normal(0, noise_level, (21, 3))
    noisy += individual_noise

    # 边界裁剪
    noisy[:, :2] = np.clip(noisy[:, :2], 0.0, 1.0)

    return noisy


def apply_occlusion(pose: np.ndarray) -> np.ndarray:
    """
    模拟手指遮挡：随机将部分关键点坐标替换为近似值

    真实场景中，手指经常被其他手指遮挡，导致部分关键点检测不准
    """
    occluded = pose.copy()

    # 随机选择被遮挡的手指（0-3根）
    num_occluded = np.random.choice([0, 1, 2, 3], p=[0.4, 0.3, 0.2, 0.1])

    all_finger_indices = [
        [1, 2, 3, 4],       # 拇指
        [5, 6, 7, 8],       # 食指
        [9, 10, 11, 12],    # 中指
        [13, 14, 15, 16],   # 无名指
        [17, 18, 19, 20],   # 小指
    ]

    occluded_fingers = np.random.choice(
        len(all_finger_indices), size=min(num_occluded, 5), replace=False
    )

    for finger_idx in occluded_fingers:
        for kp_idx in all_finger_indices[finger_idx]:
            # 用相邻关键点位置 + 噪声近似被遮挡的关键点
            occluded[kp_idx] = occluded[kp_idx] + np.random.normal(0, 0.03, 3)

    return occluded


def apply_translation(pose: np.ndarray) -> np.ndarray:
    """随机平移手在画面中的位置"""
    translated = pose.copy()
    tx = np.random.uniform(-TRANSLATION_RANGE, TRANSLATION_RANGE)
    ty = np.random.uniform(-TRANSLATION_RANGE, TRANSLATION_RANGE)
    translated[:, 0] += tx
    translated[:, 1] += ty
    translated[:, :2] = np.clip(translated[:, :2], 0.0, 1.0)
    return translated


def generate_one_sample(gesture_idx: int, sample_idx: int) -> np.ndarray:
    """
    生成单帧手势数据，经过完整的增强管线

    管线顺序：
    1. 获取标准姿态
    2. 手型个体差异
    3. 3D旋转（视角变化）
    4. 平移
    5. 遮挡模拟
    6. 检测噪声
    """
    canonical = get_canonical_pose(gesture_idx)

    # 第0帧使用标准姿态 + 轻微噪声（作为该手势的基准参考）
    if sample_idx == 0:
        return canonical + np.random.normal(0, NOISE_BASE * 0.5, canonical.shape)

    # 完整增强管线
    pose = apply_hand_variation(canonical)
    pose = apply_3d_rotation(pose)
    pose = apply_translation(pose)

    is_occluded = np.random.random() < OCCLUSION_PROB
    if is_occluded:
        pose = apply_occlusion(pose)

    pose = apply_detection_noise(pose, is_occluded)

    return pose


def generate_dataset() -> tuple:
    """生成完整数据集"""
    np.random.seed(RANDOM_SEED)

    X_list = []
    y_list = []

    total = len(LABEL_NAMES) * SAMPLES_PER_GESTURE
    count = 0

    for gesture_idx in range(len(LABEL_NAMES)):
        for sample_idx in range(SAMPLES_PER_GESTURE):
            pose = generate_one_sample(gesture_idx, sample_idx)
            features = pose.flatten()
            X_list.append(features)
            y_list.append(gesture_idx)

            count += 1
            if count % 1000 == 0:
                pct = count / total * 100
                print(f"  进度: {count}/{total} ({pct:.0f}%)")

        print(f"  [{gesture_idx}] {LABEL_NAMES[gesture_idx]:12s}: {SAMPLES_PER_GESTURE} 帧")

    X = np.array(X_list, dtype=np.float32)
    y = np.array(y_list, dtype=np.int32)

    return X, y


def save_dataset(X: np.ndarray, y: np.ndarray):
    """保存为CSV"""
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    columns = []
    for i in range(21):
        columns.extend([f"x{i}", f"y{i}", f"z{i}"])
    columns.append("label")

    with open(OUTPUT_FILE, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(columns)
        for i in range(len(X)):
            row = list(X[i]) + [int(y[i])]
            writer.writerow(row)

    print(f"\n[保存] {OUTPUT_FILE}")
    print(f"  总数据: {len(X)} 条")
    print(f"  特征维度: {X.shape[1]}")

    label_map_path = os.path.join(OUTPUT_DIR, "realistic_label_map.txt")
    with open(label_map_path, "w", encoding="utf-8") as f:
        for idx, name in enumerate(LABEL_NAMES):
            f.write(f"{idx} = {name}\n")


def main():
    print("=" * 60)
    print("  高保真手势数据生成器")
    print("=" * 60)
    print(f"\n配置:")
    print(f"  每种手势: {SAMPLES_PER_GESTURE} 帧")
    print(f"  总数据量: {len(LABEL_NAMES) * SAMPLES_PER_GESTURE} 条")
    print(f"  手型范围: {HAND_SCALE_RANGE}")
    print(f"  3D旋转: X{ROTATION_X_RANGE} Y{ROTATION_Y_RANGE} Z{ROTATION_Z_RANGE}")
    print(f"  光线条件: 50%理想 / 35%中等 / 15%低光")
    print(f"  遮挡概率: {OCCLUSION_PROB*100:.0f}%")

    print(f"\n生成数据...")
    X, y = generate_dataset()

    save_dataset(X, y)

    print(f"\n完成！运行训练:")
    print(f"  python train_gesture_deep.py --csv {OUTPUT_FILE}")


if __name__ == "__main__":
    main()