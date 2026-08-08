/**
 * 文件名：RecognitionRecord.kt
 * 作者：微光同行前端团队
 * 功能描述：药品识别记录数据类，用于存储用户的历史识别操作记录
 * 创建日期：2026-05-29
 * 所属模块：data/model（数据模型层）
 *
 * 业务场景：
 * - 用户每次使用相机拍照识别药品时生成一条记录
 * - 支持离线查看历史记录（本地Room数据库缓存）
 * - 支持联网同步到后端服务器（多设备共享）
 * - 用于统计分析用户的用药习惯和识别频率
 *
 * 数据生命周期：
 * 1. 创建：用户触发药品识别 → 生成新记录（状态=PENDING）
 * 2. 更新：后端返回识别结果 → 更新记录（状态=SUCCESS/FAILED）
 * 3. 展示：历史列表页展示 → 按时间倒序排列
 * 4. 清理：超过保留期限的旧记录自动归档或删除
 *
 * 隐私保护：
 * - 图片默认上传至服务器后立即删除（不留存原始照片）
 * - 仅保存识别结果文本和缩略图（不含EXIF位置信息）
 * - 支持用户手动删除单条或批量清空历史记录
 */

package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 药品识别记录实体
 *
 * 使用Room @Entity注解映射到数据库表。
 * 表名：recognition_records
 * 主键：id（自增Long类型）
 *
 * 索引设计：
 * - userId：按用户查询（多用户隔离）
 * - recognizedAt：按时间排序查询
 * - status：按状态筛选查询
 */
@Entity(
    tableName = "recognition_records",
    indices = [
        androidx.room.Index(value = ["userId"]),
        androidx.room.Index(value = ["recognizedAt"]),
        androidx.room.Index(value = ["status"])
    ]
)
data class RecognitionRecord(
    /**
     * 记录唯一标识符（主键）
     *
     * 由本地Room数据库自动生成的自增ID。
     * 在同步到后端时会映射为服务端的recordId。
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    /**
     * 关联的用户ID
     *
     * 标识这条记录属于哪个用户。
     * 用于多用户设备切换时的数据隔离。
     *
     * 外键关系：
     * - 关联User表的id字段
     * - 用户删除账户时级联删除其所有记录
     */
    @SerializedName("user_id")
    val userId: Long = 0,

    /**
     * 识别出的药品ID（外键）
     *
     * 如果成功识别出具体药品，
     * 则关联到drugs表的对应记录。
     *
     * 可能为null的情况：
     * - OCR未能提取有效文字信息
     * - 提取的关键词未匹配到已知药品
     * - 后端识别服务暂时不可用
     */
    @SerializedName("drug_id")
    val drugId: Long? = null,

    /**
     * 识别出的药品名称（冗余字段）
     *
     * 冗余存储药品名称以避免每次列表展示都需要JOIN查询drug表。
     * 当drugId为null时，此字段可能包含OCR提取的候选关键词。
     *
     * 示例："布洛芬"、"未识别"、"阿莫西林"
     */
    @SerializedName("drug_name")
    val drugName: String? = null,

    /**
     * 原始OCR识别文本
     *
     * 从药盒照片中完整提取的文字内容。
     * 包含批准文号、成分、用法等所有可见文字。
     *
     * 用途：
     * - 用户查看完整的识别原始结果
     * - 纠错反馈时提供给人工审核
     * - 调试和改进OCR算法准确度
     *
     * 存储说明：
     * - 文本长度通常在100-500字符之间
     * - 可能包含换行符和多余空格（需前端格式化显示）
     */
    @SerializedName("ocr_text")
    val ocrText: String? = null,

    /**
     * 缩略图URL地址
     *
     * 药品图片经过压缩处理后的缩略图地址。
     * 用于历史记录列表中的预览展示。
     *
     * 规格参数：
     * - 尺寸：150x150像素（正方形裁切）
     * - 格式：WebP（高压缩率）
     * - 大小：不超过20KB
     *
     * 注意事项：
     * - 原始大图不上传或立即删除（隐私保护）
     * - 缩略图仅用于快速预览，点击可查看详情
     */
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    /**
     * 识别状态枚举值
     *
     * 表示当前这条识别记录的处理状态。
     *
     * 状态流转图：
     * PENDING → PROCESSING → SUCCESS / FAILED
     *                              ↓
     *                         （用户可提交FEEDBACK）
     *
     * 各状态含义：
     * - "PENDING"：等待处理（刚创建，尚未发送到后端）
     * - "PROCESSING"：处理中（已发送请求，等待后端响应）
     * - "SUCCESS"：识别成功（已返回有效的药品信息）
     * - "FAILED"：识别失败（无匹配结果或网络错误）
     * - "CANCELLED"：用户取消（主动中断识别流程）
     *
     * UI交互影响：
     * - PENDING/PROCESSING：显示加载动画
     * - SUCCESS：显示药品卡片（绿色边框）
     * - FAILED：显示错误提示 + 重试按钮（红色边框）
     */
    @SerializedName("status")
    val status: String = "PENDING",

    /**
     * 识别置信度分数（0.0 - 1.0）
     *
     * AI模型对本次识别结果的信心程度评估。
     * 分数越高表示识别结果越可靠。
     *
     * 分数区间解读：
     * - 0.9 - 1.0：高度可信（清晰图像 + 完整文字）
     * - 0.7 - 0.9：基本可信（部分遮挡但关键信息可见）
     * - 0.5 - 0.7：低可信度（模糊、倾斜、光线不足）
     * - 0.0 - 0.5：不可信（建议重新拍照）
     *
     * UI展示方式：
     * - 进度条形式可视化
     * - 高分显示绿色，低分显示红色
     * - 低于阈值时提示用户重新拍摄
     */
    @SerializedName("confidence_score")
    val confidenceScore: Float = 0f,

    /**
     * 识别耗时（毫秒）
     *
     * 从用户按下拍照按钮到收到识别结果的耗时统计。
     * 用于性能监控和用户体验优化。
     *
     * 性能基准：
     * - < 2秒：优秀（用户几乎无感知延迟）
     * - 2-5秒：良好（可接受的等待时间）
     * - 5-10秒：一般（需要显示进度条）
     * - > 10秒：较差（需优化或检查网络）
     *
     * 统计用途：
     * - 计算平均识别耗时（P50/P95/P99分位数）
     * - 发现性能瓶颈并针对性优化
     * - A/B测试不同算法的性能差异
     */
    @SerializedName("duration_ms")
    val durationMs: Long = 0,

    /**
     * 识别发生的时间戳
     *
     * 用户触发识别操作的精确时间点。
     * 格式：ISO 8601 UTC时间字符串
     *
     * 排序依据：
     * - 历史记录列表默认按此字段倒序排列
     * - 最新的记录显示在最顶部
     */
    @SerializedName("recognized_at")
    val recognizedAt: String? = null,

    /**
     * 设备信息JSON字符串
     *
     * 记录发起识别请求时的设备环境信息。
     * 用于问题排查和统计分析。
     *
     * JSON结构示例：
     * ```json
     * {
     *   "platform": "Android",
     *   "sdk_version": 33,
     *   "app_version": "2.1.0",
     *   "device_model": "Pixel 7 Pro",
     *   "camera_resolution": "4032x3024"
     * }
     * ```
     *
     * 隐私说明：
     * - 不收集IMEI/MEID等永久性设备标识
     * - 不包含GPS定位信息
     * - 符合最小化数据收集原则
     */
    @SerializedName("device_info")
    val deviceInfo: String? = null,

    /**
     * 错误信息（可选）
     *
     * 当识别失败时记录具体的错误原因。
     * 帮助开发团队定位问题和改进算法。
     *
     * 常见错误类型：
     * - "NO_TEXT_DETECTED"：未检测到文字（照片模糊）
     * - "DRUG_NOT_FOUND"：未匹配到已知药品
     * - "NETWORK_ERROR"：网络连接异常
     * - "SERVER_ERROR"：服务器内部错误
     * - "TIMEOUT"：请求超时
     *
     * 用户可见性：
     * - 技术性错误码转换为用户友好的中文提示
     * - 详细错误信息仅在"调试模式"下展示
     */
    @SerializedName("error_message")
    val errorMessage: String? = null,

    /**
     * 是否已同步到后端服务器
     *
     * 标识本条记录是否已成功上传至云端。
     * 用于离线优先（Offline-First）架构的数据同步策略。
     *
     * 同步逻辑：
     * - false：仅存在于本地（待上传队列）
     * - true：已同步至服务器（可安全删除本地副本）
     *
     * 同步时机：
     * - WiFi环境下自动后台同步
     * - 用户手动触发"刷新"操作
     * - 应用启动时增量同步
     */
    @SerializedName("is_synced")
    val isSynced: Boolean = false
) {
    companion object {
        /** 状态常量定义 */
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_CANCELLED = "CANCELLED"

        /** 置信度阈值常量 */
        const val CONFIDENCE_HIGH = 0.9f
        const val CONFIDENCE_MEDIUM = 0.7f
        const val CONFIDENCE_LOW = 0.5f

        /**
         * 判断识别是否成功
         *
         * @return true表示成功识别出了药品信息
         */
        fun isSuccess(status: String): Boolean = status == STATUS_SUCCESS

        /**
         * 获取状态的中文显示名称
         *
         * @param status 英文状态代码
         * @return 中文状态描述
         */
        fun getStatusDisplayName(status: String): String {
            return when (status) {
                STATUS_PENDING -> "等待处理"
                STATUS_PROCESSING -> "识别中..."
                STATUS_SUCCESS -> "识别成功"
                STATUS_FAILED -> "识别失败"
                STATUS_CANCELLED -> "已取消"
                else -> "未知状态"
            }
        }

        /**
         * 获取置信度对应的UI颜色资源名
         *
         * @param score 置信度分数（0.0-1.0）
         * @return Material3语义化颜色名称
         */
        fun getConfidenceColor(score: Float): String {
            return when {
                score >= CONFIDENCE_HIGH -> "primary"      // 绿色（高可信）
                score >= CONFIDENCE_MEDIUM -> "tertiary"    // 橙色（中等）
                else -> "error"                             // 红色（低可信）
            }
        }

        /**
         * 格式化耗时为可读字符串
         *
         * @param durationMs 毫秒数
         * @return 格式化的时间字符串（如"1.2秒"、"350ms"）
         */
        fun formatDuration(durationMs: Long): String {
            return if (durationMs >= 1000) {
                "${durationMs / 1000}.${(durationMs % 1000) / 100}秒"
            } else {
                "${durationMs}ms"
            }
        }
    }
}
