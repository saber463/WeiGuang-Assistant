package com.weiguangplus.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weiguangplus.data.local.QuickPhraseDao
import com.weiguangplus.data.model.QuickPhrase

/**
 * 快捷短语独立数据库（G12）
 *
 * 独立于主 AppDatabase，不触碰主库版本。沿 ChatDatabase / TranscriptDatabase 先例。
 */
@Database(entities = [QuickPhrase::class], version = 1, exportSchema = false)
abstract class QuickPhraseDatabase : RoomDatabase() {
    abstract fun quickPhraseDao(): QuickPhraseDao

    companion object {
        @Volatile
        private var INSTANCE: QuickPhraseDatabase? = null

        fun getInstance(context: Context): QuickPhraseDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuickPhraseDatabase::class.java,
                    "weiguang_quick_phrases.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * 快捷短语数据仓库（G12）
 *
 * 统一短语数据源：
 *  - 首次访问时用 [presets] 幂等初始化预设短语（@Insert IGNORE，重复不覆盖）
 *  - 提供全量查询 / 添加自定义 / 删除 / 按分类分组
 *  - 供聊天、通话等页面复用同一短语库
 */
class QuickPhraseRepository(context: Context) {

    private val dao = QuickPhraseDatabase.getInstance(context).quickPhraseDao()

    /**
     * 内置预设短语（WHY：覆盖听障用户高频沟通场景，就医/出行/求助等
     * 是日常生活中最常见、最迫切表达的诉求）
     */
    private val presets: List<QuickPhrase> = listOf(
        QuickPhrase(category = "就医", text = "请帮我叫医生", sortOrder = 0),
        QuickPhrase(category = "就医", text = "我要挂号", sortOrder = 1),
        QuickPhrase(category = "就医", text = "我身体不舒服", sortOrder = 2),
        QuickPhrase(category = "出行", text = "我要去哪个站坐车？", sortOrder = 0),
        QuickPhrase(category = "出行", text = "这张票怎么买？", sortOrder = 1),
        QuickPhrase(category = "求助", text = "请帮帮我", sortOrder = 0),
        QuickPhrase(category = "求助", text = "我需要帮助", sortOrder = 1),
        QuickPhrase(category = "日常", text = "请再说一遍", sortOrder = 0),
        QuickPhrase(category = "日常", text = "谢谢你的帮助", sortOrder = 1),
        QuickPhrase(category = "日常", text = "我听不见，请用文字交流", sortOrder = 2)
    )

    /** 确保预设短语已初始化（首次调用时插入） */
    suspend fun ensurePresets() {
        // 只有库为空时才初始化，避免每次重建；用 count 判断更直观
        if (dao.getAll().isEmpty()) {
            dao.insertAll(presets)
        }
    }

    /** 查询全部短语（已初始化预设） */
    suspend fun getAllPhrases(): List<QuickPhrase> {
        ensurePresets()
        return dao.getAll()
    }

    /** 按分类分组（LinkedHashMap 保证插入顺序） */
    suspend fun getGrouped(): Map<String, List<QuickPhrase>> {
        return getAllPhrases().groupBy { it.category }
    }

    /** 添加自定义短语（重复则忽略） */
    suspend fun addCustomPhrase(text: String, category: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        if (dao.findByText(trimmed) != null) return false // 已存在则不加
        dao.insert(
            QuickPhrase(
                category = category,
                text = trimmed,
                isCustom = true,
                sortOrder = getAllPhrases().count { it.category == category }
            )
        )
        return true
    }

    /** 删除短语 */
    suspend fun deletePhrase(id: Long) {
        dao.deleteById(id)
    }
}