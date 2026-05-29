/**
 * 文件名：WeiguangApiService.kt
 * 作者：微光同行前端团队
 * 功能描述：微光同行后端API接口定义（Retrofit Service接口）
 * 创建日期：2026-05-29
 * 所属模块：network（网络层）
 *
 * 接口清单（对应API-CONTRACT.md契约文档）：
 *
 * 【认证相关接口 - 5个】
 * 1. POST /api/auth/login          - 用户登录（手机号+密码）
 * 2. POST /api/auth/register        - 用户注册（手机号+密码+残疾类型）
 * 3. POST /api/auth/logout          - 用户登出（清除服务端Session）
 * 4. POST /api/auth/token/refresh   - 刷新AccessToken（使用RefreshToken）
 * 5. GET  /api/auth/profile         - 获取当前用户信息
 *
 * 【药品相关接口 - 5个】
 * 1. POST /api/drugs/recognize      - 药品图片识别（上传药盒照片OCR识别）
 * 2. GET  /api/drugs/history        - 获取识别历史记录（分页加载）
 * 3. GET  /api/drugs/search         - 搜索药品（按名称/批准文号/成分）
 * 4. GET  /api/drugs/{id}           - 获取药品详情
 * 5. POST /api/drugs/{id}/feedback  - 提交识别结果反馈（纠错）
 *
 * 技术特性：
 * - 使用Retrofit注解声明式定义HTTP API
 * - 支持Multipart上传（药品图片）
 * - 统一返回ApiResponse<T>泛型包装
 * - 完整的KDoc中文注释便于团队协作
 */

package com.weiguangplus.network

import com.weiguangplus.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 微光同行后端API服务接口
 *
 * 这是Retrofit的核心接口定义类，
 * 所有的HTTP API调用都通过此接口进行类型安全的声明。
 *
 * 使用方式：
 * ```
 * // 在Hilt模块中通过@Inject注入此接口
 * @Inject lateinit var apiService: WeiguangApiService
 *
 * // 调用API（返回Deferred或直接挂起函数）
 * val result = apiService.login(phone, password)
 * ```
 *
 * 设计原则：
 * 1. 单一职责：每个方法只做一件事
 * 2. 类型安全：使用数据类作为参数和返回值
 * 3. 可测试性：接口易于Mock（单元测试友好）
 * 4. 幂等性：GET请求不产生副作用，POST/PUT有明确语义
 */
interface WeiguangApiService {

    // ==================== 认证模块（Auth APIs） ====================

    /**
     * 用户登录接口
     *
     * 向后端发送手机号和密码进行身份验证，
     * 成功后返回AuthToken（包含accessToken和refreshToken）。
     *
     * HTTP方法：POST
     * 端点：/api/auth/login
     * Content-Type：application/json
     *
     * 请求体示例：
     * ```json
     * {
     *   "phone": "13800138000",
     *   "password": "user123456"
     * }
     * ```
     *
     * 成功响应（200）：
     * ```json
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "accessToken": "eyJhbGci...",
     *     "refreshToken": "eyJhbGci...",
     *     "expiresIn": 86400,
     *     "tokenType": "Bearer"
     *   }
     * }
     * ```
     *
     * 错误响应：
     * - 400：参数错误（手机号格式不正确、密码为空等）
     * - 401：账号或密码错误
     * - 429：请求过于频繁（需等待冷却时间）
     * - 500：服务器内部错误
     *
     * @param phone 手机号（11位数字，中国手机号格式）
     * @param password 用户密码（明文传输，HTTPS加密保护）
     * @return 包装了AuthToken的API响应对象
     */
    @POST("api/auth/login")
    suspend fun login(
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Response<ApiResponse<AuthToken>>

    /**
     * 用户注册接口
     *
     * 创建新用户账户，需要提供基本信息和残疾类型。
     * 注册成功后自动登录并返回AuthToken。
     *
     * HTTP方法：POST
     * 端点：/api/auth/register
     * Content-Type：application/json
     *
     * 请求体示例：
     * ```json
     * {
     *   "phone": "13900139000",
     *   "password": "securePass123",
     *   "confirmPassword": "securePass123",
     *   "disabilityType": "VISUAL_IMPAIRMENT",
     *   "realName": "张三",
     *   "idCard": "510***********1234"
     * }
     * ```
     *
     * 残疾类型枚举值（DisabilityType）：
     * - VISUAL_IMPAIRMENT：视力障碍
     * - HEARING_IMPAIRMENT：听力障碍
     * - PHYSICAL_DISABILITY：肢体残疾
     * - INTELLECTUAL_DISABILITY：智力障碍
     * - SPEECH_IMPAIRMENT：言语障碍
     * - MULTIPLE_DISABILITIES：多重残疾
     *
     * @param phone 手机号（唯一标识，不可重复注册）
     * @param password 密码（长度6-20位，需包含字母和数字）
     * @param confirmPassword 确认密码（必须与password一致）
     * @param disabilityType 残疾类型（必填，用于个性化功能推荐）
     * @param realName 真实姓名（选填，用于实名认证）
     * @param idCard 身份证号（选填，用于政府补贴申领）
     * @return 包装了新建User信息的API响应对象
     */
    @POST("api/auth/register")
    suspend fun register(
        @Field("phone") phone: String,
        @Field("password") password: String,
        @Field("confirmPassword") confirmPassword: String,
        @Field("disabilityType") disabilityType: String,
        @Field("realName") realName: String? = null,
        @Field("idCard") idCard: String? = null
    ): Response<ApiResponse<User>>

    /**
     * 用户登出接口
     *
     * 清除服务端的用户会话（Session/Token黑名单），
     * 同时客户端应清除本地保存的Token。
     *
     * HTTP方法：POST
     * 端点：/api/auth/logout
     * 需要认证：是（需要在Header中携带有效的AccessToken）
     *
     * 注意事项：
     * - 此接口调用后，当前的AccessToken将立即失效
     * - 客户端应同时清除DataStore中的本地Token缓存
     * - 建议登出后跳转到登录页面
     *
     * @return 操作结果的API响应（code=200表示登出成功）
     */
    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    /**
     * Token刷新接口
     *
     * 当AccessToken过期时，使用RefreshToken获取新的访问令牌。
     * 这是保持用户登录状态的关键接口。
     *
     * HTTP方法：POST
     * 端点：/api/auth/token/refresh
     * Content-Type：application/json
     *
     * 工作机制：
     * 1. 客户端检测到401响应码
     * 2. 自动调用此接口传入RefreshToken
     * 3. 后端验证RefreshToken有效性
     * 4. 返回新的AccessToken（可能同时更新RefreshToken）
     * 5. 客户端保存新Token并用原请求重试
     *
     * 安全说明：
     * - RefreshToken的有效期通常较长（7-30天）
     * - RefreshToken只能使用一次（单次使用后失效）
     * - 如果RefreshToken也过期，需要用户重新登录
     *
     * @param refreshToken 从上次登录/刷新获取的刷新令牌
     * @return 包含新AuthToken的API响应对象
     */
    @POST("api/auth/token/refresh")
    suspend fun refreshToken(
        @Field("refreshToken") refreshToken: String
    ): Response<ApiResponse<AuthToken>>

    /**
     * 获取当前用户信息接口
     *
     * 获取已登录用户的详细资料，
     * 包括个人信息、偏好设置、统计数据等。
     *
     * HTTP方法：GET
     * 端点：/api/auth/profile
     * 需要认证：是
     *
     * 使用场景：
     * - 应用启动时恢复用户状态
     * - 个人中心页面展示用户信息
     * - 检查Token是否仍然有效（隐式验证）
     *
     * @return 包装了完整User对象的API响应
     */
    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    // ==================== 药品模块（Drug APIs） ====================

    /**
     * 药品图片识别接口
     *
     * 上传药品包装盒的照片到后端进行AI识别，
     * 返回识别出的药品信息（名称、成分、用法用量、风险提示等）。
     *
     * HTTP方法：POST
     * 端点：/api/drugs/recognize
     * Content-Type：multipart/form-data
     * 需要认证：是
     *
     * 技术实现：
     * - 后端接收图片后进行OCR文字提取
     * - 提取批准文号、药品名称等关键信息
     * - 匹配药品数据库获取详细信息
     * - 进行风险等级评估（高/中/低风险）
     * - 返回结构化的药品信息JSON
     *
     * 图片要求：
     * - 格式：JPG/PNG/WebP
     * - 大小：不超过10MB
     * - 分辨率：建议1920x1080以上
     * - 内容：清晰可见的药品包装盒正面
     *
     * @param image 药品图片文件（MultipartBody.Part格式）
     * @return 包装了识别结果Drug对象的API响应
     */
    @Multipart
    @POST("api/drugs/recognize")
    suspend fun recognizeDrug(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<Drug>>

    /**
     * 获取药品识别历史记录接口
     *
     * 分页查询当前用户的历史识别记录，
     * 支持按时间倒序排列和筛选条件过滤。
     *
     * HTTP方法：GET
     * 端点：/api/drugs/history
     * 需要认证：是
     *
     * 分页参数说明：
     * - page：页码（从1开始，默认1）
     * - size：每页条数（默认20，最大100）
     *
     * 响应结构：
     * ```json
     * {
     *   "code": 200,
     *   "data": {
     *     "content": [ ... ],      // 当前页的数据列表
     *     "totalElements": 156,    // 总记录数
     *     "totalPages": 8,         // 总页数
     *     "currentPage": 1,        // 当前页码
     *     "size": 20               // 每页大小
     *   }
     * }
     * ```
     *
     * @param page 页码（从1开始，可选，默认1）
     * @param size 每页条数（可选，默认20，最大100）
     * @return 包装了分页RecognitionRecord列表的API响应
     */
    @GET("api/drugs/history")
    suspend fun getRecognitionHistory(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<List<RecognitionRecord>>>

    /**
     * 搜索药品接口
     *
     * 根据关键词搜索药品数据库，
     * 支持按药品名、通用名、批准文号、成分等多维度模糊匹配。
     *
     * HTTP方法：GET
     * 端点：/api/drugs/search
     * 需要认证：否（公开接口，但登录用户可获取更多信息）
     *
     * 搜索示例：
     * - keyword="布洛芬" → 返回所有含"布洛芬"的药品
     * - keyword="H10900089" → 通过批准文号精确查找
     * - keyword="止痛" → 按适应症模糊搜索
     *
     * 排序方式：
     * - relevance（相关度，默认）
     * - name_asc（名称升序）
     * - name_desc（名称降序）
     *
     * @param keyword 搜索关键词（必填，最少2个字符）
     * @param page 页码（可选，默认1）
     * @param size 每页条数（可选，默认20）
     * @param sortBy 排序字段（可选，默认relevance）
     * @return 包装了搜索结果Drug列表的API响应
     */
    @GET("api/drugs/search")
    suspend fun searchDrugs(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "relevance"
    ): Response<ApiResponse<List<Drug>>>

    /**
     * 获取药品详情接口
     *
     * 根据药品ID获取完整的药品详细信息，
     * 包括成分、适应症、用法用量、禁忌、不良反应、风险提示等。
     *
     * HTTP方法：GET
     * 端点：/api/drugs/{id}
     * 需要认证：否（公开接口）
     *
     * 使用场景：
     * - 从搜索结果列表点击进入详情页
     * - 识别历史记录中查看某次识别的完整信息
     * - 药品对比功能的数据来源
     *
     * @param drugId 药品的唯一标识符（路径参数）
     * @return 包装了完整Drug详情对象的API响应
     */
    @GET("api/drugs/{id}")
    suspend fun getDrugDetail(
        @Path("id") drugId: Long
    ): Response<ApiResponse<Drug>>

    /**
     * 提交识别结果反馈接口
     *
     * 用户对AI药品识别结果进行纠错或确认，
     * 反馈数据用于改进识别算法的准确度。
     *
     * HTTP方法：POST
     * 端点：/api/drugs/{id}/feedback
     * 需要认证：是
     *
     * 反馈类型：
     * - correct：识别正确（确认无误）
     * - wrong_drug：识别错误的药品（提交正确的药品ID）
     * - incomplete：信息不完整（缺少某些字段）
     * - other：其他问题（填写具体描述）
     *
     * @param drugId 药品ID（路径参数）
     * @param feedbackType 反馈类型（correct/wrong_drug/incomplete/other）
     * @param correctDrugId 如果识别错误，提供正确的药品ID（可选）
     * @param description 详细描述（可选，最多500字）
     * @return 操作结果的API响应（code=200表示反馈提交成功）
     */
    @FormUrlEncoded
    @POST("api/drugs/{id}/feedback")
    suspend fun submitFeedback(
        @Path("id") drugId: Long,
        @Field("feedbackType") feedbackType: String,
        @Field("correctDrugId") correctDrugId: Long? = null,
        @Field("description") description: String? = null
    ): Response<ApiResponse<Unit>>
}
