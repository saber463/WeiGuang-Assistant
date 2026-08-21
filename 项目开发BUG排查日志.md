# 项目开发BUG排查日志

> **主文档已迁移至 [BUG_LOG.md](BUG_LOG.md)** — 包含全部51条BUG的完整排查记录（问题现象、报错代码、复现步骤、根因分析、修复方案、涉及文件）
> 
> 本文件保留作为历史记录引用。新增BUG请直接编辑 `bug-bot/knowledge_base_v2.json` 后运行 `python bug-bot/gen_bug_manual.py` 重新生成主文档。

---

## 快速导航

| 文档 | 说明 |
|------|------|
| [BUG_LOG.md](BUG_LOG.md) | **主文档** — 51条SafeGuard项目BUG完整手册 |
| [BUG修复日志.md](BUG修复日志.md) | 历史记录 — 旧版APP编译错误修复记录 |
| [BUG排查日志.md](BUG排查日志.md) | 历史记录 — 加密模块/FENSBox相关BUG |
| [BUG日志.md](BUG日志.md) | 历史记录 — 旧版模板 |

---

## 新增BUG填写模板

> 新BUG请使用以下模板，添加到 `bug-bot/knowledge_base_v2.json` 中：

```json
{
    "id": "bug_NNN",
    "time": "YYYY-MM-DD HH:MM",
    "category": "主分类",
    "sub_category": "子分类",
    "language": "Kotlin",
    "symptom": "问题现象描述",
    "error_code": "报错代码/异常类名",
    "trigger": "触发条件",
    "root_cause": "根因分析",
    "solution": "修复方案",
    "files": ["涉及文件1.kt", "涉及文件2.kt"],
    "severity": "critical/high/medium/low",
    "tags": ["标签1", "标签2"]
}
```

然后运行 `python bug-bot/gen_bug_manual.py` 重新生成 BUG_LOG.md。

---

## 历史BUG记录区

### 【2026-08-21 14:30】【原生库打包缺失 / UnsatisfiedLinkError】
**BUG-001**

**问题现象**：
打开手语识别页（SignLanguageScreen）立即闪退，App 进程被强制终止。首页其他功能正常，仅手语页崩溃。模拟器（test_avd, x86_64）上复现。

**报错/根源原因**：
- 异常：`java.lang.UnsatisfiedLinkError: dlopen failed: library "libmediapipe_tasks_vision_jni.so" not found`
- 调用链：`BaseVisionTaskApi.<clinit>` → `System.loadLibrary("mediapipe_tasks_vision_jni")` → 找不到 .so
- 根因分析（三层）：
  1. **AGP 8.2.0 BUG**：AAR 中的 .so 文件不会自动打包进 APK，需手动提取到 jniLibs 目录
  2. **ABI 不匹配**：MediaPipe tasks-vision 0.10.2 AAR 只含 arm64-v8a / armeabi-v7a / x86 三个 ABI 的 .so，不含 x86_64。模拟器主 ABI 为 x86_64，APK 中虽有 `lib/x86_64/`（其他依赖的 .so），但唯独缺少 `libmediapipe_tasks_vision_jni.so`
  3. **系统 ABI 回退失败**：`useLegacyPackaging=false` 时系统仅按主 ABI(x86_64) 提取 .so，找到部分 .so 后不再回退到 arm64-v8a（次 ABI），导致 MediaPipe .so 永远不会被提取到设备文件系统

**修复解决办法**：
1. **从 AAR 手动提取 x86 .so**：从 Gradle 缓存中的 `tasks-vision-0.10.2.aar` 提取 `jni/x86/libmediapipe_tasks_vision_jni.so` 到 `app/src/main/jniLibs/x86/`
2. **abiFilters 排除 x86_64**：在 `app/build.gradle.kts` 的 `defaultConfig` 中添加 `ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86") }`，排除 x86_64。这样 APK 不含 `lib/x86_64/`，系统无主 ABI 匹配 → 回退到 arm64-v8a → native bridge(libndk_translation.so) 加载并翻译 arm64-v8a 的 .so
3. **useLegacyPackaging=true**：配合 abiFilters，让 .so 保留在 APK 内由 native bridge 从 APK 直接加载（native bridge 提取 arm64-v8a .so 到文件系统时存在目录为空的兼容问题）
4. **模拟器前置摄像头**：修改 AVD 配置 `hw.camera.front` 从 `none` 改为 `emulated`（手语页使用 DEFAULT_FRONT_CAMERA，模拟器原无前置摄像头会导致 CameraX 崩溃，此为环境配置问题非业务逻辑）
5. **ndkVersion 指定**：指定 `ndkVersion = "25.1.8937393-2"`（完整版 NDK，含 llvm-strip），解决 strip 任务因 llvm-strip 缺失而硬报错的问题
6. **移除 gradle.properties 中的 android.stripDebugSymbols=false**：让 strip 任务正常运行（禁用后 packageDebug 找不到 .so 输出目录）

**涉及文件**：
- `app/build.gradle.kts` — ndkVersion、abiFilters、jniLibs srcDirs、useLegacyPackaging、aaptOptions 配置
- `gradle.properties` — 移除 android.stripDebugSymbols=false
- `app/src/main/jniLibs/x86/libmediapipe_tasks_vision_jni.so` — 从 AAR 手动提取的原生库
- `C:\Users\Fenis\.android\avd\test_avd.avd\config.ini` — hw.camera.front 改为 emulated
- `docs/验收截图/09_手语页修复后.png` — 修复后手语页正常运行截图