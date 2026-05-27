# Tasks
- [x] Task 1: TTSManager 添加初始化重试机制和状态反馈
  - 在 TTSManager.initialize() 中添加最多 3 次重试（每次间隔 2 秒）
  - 添加 initializationAttempts 计数器
  - 添加状态提示文本，供 UI 展示 TTS 状态
  - 在 MainActivity 中对 TTS 初始化状态进行监听，3秒未就绪时在主页显示状态提示条

- [x] Task 2: SignLanguageManager 安全降级（彻底禁用 MediaPipe）
  - 确保 initializeHandGestureRecognizer 不做任何 MediaPipe 原生库加载
  - 确保 isHandTrackingSupported = false 且后续所有代码路径不会尝试加载
  - DualSignScreen 中的 bindFrontCameraDual 仅在 isHandTrackingSupported 为 true 时调用（当前已经做了判断）

- [x] Task 3: MainActivity 启动 TTS 自动播报 + 状态提示条
  - TTS 就绪后自动播报欢迎语（只播一次）
  - 添加 TTS 状态提示条组件（3秒未就绪显示在主页顶部）
  - 添加手动重试 TTS 初始化的按钮（提示条中）

- [x] Task 4: 首页功能模块 UI 重构（移除状态标签 + 改为 Dialog 弹窗）
  - 移除 HomeScreen 中所有 ModuleSummary 卡片的"已启用"假状态标签
  - 为每个卡片添加点击事件，弹出详细功能说明 Dialog
  - Dialog 中显示标题、图标、功能说明、使用场景提示
  - Dialog 底部显示"前往使用"（导航到对应页面）和"关闭"按钮

- [x] Task 5: 视觉模块物品识别结果改进
  - 改进 VisionScreen 识别结果为空时的展示文案，替换为友好提示
  - 移除 "置信度 N/A" 等无意义显示
  - 当 ML Kit 返回结果置信度过低时显示引导提示

- [x] Task 6: 学习中心 TTS 播报状态反馈
  - 在 "朗读此课程" 按钮中添加 TTS 状态检测
  - 如果 TTS 未就绪，按钮灰色不可点击并显示"语音引擎未就绪"
  - 点击时如果 TTS 就绪则播报并添加简短状态反馈

- [x] Task 7: 编译验证与功能完整性测试
  - 确保所有修改后的代码能成功编译
  - 验证所有页面可正常打开无闪退

# Task Dependencies
- [Task 1] depends on [Task -] (无依赖，可最先执行)
- [Task 2] depends on [Task -] (无依赖，可和 Task 1 并行)
- [Task 3] depends on [Task 1]
- [Task 4] depends on [Task -] (无依赖，可和 Task 1/2 并行)
- [Task 5] depends on [Task -] (无依赖)
- [Task 6] depends on [Task 1]
- [Task 7] depends on all other tasks