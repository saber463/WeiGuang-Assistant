# 语音助手控制面板 — VoiceAssistantScreen

## 概述

`VoiceAssistantScreen` 是微光畅行 App 中的语音助手控制面板界面，为用户提供设备语音助手的统一管控入口。用户可在此查看设备信息、一键唤醒系统语音助手、浏览常见品牌助手的安装状态并进行针对性唤醒。

## 文件信息

| 属性 | 值 |
|------|-----|
| 文件路径 | `app/src/main/java/com/weiguangchangxing/weiguang_plus/feature/assistant/VoiceAssistantScreen.kt` |
| 包名 | `com.weiguangchangxing.weiguang_plus.feature.assistant` |
| 主入口 | `@Composable fun VoiceAssistantScreen(modifier: Modifier)` |
| 框架 | Jetpack Compose + Material3 |

## 依赖关系

### 内部依赖

| 依赖类 | 路径 | 用途 |
|--------|------|------|
| `VoiceAssistantLauncher` | `core/assistant/VoiceAssistantLauncher.kt` | 获取已安装助手列表、唤醒指定助手、品牌识别 |
| `AssistantInfo` | `core/assistant/VoiceAssistantLauncher.kt` | 助手信息数据类（名称/包名/安装状态/启动Intent） |
| `TTSManager` | `core/tts/TTSManager.kt` | 操作反馈的语音播报 |

### 外部依赖

| 依赖 | 用途 |
|------|------|
| `android.os.Build` | 读取设备型号（MODEL）和制造商（MANUFACTURER） |
| Material3 Compose | UI 组件（Card/Button/Text 等） |

## 功能模块

### 1. 设备品牌自动识别

```
VoiceAssistantLauncher.getAssistantNameByManufacturer()
    ↓
根据 Build.MANUFACTURER 返回对应品牌名称：
  xiaomi  → "小爱"
  huawei  → "小艺"
  oppo    → "小布"
  vivo    → "小V"
  honor   → "YOYO"
  samsung → "Bixby"
  google  → "Google助手"
  其他    → "系统语音助手"
```

### 2. 一键唤醒

- 调用 `VoiceAssistantLauncher.launchBestAssistant(context)`
- 智能匹配策略：在已安装的助手中选择第一个 `isInstalled && launchIntent != null` 的助手
- 若没有匹配的已安装助手，降级为系统语音助手（`RecognizerIntent.ACTION_RECOGNIZE_SPEECH` → `Intent.ACTION_VOICE_COMMAND` → `Intent.ACTION_SEARCH`）

### 3. 助手列表展示

通过 `VoiceAssistantLauncher.getInstalledAssistants(context)` 获取 7 个预配置助手的安装状态：

| 助手名称 | 包名 |
|---------|------|
| 小米小爱 | `com.miui.voiceassist` |
| 华为小艺 | `com.huawei.vassistant` |
| OPPO小布 | `com.oppo.voiceassist` |
| vivo小V | `com.vivo.voiceassistant` |
| 荣耀YOYO | `com.hihonor.voiceassistant` |
| 三星Bixby | `com.samsung.android.bixby.wakeup` |
| Google助手 | `com.google.android.apps.gsa` |

每个助手卡片：
- 左侧：助手名称（titleMedium 加粗）+ 包名 + 安装状态标签
- 右侧：已安装 → "唤醒"按钮（实心Button）；未安装 → "未安装"按钮（OutlinedButton，点击提示安装）

### 4. TTS 操作反馈机制

```
用户点击按钮 → feedbackText 状态更新
         ↓
LaunchedEffect(feedbackText) 触发
         ↓
TTSManager.speakNow(feedbackText) 播报
         ↓
feedbackText 自动置 null，避免重复播报
```

反馈文本对照表：

| 操作 | 成功 | 失败 |
|------|------|------|
| 一键唤醒 | "正在唤醒${brandName}，请稍候" | "唤醒失败，请在系统中检查语音助手设置" |
| 指定助手唤醒 | "正在打开${assistant.name}" | "${assistant.name}启动失败" |
| 点击未安装助手 | — | "${assistant.name}未安装，请在应用商店搜索后安装" |

## UI 布局结构

```
ScrollPage (可滚动)
 ├── HeroCard (primaryContainer 背景)
 │    ├── Title: "语音助手"
 │    ├── Subtitle: "已识别品牌：小爱"
 │    └── Body: 功能介绍文字
 ├── SectionTitle: "设备信息"
 ├── HighlightCard (secondaryContainer 背景)
 │    ├── Title: "当前设备"
 │    ├── Value: Build.MODEL (如 "Mi 13")
 │    └── Note: 制造商 + 品牌匹配
 ├── SectionTitle: "一键唤醒"
 ├── OneClickWakeCard (InfoCard 容器)
 │    ├── 说明文字
 │    ├── 支持列表文字
 │    └── Button: "唤醒语音助手" (全宽)
 ├── SectionTitle: "系统助手列表（7）"
 └── AssistantCard × N
      ├── 左侧：名称 + StatusChip(包名 + 安装状态)
      └── 右侧：Button("唤醒") / OutlinedButton("未安装")
```

## 组件树

| Composable | 可见性 | 功能 |
|-----------|--------|------|
| `VoiceAssistantScreen` | `public` | 主入口，管理状态和布局组合 |
| `ScrollPage` | `private` | 可滚动容器，统一内边距 |
| `HeroCard` | `private` | 首屏大标题卡片 |
| `HighlightCard` | `private` | 高亮信息卡片 |
| `InfoCard` | `private` | 普通白色卡片容器 |
| `SectionTitle` | `private` | 章节标题 |
| `StatusChip` | `private` | 状态标签（胶囊形） |
| `OneClickWakeCard` | `private` | 一键唤醒按钮卡片 |
| `AssistantCard` | `private` | 单个助手信息卡片 |

## 使用方式

在任意 Material3 主题下的 Composable 中直接调用：

```kotlin
// 在 Scaffold 的 content 中
VoiceAssistantScreen(modifier = Modifier.padding(innerPadding))

// 或在页面中作为独立 section
Column {
    VoiceAssistantScreen()
}
```

## 注意事项

1. **TTS 初始化**：确保在 `WeiguangPlusApplication.onCreate()` 中调用 `TTSManager.initialize(this)`，否则 TTS 播报不会生效
2. **助手列表缓存**：`getInstalledAssistants()` 仅在首次 composition 时调用（`remember` 无 key），应用生命周期内不会刷新。如需刷新可销毁重组
3. **品牌识别精度**：`getAssistantNameByManufacturer()` 基于 `Build.MANUFACTURER` 的 `lowercase()` 包含匹配，可能存在 OEM 定制 ROM 的误判
4. **启动 Intent 兼容性**：不同 ROM 版本的语音助手 Activity 路径可能变化，`VoiceAssistantLauncher` 中的路径基于主流版本配置，极端情况下可能无法正确匹配
5. **`isTtsReady` 目前控制一键唤醒按钮的 enable 状态**，即使 TTS 未就绪，唤醒操作仍可执行（只是没有语音反馈）

## 未来可扩展

- 添加助手列表下拉刷新机制
- 支持用户自定义添加第三方语音助手
- 添加语音助手版本号和详细状态展示
- 集成系统语音输入（`SpeechRecognizer`）的快捷入口