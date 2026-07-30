package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = true
)

data class Conversation(
    val id: String,
    val contactName: String,
    val contactAvatar: String = "",
    val lastMessage: String = "",
    val lastTime: Long = 0,
    val unreadCount: Int = 0
)
