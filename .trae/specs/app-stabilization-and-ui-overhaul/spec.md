# 应用全面稳定化与功能修复 Spec

## Why
当前应用存在多处闪退（手语页面、服务中心部分页面）、TTS 播报不可用、首页 UI 杂乱（静态"已启用"标签不具实际意义）、视觉识别模块无法正确识别物品等问题，严重影响了用户体验。

## What Changes
- 修复所有剩余闪退问题：DualSignScreen 的 SignLanguageManager 初始化安全化、ServiceBookingScreen Crash 兜底
- TTS 启动时自动播报 + 状态验证：添加 TTS 就绪后的强制重试机制 + 手动增加 TTS 初始化状态弹窗提醒
- 首页 UI 重构：移除功能卡片的"已启用/状态"标识，改为点击弹出完整功能说明的 Dialog
- 视觉识别模块改进：ML Kit 图片标签显示更多有意义的信息/错误友好提示
- 学习中心 TTS 播报修复：添加状态检查与重试机制
- 权限集中申请优化

## Impact
- Affected code: MainActivity.kt, SignLanguageManager.kt, HomeScreen, VisionScreen, LearningCenterScreen, TTSManager.kt

## ADDED Requirements
### Requirement: 启动时 TTS 自动播报 + 状态反馈
The system SHALL 在应用启动后自动检查 TTS 引擎就绪状态
#### Scenario: TTS 就绪场景
- **WHEN** 应用启动完成 && TTS 引擎初始化就绪
- **THEN** 系统自动播报 "欢迎使用微光畅行，无障碍出行助手"，且用户能听到完整播报
#### Scenario: TTS 未就绪反馈
- **WHEN** 应用启动后 3 秒内 TTS 仍未就绪
- **THEN** 主页顶部显示 TTS 状态提示条，告知用户当前 TTS 不可用及可能原因

### Requirement: 首页功能卡片改为 Dialog 弹窗
The system SHALL 将首页静态功能模块卡片改为可点击的 Dialog 弹窗
#### Scenario: 用户点击首页功能卡片
- **WHEN** 用户点击任意功能模块卡片
- **THEN** 弹出详细功能说明 Dialog（含标题、图标、说明文字、推荐使用场景），底部显示"前往使用"和"关闭"按钮
- **AND** 不再显示虚假的"已启用"/未启用状态

### Requirement: DualSignScreen 的安全初始化
The system SHALL 确保 DualSignScreen 不会因 MediaPipe 加载失败而崩溃
#### Scenario: 进入双向手语页面
- **WHEN** 用户点击进入"沟通"→"双向对话"页面
- **THEN** SignLanguageManager 初始化时不尝试加载 MediaPipe 原生库，静默降级为仅语音识别模式
- **AND** 界面友好提示"手势识别暂不可用，语音→手语模式正常运行"

### Requirement: 学习中心 TTS 播报状态反馈
The system SHALL 在用户点击"朗读此课程"时给出明确的状态反馈
#### Scenario: 点击朗读课程
- **WHEN** 用户点击"朗读此课程"按钮
- **THEN** 如果 TTS 就绪则立即播报并按钮短暂显示"正在朗读..."
- **AND** 如果 TTS 未就绪则按钮显示"语音引擎未就绪"并灰色不可点击（点击后 Snackbar 提示原因）

### Requirement: Vision 模块识别结果优化
The system SHALL 改进 ML Kit 识别结果的展示方式
#### Scenario: 物品识别无结果
- **WHEN** 相机启动识别但 ML Kit 返回空结果或置信度过低
- **THEN** 展示"无法识别当前物品，建议对准更清晰的物体或增加光照"提示
- **AND** 不再显示 "置信度 N/A" 等无意义信息

## MODIFIED Requirements
### Requirement: TTSManager 初始化重试机制
- 在 TTSManager.initialize() 中添加最多 3 次重试（间隔 2 秒），每次重试前重新创建 TextToSpeech 实例
- 添加 `initializationAttempts` 计数器，3 次都失败后设置 `isReady = false` 并更新 `errorMessage`

## REMOVED Requirements
### Requirement: 首页功能状态标签
**Reason**: 所有"已启用"/功能状态标签均为静态硬编码文字，不代表真实功能状态，造成用户误解
**Migration**: 全部移除，改为点击弹出功能说明 Dialog