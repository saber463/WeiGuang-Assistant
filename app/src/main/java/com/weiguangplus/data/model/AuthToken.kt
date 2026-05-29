/**
 * 文件名：AuthToken.kt
 * 作者：微光同行前端团队
 * 功能描述：用户认证Token数据类，封装AccessToken和RefreshToken的完整信息
 * 创建日期：2026-05-29
 * 所属模块：data/model（数据模型层）
 *
 * 技术背景：
 * 本项目采用OAuth 2.0 + JWT（JSON Web Token）的双Token认证机制：
 *
 * 1. AccessToken（访问令牌）
 *    - 有效期较短（通常15分钟 - 24小时）
 *    - 携带在每次API请求的Authorization头中
 *    - 包含用户ID、角色、权限等声明（Claims）
 *    - 过期后使用RefreshToken刷新获取新的AccessToken
 *
 * 2. RefreshToken（刷新令牌）
 *    - 有效期较长（通常7天 - 30天）
 *    - 仅用于刷新AccessToken，不能用于API调用
 *    - 存储在服务端数据库中，可被主动撤销
 *    - 支持单次使用策略（使用后立即失效并发放新RefreshToken）
 *
 * 安全机制：
 * - Token通过HTTPS加密传输
 * - 本地存储在Android DataStore（加密存储，需要设备锁屏密码）
 * - TokenRefreshInterceptor自动处理过期刷新流程
 * - 支持多端登录互踢（同一账号仅允许一个活跃Token）
 *
 * 数据流转图：
 * 登录成功 → 服务端返回AuthToken → 客户端保存到DataStore
 *     ↓
 * 发起API请求 → AuthInterceptor注入AccessToken → 服务端验证
 *     ↓ （如果401过期）
 * TokenRefreshInterceptor拦截 → 使用RefreshToken调用刷新接口
 *     ↓
 * 获取新AuthToken → 更新DataStore → 用新Token重试原请求 → 成功！
 */

package com.weiguangplus.data.model

import com.google.gson.annotations.SerializedName

/**
 * 认证令牌数据类
 *
 * 封装从后端登录/注册/刷新接口返回的完整Token信息。
 * 不使用Room @Entity注解（不持久化到本地数据库），
 * 仅在运行时内存中使用和通过DataStore轻量存储关键字段。
 *
 * 设计原则：
 * 1. 不可变性（所有属性为val）：确保线程安全
 * 2. 可序列化：支持Gson JSON转换和网络传输
 * 3. 最小化存储：仅保存必要的Token字段到本地
 */
data class AuthToken(
    /**
     * 访问令牌（Access Token）
     *
     * JWT格式的字符串，由三部分组成并用点号分隔：
     * Header.Payload.Signature
     *
     * 示例（简化版）：
     * ```
     * eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.
     * eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IuW8oOS4iSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNzE2OTk5NjAwfQ.
     * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
     * ```
     *
     * Payload部分包含的信息（Base64解码后可见）：
     * - sub（Subject）：用户ID
     * - name：用户姓名
     * - role：用户角色权限
     * - exp（Expiration Time）：过期时间戳
     * - iat（Issued At）：签发时间戳
     *
     * 安全注意事项：
     * - ⚠️ 即使是编码后的JWT也不应记录到日志中（可能泄露用户身份）
     * - ⚠️ 不要在URL查询参数中传递Token（可能出现在服务器日志或浏览器历史中）
     * - ✅ 必须放在HTTP Header的Authorization字段中
     * - ✅ 生产环境必须使用HTTPS协议传输
     *
     * JSON字段名：access_token / token
     */
    @SerializedName("access_token")
    val accessToken: String,

    /**
     * 刷新令牌（Refresh Token）
     *
     * 用于在AccessToken过期后获取新的访问令牌。
     * 比AccessToken更长且更安全（不包含用户详细信息）。
     *
     * 与AccessToken的区别：
     * | 特性          | AccessToken        | RefreshToken       |
     * |---------------|--------------------|-------------------|
     * | 有效期         | 短（分钟~小时级）   | 长（天~周级）      |
     * | 包含信息       | 用户详情+权限      | 仅Token标识符      |
     * | 使用场景       | 每次API请求认证     | 仅用于刷新操作     |
     * | 存储位置       | 内存+DataStore     | 仅DataStore加密    |
     * | 泄露风险       | 中等（短期有效）    | 较高（长期有效）    |
     * | 撤销方式       | 等待自动过期        | 服务端黑名单即时撤销 |
     *
     * 刷新流程触发条件：
     * - API响应返回HTTP 401（Unauthorized）
     * - API响应返回HTTP 403（Forbidden）
     * - 前端检测到AccessToken即将过期（提前30秒刷新）
     *
     * JSON字段名：refresh_token / refresh
     */
    @SerializedName("refresh_token")
    val refreshToken: String,

    /**
     * AccessToken的有效期时长（秒）
     *
     * 表示从签发时刻起，AccessToken保持有效的总秒数。
     * 用于客户端计算Token是否即将过期并提前刷新。
     *
     * 常见有效期设置：
     * - 开发环境：86400秒（24小时，方便调试）
     * - 测试环境：3600秒（1小时）
     * - 生产环境：900秒（15分钟，安全性最佳）
     *
     * 计算公式：
     * ```
     * 过期时间 = 当前时间 + expiresIn秒
     * 提前刷新时间 = 过期时间 - 30秒缓冲
     * ```
     *
     * 前端使用示例：
     * ```kotlin
     * val expirationTime = System.currentTimeMillis() + (authToken.expiresIn * 1000)
     * val shouldRefresh = System.currentTimeMillis() > (expirationTime - 30_000L)
     * ```
     *
     * JSON字段名：expires_in / expires
     */
    @SerializedName("expires_in")
    val expiresIn: Long,

    /**
     * Token类型标识
     *
     * 标识当前使用的认证方案类型。
     * 标准OAuth 2.0实现通常为"Bearer"。
     *
     * 格式要求：
     * - 大小写敏感（Bearer不是bearer）
     * - 在Authorization头中的格式："Bearer {accessToken}"
     *
     * HTTP请求头示例：
     * ```
     * Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
     * ```
     *
     * 其他可能的值（非标准）：
     * - "Mac"：基于消息认证码的Token
     * - "Basic"：Base64编码的用户名:密码（本项目不使用）
     *
     * JSON字段名：token_type / type
     */
    @SerializedName("token_type")
    val tokenType: String = "Bearer"
) {
    companion object {
        /** 默认的有效期常量（24小时，用于开发环境） */
        const val DEFAULT_EXPIRES_IN = 86400L

        /** 标准的Token类型值（OAuth 2.0规范） */
        const val TOKEN_TYPE_BEARER = "Bearer"

        /**
         * 检查Token是否即将过期
         *
         * 根据当前时间和expiresIn计算，
         * 如果距离过期时间不足缓冲时间则认为"即将过期"。
         *
         * @param authToken 要检查的Token对象
         * @param bufferSeconds 提前刷新的缓冲时间（默认60秒）
         * @return true表示应该立即刷新Token
         */
        fun isExpiringSoon(authToken: AuthToken?, bufferSeconds: Long = 60L): Boolean {
            if (authToken == null) return true

            // 计算Token的理论过期时间点（毫秒）
            // 注意：这里假设expiresIn是从签发时开始计算的绝对秒数
            // 实际场景中可能需要结合iat（issuedAt）字段更精确计算
            val expirationTimestamp = System.currentTimeMillis() + (authToken.expiresIn * 1000)

            // 当前时间 + 缓冲时间 是否已超过过期时间点
            val currentTimeWithBuffer = System.currentTimeMillis() + (bufferSeconds * 1000)

            return currentTimeWithBuffer >= expirationTimestamp
        }

        /**
         * 从JSON字符串解析AuthToken对象
         *
         * 用于从DataStore或其他存储介质恢复Token数据。
         *
         * @param jsonStr JSON格式字符串
         * @return 解析成功的AuthToken对象，解析失败返回null
         */
        fun fromJson(jsonStr: String?): AuthToken? {
            if (jsonStr.isNullOrBlank()) return null

            return try {
                com.google.gson.Gson().fromJson(jsonStr, AuthToken::class.java)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 将AuthToken对象序列化为JSON字符串
         *
         * 用于持久化存储到DataStore。
         * 注意：出于安全考虑，生产环境建议加密后再存储。
         *
         * @param authToken 要序列化的Token对象
         * @return JSON格式字符串
         */
        fun toJson(authToken: AuthToken): String {
            return com.google.gson.Gson().toJson(authToken)
        }
    }
}
