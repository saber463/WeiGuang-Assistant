package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 言语康复训练课程（G3 数据模型，非持久化）
 *
 * 描述一条训练内容：一个音素 / 音节 / 词语 / 短句目标。
 * 训练时由 TTS 播放标准发音示范，用户跟读后交发音评估引擎比对评分。
 *
 * @param id 课程唯一标识（英文）
 * @param target 目标训练文本（该音素/词语/句子）
 * @param stage 训练阶段：单音素 → 音节 → 词语 → 短语（从小到大的递进）
 * @param category 内容分类（如 声母/韵母/日常用语），用于分组展示
 * @param difficulty 难度 1~3
 * @param mouthDesc 口型动作描述（引导用户理解正确发音动作，替代逐帧口型动画的轻量方案）
 * @param breathDesc 气息/舌位纠正要点（供评估未通过时展示给用户）
 */
data class RehabLesson(
    val id: String,
    val target: String,
    val stage: String,
    val category: String,
    val difficulty: Int,
    val mouthDesc: String,
    val breathDesc: String
)

/** 训练阶段的展示名与升序 */
enum class RehabStage(val label: String) {
    PHONE("单音素"),
    SYLLABLE("音节"),
    WORD("词语"),
    PHRASE("短语")
}

// ───────────────── 训练记录（Room 持久化）─────────────────

/**
 * 每日训练记录实体（G3）
 *
 * 记录一次跟读训练的结果，用于进度曲线与连续打卡 streak 统计。
 * 采用独立 RehabDatabase（见 RehabRepository），不触碰主库版本。
 */
@Entity(tableName = "rehab_records")
data class RehabRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 训练发生的日期（本地时区 yyyy-MM-dd），作 streak 与日曲线聚合键 */
    val date: String,
    /** 训练课程 id（RehabLesson.id） */
    val lessonId: String,
    /** 训练目标文本（冗余存储，便于历史展示） */
    val target: String,
    /** 发音评估得分 0~100 */
    val score: Int,
    /** 本次训练时长（毫秒，用于每日时长统计） */
    val durationMs: Long,
    /** 训练时间戳（毫秒） */
    val timestamp: Long = System.currentTimeMillis()
)