/**
 * 文件名：ApiResponse.kt
 * 作者：微光同行前端团队
 * 功能描述：API通用响应封装类，用于统一处理后端返回的数据结构
 * 创建日期：2026-05-29
 * 所属模块：network（网络层）
 *
 * 设计说明：
 * 1. 使用泛型<T>支持不同类型的业务数据
 * 2. 采用sealed class实现类型安全的响应状态管理
 * 3. 符合前后端接口契约（API-CONTRACT.md）定义的统一响应格式
 * 4. 支持成功/失败/加载中三种状态，便于UI层状态展示
 */

package com.weiguangplus.network

import com.google.gson.annotations.SerializedName

/**
 * API统一响应数据结构
 *
 * 对应后端标准响应格式：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 *
 * @param T 业务数据类型（如User、Drug、List<Drug>等）
 * @property code 响应状态码（200=成功，400=参数错误，401=未授权，500=服务器错误）
 * @property message 响应消息（可用于Toast/Snackbar提示）
 * @property data 业务数据载荷（根据接口不同返回不同类型）
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int = -1,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: T? = null
) {
    /**
     * 判断请求是否成功
     *
     * @return true表示业务处理成功（code == 200）
     */
    val isSuccess: Boolean
        get() = code == 200

    /**
     * 判断是否为认证失败（Token过期或无效）
     *
     * @return true表示需要重新登录或刷新Token
     */
    val isAuthError: Boolean
        get() = code == 401 || code == 403

    /**
     * 获取业务数据或抛出异常
     *
     * 用于在Repository层安全地提取数据，
     * 当data为null时抛出IllegalStateException
     *
     * @return 非空的业务数据
     * @throws IllegalStateException 当data为null时抛出
     */
    fun getOrThrow(): T {
        return data ?: throw IllegalStateException(
            "API请求失败：$code - $message"
        )
    }

    companion object {
        /** 成功状态码常量 */
        const val CODE_SUCCESS = 200

        /** 参数错误状态码 */
        const val CODE_BAD_REQUEST = 400

        /** 未授权状态码（Token无效/过期） */
        const val CODE_UNAUTHORIZED = 401

        /** 服务器内部错误状态码 */
        const val CODE_SERVER_ERROR = 500

        /**
         * 创建成功响应的便捷方法
         *
         * @param data 业务数据
         * @param message 成功消息（默认"操作成功"）
         * @return 包装了数据的成功响应对象
         */
        fun <T> success(data: T, message: String = "操作成功"): ApiResponse<T> {
            return ApiResponse(
                code = CODE_SUCCESS,
                message = message,
                data = data
            )
        }

        /**
         * 创建错误响应的便捷方法
         *
         * @param code 错误码
         * @param message 错误描述
         * @return 包含错误信息的响应对象
         */
        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(
                code = code,
                message = message,
                data = null
            )
        }
    }
}

/**
 * API响应状态的密封类（Sealed Class）
 *
 * 用于在ViewModel和UI层进行模式匹配（when表达式），
 * 提供编译时类型安全的响应状态管理。
 *
 * 使用示例：
 * ```
 * when (val response = apiResponse) {
 *     is ApiResult.Success -> showData(response.data)
 *     is ApiResult.Error -> showError(response.message)
 *     is ApiResult.Loading -> showProgress()
 * }
 * ```
 */
sealed class ApiResult<out T> {

    /**
     * 请求成功状态
     *
     * @property data 从后端获取的业务数据
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * 请求失败状态
     *
     * @property message 用户友好的错误信息（可直接显示在UI上）
     * @property code 错误码（用于日志记录或特殊处理）
     * @property exception 原始异常对象（可选，用于调试）
     */
    data class Error(
        val message: String,
        val code: Int = -1,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()

    /**
     * 加载中状态（用于显示进度条等UI反馈）
     */
    object Loading : ApiResult<Nothing>()

    /**
     * 将[ApiResponse]转换为[ApiResult]
     *
     * 这是网络层到ViewModel层的数据转换桥梁，
     * 将HTTP响应映射为UI可消费的状态对象。
     *
     * @param response Retrofit返回的标准API响应
     * @return 对应的ApiResult状态对象
     */
    fun <T> fromResponse(response: ApiResponse<T>): ApiResult<T> {
        return if (response.isSuccess) {
            Success(response.data!!)
        } else {
            Error(
                message = response.message.ifEmpty { "未知错误" },
                code = response.code
            )
        }
    }
}
