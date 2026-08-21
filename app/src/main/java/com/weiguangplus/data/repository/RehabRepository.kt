package com.weiguangplus.data.repository

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weiguangplus.data.model.RehabRecord

/**
 * 言语康复训练记录 DAO（G3）
 *
 * 提供训练记录的插入、按日期汇总、每日时长、日期范围查询等，
 * 支撑「进度曲线 + 连续打卡 streak」统计。
 */
@Dao
interface RehabRecordDao {

    /** 插入一次训练记录（冲突忽略，通常不会冲突） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RehabRecord): Long

    /** 全部记录，按时间倒序 */
    @Query("SELECT * FROM rehab_records ORDER BY timestamp DESC")
    suspend fun getAll(): List<RehabRecord>

    /** 指定日期范围的记录，用于进度曲线 */
    @Query("SELECT * FROM rehab_records WHERE date >= :startDate ORDER BY timestamp ASC")
    suspend fun getSince(startDate: String): List<RehabRecord>

    /** 每日练习去重日期（用于连续打卡统计） */
    @Query("SELECT DISTINCT date FROM rehab_records")
    suspend fun getDistinctDates(): List<String>
}

/**
 * 言语康复训练记录独立数据库（G3）
 *
 * 独立于主 AppDatabase，不触碰主库版本，沿 ChatDatabase / QuickPhraseDatabase 先例。
 */
@Database(entities = [RehabRecord::class], version = 1, exportSchema = false)
abstract class RehabDatabase : RoomDatabase() {
    abstract fun rehabRecordDao(): RehabRecordDao

    companion object {
        @Volatile
        private var INSTANCE: RehabDatabase? = null

        fun getInstance(context: Context): RehabDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RehabDatabase::class.java,
                    "weiguang_rehab.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * 言语康复进度管理仓库（G3）
 *
 * 统一训练记录的写入与进度统计：
 * - 记录一次训练结果
 * - 查询训练历史与每日时长
 * - 计算连续打卡 streak（按本地日期，跨天连续 +1，中断归零）
 * - 汇总日均分（近 N 条或按日期）
 */
class RehabRepository(private val context: Context) {

    private val dao = RehabDatabase.getInstance(context).rehabRecordDao()

    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
    private val timeFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)

    /** 记录一次训练，返回记录主键 */
    suspend fun recordTraining(
        lessonId: String,
        target: String,
        score: Int,
        durationMs: Long
    ): Long {
        val record = RehabRecord(
            date = dateFormat.format(java.util.Date()),
            lessonId = lessonId,
            target = target,
            score = score.coerceIn(0, 100),
            durationMs = durationMs
        )
        return dao.insert(record)
    }

    /** 全部训练历史（时间倒序） */
    suspend fun getAllRecords(): List<RehabRecord> = dao.getAll()

    /** 近 N 天的后台标准化：返回最近 totalDays 天的进展数据（无记录日得分为 0，用于曲线连续） */
    suspend fun recentSeries(totalDays: Int = 7): List<DayStat> {
        val today = dateFormat.format(java.util.Date())
        val cal = java.util.Calendar.getInstance()
        val all = dao.getAll().associateBy { it.date } // 每天只取当日最后一次/聚合用
        return (totalDays - 1 downTo 0).map { offset ->
            cal.timeInMillis = System.currentTimeMillis() - offset * 86400000L
            val d = dateFormat.format(cal.time)
            val daily = all[d]?.let { r ->
                DayStat(
                    date = d,
                    score = r.score,
                    count = 1,
                    durationMs = r.durationMs
                )
            } ?: DayStat(date = d, score = 0, count = 0, durationMs = 0)
            daily
        }
    }

    /** 每日统计（用于进度曲线） */
    data class DayStat(
        val date: String,
        val score: Int,
        val count: Int,
        val durationMs: Long
    )

    /** 连续打卡天数：从今天往前数，出现记录的天数连续则叠加 */
    suspend fun currentStreak(): Int {
        val dates = dao.getDistinctDates().toHashSet()
        if (dates.isEmpty()) return 0
        var streak = 0
        val cal = java.util.Calendar.getInstance()
        for (i in 0..365) {
            cal.timeInMillis = System.currentTimeMillis() - i * 86400000L
            val d = dateFormat.format(cal.time)
            if (dates.contains(d)) {
                streak++
            } else {
                break // 不论是今天未练还是中途断签，都停止
            }
        }
        return streak
    }
}