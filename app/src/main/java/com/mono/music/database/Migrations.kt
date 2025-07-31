package com.mono.music.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Playlist ADD COLUMN isLocal BOOLEAN NOT NULL DEFAULT false")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Playlist ADD COLUMN dateAdded INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Playlist ADD COLUMN isBuiltin INTEGER NOT NULL DEFAULT 0")
    }
}


val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create a new table with the updated primary key (autoGenerate = false)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `Playlist_new` (
                `playlistId` INTEGER NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `artists` TEXT NOT NULL,
                `songs` TEXT,
                `image` TEXT,
                `songsCount` INTEGER NOT NULL,
                `year` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `downloadable` INTEGER NOT NULL,
                `dateAdded` INTEGER NOT NULL,
                `isBuiltin` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Step 2: Copy the data from the old table to the new table
        database.execSQL("""
            INSERT INTO `Playlist_new` (
                `playlistId`, `name`, `artists`, `songs`, `image`, `songsCount`, `year`,
                `type`, `downloadable`, `dateAdded`, `isBuiltin`
            )
            SELECT 
                `playlistId`, `name`, `artists`, `songs`, `image`, `songsCount`, `year`,
                `type`, `downloadable`, `dateAdded`, `isBuiltin`
            FROM `Playlist`
        """.trimIndent())

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE `Playlist`")

        // Step 4: Rename the new table to the old table's name
        database.execSQL("ALTER TABLE `Playlist_new` RENAME TO `Playlist`")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create a new table with the updated primary key (autoGenerate = false)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `Playlist_new` (
                `playlistId` INTEGER NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `artists` TEXT,
                `songs` TEXT,
                `image` TEXT,
                `songsCount` INTEGER NOT NULL,
                `year` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `downloadable` INTEGER NOT NULL,
                `dateAdded` INTEGER NOT NULL,
                `isBuiltin` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Step 2: Copy the data from the old table to the new table
        database.execSQL("""
            INSERT INTO `Playlist_new` (
                `playlistId`, `name`, `artists`, `songs`, `image`, `songsCount`, `year`,
                `type`, `downloadable`, `dateAdded`, `isBuiltin`
            )
            SELECT 
                `playlistId`, `name`, `artists`, `songs`, `image`, `songsCount`, `year`,
                `type`, `downloadable`, `dateAdded`, `isBuiltin`
            FROM `Playlist`
        """.trimIndent())

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE `Playlist`")

        // Step 4: Rename the new table to the old table's name
        database.execSQL("ALTER TABLE `Playlist_new` RENAME TO `Playlist`")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Playlist ADD COLUMN isAlbum INTEGER NOT NULL DEFAULT 0")
    }
}