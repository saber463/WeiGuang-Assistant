# MediaPipeHandLandmarker 模型下载与离线使用增强方案

## 修改文件
`MediaPipeHandLandmarker.kt`

## 需求背景
手语识别功能首次启动时需要从 Google CDN 下载约 15MB 的模型文件（hand_landmarker.task）。  
原实现中无网络检测、无进度回调、无重试机制，导致在无网络环境下首次启动时手语识别完全不可用。

## 增强目标
1. 下载前检测网络可用性
2. 提供下载进度回调，支持 UI 显示下载百分比
3. 失败后自动重试（最多 2 次）
4. 在 SharedPreferences 中持久化下载状态
5. 无网络时给出明确提示

## 具体修改项

### 1. 新增 Import
```kotlin
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
```

### 2. 新增 DownloadProgressListener 接口
位于文件顶部、class 定义之前，定义三个回调方法：
- `onProgress(bytesDownloaded, totalBytes)` — 下载进度
- `onComplete()` — 下载完成
- `onError(message)` — 下载失败

### 3. Companion object 新增常量
| 常量名 | 值 | 用途 |
|--------|-----|------|
| RETRY_MAX | 2 | 最大重试次数 |
| PREF_NAME | "hand_model_download" | SharedPreferences 文件名 |
| PREF_DOWNLOADED | "model_downloaded" | 标记是否已下载 |
| PREF_MODEL_VERSION | "model_version" | 模型版本号 |

### 4. 新增成员变量
- `downloadProgressListener: DownloadProgressListener?` — 进度回调持有者
- `retryCount: Int` — 当前重试次数
- `totalBytes: Long` — 文件总大小（用于进度计算）

### 5. initialize() 方法增强
**增强前：** 文件不存在则直接下载  
**增强后：**
- 先检查 SharedPreferences 状态：若标记已下载但文件不存在，清除过期标记
- 文件存在 → 直接加载
- 文件不存在 → 先调 `isNetworkAvailable()` 检测网络
  - 有网络 → 下载
  - 无网络 → 通过 `onError` 回调提示用户

### 6. isNetworkAvailable() 方法
- 通过 `ConnectivityManager` 获取当前网络状态
- Android M+ 使用 `NetworkCapabilities` 检测 `NET_CAPABILITY_INTERNET`
- 低版本使用 `activeNetworkInfo.isConnectedOrConnecting`
- 异常捕获，失败时返回 false

### 7. downloadModel() 方法重写
**增强前：** 使用 `inputStream.copyTo(outputStream)` 一次性写入，无进度、无重试  
**增强后：**
- 使用 8KB buffer 循环读取写入，实时累加 `totalRead`
- 每次循环回调 `downloadProgressListener.onProgress(totalRead, totalBytes)`
- 下载成功后：
  - 重置 `retryCount = 0`
  - 调用 `saveDownloadState()` 持久化状态
  - 回调 `downloadProgressListener.onComplete()`
- 下载失败时：
  - 删除损坏文件
  - `retryCount++`
  - 若 `retryCount <= RETRY_MAX`（2次），等待 `1秒 × retryCount` 后递归重试
  - 超过最大重试次数，重置计数器并回调错误

### 8. setDownloadProgressListener() 方法
对外暴露，供 UI 层注册下载进度监听

### 9. saveDownloadState() & isModelDownloadedBefore()
- `saveDownloadState()`：将 `model_downloaded=true` 和版本号写入 SharedPreferences
- `isModelDownloadedBefore()`：读取 SharedPreferences 中是否标记已下载

### 10. isReady() 方法增强
```kotlin
// 增强前
return handLandmarker != null && modelDownloaded
// 增强后
return handLandmarker != null && modelDownloaded && getModelFile().exists()
```
增加文件存在性校验，防止标记被篡改或文件被误删后仍返回就绪状态

## 调用方使用示例
```kotlin
val landmarker = MediaPipeHandLandmarker(context)

// 注册下载进度监听
landmarker.setDownloadProgressListener(object : DownloadProgressListener {
    override fun onProgress(bytesDownloaded: Long, totalBytes: Long) {
        val percent = (bytesDownloaded * 100 / totalBytes).toInt()
        // 更新 UI 进度条
    }
    override fun onComplete() {
        // 下载完成，开始识别
    }
    override fun onError(message: String) {
        // 显示错误提示
    }
})

landmarker.initialize(
    onReady = { /* 模型就绪 */ },
    onError = { msg -> /* 初始化失败 */ }
)
```

## 数据流图
```
initialize()
  ├─ SDK 版本检查 → 不通过 → onError("需要 Android 7.0+")
  ├─ 文件存在 → loadModel() → onReady()
  └─ 文件不存在
       ├─ SharedPreferences 标记清理（标记已下载但文件丢失）
       ├─ isNetworkAvailable()
       │    ├─ false → onError("当前无网络连接...")
       │    └─ true → downloadModel()
       │           ├─ 成功 → saveDownloadState() → loadModel() → onComplete()
       │           └─ 失败 → retryCount ≤ 2 → 重试
       │                   └─ retryCount > 2 → onError("下载失败")
```

## 备注
- 修复了原始方案中 `retryCount` 重置后用于错误消息的 Bug，改用 `finalRetryCount` 临时变量保留重试次数
- 所有网络操作在 `downloadExecutor` 单线程池中执行，不阻塞主线程