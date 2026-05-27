[OPEN] debug session: app-startup-crash

目标：定位“手机刚打开 App 就闪退”的根因，收集可复现的运行时证据，并用最小改动修复与验证。

## 现象
- 实际：启动后立即闪退
- 期望：进入首屏（Splash/Main）

## 复现路径（待补全）
- 冷启动：从桌面点击图标启动
- 热启动：后台切回

## 运行时证据（已收集）
- Room 打开预置离线库时抛出：
  - `java.lang.IllegalStateException: Pre-packaged database has an invalid schema: drug_master(...)`
  - 差异点包括：
    - `drug_id` 的 NOT NULL 约束识别不一致
    - `created_at/updated_at` 默认值（预置库存在 `strftime('%s','now')`，Room 期望 `undefined`）

## 假设（可证伪）
H1：Application/首个 Activity 的 onCreate 中发生未捕获异常（典型：空指针、资源缺失、反射失败），导致进程崩溃。
H2：AndroidManifest 配置或启动 Activity/Provider 初始化失败（典型：authority 冲突、exported 配置、FileProvider 路径/元数据错误）。
H3：第三方 SDK 初始化在主线程抛异常或因缺少权限/配置导致崩溃（典型：推送、统计、地图、WebView、X5 等）。
H4：资源/ABI/So 加载问题（典型：UnsatisfiedLinkError、Split APK/多架构缺失）在启动阶段触发。
H5：混淆/多进程/反射相关（典型：ClassNotFoundException、NoSuchMethodError）在启动时触发。

## 证据采集计划
1) 启动 Debug Server 接收端（本机）
2) 在启动链路埋点：Application.attachBaseContext / onCreate、首个 Activity.onCreate/onResume、以及全局 UncaughtExceptionHandler
3) 通过 logcat/崩溃堆栈与埋点事件对齐，确认根因假设

## 结论
- 根因：预置离线数据库 `assets/db/drugs.db` 的表结构与 Room Entity 不一致，Room 校验失败直接抛异常导致闪退。

## 修复动作（已落地）
- 统一 SQL Schema：移除与 Room 不一致的 DEFAULT 定义，改为构建脚本补齐必须字段。
- 修复构建脚本：为 `drug_master` 导入时补齐 `created_at/updated_at`。
- 重新生成 `assets/db/drugs.db`。
- 运行时兜底：检测到 “invalid schema” 时自动切换为不使用预置库并删除重建，避免启动即崩。

## 变更记录
- 2026-05-19：创建调试会话文档
 - 2026-05-19：确认根因为 Room 预置库 schema 不匹配并修复

