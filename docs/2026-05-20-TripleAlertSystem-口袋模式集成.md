# TripleAlertSystem 口袋模式集成 — 修改文档

**日期**: 2026-05-20  
**模块**: 全局强提醒模块  
**修改者**: 微光畅行开发团队  

---

## 一、修改概述

本次修改在已有的 `TripleAlertSystem`（三重提醒系统）基础上，集成了 `PocketAlertManager`（口袋模式管理器），增加了**接近传感器检测 → 口袋状态判断 → 多模态强提醒**的完整链路。

---

## 二、新增文件

### 2.1 PocketAlertManager.kt

- **路径**: `app/src/main/java/com/weiguangchangxing/weiguang_plus/core/alert/PocketAlertManager.kt`
- **定位**: core 层基础能力，不依赖任何上层 UI 组件
- **核心功能**:

| 功能 | 方法 | 说明 |
|------|------|------|
| 启动接近传感器监测 | `startProximityMonitoring()` | 注册 SensorEventListener，SENSOR_DELAY_NORMAL 采样率 |
| 停止接近传感器监测 | `stopProximityMonitoring()` | 注销监听器，释放传感器资源 |
| 查询口袋状态 | `isInPocketMode()` | 返回当前是否检测到手机在口袋中 |
| 触发强提醒 | `triggerPocketAlert(durationMs)` | 多线程执行震动+闪光+声音+亮屏四重提醒 |
| 停止强提醒 | `stopAlert()` | 中断线程，关闭闪光灯，释放资源 |
| 释放全部资源 | `release()` | 停止提醒 + 停止监测 |

- **口袋判断逻辑**: 接近传感器 `event.values[0] < maxRange * 0.9f` 时判定为口袋状态（兼容二值型传感器和距离型传感器）
- **四重提醒循环**（独立子线程）:
  1. 最大振幅震动（255）— 500ms 震 + 200ms 停 × 3 次
  2. 闪光灯闪烁 — 亮 300ms → 灭 200ms × 2 次
  3. 强制声音 — 仅在判断为口袋中时播放，避免取出后吓到用户
  4. 唤醒屏幕 — PowerManager.SCREEN_BRIGHT_WAKE_LOCK

---

## 三、修改文件

### 3.1 TripleAlertSystem.kt

**路径**: `app/src/main/java/com/weiguangchangxing/weiguang_plus/feature/notification/TripleAlertSystem.kt`

**文件变化**: 原 325 行 → 现 409 行（+84 行）

#### 修改点汇总

| # | 修改位置 | 修改类型 | 说明 |
|---|----------|----------|------|
| 1 | import 区域 | **新增** | + `import android.hardware.Sensor` |
|   |            |          | + `import android.os.PowerManager` |
|   |            |          | + `import com.weiguangchangxing.weiguang_plus.core.alert.PocketAlertManager` |
| 2 | 类属性 | **新增** | `private val pocketAlertManager = PocketAlertManager(context)` |
|   |         |          | `private var isPocketModeEnabled = true` |
| 3 | `triggerTripleAlert()` | **修改** | 在震动/声音/灯光之后增加口袋模式判断： |
|   |                       |          | - `level.ordinal >= AlertLevel.HIGH.ordinal` 时才触发 |
|   |                       |          | - EMERGENCY → 30 秒，HIGH → 15 秒，其他 → 8 秒 |
| 4 | 新增方法 | **新增** | `startPocketModeMonitoring()` — 启动传感器监测 |
|   |          |          | `stopPocketModeMonitoring()` — 停止传感器监测 |
|   |          |          | `setPocketModeEnabled(Boolean)` — 动态开关口袋模式 |
| 5 | `getVibrationPattern()` | **修改** | 所有等级震动模式增强： |
|   |                         |          | - LOW: `[0, 200, 300]`（1 段长震） |
|   |                         |          | - MEDIUM: `[0, 300, 200, 300, 200]`（2 段长震） |
|   |                         |          | - HIGH: `[0, 500, 150, 500, 150, 500]`（3 段长震交替） |
|   |                         |          | - EMERGENCY: `[0, 800, 100, 800, 100, 800, 200, 400, 200, 400, 200]`（3 段极长震 + 2 段中震） |
| 6 | `getVibrationAmplitudes()` | **修改** | 从动态计算改为静态匹配，与新模式一一对应： |
|   |                           |          | - LOW: `[0, 150, 0]` |
|   |                           |          | - MEDIUM: `[0, 180, 0, 180, 0]` |
|   |                           |          | - HIGH: `[0, 220, 0, 220, 0, 220]` |
|   |                           |          | - EMERGENCY: `[0, 255, 0, 255, 0, 255, 0, 200, 0, 200, 0]` |
| 7 | `stopAllAlerts()` | **修改** | 调用链中增加 `pocketAlertManager.stopAlert()` |
|   |                   |          | 确保停止所有提醒时同步停止口袋强提醒 |

---

## 四、调用链路

```
触发通知/告警
       │
       ▼
triggerTripleAlert(level)
       │
       ├── triggerVibration(level)    ← 增强后的震动模式
       ├── triggerSound(level)
       ├── triggerLight(level)
       │
       └── if (口袋模式开启 && 等级 >= HIGH)
                │
                ▼
           pocketAlertManager.triggerPocketAlert(duration)
                │
                ├── triggerMaxVibration()    ← 255最大振幅
                ├── toggleFlashlight()        ← 闪光灯闪烁
                ├── forceSoundAlert()         ← 强制声音（口袋中）
                ├── wakeScreen()              ← 唤醒屏幕
                └── 循环直到超时或被中断
```

---

## 五、兼容性

- **最低 API**: 23 (Android 6.0)
- **接近传感器**: 不具备的设备直接跳过口袋模式（不报错）
- **振幅控制**: API 26+ 支持振幅调节，旧版本使用默认振幅
- **WakeLock**: API 28+ 使用 `acquire(timeout)` 带超时版本，旧版本使用弃用API

---

## 六、使用示例

```kotlin
// 初始化
val alertSystem = TripleAlertSystem(context)

// 启动口袋模式监测（建议在 Application.onCreate 中调用）
alertSystem.startPocketModeMonitoring()

// 触发提醒（HIGH及以上等级会自动判断口袋模式）
alertSystem.triggerTripleAlert(AlertLevel.EMERGENCY)

// 动态开关口袋模式
alertSystem.setPocketModeEnabled(false)  // 关闭
alertSystem.setPocketModeEnabled(true)   // 开启

// 停止所有提醒
alertSystem.stopAllAlerts()

// 释放资源（Activity/Service 销毁时）
alertSystem.release()
```