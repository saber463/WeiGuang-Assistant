package com.weiguangplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.weiguangplus.data.model.Drug
import com.weiguangplus.data.model.User
import com.weiguangplus.data.model.RecognitionRecord
import com.weiguangplus.data.model.ChatMessage

@Database(
    entities = [User::class, Drug::class, RecognitionRecord::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
}
