package com.mono.music.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mono.music.domain.dao.SongDao
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.Song


@Database(entities = [Playlist::class, Song::class, PlaylistSongCrossRef::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun songDao(): SongDao
}