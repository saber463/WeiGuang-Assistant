# 手势识别训练工具包 —— 微光同行APP

## 目录结构

```
training/
├── collect_gesture_data.py      # 数据采集脚本（摄像头 + 键盘打标签）
├── train_gesture_classifier.py  # 机器学习训练（RandomForest/SVM → TFLite）
├── train_gesture_deep.py        # 深度学习训练（DNN → TFLite）
├── requirements.txt             # Python依赖清单
├── dataset/                     # 采集的数据（CSV文件）
│   ├── gesture_data_*.csv       # 手势数据
│   └── label_map.txt            # 标签映射
└── model/                       # 训练产物
    ├── gesture_dnn_float32.tflite   # FP32模型（Android用）
    ├── gesture_dnn_int8.tflite      # INT8量化模型（体积更小）
    ├── gesture_label_map.json       # 标签映射
    ├── gesture_scaler_params.json   # 归一化参数
    ├── training_report.txt          # 训练报告
    └── training_history.png         # 训练曲线
```

## 快速开始

### 1. 安装依赖

```bash
cd training
pip install -r requirements.txt
```

### 2. 采集手势数据

```bash
python collect_gesture_data.py
```

**操作说明：**

| 按键 | 功能 |
|------|------|
| `0-9` | 切换当前标签 |
| `空格` | 采集一帧数据 |
| `S` | 保存数据到 CSV |
| `Q` | 退出 |

**标签对照表：**

| 编号 | 英文标签 | 中文含义 | APP用途 |
|------|---------|----------|---------|
| 0 | fist | 握拳 | SOS求救 |
| 1 | open_palm | 手掌张开 | 停止/求助 |
| 2 | thumbs_up | 竖大拇指 | 确认/好 |
| 3 | point_index | 食指指向 | 方向/那个 |
| 4 | peace | 剪刀手 | 胜利/V |
| 5 | ok_sign | OK手势 | 没问题 |
| 6 | wave | 摆手 | 你好/再见 |
| 7 | heart | 比心 | 谢谢/爱心 |
| 8 | call_me | 打电话 | 六的手势 |
| 9 | neutral | 无手势 | 负样本/背景 |

**采集建议：**
- 每个手势至少采集 200 帧
- 变换手的位置、角度、距离摄像头的远近
- 用不同手（左右手）各采集一遍
- 采集时背景尽量干净，光线充足
- 每采集完一个手势按 `S` 保存，防止数据丢失

### 3. 训练模型

**方式一：机器学习（推荐起步，数据量小也能跑）**

```bash
python train_gesture_classifier.py --csv dataset/gesture_data_20260722.csv
```

自动训练 RandomForest + SVM，选最优导出 TFLite。

**方式二：深度学习（数据量大时效果更好）**

```bash
python train_gesture_deep.py --csv dataset/gesture_data_20260722.csv
```

训练 3 层全连接神经网络（128→64→32），导出 FP32 + INT8 两种 TFLite。

**合并多个采集文件：**

```bash
python train_gesture_deep.py \
    --csv dataset/gesture_data_20260722.csv \
    --csv dataset/gesture_data_20260723.csv \
    --csv dataset/gesture_data_20260724.csv
```

### 4. 部署到 Android

训练完成后，将以下文件复制到 Android 项目：

```
model/gesture_dnn_int8.tflite       → app/src/main/assets/gesture_model.tflite
model/gesture_label_map.json        → app/src/main/assets/gesture_label_map.json
model/gesture_scaler_params.json    → app/src/main/assets/gesture_scaler_params.json
```

然后在 Android 端使用 TensorFlow Lite Interpreter 加载模型进行推理。

## 技术原理

### 数据流

```
摄像头 → MediaPipe Hand Landmarker → 21个关键点(x,y,z) → CSV文件
                                                              ↓
                                         训练脚本(特征工程+训练) → TFLite模型
                                                              ↓
Android端: CameraX → MediaPipe → 21个关键点 → TFLite推理 → 手势标签
```

### 特征工程

除了原始 63 维坐标（21点 × 3坐标），训练脚本还会提取 30 维几何特征：

- **手指弯曲度**（5维）：指尖到MCP关节的距离
- **关节角度**（5维）：指尖-DIP-PIP 的余弦角度
- **指尖到手腕距离**（5维）：判断手指是否伸展
- **相邻指尖间距**（4维）：判断手指是否并拢
- **拇指到各指尖距离**（4维）：判断拇指是否接触其他手指
- **手掌尺寸**（2维）：手掌宽高比

总计：63 + 30 = 93 维特征

### 模型对比

| 特性 | 机器学习版 | 深度学习版 |
|------|-----------|-----------|
| 模型 | RandomForest / SVM | 全连接神经网络 |
| 训练速度 | 快（秒级） | 较慢（分钟级） |
| 数据需求 | 几百条即可 | 推荐几千条以上 |
| 模型体积 | 较大（RF ~1MB） | 较小（INT8 ~50KB） |
| 推理速度 | 中等 | 快 |
| 可解释性 | 高（特征重要性） | 低（黑盒） |
| 导出方式 | sklearn → ONNX → TFLite | 原生 TFLite |

**建议策略：先跑 ML 版快速验证，数据量上来后切换到 DL 版。**

## 常见问题

**Q: 摄像头打不开？**
A: 检查 `CAMERA_ID` 参数（在 `collect_gesture_data.py` 顶部），尝试改为 0、1、2。

**Q: 手部检测不稳定？**
A: 调整 `min_detection_confidence` 参数（默认 0.7），降低到 0.5 可以更敏感但可能误检。

**Q: TensorFlow 安装失败？**
A: 尝试 `pip install tensorflow-cpu`，或使用 conda 安装。

**Q: 模型准确率低？**
A: 数据不够 → 多采集几轮；过拟合 → 加大 dropout 或 L2 正则化；样本不均衡 → 确保每个手势采集量相近。