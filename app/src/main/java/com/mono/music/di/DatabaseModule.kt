package com.mono.music.di

import android.content.Context
import androidx.room.Room
import com.mono.music.database.AppDatabase
import com.mono.music.database.Converters
import com.mono.music.database.MIGRATION_1_2
import com.mono.music.database.MIGRATION_2_3
import com.mono.music.database.MIGRATION_3_4
import com.mono.music.database.MIGRATION_4_5
import com.mono.music.database.MIGRATION_5_6
import com.mono.music.database.MIGRATION_6_7
import com.mono.music.domain.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).addTypeConverter(Converters())
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .build()
    }

    @Provides
    fun provideSongDao(appDatabase: AppDatabase): SongDao {
        return appDatabase.songDao()
    }
}

const val DATABASE_NAME = "MonaDb"