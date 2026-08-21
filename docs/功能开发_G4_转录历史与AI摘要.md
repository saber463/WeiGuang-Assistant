# 功能开发：G4 转录历史存储与 AI 摘要

> 版本：v1.0 | 开发日期：2026-08-11 | 时区：UTC+8 北京时间
> 对应待开发文档：`docs/微光守护_待开发文档.md` §3.2 G4 | 路线图：阶段5
> 状态：**已完成**（Build Successful @ 2026-08-11）

---

## 一、背景与目标

保存通话转写与悬浮窗字幕的转录记录，支持按时间回溯查看，并提供 AI 摘要能力（提取关键信息，如医嘱要点、办事结论）。

## 二、技术方案

### 2.1 数据库：独立 Room 数据库（关键决策）
**不触碰主 `AppDatabase`**，采用项目已有的"独立数据库"先例（如 `ChatDatabase`）：
- 新建 `TranscriptDatabase`（独立 .db 文件），新增实体不触发主库版本迁移
- 数据访问走 `TranscriptRepository`，封装增删查

这样扩展安全，不影响现有 4 实体主库。

### 2.2 AI 摘要（本次范围）
待开发文档建议对接大模型 API。本次实现：
- 定义 `TranscriptRepository.generateSummary()` **接口方法与抽象**（预留）
- 因无真实大模型 API Key 环境，**不接入真实外部调用**，摘要字段由调用方按需填充（可本地降级为"截取前 N 字"）。
- 后续接大模型时只需补全 repository 中预留的方法体。

### 2.3 UI
- `TranscriptHistoryScreen`：LazyColumn 展示按时间倒序的转录记录（时间 + 摘要预览）
- 点击条目展开查看全文

## 三、实现文件

| 文件 | 类型 | 职责 |
|------|------|------|
| `data/model/TranscriptRecord.kt` | 新增 | Room 实体 + 摘要字段 |
| `data/local/TranscriptDao.kt` | 新增 | 增删查 DAO |
| `data/repository/TranscriptRepository.kt` | 新增 | 数据仓库 + 独立 TranscriptDatabase + 摘要预留接口 |
| `ui/screen/transcript/TranscriptHistoryScreen.kt` | 新增 | 转录历史列表/详情 UI |

**修改**：MainActivity 增加 `transcript` 路由；MainScreen 增加入口卡片。

## 四、验证
1. `gradlew.bat :app:assembleDebug` 编译通过。
2. 首页进入"转录历史"页，可看到新增/查询记录（暂无运行时自动写入，报表用入口可查看）。