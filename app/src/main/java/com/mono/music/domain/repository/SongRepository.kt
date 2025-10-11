package com.mono.music.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.mono.music.domain.dao.SongDao
import com.mono.music.domain.models.Artist
import com.mono.music.domain.models.MainScreenData
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Paging
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.PlaylistWithSongs
import com.mono.music.domain.models.RequestLikeSong
import com.mono.music.domain.models.ResponseSong
import com.mono.music.domain.models.SearchData
import com.mono.music.domain.models.Song
import com.mono.music.domain.models.SongWithPlaylists
import com.mono.music.domain.paging.AlbumsPagingSource
import com.mono.music.domain.paging.PlaylistPagingSource
import com.mono.music.domain.paging.SongPagingSource
import com.mono.music.domain.service.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.Date
import javax.inject.Inject

class SongRepository @Inject constructor(
    private val apiService: ApiService,
    private val songDao: SongDao,
) {


    suspend fun getMainScreen(): MainScreenData {
        return apiService.getMainScreenData()
    }


    suspend fun search(searchStr: String, type: String): SearchData {
        return apiService.search(searchStr, type)
    }

    suspend fun getArtist(id: Long): Artist {
        return apiService.getArtist(id)
    }


    suspend fun getPlaylist(id: Long, type: String): Playlist {
        return apiService.getPlaylist(type, id)
    }

    suspend fun getFavoriteSongs():Paging<ResponseSong>{
        return apiService.favoriteSongs()
    }

    suspend fun getLocalPlaylist(id: Long): Playlist {
        return apiService.getLocalPlaylist(id)
    }

    suspend fun getSimilarPlaylist(id: Long): List<Playlist> {
        return apiService.getSimilarPlaylist(id);
    }

    suspend fun getSimilarArtists(id: Long): List<Artist> {
        return apiService.getSimilarArtists(id);
    }

    suspend fun getSimilarAlbums(id: Long): List<Playlist> {
        return apiService.getSimilarAlbums(id);
    }

    suspend fun playlistToLibrary(
        body: PlaylistAction
    ): Message {
        return apiService.playlistToLibrary(body)
    }

    suspend fun albumToLibrary(
        body: PlaylistAction
    ): Message {
        return apiService.albumToLibrary(body)
    }

    suspend fun customPlaylistToLibrary(
        body: PlaylistAction
    ): Playlist {
        return apiService.customPlaylistToLibrary(body)
    }

    suspend fun songToPlaylist(
        body: PlaylistAction
    ): Message {
        return apiService.songToPlaylist(body)
    }

    fun getPlaylistWithSongs(id: Long): Flow<PlaylistWithSongs> {
        return songDao.getPlaylistWithSongs(id)
    }

    fun getAllPlaylists(type: String): Flow<MutableList<Playlist>> {
        return songDao.getAllPlaylists(type)
    }

    fun getAllDownloadedPlaylists(): Flow<List<Playlist>> {
        return songDao.getAllDownloadedPlaylists()
    }

    fun getLocalPlaylists(): Flow<MutableList<Playlist>> {
        return songDao.getLocalPlaylists()
    }

    fun getAlbums(artistId: Long) = Pager(
        PagingConfig(
            DEFAULT_PAGE_SIZE, prefetchDistance = 1
        )
    ) {
        AlbumsPagingSource(
            artistId = artistId, apiService = apiService
        )
    }.flow

    suspend fun listenedSong(
        id: Long
    ): Message {
        return apiService.listenedSong(id)
    }

    fun getSongs(
        artistId: Long, isTop: Int, isSingle: Int
    ) = Pager(
        PagingConfig(
            DEFAULT_PAGE_SIZE, prefetchDistance = 1
        )
    ) {
        SongPagingSource(
            artistId = artistId, isTop = isTop, isSingle = isSingle, apiService = apiService
        )
    }.flow

    suspend fun getMyPlaylists(page: Int): Paging<Playlist> {
        return apiService.getMyPlaylists(page)
    }

    suspend fun insertPlaylist(playlist: Playlist) {
        return songDao.insertPlaylist(playlist.apply { dateAdded = Date().time })
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        return songDao.updatePlaylist(playlist.playlistId, playlist.name)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        return songDao.deletePlaylist(playlist)
    }

    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long) {
        return songDao.deleteSongFromPlaylist(playlistId, songId)
    }

    suspend fun insertSong(song: Song) {
        return songDao.insertSong(song.apply { dateAdded = Date().time })
    }


    suspend fun insertPlaylistSongCrossRef(playlistSongCrossRef: PlaylistSongCrossRef) {
        return songDao.insertPlaylistSongCrossRef(playlistSongCrossRef)
    }


    suspend fun updateDownloadStatus(id: Long, downloadable: Boolean) {
        return songDao.updateDownloadStatus(id, downloadable)
    }

    suspend fun getPlaylistSongs(id: Long): PlaylistWithSongs {
        return songDao.getPlaylistSongs(id)

    }

    suspend fun getSongWithDownloadablePlaylists(id: Long): SongWithPlaylists {
        return songDao.getSongWithDownloadablePlaylists(id)

    }

    fun playlistExists(id: Long): Flow<Boolean> {
        return songDao.playlistExists(id)

    }

    suspend fun updateAllPlaylistsDownloadStatus(downloadable: Boolean) {
        // Update all downloadable playlists
        val downloadablePlaylists = getAllDownloadedPlaylists().first()
        downloadablePlaylists.forEach { playlist ->
            updateDownloadStatus(playlist.playlistId, downloadable)
        }
    }

    fun isSongInAnyPlaylist(songId: Long): Flow<Boolean> {
        return songDao.isSongInAnyPlaylist(songId)
    }

    fun songExists(id: Long): Flow<Boolean> {
        return songDao.songExists(id)
    }




    suspend fun likeSong(songId:Long, liked: Boolean):Message{
        val action = if (liked)   "unlike" else "like"
        val requestBody = RequestLikeSong(songId, action)
        return  apiService.likeSong(requestBody)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val ACTION_ADD = "add"
        const val ACTION_DELETE = "delete"
        const val ACTION_UPDATE = "update"
    }

}
