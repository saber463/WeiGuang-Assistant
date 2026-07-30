"""
YAMNet 迁移学习 —— 弱类合并优化版
===================================
功能：基于 YAMNet 嵌入特征，将易混淆的弱类合并为父类，提升整体分类准确率

合并策略（4组）：
  1. airplane + helicopter          → aircraft（航空器）
  2. keyboard_typing + mouse_click  → typing（键盘鼠标）
  3. pouring_water + water_drops    → water_flow（流水声）
  4. vacuum_cleaner + washing_machine → home_appliance（家电）

合并理由：
  - 飞机和直升机在 ESC-50 中都是引擎轰鸣声，YAMNet 难以区分
  - 键盘打字和鼠标点击都是短暂敲击声，频谱特征相近
  - 倒水和水滴都是液体声，训练数据中经常混淆
  - 吸尘器和洗衣机都是持续低频机械噪音

效果预期：
  - 原始 50 类 5折CV平均: 82.0% ±4.4%
  - 合并后 46 类预期: 85-88%（弱类合并提升稳定性）

依赖：
  pip install tensorflow tensorflow-hub pandas numpy scikit-learn

输出文件：
  - model/audio_yamnet_merged_float32.tflite  (FP32 分类器)
  - model/audio_yamnet_merged_int8.tflite     (INT8 量化分类器)
  - model/audio_yamnet_merged_label_map.json  (标签映射)
  - model/audio_yamnet_merged_cv_report.txt   (CV报告)
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

BASE_DIR = os.path.dirname(__file__)
DATA_DIR = os.path.join(BASE_DIR, "data", "ESC-50-extracted")
AUDIO_DIR = os.path.join(DATA_DIR, "audio")
META_FILE = os.path.join(DATA_DIR, "meta", "esc50.csv")
MODEL_DIR = os.path.join(BASE_DIR, "model")

YAMNET_URL = "https://tfhub.dev/google/yamnet/1"
SAMPLE_RATE = 16000
DURATION_SECONDS = 5.0
BATCH_SIZE = 32
EPOCHS = 200
LEARNING_RATE = 0.001
EARLY_STOPPING_PATIENCE = 30
RANDOM_SEED = 42
EMBEDDING_DIM = 1024

tf.random.set_seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)

# ─────────────────────────────────────────────────────────────────────────────
# 弱类合并映射表
# ─────────────────────────────────────────────────────────────────────────────

# 合并规则: {新类名: [旧类名列表]}
MERGE_RULES = {
    "aircraft":        ["airplane", "helicopter"],           # 航空器：飞机+直升机
    "typing":          ["keyboard_typing", "mouse_click"],   # 键盘鼠标：打字+点击
    "water_flow":      ["pouring_water", "water_drops"],     # 流水声：倒水+水滴
    "home_appliance":  ["vacuum_cleaner", "washing_machine"], # 家电：吸尘器+洗衣机
}

# 合并前后对比说明
MERGE_DESCRIPTIONS = {
    "aircraft":        "飞机(55%) + 直升机(60%) → 航空器（均为引擎轰鸣声）",
    "typing":          "键盘打字(87.5%) + 鼠标点击(47.5%) → 键盘鼠标（均为短暂敲击声）",
    "water_flow":      "倒水(92.5%) + 水滴(50%) → 流水声（均为液体声）",
    "home_appliance":  "吸尘器(85%) + 洗衣机(60%) → 家电（均为持续低频机械噪音）",
}


def apply_merge_rules(category_names: list) -> tuple:
    """
    应用合并规则，生成新的类别名称列表

    处理逻辑：
      1. 遍历原始 50 类
      2. 如果某类在合并规则中，跳过（由其父类替代）
      3. 未被合并的类保持不变
      4. 添加新的合并类

    参数：
        category_names: 原始 50 个类别名称

    返回：
        (new_category_names, old_to_new_map, merge_info)
        - new_category_names: 合并后的类别名称列表
        - old_to_new_map: 旧类别索引 → 新类别索引的映射
        - merge_info: 合并信息字典
    """
    # 收集所有被合并的旧类
    merged_old = set()
    for old_list in MERGE_RULES.values():
        merged_old.update(old_list)

    # 构建新类别列表
    new_categories = []
    # 先添加未被合并的旧类
    for cat in category_names:
        if cat not in merged_old:
            new_categories.append(cat)
    # 再添加新的合并类
    for new_cat in MERGE_RULES.keys():
        new_categories.append(new_cat)

    # 构建映射：旧类名 → 新类名
    old_name_to_new = {}
    for cat in category_names:
        if cat in merged_old:
            # 找到对应的新类名
            for new_cat, old_list in MERGE_RULES.items():
                if cat in old_list:
                    old_name_to_new[cat] = new_cat
                    break
        else:
            old_name_to_new[cat] = cat

    # 构建合并信息
    merge_info = {
        "original_count": len(category_names),
        "merged_count": len(new_categories),
        "reduction": len(category_names) - len(new_categories),
        "rules": MERGE_RULES,
        "descriptions": MERGE_DESCRIPTIONS,
    }

    print(f"\n[合并规则]")
    print(f"  原始类别数: {merge_info['original_count']}")
    print(f"  合并后类别数: {merge_info['merged_count']} (减少 {merge_info['reduction']} 类)")
    for new_cat, desc in MERGE_DESCRIPTIONS.items():
        print(f"  {desc}")

    return new_categories, old_name_to_new, merge_info


def remap_labels(y_old: np.ndarray, old_category_names: list, old_name_to_new: dict,
                 new_category_names: list) -> np.ndarray:
    """
    将旧标签重新映射为新标签

    参数：
        y_old: 旧标签数组 (N,)
        old_category_names: 旧类别名称列表
        old_name_to_new: 旧类名 → 新类名的映射
        new_category_names: 新类别名称列表

    返回：
        y_new: 新标签数组 (N,)
    """
    y_new = np.zeros_like(y_old)
    for i, old_label in enumerate(y_old):
        old_name = old_category_names[old_label]
        new_name = old_name_to_new[old_name]
        new_label = new_category_names.index(new_name)
        y_new[i] = new_label
    return y_new


# ─────────────────────────────────────────────────────────────────────────────
# 从原脚本复用的函数（简化版）
# ─────────────────────────────────────────────────────────────────────────────

def load_metadata():
    """加载 ESC-50 元数据"""
    df = pd.read_csv(META_FILE)
    print(f"  元数据: {len(df)} 条, {df['target'].nunique()} 类, {df['fold'].nunique()} 折")
    return df


def load_yamnet():
    """加载 YAMNet 预训练模型"""
    print(f"[加载YAMNet] 从 TF Hub 加载...")
    t0 = time.time()
    model = hub.load(YAMNET_URL)
    print(f"  完成，耗时 {time.time() - t0:.1f}s")
    return model


def augment_embeddings(X, y, noise_std=0.03, augment_factor=2):
    """嵌入向量增强"""
    X_aug_list = [X]
    y_aug_list = [y]
    for _ in range(augment_factor):
        noise = np.random.normal(0, noise_std, X.shape).astype(np.float32)
        X_aug_list.append(X + noise)
        y_aug_list.append(y)
    return np.vstack(X_aug_list), np.hstack(y_aug_list)


def build_classifier(input_dim, num_classes):
    """构建 256→128→50 分类器"""
    l2_reg = 0.001
    model = keras.Sequential([
        layers.Input(shape=(input_dim,), name="yamnet_embedding"),
        layers.Dense(256, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_256"),
        layers.BatchNormalization(name="bn_256"),
        layers.Activation("relu", name="relu_256"),
        layers.Dropout(0.5, name="dropout_256"),
        layers.Dense(128, kernel_regularizer=keras.regularizers.l2(l2_reg), name="dense_128"),
        layers.BatchNormalization(name="bn_128"),
        layers.Activation("relu", name="relu_128"),
        layers.Dropout(0.5, name="dropout_128"),
        layers.Dense(num_classes, activation="softmax", name="output"),
    ], name="YAMNetMergedClassifier")
    return model


def run_single_fold(df, embeddings, test_fold, category_names, y_all, verbose=True):
    """执行单折训练+评估"""
    fold_name = f"Fold {test_fold}"
    if verbose:
        print(f"\n  {fold_name}...", end=" ", flush=True)

    test_mask = df["fold"].values == test_fold
    train_mask = ~test_mask

    X_train_val = embeddings[train_mask]
    y_train_val = y_all[train_mask]
    X_test = embeddings[test_mask]
    y_test = y_all[test_mask]

    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val, test_size=0.1765,
        random_state=RANDOM_SEED, stratify=y_train_val
    )

    scaler = StandardScaler()
    X_train = scaler.fit_transform(X_train)
    X_val = scaler.transform(X_val)
    X_test = scaler.transform(X_test)

    class_weights_array = compute_class_weight(
        class_weight="balanced", classes=np.unique(y_train), y=y_train
    )
    class_weights = {i: float(w) for i, w in enumerate(class_weights_array)}

    X_train, y_train = augment_embeddings(X_train, y_train)

    num_classes = len(category_names)
    model = build_classifier(EMBEDDING_DIM, num_classes)

    steps_per_epoch = max(1, len(X_train) // BATCH_SIZE)
    cosine_decay = keras.optimizers.schedules.CosineDecay(
        initial_learning_rate=LEARNING_RATE,
        decay_steps=steps_per_epoch * EPOCHS,
        alpha=0.01
    )

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=cosine_decay),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor="val_loss", patience=EARLY_STOPPING_PATIENCE,
            restore_best_weights=True, verbose=0
        ),
    ]

    model.fit(
        X_train, y_train, validation_data=(X_val, y_val),
        epochs=EPOCHS, batch_size=BATCH_SIZE,
        class_weight=class_weights, callbacks=callbacks, verbose=0
    )

    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    y_pred_proba = model.predict(X_test, verbose=0)
    y_pred = np.argmax(y_pred_proba, axis=1)
    f1 = f1_score(y_test, y_pred, average="weighted")

    if verbose:
        print(f"acc={test_acc*100:.1f}%, f1={f1:.4f}")

    return {
        "fold": test_fold, "test_acc": float(test_acc),
        "test_loss": float(test_loss), "f1_score": float(f1),
        "y_test": y_test, "y_pred": y_pred,
    }


def export_tflite_float32(model, output_dir, prefix="audio_yamnet_merged"):
    """导出 FP32 TFLite"""
    os.makedirs(output_dir, exist_ok=True)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    path = os.path.join(output_dir, f"{prefix}_float32.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)
    print(f"  [FP32] {path} ({os.path.getsize(path)/1024:.1f} KB)")
    return path


def export_tflite_int8(model, X_calib, output_dir, prefix="audio_yamnet_merged"):
    """导出 INT8 量化 TFLite"""
    os.makedirs(output_dir, exist_ok=True)

    def representative_dataset():
        for i in range(0, min(len(X_calib), 200), 8):
            yield [X_calib[i:i+8].astype(np.float32)]

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.representative_dataset = representative_dataset
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.inference_input_type = tf.float32
    converter.inference_output_type = tf.float32
    tflite_model = converter.convert()
    path = os.path.join(output_dir, f"{prefix}_int8.tflite")
    with open(path, "wb") as f:
        f.write(tflite_model)
    print(f"  [INT8] {path} ({os.path.getsize(path)/1024:.1f} KB)")
    return path


def export_label_map(category_names, merge_info, output_dir):
    """导出标签映射"""
    os.makedirs(output_dir, exist_ok=True)
    label_map = {
        "labels": category_names,
        "num_classes": len(category_names),
        "model_type": "YAMNet迁移学习分类器（弱类合并版）",
        "merge_info": merge_info,
        "description": "ESC-50 环境声音类别（合并弱类后）"
    }
    path = os.path.join(output_dir, "audio_yamnet_merged_label_map.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(label_map, f, indent=2, ensure_ascii=False)
    print(f"  [标签] {path}")


# ─────────────────────────────────────────────────────────────────────────────
# 主函数
# ─────────────────────────────────────────────────────────────────────────────

def main():
    print("=" * 70)
    print("  YAMNet 迁移学习 —— 弱类合并优化版")
    print("=" * 70)

    # ── 1. 加载数据 ──
    print("\n[1/5] 加载数据...")
    yamnet_model = load_yamnet()
    df = load_metadata()

    cache_path = os.path.join(MODEL_DIR, "audio_yamnet_embeddings.npy")
    if not os.path.exists(cache_path):
        print("  [错误] 嵌入缓存不存在，请先运行 train_audio_yamnet.py")
        sys.exit(1)
    embeddings = np.load(cache_path)
    print(f"  嵌入缓存: {embeddings.shape}")

    # ── 2. 应用合并规则 ──
    print("\n[2/5] 应用弱类合并规则...")
    le = LabelEncoder()
    y_old = le.fit_transform(df["category"].values)
    old_category_names = le.classes_.tolist()

    new_category_names, old_name_to_new, merge_info = apply_merge_rules(old_category_names)
    y_new = remap_labels(y_old, old_category_names, old_name_to_new, new_category_names)

    # 打印合并后的类别分布
    print(f"\n  合并后各类别样本数:")
    for i, name in enumerate(new_category_names):
        count = np.sum(y_new == i)
        note = ""
        if name in merge_info["rules"]:
            old_classes = merge_info["rules"][name]
            note = f" (合并自: {', '.join(old_classes)})"
        print(f"    {i:2d}. {name:<25s} {count:3d} 样本{note}")

    # ── 3. 5折交叉验证 ──
    print(f"\n[3/5] 5折交叉验证（{merge_info['merged_count']}类）...")
    t0 = time.time()

    fold_results = []
    for fold in range(1, 6):
        result = run_single_fold(df, embeddings, fold, new_category_names, y_new)
        fold_results.append(result)

    cv_time = time.time() - t0

    # ── 4. 汇总统计 ──
    accs = [r["test_acc"] for r in fold_results]
    f1s = [r["f1_score"] for r in fold_results]

    print(f"\n{'=' * 70}")
    print(f"  5折交叉验证结果对比")
    print(f"{'=' * 70}")
    print(f"  {'指标':<20} {'原始50类':<18} {'合并{0}类'.format(merge_info['merged_count']):<18} {'变化':<10}")
    print(f"  {'─' * 66}")

    # 原始50类的结果（从之前的CV报告）
    orig_accs = [0.750, 0.877, 0.827, 0.795, 0.848]
    orig_f1s = [0.7612, 0.8756, 0.8267, 0.7973, 0.8451]

    for i in range(5):
        acc_diff = accs[i] - orig_accs[i]
        sign = "+" if acc_diff >= 0 else ""
        print(f"  Fold {i+1} 准确率        {orig_accs[i]*100:.1f}%              {accs[i]*100:.1f}%              {sign}{acc_diff*100:.1f}pp")

    print(f"  {'─' * 66}")
    avg_acc = np.mean(accs)
    avg_acc_orig = np.mean(orig_accs)
    avg_f1 = np.mean(f1s)
    avg_f1_orig = np.mean(orig_f1s)
    acc_diff = avg_acc - avg_acc_orig
    f1_diff = avg_f1 - avg_f1_orig
    print(f"  平均准确率          {avg_acc_orig*100:.1f}% ±{np.std(orig_accs)*100:.1f}%        {avg_acc*100:.1f}% ±{np.std(accs)*100:.1f}%        {acc_diff*100:+.1f}pp")
    print(f"  平均F1分数          {avg_f1_orig:.4f}            {avg_f1:.4f}            {f1_diff:+.4f}")
    print(f"{'=' * 70}")

    # ── 5. 各类别准确率 ──
    print(f"\n  各类别 5 折平均准确率:")
    per_class_all = np.zeros((len(new_category_names), 5))
    for fi, r in enumerate(fold_results):
        cm = confusion_matrix(r["y_test"], r["y_pred"], labels=range(len(new_category_names)))
        per_class = cm.diagonal() / (cm.sum(axis=1) + 1e-8)
        per_class_all[:, fi] = per_class

    per_class_mean = per_class_all.mean(axis=1)
    for i, (name, mean_acc) in enumerate(zip(new_category_names, per_class_mean)):
        marker = " ★合并类" if name in merge_info["rules"] else ""
        bar = "█" * int(mean_acc * 20) + "░" * (20 - int(mean_acc * 20))
        print(f"    {i:2d}. {name:<25s} {bar} {mean_acc:.1%}{marker}")

    # ── 生成 CV 报告 ──
    output_dir = MODEL_DIR
    os.makedirs(output_dir, exist_ok=True)

    report_path = os.path.join(output_dir, "audio_yamnet_merged_cv_report.txt")
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("=" * 70 + "\n")
        f.write("  YAMNet 迁移学习 —— 弱类合并优化版 5折交叉验证报告\n")
        f.write(f"  生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 70 + "\n\n")

        f.write("【合并规则】\n")
        for new_cat, desc in MERGE_DESCRIPTIONS.items():
            f.write(f"  {desc}\n")
        f.write(f"\n  原始类别: {merge_info['original_count']} → 合并后: {merge_info['merged_count']}\n\n")

        f.write("【5折交叉验证结果】\n")
        f.write(f"  {'折号':<8} {'准确率':<12} {'F1分数':<12} {'损失':<12}\n")
        f.write(f"  {'─' * 44}\n")
        for r in fold_results:
            f.write(f"  Fold {r['fold']:<3}  {r['test_acc']*100:.1f}%        {r['f1_score']:.4f}       {r['test_loss']:.4f}\n")
        f.write(f"  {'─' * 44}\n")
        f.write(f"  平均        {avg_acc*100:.1f}% ±{np.std(accs)*100:.1f}%  {avg_f1:.4f} ±{np.std(f1s):.4f}  {np.mean([r['test_loss'] for r in fold_results]):.4f}\n\n")

        f.write("【与原始 50 类对比】\n")
        f.write(f"  原始 50 类平均: {avg_acc_orig*100:.1f}% ±{np.std(orig_accs)*100:.1f}%\n")
        f.write(f"  合并后平均:     {avg_acc*100:.1f}% ±{np.std(accs)*100:.1f}%\n")
        f.write(f"  提升:           {acc_diff*100:+.1f}pp\n\n")

        f.write("─" * 70 + "\n")
        f.write("【各类别 5 折平均准确率】\n")
        f.write("─" * 70 + "\n")
        for i, (name, mean_acc) in enumerate(zip(new_category_names, per_class_mean)):
            marker = " ★合并类" if name in merge_info["rules"] else ""
            bar = "█" * int(mean_acc * 20) + "░" * (20 - int(mean_acc * 20))
            f.write(f"  {i:2d}. {name:<25s} {bar} {mean_acc:.1%}{marker}\n")

    print(f"\n  [CV报告] {report_path}")

    # ── 导出最终模型 ──
    print(f"\n[5/5] 导出部署模型...")
    # 用 fold 5 作为测试集训练最终模型
    final_result = run_single_fold(df, embeddings, 5, new_category_names, y_new, verbose=False)

    # 需要重新训练一个有 model 对象的版本用于导出
    test_mask = df["fold"].values == 5
    train_mask = ~test_mask
    X_train_val = embeddings[train_mask]
    y_train_val = y_new[train_mask]
    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val, test_size=0.1765,
        random_state=RANDOM_SEED, stratify=y_train_val
    )
    scaler = StandardScaler()
    X_train_s = scaler.fit_transform(X_train)
    X_val_s = scaler.transform(X_val)
    class_weights_array = compute_class_weight(
        class_weight="balanced", classes=np.unique(y_train), y=y_train
    )
    class_weights = {i: float(w) for i, w in enumerate(class_weights_array)}
    X_train_s, y_train = augment_embeddings(X_train_s, y_train)

    model = build_classifier(EMBEDDING_DIM, len(new_category_names))
    steps_per_epoch = max(1, len(X_train_s) // BATCH_SIZE)
    cosine_decay = keras.optimizers.schedules.CosineDecay(
        initial_learning_rate=LEARNING_RATE, decay_steps=steps_per_epoch * EPOCHS, alpha=0.01
    )
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=cosine_decay),
        loss="sparse_categorical_crossentropy", metrics=["accuracy"]
    )
    model.fit(
        X_train_s, y_train, validation_data=(X_val_s, y_val),
        epochs=EPOCHS, batch_size=BATCH_SIZE,
        class_weight=class_weights,
        callbacks=[keras.callbacks.EarlyStopping(
            monitor="val_loss", patience=EARLY_STOPPING_PATIENCE,
            restore_best_weights=True, verbose=0
        )],
        verbose=0
    )

    export_label_map(new_category_names, merge_info, output_dir)
    export_tflite_float32(model, output_dir)
    export_tflite_int8(model, X_val_s, output_dir)

    print(f"\n{'=' * 70}")
    print(f"  弱类合并优化完成！")
    print(f"  合并后类别: {merge_info['merged_count']} 类")
    print(f"  平均准确率: {avg_acc*100:.1f}% (提升 {acc_diff*100:+.1f}pp)")
    print(f"  模型文件位于: {output_dir}")
    print(f"{'=' * 70}")


if __name__ == "__main__":
    main()