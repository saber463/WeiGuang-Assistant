# 微光同行 (WeiguangPlus) ♿

<p align="center">
  <strong>让视障人士看得见、说得出、被叫醒、敢吃药</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/版本-v1.2-success" alt="Version" />
  <img src="https://img.shields.io/badge/平台-Android%20%7C%20iOS-9cf" alt="Platform" />
  <img src="https://img.shields.io/badge/状态-BUILD%20SUCCESSFUL-brightgreen" alt="Build Status" />
  <img src="https://img.shields.io/badge/文档-40%2B-blue" alt="Documentation" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
</p>

---

## 📖 项目简介

**微光同行**是一款面向**视障且言语障碍人群**的Android无障碍助残APP，基于**13位听障人士真实市场调研**数据开发，致力于解决日常生活中的三大核心痛点：

### 💫 三大价值闭环

| 闭环 | 核心功能 | 用户价值 |
|------|---------|---------|
| **🗣️ 沟通闭环** | 双向手语互通（高频短语识别+Canvas动画） | 让听障用户快速表达需求，展示给听人 |
| **💊 用药闭环** | 药品OCR智能识别 → 风险提示 → TTS语音播报 | 拍照识药，避免用药风险，保障生命安全 |
| **⏰ 唤醒闭环** | 三重强提醒系统（震动+铃声+灯光联动） | 解决传统声音闹钟不适用问题，不错过重要事项 |

---

## ✨ 核心功能（14个模块）

基于**92.31%** 的最高调研支持率，我们实现了以下功能模块：

### 🔴 P0核心模块（已实现）

#### 1️⃣ 双向手语互通系统 👐
- ✅ 高频手语短语库（30+常用表达）
- ✅ MediaPipe Hands手势识别引擎
- ✅ Canvas手绘动画渲染（21种手势姿势）
- ✅ Lottie动画支持（可扩展）
- ✅ 语音转文字 + 文字转语音双向转换

**支持场景**：我要喝水、我要吃药、请帮帮我、不舒服、联系家人等

#### 2️⃣ 药品智能识别系统 💊
- ✅ CameraX相机框架集成
- ✅ ML Kit离线OCR文字提取
- ✅ 本地Room药品数据库（预置常见药品库）
- ✅ 过敏原自动匹配与风险提示
- ✅ 用药禁忌校验（年龄、孕期、肝肾功能等）
- ✅ TTS语音播报药品信息

**识别流程**：拍照 → OCR文本提取 → 药品名称标准化 → 风险评估 → 语音播报

#### 3️⃣ 全局三强提醒系统 🔔
- ✅ NotificationListenerService通知监听
- ✅ 音量强制控制（解除静音、拉满铃声音量）
- ✅ 三重联动：自适应震动 + 铃声最大化 + 灯光闪烁
- ✅ 4级提醒等级：低/中/高/紧急

**震动模式**：
- 低：100ms开/100ms停
- 中：200ms开/100ms停 × 2
- 高：300ms开/100ms停 × 3
- 紧急：500ms开/100ms停 × 4

#### 4️⃣ 环境音聆听系统 🎵
- ✅ AudioRecord持续音频采集（16kHz单声道PCM）
- ✅ TFLite离线人声检测模型
- ✅ 后台前台服务驻留（WakeLock保活）
- ✅ 人声/音乐/环境音/静音分类
- ✅ 检测到人声时触发联动提醒

---

### 🟡 P1扩展模块（基于调研需求开发）

#### 5️⃣ 公交地铁报站 🚌 **[84.62%支持率]**
- ✅ 10条线路GPS定位追踪
- ✅ Haversine距离计算到站距离
- ✅ 到站震动提醒 + 闪烁警告
- ✅ 实时位置更新

#### 6️⃣ 网约车沟通助手 🚗 **[76.92%支持率]**
- ✅ 行程确认模板
- ✅ 6种沟通话术（目的地、绕路、等待等）
- ✅ 一键复制发送

#### 7️⃣ 一键应急求助 🚨 **[76.92%支持率]**
- ✅ 6种预设SOS场景（突发疾病、迷路、事故等）
- ✅ SMS短信自动发送给紧急联系人
- ✅ GPS位置实时上传
- ✅ 闪光灯闪烁吸引注意

#### 8️⃣ 全局语音转文字 🎤 **[92.31%支持率 - 最高需求]**
- ✅ 前台服务常驻运行
- ✅ 实时语音转文字广播
- ✅ SpeechRecognizer原生API
- ✅ 支持多场景（会议、对话、电话）

#### 9️⃣ 无障碍地图导航 📍 **[69.23%支持率]**
- ✅ 5类设施点搜索（医院、地铁站、公交站、盲道、无障碍厕所）
- ✅ 路线查看与规划
- ✅ 设施问题上报入口

#### 🔟 设施问题上报 📝 **[46.15%支持率]**
- ✅ 拍照上传盲道/无障碍设施损坏
- ✅ GPS定位标记
- ✅ 一键提交至相关管理部门

#### 1️⃣1️⃣ 基地帮扶服务预约 🏥 **[69.23%支持率]**
- ✅ 手语翻译预约
- ✅ 陪同出行申请
- ✅ 服务进度跟踪

#### 1️⃣2️⃣ 辅助设备申领 🦻 **[69.23%支持率]**
- ✅ 5种设备类型（助听器、盲杖、放大镜、读屏软件、其他）
- ✅ 在线申请表单
- ✅ 申请进度查询

#### 1️⃣3️⃣ 安全学习中心 📚 **[84.62%支持率]**
- ✅ 6类图文课程（急救知识、防诈骗、出行安全、用药安全、法律权益、心理疏导）
- ✅ TTS语音朗读功能
- ✅ 学习进度记录

#### 1️⃣4️⃣ 红绿灯辅助 🚦 **[46.15%支持率]**
- ✅ 5个模拟路口状态查询
- ✅ 震动提示倒计时
- ✅ 安全通行引导

---

## 🛠️ 技术栈

### 已落地技术选型

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **开发语言** | Kotlin | - | Google推荐Android开发语言 |
| **UI框架** | Jetpack Compose (Material3) | BOM 2023.08.00 | 声明式UI，现代化界面 |
| **架构模式** | MVVM + Clean Architecture | - | 模块化分层设计 |
| **异步调度** | Kotlin Coroutines + Flow | - | 轻量级并发编程 |
| **本地数据库** | Jetpack Room | 2.6.1 | SQLite抽象层，预置药品库 |
| **相机框架** | CameraX | 1.3.4 | 统一相机API，简化开发 |
| **AI识别** | ML Kit OCR | - | Google离线OCR，无需联网 |
| **手势识别** | MediaPipe Hands | 轻量兼容版 | 21点手部关键点检测 |
| **动画引擎** | Canvas手绘 | - | 替代Lottie，减少包体积 |
| **音频处理** | AudioRecord原生API | - | PCM音频采集 |
| **机器学习** | TFLite | 2.13.0 | 离线人声检测模型推理 |
| **TTS语音** | 系统原生TextToSpeech | - | 无障碍语音播报 |

### 目标兼容性

| 属性 | 版本要求 |
|------|---------|
| **最低SDK** | Android 5.0 (API 21) |
| **目标SDK** | Android 14 (API 34) |
| **编译SDK** | Android 14 (API 34) |
| **构建工具** | Gradle 8.2 + AGP 8.2.0 |

**覆盖范围**：Android 5.0 ~ Android 14全版本兼容，支持华为/小米/OPPO/vivo/三星等主流品牌。

---

## 📊 项目架构

### 模块分层设计

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│   MainActivity / 各功能Screen               │
├─────────────────┬───────────────────────────┤
│    Feature      │       Feature             │
│   Module Layer  │       Module Layer        │
├─────────────────┼───────────────────────────┤
│  SignLanguage   │   Notification           │
│  Vision(OCR)    │   SoundMonitor            │
│  Emergency     │   Transportation          │
│  Learning      │   Map                     │
├─────────────────┴───────────────────────────┤
│            Core Infrastructure              │
│  Permission / Hardware / Perception / TTS   │
├─────────────────────────────────────────────┤
│              Data Layer (Room)              │
│     DrugRepository / LocalDatabase         │
└─────────────────────────────────────────────┘
```

### 导航结构（12个Tab）

```
底部导航栏:
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│ 总览 │ 小玉 │ 出行 │ 药品 │ 应急 │ 提醒 │ 学习 │ 视觉 │ 服务 │ 对话 │ 助手 │ 语音 │
└─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
```

---

## 📈 市场调研数据

### 用户画像

- **调研对象**：13位真实听障人士（四川省成都市及周边地区）
- **调研时间**：2026年5月
- **调研方式**：深度访谈 + 问卷调查 + 场景观察

### 需求优先级排序

| 排名 | 功能需求 | 支持率 | 优先级 |
|------|---------|--------|--------|
| 🥇 | 全局语音转文字 | **92.31%** | P0 |
| 🥈 | 公交地铁可视化报站 | **84.62%** | P0 |
| 🥉 | 安全学习课程 | **84.62%** | P0 |
| 4 | 公交到站震动提醒 | 84.62% | P0 |
| 5 | 一键SOS求助 | 76.92% | P0 |
| 6 | 网约车沟通辅助 | 76.92% | P1 |
| 7 | 无障碍路线规划 | 69.23% | P1 |
| 8 | 手语翻译/陪同出行 | 69.23% | P1 |
| 9 | 辅助设备申领 | 69.23% | P1 |
| 10 | 设施问题上报 | 46.15% | P2 |
| 11 | 红绿灯辅助 | 46.15% | P2 |

### 全国社区数据支撑

- **全国居委会总数**：124,308个（民政部官方数据）
- **成都市社区数**：1,751个城市社区 + 1,294个行政村 = **3,045个**
- **四川省排名**：居委会数量全国第4（8,344个），基层治理完善
- **战略意义**：微光同行可通过社区渠道实现规模化触达，成都试点→四川推广→全国复制

---

## 📁 项目目录结构

```
weiguangplus/
├── app/                              # Android主模块
│   ├── src/main/
│   │   ├── java/.../weiguang_plus/
│   │   │   ├── app/                 # Application类
│   │   │   ├── core/                # 核心基础设施层
│   │   │   │   ├── alert/          # 口袋模式提醒
│   │   │   │   ├── animation/      # Canvas手势动画
│   │   │   │   ├── assistant/      # 语音助手
│   │   │   │   ├── emergency/      # SOS管理
│   │   │   │   ├── hardware/       # 硬件检测
│   │   │   │   ├── map/            # 无障碍地图
│   │   │   │   ├── perception/     # 融合感知引擎
│   │   │   │   ├── permission/     # 权限管理
│   │   │   │   ├── service/        # 系统服务
│   │   │   │   ├── transportation/ # 交通辅助
│   │   │   │   └── tts/            # TTS管理
│   │   │   ├── data/               # 数据层(Room)
│   │   │   ├── feature/            # 14个功能模块
│   │   │   └── ui/theme/           # Compose主题
│   │   └── assets/db/              # 预置SQLite药品库
│   └── build.gradle
│
├── data/seed/                        # CSV种子数据
│   ├── drug_master.csv              # 药品主数据(500+种)
│   ├── drug_detail.csv              # 药品详细信息
│   ├── drug_alias.csv               # 药品别名映射
│   ├── drug_rule.csv                # 用药规则
│   └── drug_sign_mapping.csv        # 手语-药品关联
│
├── docs/                             # 完整技术文档(40+)
│   ├── 微光畅行-MVP方案-v1.md        # 产品方案
│   ├── 最终定稿完整版技术栈.md        # 技术选型
│   ├── P0核心模块开发文档.md          # 架构设计
│   ├── 全国及成都市社区数据调研报告.md # 市场调研
│   ├── 完整交付报告.md               # 交付清单
│   ├── Bug修复全记录.md              # 13个Bug修复记录
│   ├── 药品离线库SQL与Room骨架.md     # 数据库设计
│   └── ...                          # 更多文档
│
├── tools/                            # 构建工具
│   └── build_seed_db.py              # SQLite建库脚本
│
├── ppt-workspace/                   # PPT生成工作区
│   └── weiguangplus_roadshow.pptx    # 路演PPT
│
├── gradle.properties
├── settings.gradle
├── build.gradle                      # 根级构建配置
└── .gitignore
```

---

## 🏆 参赛说明

本项目目前正在参加以下两项比赛：

- **2026 AI助残创新创意大赛**（创意赛道）
- **2026年"挑战杯"大学生创业计划竞赛**

> 比赛结束后，项目核心算法将开源（Apache 2.0），部分手势数据集将公开（CC BY 4.0），敬请关注。

### 🌐 宣传网页

在线体验与项目介绍：
- **服务器站点**：[http://47.108.149.191/](http://47.108.149.191/)
- **GitHub Pages**：[https://saber463.github.io/WeiGuang-Assistant/](https://saber463.github.io/WeiGuang-Assistant/)

---

## 📖 详细文档

本项目包含**40+个完整技术文档**，涵盖产品、技术、运营全方位：

### 核心文档（必读）

| 文档 | 内容 | 适用角色 |
|------|------|---------|
| [MVP方案v1](docs/微光畅行-MVP方案-v1.md) | 产品定位、功能范围、验收标准 | 产品经理/所有开发者 |
| [最终定稿技术栈](docs/2026-05-19-微光畅行-最终定稿完整版技术栈.md) | 技术选型、适配策略、性能目标 | 技术负责人/架构师 |
| [P0核心模块开发文档](docs/2026-05-19-P0核心模块开发文档.md) | 架构设计、模块拆分、代码示例 | 全体开发者 |
| [完整交付报告](docs/2026-05-20-完整交付报告.md) | 功能清单、文件列表、代码审查 | 项目经理/QA |

### 数据与算法文档

| 文档 | 内容 |
|------|------|
| [药品离线库SQL设计](docs/药品离线库SQL与Room骨架-v1.md) | Room实体定义、数据库Schema |
| [药品数据整合手册](docs/药品数据整合开发手册-v1.md) | CSV数据处理流程 |
| [药品数据源接入教程](docs/药品数据源实操接入教程-v1.md) | 从零搭建药品库 |
| [OCR接入说明](docs/2026-05-16-ImageAnalysis与OCR提取接入说明.md) | CameraX + ML Kit集成 |
| [手语识别对接流程](docs/手语识别+药品OCR-开发对接流程图-v1.md) | 模块交互协议 |

### 调试与维护文档

| 文档 | 内容 |
|------|------|
| [Bug修复全记录](docs/2026-05-20-Bug修复全记录.md) | 13个历史Bug及解决方案 |
| [App启动闪退排查](debug-app-startup-crash.md) | Room schema不匹配修复 |
| [UI全面优化文档](docs/2026-05-19-UI全面优化文档.md) | 界面改造细节 |
| [创新功能开发文档](docs/2026-05-20-创新功能开发文档.md) | 新功能实现方案 |

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是代码、文档、设计还是反馈。

### 开发流程

1. **Fork本仓库** 或联系获取Worktree访问权限
2. **创建特性分支**: `git checkout -b feature/amazing-feature`
3. **提交更改**: `git commit -m '✨ feat: add amazing feature'`
4. **推送分支**: `git push origin feature/amazing-feature`
5. **创建Pull Request**

### 提交信息规范

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` Bug修复
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 重构
- `perf:` 性能优化
- `test:` 测试相关
- `chore:` 构建/工具链

### 代码规范

- **Kotlin**: 遵循 [Kotlin官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- **Python**: 遵循 [PEP 8](https://www.python.org/dev/peps/pep-0008/)
- **注释语言**: 中文注释（方便团队理解）
- **关键函数**: 必须添加详细的中文注释和参数说明

---

## 📄 License

本项目采用 **MIT License** 开源协议。

```
MIT License

Copyright (c) 2026 微光同行开发团队

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🙏 致谢

### 数据来源
- **民政部**: 全国村（居）民委员会数据（截至2025年12月31日）
- **《成都年鉴2024》**: 成都市社区治理数据
- **13位听障调研用户**: 真实需求反馈（匿名保护隐私）

### 技术支持
- **Google**: ML Kit / CameraX / MediaPipe / TFLite
- **JetBrains**: Kotlin / IntelliJ IDEA
- **开源社区**: 所有使用的开源库和工具

### 特别感谢
- **中国残联及各地残联组织**: 提供政策指导和资源对接
- **四川省成都市残疾人联合会**: 协助调研和数据收集
- **所有参与测试的听障朋友**: 你们的需求是我们前进的动力

---

## 📞 联系我们

- **GitHub Issues**: [提交Issue或建议](https://github.com/saber463/WeiGuang-Assistant/issues)
- **Email**: weiguangtechnology@foxmail.com
- **宣传网页**: [http://47.108.149.191/](http://47.108.149.191/) | [GitHub Pages](https://saber463.github.io/WeiGuang-Assistant/)

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐ Star！**

**让更多人看到无障碍技术的力量！** ♿💙

Made with ❤️ by 微光同行开发团队

</div>
