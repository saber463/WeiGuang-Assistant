/**
 * 文件名：User.kt
 * 作者：微光同行前端团队
 * 功能描述：用户信息数据类，对应后端User实体和数据库表结构
 * 创建日期：2026-05-29
 * 所属模块：data/model（数据模型层）
 *
 * 数据来源：
 * 1. 后端API响应（注册成功、登录成功、获取个人信息接口返回）
 * 2. Room本地数据库缓存（离线时读取用户基本信息）
 * 3. DataStore轻量级存储（仅保存关键字段如userId、phone等）
 *
 * 字段说明（共15个核心属性）：
 * - 基础信息：id、phone、realName、avatarUrl
 * - 残疾信息：disabilityType、disabilityLevel、disabilityCertNo
 * - 联系方式：emergencyContact、emergencyPhone
 * - 系统字段：createdAt、updatedAt、lastLoginAt
 * - 状态字段：isActive、isVerified、role
 *
 * 使用场景：
 * - 个人中心页面展示用户资料
 * - 认证流程中的身份验证
 * - 应急求助功能中的紧急联系人信息
 * - 统计分析中的用户画像数据
 */

package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 用户实体数据类
 *
 * 使用Room @Entity注解标记为数据库表，
 * 同时使用Gson @SerializedName注解支持JSON序列化。
 *
 * 表名：users（Room自动生成）
 * 主键：id（Long类型，自增或后端分配）
 *
 * 设计原则：
 * 1. 所有字段使用可空类型（?）防止JSON解析异常
 * 2. 提供默认值确保数据类实例化安全
 * 3. 字段命名遵循Kotlin camelCase规范
 * 4. 通过@SerializedName映射后端snake_case字段名
 */
@Entity(tableName = "users")
data class User(
    /**
     * 用户唯一标识符（主键）
     *
     * 由后端数据库自增生成或UUID格式。
     * 在整个系统中唯一标识一个用户账户。
     *
     * JSON字段名：user_id / id
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    /**
     * 手机号（登录账号）
     *
     * 中国大陆手机号格式（11位数字）。
     * 作为用户的唯一登录凭证和联系方式。
     *
     * 格式校验规则：
     * - 长度必须为11位
     * - 必须以1开头
     * - 第二位为3-9的数字
     * - 示例：13800138000、15912345678
     *
     * JSON字段名：phone / mobile
     */
    @SerializedName("phone")
    val phone: String? = null,

    /**
     * 真实姓名
     *
     * 用于实名认证和政府补贴申领。
     * 敏感信息，在UI展示时应部分隐藏（如：张*三）。
     *
     * 存储要求：
     * - 最长50个字符
     * - 支持中文、少数民族文字
     * - 不允许特殊符号
     *
     * JSON字段名：real_name / name
     */
    @SerializedName("real_name")
    val realName: String? = null,

    /**
     * 头像URL地址
     *
     * 用户上传的个人头像图片地址。
     * 支持相对路径（需拼接CDN基础URL）或完整URL。
     *
     * 图片规格建议：
     * - 格式：JPG/PNG/WebP
     * - 尺寸：200x200像素（正方形）
     * - 大小：不超过500KB
     *
     * 默认值：系统提供的默认头像占位图
     *
     * JSON字段名：avatar_url / avatar
     */
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    /**
     * 残疾类型枚举值
     *
     * 表示用户的主要残疾类别，用于个性化功能推荐和无障碍适配。
     *
     * 可选值（对应DisabilityType枚举）：
     * - "VISUAL_IMPAIRMENT"：视力障碍
     * - "HEARING_IMPAIRMENT"：听力障碍
     * - "PHYSICAL_DISABILITY"：肢体残疾
     * - "INTELLECTUAL_DISABILITY"：智力障碍
     * - "SPEECH_IMPAIRMENT"：言语障碍
     * - "MULTIPLE_DISABILITIES"：多重残疾
     *
     * 业务影响：
     * - 决定App启动时的默认功能页面
     * - 影响TTS语音播报的内容和语速
     * - 控制振动反馈的强度模式
     *
     * JSON字段名：disability_type
     */
    @SerializedName("disability_type")
    val disabilityType: String? = null,

    /**
     * 残疾等级
     *
     * 根据中国残疾人残疾分级标准划分。
     * 用于政府补贴申领资格判断和服务优先级排序。
     *
     * 分级标准：
     * - "LEVEL_1"：一级（极重度）
     * - "LEVEL_2"：二级（重度）
     * - "LEVEL_3"：三级（中度）
     * - "LEVEL_4"：四级（轻度）
     *
     * JSON字段名：disability_level
     */
    @SerializedName("disability_level")
    val disabilityLevel: String? = null,

    /**
     * 残疾证编号
     *
     * 中华人民共和国残疾人证号码。
     * 用于实名认证和优惠政策核验。
     *
     * 格式示例：510XXXXXXXXXXXXXXX（17位数字+校验码）
     *
     * 安全提示：
     * - 属于个人敏感信息，需要加密存储
     * - UI展示时应脱敏处理（如：510***********123X）
     * - 仅在必要时展示给用户本人确认
     *
     * JSON字段名：disability_cert_no
     */
    @SerializedName("disability_cert_no")
    val disabilityCertNo: String? = null,

    /**
     * 紧急联系人姓名
     *
     * 用户预设的紧急情况下的联络人。
     * 在SOS一键求助功能中使用。
     *
     * 建议设置：
     * - 家属、监护人或护理人员
     * - 确保对方知晓并同意作为紧急联系人
     * - 定期更新联系信息确保有效性
     *
     * JSON字段名：emergency_contact
     */
    @SerializedName("emergency_contact")
    val emergencyContact: String? = null,

    /**
     * 紧急联系人电话
     *
     * 与紧急联系人对应的手机号码。
     * 在触发SOS求助时自动拨打或发送短信。
     *
     * 格式要求：
     * - 与手机号格式相同（11位数字）
     * - 可设置多个联系人（逗号分隔）
     *
     * JSON字段名：emergency_phone
     */
    @SerializedName("emergency_phone")
    val emergencyPhone: String? = null,

    /**
     * 账户创建时间戳
     *
     * 用户完成注册的时间点（UTC时间）。
     * 格式：ISO 8601标准（yyyy-MM-dd'T'HH:mm:ss'Z'）
     *
     * 用途：
     * - 统计用户新增趋势
     * - 判断账户年龄（新用户引导流程）
     * - 数据归档和清理策略依据
     *
     * JSON字段名：created_at
     */
    @SerializedName("created_at")
    val createdAt: String? = null,

    /**
     * 最后更新时间戳
     *
     * 用户资料最后一次修改的时间点。
     * 用于客户端数据同步和增量更新策略。
     *
     * JSON字段名：updated_at
     */
    @SerializedName("updated_at")
    val updatedAt: String? = null,

    /**
     * 最后登录时间戳
     *
     * 用户最后一次成功认证登录的时间点。
     * 用于安全检测（异常登录提醒）和活跃度统计。
     *
     * 安全机制：
     * - 如果发现异地登录，推送安全警告通知
     * - 长时间未登录的用户可发送召回消息
     *
     * JSON字段名：last_login_at
     */
    @SerializedName("last_login_at")
    val lastLoginAt: String? = null,

    /**
     * 账户激活状态标志
     *
     * 标识当前账户是否处于正常可用状态。
     *
     * 可能状态：
     * - true：正常（可正常使用所有功能）
     * - false：禁用/冻结（违规操作或用户主动注销）
     *
     * 冻结原因示例：
     * - 违反社区准则
     * - 长期未使用（休眠账户）
     * - 用户主动申请注销
     *
     * JSON字段名：is_active
     */
    @SerializedName("is_active")
    val isActive: Boolean = true,

    /**
     * 实名认证状态标志
     *
     * 标识用户是否已完成身份验证流程。
     *
     * 认证方式：
     * - 身份证OCR识别 + 人脸比对
     * - 残疾证信息核验
     * - 人工审核（特殊情况下）
     *
     * 未认证影响：
     * - 无法申领政府补贴
     * - 无法使用设备租赁服务
     * - 部分功能受限使用
     *
     * JSON字段名：is_verified
     */
    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    /**
     * 用户角色权限
     *
     * 定义用户在系统中的权限级别和可访问的功能范围。
     *
     * 角色列表：
     * - "USER"：普通用户（默认角色）
     * - "VIP_USER"：VIP会员（付费订阅用户）
     * - "VOLUNTEER"：志愿者（提供帮扶服务的人员）
     * - "STAFF"：工作人员（基地服务人员）
     * - "ADMIN"：管理员（系统管理权限）
     *
     * 权限控制：
     * - 不同角色可见不同的菜单项
     * - 特殊功能需要特定角色才能解锁
     * - API接口层也会进行权限校验
     *
     * JSON字段名：role
     */
    @SerializedName("role")
    val role: String = "USER"
) {
    companion object {
        /** 默认角色常量 */
        const val ROLE_USER = "USER"
        const val ROLE_VIP = "VIP_USER"
        const val ROLE_VOLUNTEER = "VOLUNTEER"
        const val ROLE_STAFF = "STAFF"
        const val ROLE_ADMIN = "ADMIN"

        /** 残疾类型常量 */
        const val TYPE_VISUAL = "VISUAL_IMPAIRMENT"
        const val TYPE_HEARING = "HEARING_IMPAIRMENT"
        const val TYPE_PHYSICAL = "PHYSICAL_DISABILITY"
        const val TYPE_INTELLECTUAL = "INTELLECTUAL_DISABILITY"
        const val TYPE_SPEECH = "SPEECH_IMPAIRMENT"
        const val TYPE_MULTIPLE = "MULTIPLE_DISABILITIES"

        /** 残疾等级常量 */
        const val LEVEL_1 = "LEVEL_1"  // 极重度
        const val LEVEL_2 = "LEVEL_2"  // 重度
        const val LEVEL_3 = "LEVEL_3"  // 中度
        const val LEVEL_4 = "LEVEL_4"  // 轻度

        /**
         * 获取残疾类型的中文显示名称
         *
         * 将英文枚举值转换为用户友好的中文标签，
         * 用于UI界面的文本展示。
         *
         * @param type 英文类型的枚举值
         * @return 对应的中文名称，未知类型返回"未指定"
         */
        fun getDisabilityTypeName(type: String?): String {
            return when (type) {
                TYPE_VISUAL -> "视力障碍"
                TYPE_HEARING -> "听力障碍"
                TYPE_PHYSICAL -> "肢体残疾"
                TYPE_INTELLECTUAL -> "智力障碍"
                TYPE_SPEECH -> "言语障碍"
                TYPE_MULTIPLE -> "多重残疾"
                else -> "未指定"
            }
        }

        /**
         * 获取残疾等级的中文显示名称
         *
         * @param level 英文等级的枚举值
         * @return 对应的中文名称
         */
        fun getDisabilityLevelName(level: String?): String {
            return when (level) {
                LEVEL_1 -> "一级（极重度）"
                LEVEL_2 -> "二级（重度）"
                LEVEL_3 -> "三级（中度）"
                LEVEL_4 -> "四级（轻度）"
                else -> "未评定"
            }
        }

        /**
         * 获取脱敏后的真实姓名
         *
         * 保护用户隐私，仅显示姓氏和末字。
         * 示例："张三丰" → "张*丰"，"欧阳修" → "欧*修"
         *
         * @param fullName 完整的真实姓名
         * @return 脱敏后的姓名字符串
         */
        fun maskRealName(fullName: String?): String {
            if (fullName.isNullOrBlank() || fullName.length <= 2) {
                return fullName ?: "未填写"
            }
            return "${fullName.first()}*${fullName.last()}"
        }

        /**
         * 获取脱敏后的手机号
         *
         * 隐藏中间4位数字，保留前3位和后4位。
         * 示例："13800138000" → "138****8000"
         *
         * @param phone 完整的手机号
         * @return 脱敏后的手机号字符串
         */
        fun maskPhone(phone: String?): String {
            if (phone.isNullOrBlank() || phone.length != 11) {
                return phone ?: "未绑定"
            }
            return "${phone.take(3)}****${phone.takeLast(4)}"
        }
    }
}
