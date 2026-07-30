/**
 * 文件名：DatabaseModule.kt
 * 作者：微光同行前端团队
 * 功能描述：Hilt数据库模块，提供Room数据库实例和DAO访问对象
 * 创建日期：2026-05-29
 * 所属模块：di（依赖注入层）
 *
 * 核心职责：
 * 1. 配置Room DatabaseBuilder（数据库名称、版本、迁移策略）
 * 2. 提供AppDatabase单例实例（全局共享一个连接）
 * 3. 提供各Entity对应的DAO对象（UserDao、DrugDao等）
 */

package com.weiguangplus.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库层Hilt模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** 数据库文件名常量 */
    private const val DATABASE_NAME = "weiguangplus_database.db"

    /**
     * 提供Room Database实例
     *
     * @param context Application Context
     * @return AppDatabase单例对象
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): androidx.room.RoomDatabase {
        return Room.databaseBuilder(
            context,
            com.weiguangplus.data.local.AppDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()  // 开发模式：版本不匹配时重建
        .build()
    }
}
