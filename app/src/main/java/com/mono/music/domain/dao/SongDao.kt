package com.mono.music.domain.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.PlaylistWithSongs
import com.mono.music.domain.models.Song
import com.mono.music.domain.models.SongWithPlaylists
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


@Dao
interface SongDao {


    @Query(
        "SELECT Playlist.*," +
                "( SELECT  COUNT(*) FROM  PlaylistSongCrossRef " +
                "WHERE   playlistId = playlist.playlistId) AS songsCount  " +
                "FROM  playlist WHERE type LIKE '%' || :type || '%' "
    )
    fun getAllPlaylists(type: String): Flow<MutableList<Playlist>>



    @Query(
        "SELECT Playlist.*," +
                "( SELECT  COUNT(*) FROM  PlaylistSongCrossRef " +
                "WHERE   playlistId = playlist.playlistId) AS songsCount  " +
                "FROM  playlist WHERE downloadable=1  ORDER BY dateAdded DESC"
    )
    fun getAllDownloadedPlaylists(): Flow<MutableList<Playlist>>

    @Query(
        "SELECT Playlist.*," +
                "( SELECT  COUNT(*) FROM  PlaylistSongCrossRef " +
                "WHERE   playlistId = playlist.playlistId) AS songsCount  " +
                "FROM  playlist  WHERE isBuiltin=0 ORDER BY dateAdded DESC"
    )
    fun getLocalPlaylists(): Flow<MutableList<Playlist>>



//    @Query("SELECT Playlist.*," +
//            "( SELECT  COUNT(*) FROM  PlaylistSongCrossRef " +
//            "WHERE   playlistId = playlist.playlistId) AS songsCount  " +
//            "FROM  playlist WHERE type LIKE :type")
//    fun getAllPagingPlaylists(type:String): PagingSource<Int, Playlist>


    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId= :id ")
    fun getPlaylistWithSongs(id: Long): Flow<PlaylistWithSongs>

    @Upsert()
    fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlaylist(playlist: Playlist)


    @Query("UPDATE Playlist SET name = :name WHERE playlistId= :id")
    fun updatePlaylist(id: Long, name: String)


    @Upsert()
    fun insertPlaylistSongCrossRef(playlistSongCrossRef: PlaylistSongCrossRef)


    @Query("DELETE FROM playlist WHERE playlistId = :playlistId")
    fun deletePlaylistSongs(playlistId: Long)


    @Query("UPDATE Playlist SET downloadable = :downloadable WHERE playlistId =:id")
    fun updateDownloadStatus(id: Long, downloadable: Boolean)


    @Delete
    fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId= :playlistId AND songId = :songId")
    fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId= :id")
    fun getPlaylistSongs(id: Long): PlaylistWithSongs

    @Transaction
    @Query("SELECT * FROM Song WHERE songId= :id")
    fun getSongWithDownloadablePlaylists(id: Long): SongWithPlaylists

    @Query("SELECT EXISTS(SELECT * FROM Playlist WHERE playlistId = :id)")
    fun playlistExists(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM Song WHERE songId = :id)")
    fun songExists(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM PlaylistSongCrossRef WHERE songId = :songId)")
    fun isSongInAnyPlaylist(songId: Long): Flow<Boolean>
}
