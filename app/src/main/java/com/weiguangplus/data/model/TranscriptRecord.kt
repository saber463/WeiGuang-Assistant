package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 转录记录实体（G4）
 *
 * 保存一次语音识别（通话转写 / 悬浮窗字幕）的完整内容。
 * 独立存储于 TranscriptDatabase（weiguang_transcripts.db），不触碰主库。
 *
 * 字段说明（WHY）：
 *  - `type`：区分来源（call=通话转写 / caption=悬浮窗字幕），供 UI 打标签
 *  - `summary`：AI 摘要预留字段。本次未接真实大模型，可由调用方本地填充；
 *    后续在大模型 API 就绪后由 TranscriptRepository.generateSummary() 生成
 */
@Entity(tableName = "transcript_records")
data class TranscriptRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 转录来源类型：call / caption */
    val type: String,
    /** 转录全文 */
    val fullText: String,
    /** 摘要（AI 摘要或本地降级摘要） */
    val summary: String = "",
    /** 创建时间戳（epoch ms） */
    val timestamp: Long = System.currentTimeMillis()
)