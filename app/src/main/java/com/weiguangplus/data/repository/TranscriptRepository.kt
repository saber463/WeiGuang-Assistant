package com.weiguangplus.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weiguangplus.data.local.TranscriptDao
import com.weiguangplus.data.model.TranscriptRecord

/**
 * 转录历史独立数据库（G4）
 *
 * 独立于主 AppDatabase，不影响主库版本迁移。参照聊天独立库（ChatDatabase）先例。
 * 采用 fallbackToDestructiveMigration：开发阶段 schema 变更直接重建，不保留旧数据。
 */
@Database(entities = [TranscriptRecord::class], version = 1, exportSchema = false)
abstract class TranscriptDatabase : RoomDatabase() {
    abstract fun transcriptDao(): TranscriptDao

    companion object {
        @Volatile
        private var INSTANCE: TranscriptDatabase? = null

        fun getInstance(context: Context): TranscriptDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TranscriptDatabase::class.java,
                    "weiguang_transcripts.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * 转录记录数据仓库（G4）
 *
 * 封装 [TranscriptDatabase] 的增删查，并提供 AI 摘要预留接口。
 *
 * 关于 AI 摘要（设计决策，WHY）：
 * 项目当前无可用大模型 API Key 环境，未接入真实外部调用。
 * [generateSummary] 现为预留方法：若外部已传入 summary 则直接返回，
 * 否则本地降级为截取全文前部，保证 UI 始终有内容可展示。
 * 后续接入大模型时，仅需在此方法内补充 HTTP 调用（Retrofit），接口不变。
 */
class TranscriptRepository(context: Context) {

    private val dao = TranscriptDatabase.getInstance(context).transcriptDao()

    /**
     * 保存转录并生成摘要
     *
     * @param type 来源类型：call / caption
     * @param fullText 转录全文
     * @return 是否保存成功
     */
    suspend fun saveTranscript(
        type: String,
        fullText: String,
        externalSummary: String = ""
    ): Boolean {
        if (fullText.isBlank()) return false
        val record = TranscriptRecord(
            type = type,
            fullText = fullText,
            summary = generateSummary(fullText, externalSummary)
        )
        dao.insert(record)
        return true
    }

    /** 查询全部转录记录（倒序） */
    suspend fun getAllTranscripts(): List<TranscriptRecord> = dao.getAll()

    /** 按 id 查询单条 */
    suspend fun getTranscript(id: Long): TranscriptRecord? = dao.getById(id)

    /** 总条数 */
    suspend fun getCount(): Int = dao.count()

    /**
     * 生成摘要（预留接口，后续接大模型）
     *
     * @param fullText 转录全文
     * @param externalSummary 外部提供的摘要（如有真实 AI 生成则优先使用）
     */
    private fun generateSummary(fullText: String, externalSummary: String): String {
        if (externalSummary.isNotBlank()) return externalSummary
        // 本地降级：截取前 60 字作为摘要预览
        return if (fullText.length <= 60) fullText else fullText.substring(0, 60) + "…"
    }
}