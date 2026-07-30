package com.weiguangplus.data.repository

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weiguangplus.data.model.ChatMessage
import com.weiguangplus.data.model.Conversation

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    suspend fun getMessages(convId: String): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(convId: String): ChatMessage?

    @Query("SELECT DISTINCT conversationId FROM chat_messages ORDER BY (SELECT MAX(timestamp) FROM chat_messages m2 WHERE m2.conversationId = chat_messages.conversationId) DESC")
    suspend fun getAllConversationIds(): List<String>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :convId AND isSent = 0")
    suspend fun getUnreadCount(convId: String): Int
}

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "weiguang_chat.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

class ChatRepository(context: Context) {
    private val dao = ChatDatabase.getInstance(context).chatMessageDao()

    // Predefined demo contacts for WeChat-like experience
    private val demoContacts = listOf(
        Conversation("family", "家人群聊", "👨‍👩‍👧", "女儿: 爸,今晚回来吃饭吗?", System.currentTimeMillis() - 300000, 3),
        Conversation("mom", "妈妈", "👩", "路上注意安全,我等你回来", System.currentTimeMillis() - 1200000, 1),
        Conversation("doctor_li", "李医生", "👨‍⚕️", "药记得按时吃,不舒服随时联系我", System.currentTimeMillis() - 3600000, 0),
        Conversation("friend_wang", "王姐(手语老师)", "👩‍🏫", "下次手语课改到周三下午了", System.currentTimeMillis() - 7200000, 0),
        Conversation("volunteer", "志愿者小陈", "🧑", "周六陪您去医院复查,我8点到", System.currentTimeMillis() - 86400000, 0),
        Conversation("community", "微光社区", "💡", "新功能上线:手语翻译支持更多手势", System.currentTimeMillis() - 172800000, 5),
        Conversation("neighbor", "邻居老张", "👴", "你家快递我帮你收了", System.currentTimeMillis() - 259200000, 0)
    )

    private val demoMessages = mapOf(
        "family" to listOf(
            ChatMessage(1, "family", "other_1", "女儿", "爸,吃药了吗?", System.currentTimeMillis() - 3600000, false),
            ChatMessage(2, "family", "me", "我", "吃了,放心。你吃饭了没?", System.currentTimeMillis() - 3500000, true),
            ChatMessage(3, "family", "other_2", "儿子", "爸,天气预报说今天会下雨,出门带伞", System.currentTimeMillis() - 1200000, false),
            ChatMessage(4, "family", "me", "我", "好的,谢谢提醒", System.currentTimeMillis() - 1100000, true),
            ChatMessage(5, "family", "other_1", "女儿", "爸,今晚回来吃饭吗?", System.currentTimeMillis() - 300000, false)
        ),
        "mom" to listOf(
            ChatMessage(6, "mom", "mom", "妈妈", "儿子,今天天气冷,多穿件衣服", System.currentTimeMillis() - 3600000, false),
            ChatMessage(7, "mom", "me", "我", "知道了妈,您也注意身体", System.currentTimeMillis() - 3500000, true),
            ChatMessage(8, "mom", "mom", "妈妈", "晚上我做了你最爱吃的红烧肉", System.currentTimeMillis() - 2000000, false),
            ChatMessage(9, "mom", "me", "我", "太好了!我今天早点回来", System.currentTimeMillis() - 1800000, true),
            ChatMessage(10, "mom", "mom", "妈妈", "路上注意安全,我等你回来", System.currentTimeMillis() - 1200000, false)
        ),
        "doctor_li" to listOf(
            ChatMessage(11, "doctor_li", "doctor", "李医生", "您好,上次开的降压药按时吃了吗?", System.currentTimeMillis() - 7200000, false),
            ChatMessage(12, "doctor_li", "me", "我", "每天都按时吃了,感觉血压稳定多了", System.currentTimeMillis() - 7000000, true),
            ChatMessage(13, "doctor_li", "doctor", "李医生", "很好,继续保持。下周复查时把药盒带来我看看", System.currentTimeMillis() - 4000000, false),
            ChatMessage(14, "doctor_li", "me", "我", "好的,谢谢李医生", System.currentTimeMillis() - 3800000, true),
            ChatMessage(15, "doctor_li", "doctor", "李医生", "药记得按时吃,不舒服随时联系我", System.currentTimeMillis() - 3600000, false)
        ),
        "friend_wang" to listOf(
            ChatMessage(16, "friend_wang", "wang", "王姐", "最近手语练得怎么样?", System.currentTimeMillis() - 86400000, false),
            ChatMessage(17, "friend_wang", "me", "我", "每天都在练习,感觉进步了不少", System.currentTimeMillis() - 85000000, true),
            ChatMessage(18, "friend_wang", "wang", "王姐", "太好了!下次手语课改到周三下午了", System.currentTimeMillis() - 7200000, false)
        ),
        "volunteer" to listOf(
            ChatMessage(19, "volunteer", "chen", "志愿者小陈", "您好,我是新分配的志愿者小陈", System.currentTimeMillis() - 172800000, false),
            ChatMessage(20, "volunteer", "me", "我", "你好小陈,谢谢你愿意帮助我", System.currentTimeMillis() - 170000000, true),
            ChatMessage(21, "volunteer", "chen", "志愿者小陈", "不客气!周六陪您去医院复查,我8点到", System.currentTimeMillis() - 86400000, false)
        ),
        "community" to listOf(
            ChatMessage(22, "community", "admin", "微光助手", "欢迎加入微光社区!这里可以和其他听障朋友交流", System.currentTimeMillis() - 604800000, false),
            ChatMessage(23, "community", "user_a", "听友小王", "有人知道怎么设置震动提醒的强度吗?", System.currentTimeMillis() - 259200000, false),
            ChatMessage(24, "community", "user_b", "听友老李", "在提醒设置里可以调整,有三个级别", System.currentTimeMillis() - 258000000, false),
            ChatMessage(25, "community", "admin", "微光助手", "新功能上线:手语翻译支持更多手势", System.currentTimeMillis() - 172800000, false)
        ),
        "neighbor" to listOf(
            ChatMessage(26, "neighbor", "zhang", "邻居老张", "小赵,你今天不在家吗?", System.currentTimeMillis() - 300000000, false),
            ChatMessage(27, "neighbor", "me", "我", "我在医院复查,下午回来", System.currentTimeMillis() - 298000000, true),
            ChatMessage(28, "neighbor", "zhang", "邻居老张", "你家快递我帮你收了", System.currentTimeMillis() - 259200000, false)
        )
    )

    suspend fun getConversations(): List<Conversation> {
        val ids = dao.getAllConversationIds()
        val existingIds = ids.toSet()
        val convs = demoContacts.map { contact ->
            val lastMsg = dao.getLastMessage(contact.id)
            if (existingIds.contains(contact.id)) {
                val unread = dao.getUnreadCount(contact.id)
                contact.copy(
                    lastMessage = lastMsg?.content ?: contact.lastMessage,
                    lastTime = lastMsg?.timestamp ?: contact.lastTime,
                    unreadCount = unread
                )
            } else {
                contact
            }
        }
        return convs.sortedByDescending { it.lastTime }
    }

    suspend fun initDemoData() {
        val existingIds = dao.getAllConversationIds().toSet()
        for ((convId, messages) in demoMessages) {
            if (convId !in existingIds) {
                messages.forEach { dao.insertMessage(it) }
            }
        }
    }

    suspend fun getMessages(conversationId: String): List<ChatMessage> {
        return dao.getMessages(conversationId)
    }

    suspend fun sendMessage(conversationId: String, content: String): ChatMessage {
        val msg = ChatMessage(
            conversationId = conversationId,
            senderId = "me",
            senderName = "我",
            content = content,
            timestamp = System.currentTimeMillis(),
            isSent = true
        )
        dao.insertMessage(msg)
        return msg
    }

    suspend fun getUnreadCount(conversationId: String): Int {
        return dao.getUnreadCount(conversationId)
    }
}
