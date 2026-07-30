# GestureVectorDB 2.0 实施方案
> 手语向量数据库开源版 + 离线推理 + 在线增量更新 + 用户自定义手势
> 文档版本：v1.0 · 2026年7月

---

## 一、架构总览

```
┌──────────────────────────────────────────────────────────────────────┐
│                   GestureVectorDB 2.0 全栈架构                          │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │              服务器端 (47.108.149.191)                        │    │
│  │  ┌──────────────────┐  ┌──────────────────┐                 │    │
│  │  │ 手势数据API       │  │ 社区审核系统      │                 │    │
│  │  │ GET /version      │  │ 用户贡献→审核     │                 │    │
│  │  │ GET /delta        │  │ →纳入公开数据集   │                 │    │
│  │  │ GET /public       │  │                   │                 │    │
│  │  │ POST /contribute  │  │                   │                 │    │
│  │  └──────────────────┘  └──────────────────┘                 │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                          │ WiFi 自动同步 / 手动触发                    │
│                          ▼                                            │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │              手机端 (100% 离线推理)                            │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │    │
│  │  │ GestureDataSync│  │  SQLite DB   │  │ GestureVectorDB  │   │    │
│  │  │ 增量同步引擎   │  │ 手势向量存储  │  │ 余弦相似度匹配   │   │    │
│  │  │ WiFi自动拉取   │  │ 100+手势     │  │ <1ms 推理       │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘   │    │
│  │                                                              │    │
│  │  ┌──────────────────────────────────────────────────────┐   │    │
│  │  │           用户自定义手势采集模块                        │   │    │
│  │  │  摄像头采集N次 → 计算质心 → 本地存储 → 可选上传共享    │   │    │
│  │  └──────────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

**核心原则**：
- 推理永远在手机本地（离线可用，<1ms 延迟）
- 网络只在 WiFi 下用于数据同步（不依赖网络运行）
- 用户数据主权在用户手中（本地存储，上传可选）

---

## 二、阶段一：GestureVectorDB 2.0 核心重构（2-3天）

### 2.1 SQLite 数据库设计

**数据库文件**：`gesture_vectors.db`（存储在 app 内部存储）

**表结构**：

```sql
CREATE TABLE gesture_vectors (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    label       TEXT    NOT NULL,          -- 英文标签（如 "fist"）
    label_zh    TEXT    NOT NULL,          -- 中文标签（如 "握拳(SOS)"）
    vector_json TEXT    NOT NULL,          -- 63维向量 JSON 数组
    sample_count INTEGER DEFAULT 1,        -- 样本数（用于加权更新）
    version     INTEGER DEFAULT 1,         -- 数据版本号
    source      TEXT    DEFAULT 'builtin',  -- 来源：builtin/server/user
    is_shared   INTEGER DEFAULT 0,         -- 是否已上传共享（0/1）
    created_at  TEXT,                      -- 创建时间 ISO 8601
    updated_at  TEXT,                      -- 更新时间 ISO 8601
    UNIQUE(label)                          -- 每个标签唯一
);

-- 版本元数据表
CREATE TABLE sync_metadata (
    key         TEXT PRIMARY KEY,
    value       TEXT
);
-- 初始数据：('data_version', '1'), ('last_sync_time', '')
```

**索引**：
```sql
CREATE INDEX idx_gesture_label ON gesture_vectors(label);
CREATE INDEX idx_gesture_source ON gesture_vectors(source);
```

### 2.2 GestureVectorDB 2.0 Kotlin 实现

**文件**：`app/src/main/java/com/weiguangchangxing/tonghang/core/GestureVectorDB2.kt`

**核心类结构**：

```kotlin
/**
 * 手势向量数据库 2.0（开源版）
 *
 * 升级点：
 *   1. 从内存 Map 升级为 SQLite 持久化存储
 *   2. 支持 100+ 手势，不限制数量
 *   3. 支持增量更新（从服务器拉取数据，不重装APP）
 *   4. 支持用户自定义手势（本地采集→计算质心→存储）
 *   5. 支持手势数据来源标记（builtin/server/user）
 */
class GestureVectorDB2(private val context: Context) {

    // 数据库
    private val db: SQLiteDatabase

    // 内存缓存（加速匹配，避免每次查询数据库）
    private val cache = mutableMapOf<String, GestureEntry>()

    // 公开方法
    fun match(inputVector: FloatArray): GestureMatchResult    // 匹配手势
    fun addGesture(label, labelZh, vector): Boolean           // 添加/更新手势
    fun importFromJson(jsonString: String): Int               // 批量导入JSON
    fun removeGesture(label: String): Boolean                 // 删除手势
    fun getAllGestures(): List<GestureEntry>                  // 获取所有手势
    fun getGestureCount(): Int                                // 手势数量
    fun getBySource(source: String): List<GestureEntry>       // 按来源筛选
    fun exportToJson(): String                                // 导出为JSON
    fun clearCache()                                          // 清除缓存
}
```

**关键实现细节**：

1. **缓存策略**：启动时从 SQLite 加载全部手势到内存 `cache`（100个手势 × 63维 × 4字节 = 约25KB，内存占用极小）
2. **匹配性能**：`match()` 直接遍历内存缓存，不查询数据库，保证 <1ms 延迟
3. **写入策略**：`addGesture()` 同时写入 SQLite 和更新内存缓存
4. **向量存储**：63维向量以 JSON 数组字符串存入 SQLite，读取时解析为 FloatArray

### 2.3 预置数据初始化

**文件**：`app/src/main/assets/gesture_data_v1.json`

启动时检测数据库中是否有数据，若无则从 assets 中的预置 JSON 初始化：

```kotlin
fun initializeIfNeeded() {
    val count = db.rawQuery("SELECT COUNT(*) FROM gesture_vectors", null)
    if (count == 0) {
        val json = context.assets.open("gesture_data_v1.json")
            .bufferedReader().readText()
        importFromJson(json)
    }
}
```

---

## 三、阶段二：增量同步引擎（1-2天）

### 3.1 GestureDataSync 实现

**文件**：`app/src/main/java/com/weiguangchangxing/tonghang/core/GestureDataSync.kt`

**同步流程**：

```
                    ┌─────────────────┐
                    │ 检查网络状态      │
                    │ 仅 WiFi 下同步    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ GET /api/gestures│
                    │ /version        │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ 服务器版本 > 本地?│
                    │ 否 → 跳过        │
                    └────────┬────────┘
                             │ 是
                             ▼
                    ┌─────────────────┐
                    │ GET /api/gestures│
                    │ /delta?since=v1  │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ 解析 JSON        │
                    │ 逐条 upsert 到   │
                    │ SQLite + 缓存     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ 更新本地版本号    │
                    │ 同步完成          │
                    └─────────────────┘
```

**同步策略**：
- 静默同步：APP 启动时在后台线程执行，不阻塞 UI
- 仅 WiFi：避免消耗移动数据流量
- 手动触发：设置页面提供"检查更新"按钮
- 冲突处理：服务器版本优先覆盖本地同 label 数据（source='builtin' 的保留）

### 3.2 服务器端 API 设计

**部署位置**：`47.108.149.191`（与现有网站同一服务器）

**API 端点**：

| 端点 | 方法 | 说明 | 请求/响应 |
|------|------|------|-----------|
| `/api/gestures/version` | GET | 获取当前数据版本 | `{"version": 3, "gesture_count": 120, "updated_at": "2026-08-01"}` |
| `/api/gestures/delta` | GET | 获取增量更新 | `?since=1` → 返回版本2和3的新增/修改手势 |
| `/api/gestures/public` | GET | 下载公开数据集 | 返回所有标记为 public 的手势质心向量 |
| `/api/gestures/contribute` | POST | 用户提交自定义手势 | 提交后进入审核队列 |

**技术栈**：Python Flask + SQLite（与现有网站共用 Nginx 反向代理）

---

## 四、阶段三：用户自定义手势（2-3天）

### 4.1 采集流程

```
用户点击"学习新手势"
        │
        ▼
输入手势名称（如"家"）
        │
        ▼
┌───────────────────┐
│  学习模式界面       │
│  "请做出'家'的手势" │
│  倒计时：3...2...1  │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  MediaPipe 提取    │
│  21点关键点→63维   │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  重复采集 N 次     │
│  默认 10 次        │
│  显示进度：5/10     │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  计算质心向量      │
│  均值(N个样本)      │
│  检查方差（质量）   │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  存储到本地 SQLite  │
│  source = "user"   │
│  立即可用于匹配     │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  询问是否上传共享   │
│  是 → POST /api/   │
│       gestures/    │
│       contribute   │
│  否 → 仅本地使用   │
└───────────────────┘
```

### 4.2 质量控制

采集完成后自动评估手势质量：
- **方差检查**：多次采集的向量标准差过大 → 提示"手势不够稳定，建议重新采集"
- **去重检查**：新采集的质心与已有手势相似度 > 0.95 → 提示"可能已存在相似手势"
- **最小样本数**：至少采集 5 次才允许保存

### 4.3 用户手势管理

**UI 功能**：
- 查看所有自定义手势列表（source = "user"）
- 删除自定义手势
- 重新采集（更新质心）
- 上传到社区共享（source 改为 "user_shared"）

---

## 五、开源仓库结构设计

### 5.1 GitHub 仓库：`weiguangtongxing/gesture-vector-db`

```
gesture-vector-db/
├── README.md                  # 项目介绍、快速开始
├── LICENSE                    # Apache 2.0
├── CONTRIBUTING.md            # 贡献指南
│
├── android/                   # Android 端核心库
│   ├── build.gradle
│   └── src/main/java/com/weiguangtongxing/gesturedb/
│       ├── GestureVectorDB.kt       # 核心匹配引擎
│       ├── GestureEntry.kt          # 数据模型
│       ├── GestureMatchResult.kt    # 匹配结果
│       ├── GestureDataSync.kt       # 增量同步引擎
│       └── db/
│           └── GestureDatabaseHelper.kt  # SQLite 助手
│
├── training/                  # 训练工具链
│   ├── extract_landmarks.py   # MediaPipe 关键点提取
│   ├── compute_centroids.py   # 质心计算
│   ├── data_augmentation.py   # 数据增强
│   └── requirements.txt
│
├── server/                    # 服务器端 API
│   ├── app.py                 # Flask API 服务
│   ├── requirements.txt
│   └── data/
│       └── gesture_data_v1.json  # 公开手势数据集
│
├── data/                      # 开放数据
│   └── v1/
│       ├── gesture_centroids.json   # 11种基础手势质心
│       └── README.md                # 数据使用说明
│
└── docs/
    ├── API_SPEC.md             # 服务器 API 规范
    └── FORMAT_SPEC.md          # 手势向量数据格式规范
```

### 5.2 开源协议

| 内容 | 协议 | 理由 |
|------|------|------|
| Android 核心库 | Apache 2.0 | 允许商业使用，专利保护 |
| Python 训练工具 | MIT | 宽松，方便学术引用 |
| 公开手势数据 | CC BY 4.0 | 署名即可，促进学术研究 |

---

## 六、实施时间线

| 阶段 | 内容 | 工期 | 产出 |
|------|------|------|------|
| 阶段一 | GestureVectorDB 2.0 + SQLite | 2-3天 | 新 Kotlin 文件 + 数据库 |
| 阶段二 | 增量同步引擎 + 服务器 API | 1-2天 | GestureDataSync + Flask API |
| 阶段三 | 用户自定义手势采集 | 2-3天 | 采集 UI + 学习逻辑 |
| 阶段四 | 开源仓库搭建 + 文档 | 1天 | GitHub 仓库 |
| 阶段五 | 项目计划书更新 + 部署 | 1天 | 更新 Word 文档 |

**总计**：约 7-10 天

---

## 七、与现有系统的兼容性

| 现有组件 | 升级策略 |
|----------|----------|
| GestureVectorDB.kt | 保留原文件，新增 GestureVectorDB2.kt，渐进替换 |
| GestureReferenceVectors.kt | 数据迁移到 SQLite 后废弃 |
| SignLanguageAnalyzer.kt | 切换 `GestureVectorDB()` → `GestureVectorDB2(context)` |
| gesture_centroids.json | 转为 SQLite 预置数据源 |
| 训练脚本 | 独立为开源仓库，PC 端继续使用 |

---

## 八、风险与应对

| 风险 | 应对 |
|------|------|
| 用户自定义手势质量差 | 方差检查 + 去重检查 + 最小样本数限制 |
| 服务器同步数据量大 | 增量更新（只传变化），JSON 压缩 |
| 开源后竞品复制 | 算法开源但数据壁垒保留（100+手势质心数据暂不公开） |
| 用户上传不良手势 | 服务器端审核机制 + 关键词过滤 |