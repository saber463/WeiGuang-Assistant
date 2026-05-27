package com.weiguangchangxing.weiguang_plus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.weiguangchangxing.weiguang_plus.data.local.dao.DrugDao
import com.weiguangchangxing.weiguang_plus.data.local.dao.DrugRuleDao
import com.weiguangchangxing.weiguang_plus.data.local.dao.UserProfileDao
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugAliasEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugDetailEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugMasterEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugRuleEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugSignMappingEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.UserProfileEntity

// 应用本地数据库入口：
// 这是运行时唯一的数据库结构权威，SQL 脚本、CSV 表头和构建脚本都必须跟随这里同步。
// version = 4 对应本轮正式修复后的基线：
// 1. drug_master 补齐 pinyin_key、initials_key、search_tokens 三类检索字段
// 2. drug_detail 补齐 composition、tts_summary、source_tag
// 3. drug_rule 拆分为 display_message 和 tts_message
@Database(
    entities = [
        DrugMasterEntity::class,
        DrugAliasEntity::class,
        DrugDetailEntity::class,
        DrugSignMappingEntity::class,
        DrugRuleEntity::class,
        UserProfileEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun drugRuleDao(): DrugRuleDao
    abstract fun userProfileDao(): UserProfileDao
}
