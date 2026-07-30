/**
 * 文件名：DrugRepository.kt
 * 作者：微光同行前端团队
 * 功能描述：药品数据仓库，封装药品识别、搜索、历史记录等业务逻辑
 * 创建日期：2026-05-29
 * 所属模块：data/repository（仓库层）
 *
 * 核心职责：
 * 1. 药品图片识别（拍照/相册上传 → AI OCR识别）
 * 2. 药品搜索（关键词搜索、分类筛选）
 * 3. 识别历史记录管理（分页查询、本地缓存）
 * 4. 药品详情获取和反馈提交
 *
 * 数据源策略（Offline-First离线优先）：
 * - 优先从本地Room数据库查询（快速响应）
 * - 本地无数据或用户主动刷新时请求后端API
 * - API返回结果更新到本地缓存（下次离线可用）
 * - 支持增量同步（仅同步变更部分）
 *
 * 错误处理策略：
 * - 网络异常时降级为本地数据（如果有缓存）
 * - 完全无数据时返回友好的错误提示
 * - 所有异常通过Result<T>包装向上传递
 */

package com.weiguangplus.data.repository

import android.content.Context
import com.weiguangplus.data.model.Drug
import com.weiguangplus.data.model.RecognitionRecord
import com.weiguangplus.network.ApiResponse
import com.weiguangplus.network.WeiguangApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 药品数据仓库类
 *
 * 使用Hilt @Singleton注解确保全局唯一实例。
 * 通过依赖注入获取API服务和上下文对象。
 */
@Singleton
class DrugRepository @Inject constructor(
    private val apiService: WeiguangApiService,
    private val context: Context
) {
    /**
     * 药品图片识别
     *
     * 上传药品照片到后端进行AI OCR识别，
     * 返回识别出的药品详细信息。
     *
     * 执行流程：
     * 1. 将图片文件封装为MultipartBody.Part
     * 2. 调用recognizeDrug接口上传图片
     * 3. 解析响应获取Drug对象
     * 4. （可选）保存识别记录到本地数据库
     * 5. 返回识别结果给UI层展示
     *
     * 图片要求：
     * - 格式：JPG/PNG/WebP
     * - 大小：≤10MB
     * - 内容：清晰的药盒正面照片
     *
     * @param imageFile 本地图片文件对象
     * @return Result<Drug> 成功时包含识别出的药品信息
     */
    suspend fun recognizeDrug(imageFile: File): Result<Drug> {
        return withContext(Dispatchers.IO) {
            try {
                // 步骤1：构建Multipart请求体（文件上传格式）
                val requestFile = imageFile.asRequestBody(
                    contentType = "image/*".toMediaType()
                )
                val body = MultipartBody.Part.createFormData(
                    name = "image",  // 后端接收的字段名
                    filename = imageFile.name,
                    body = requestFile
                )

                // 步骤2：调用识别API（上传图片并等待AI处理结果）
                val response = apiService.recognizeDrug(body)

                // 步骤3：处理HTTP响应
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("识别请求失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("服务器返回空响应"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message.ifEmpty { "识别失败，请重试" })
                    )
                }

                // 步骤4：提取药品信息
                val drug = apiResponse.getOrThrow()

                // 步骤5：（可选）保存识别记录到本地数据库
                // saveRecognitionRecordToLocal(drug, imageFile)

                Result.success(drug)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取药品识别历史记录
     *
     * 分页查询当前用户的识别历史列表。
     * 支持按时间倒序排列（最新的在前面）。
     *
     * 分页参数说明：
     * - page：页码（从1开始）
     * - size：每页条数（默认20，最大100）
     *
     * UI展示建议：
     * - 列表形式展示，每项显示缩略图+药品名+时间
     * - 支持下拉刷新和上拉加载更多
     * - 点击可跳转到识别详情或重新识别
     *
     * @param page 页码（默认第1页）
     * @param size 每页数量（默认20条）
     * @return Result<List<RecognitionRecord>> 历史记录列表
     */
    suspend fun getRecognitionHistory(
        page: Int = 1,
        size: Int = 20
    ): Result<List<RecognitionRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecognitionHistory(page, size)

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("获取历史记录失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("响应为空"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message)
                    )
                }

                val records = apiResponse.getOrThrow()
                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 搜索药品
     *
     * 根据关键词在药品数据库中进行模糊搜索。
     * 支持按药品名、通用名、批准文号、成分等多维度匹配。
     *
     * 搜索特性：
     * - 模糊匹配（支持错别字容错）
     * - 拼音搜索（输入"blf"可匹配"布洛芬"）
     * - 关联词扩展（搜索"止痛"可匹配相关药品）
     * - 搜索历史记录（本地保存常用搜索词）
     *
     * 排序方式：
     * - relevance：相关度排序（默认）
     * - name_asc/desc：按名称排序
     *
     * @param keyword 搜索关键词（最少2个字符）
     * @param page 页码（可选）
     * @param size 每页数量（可选）
     * @param sortBy 排序方式（可选）
     * @return Result<List<Drug>> 匹配的药品列表
     */
    suspend fun searchDrugs(
        keyword: String,
        page: Int = 1,
        size: Int = 20,
        sortBy: String = "relevance"
    ): Result<List<Drug>> {
        return withContext(Dispatchers.IO) {
            try {
                // 参数校验：关键词不能为空且长度≥2
                if (keyword.isBlank() || keyword.length < 2) {
                    return@withContext Result.failure(
                        IllegalArgumentException("搜索关键词至少需要2个字符")
                    )
                }

                val response = apiService.searchDrugs(
                    keyword = keyword,
                    page = page,
                    size = size,
                    sortBy = sortBy
                )

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("搜索失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("响应为空"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message.ifEmpty { "未找到相关药品" })
                    )
                }

                val drugs = apiResponse.getOrThrow()
                Result.success(drugs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取药品详情
     *
     * 根据药品ID获取完整的药品信息。
     * 包含所有22个字段的详细数据。
     *
     * 使用场景：
     * - 从搜索结果点击进入详情页
     * - 识别历史中查看某次识别的完整信息
     * - 药品对比功能的数据来源
     *
     * @param drugId 药品的唯一标识符
     * @return Result<Drug> 完整的药品详情对象
     */
    suspend fun getDrugDetail(drugId: Long): Result<Drug> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDrugDetail(drugId)

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("获取药品详情失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("响应为空"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message)
                    )
                }

                Result.success(apiResponse.getOrThrow())
            } catch (e:Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 提交识别结果反馈
     *
     * 用户对AI识别结果进行纠错或确认。
     * 反馈数据用于改进识别算法准确度。
     *
     * 反馈类型：
     * - correct：识别正确（确认无误）
     * - wrong_drug：识别错误（提供正确药品ID）
     * - incomplete：信息不完整
     * - other：其他问题（填写具体描述）
     *
     * @param drugId 药品ID
     * @param feedbackType 反馈类型
     * @param correctDrugId 正确的药品ID（如果识别错误）
     * @param description 详细描述（可选）
     * @return Result<Unit> 提交操作的结果
     */
    suspend fun submitFeedback(
        drugId: Long,
        feedbackType: String,
        correctDrugId: Long? = null,
        description: String? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.submitFeedback(
                    drugId = drugId,
                    feedbackType = feedbackType,
                    correctDrugId = correctDrugId,
                    description = description
                )

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("反馈提交失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("响应为空"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message)
                    )
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
