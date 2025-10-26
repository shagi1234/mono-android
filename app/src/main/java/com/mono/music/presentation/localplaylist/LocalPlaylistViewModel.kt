package com.mono.music.presentation.localplaylist

import android.app.Application
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.google.common.base.Preconditions
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.PlaylistWithSongs
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_DELETE
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject


@HiltViewModel
class LocalPlaylistViewModel @Inject constructor(
    private val application: Application,
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(BaseUIState<PlaylistWithSongs>())

    val uiState = _uiState

    lateinit var selectedSong: Song

    fun getPlayerController() = playerController

    fun getDownloadTracker() = downloadTracker

    // Добавьте StateFlow для плейлиста
    private val _currentPlaylist = MutableStateFlow<PlaylistWithSongs?>(null)
    val currentPlaylist: StateFlow<PlaylistWithSongs?> = _currentPlaylist.asStateFlow()


    // Добавьте функцию для загрузки плейлиста
    fun loadPlaylist(id: Long, isFavorites: Boolean) {
        viewModelScope.launch {
            getPlaylist(id, isFavorites).collect { playlist ->
                _currentPlaylist.value = playlist
            }
        }
    }

    // Функция для обновления плейлиста (вызывайте после добавления/удаления)
    fun refreshPlaylist(id: Long, isFavorites: Boolean) {
        loadPlaylist(id, isFavorites)
    }

    fun getPlaylist(id: Long, isFavorites: Boolean): Flow<PlaylistWithSongs?> {
        return if (isFavorites) {
            flow {
                _uiState.update { it.updateToLoading() }
                try {
                    val favorites = songRepository.getFavoriteSongs()

                    val favoriteSongs = favorites.results.map { favoriteResponse ->
                        favoriteResponse.song.copy(isLiked = true)
                    }
                    val playlistWithSongs =
                        PlaylistWithSongs(
                            playlist = Playlist(
                                playlistId = -1L,
                                name = application.getString(R.string.favorites),
                                songsCount = favorites.total ?: 0,
                                isFavorites = true
                            ),
                            songs = favoriteSongs
                        )

                    syncLikedSongsToFavorites(playlistWithSongs.songs)
                    emit(playlistWithSongs)
                    _uiState.update { it.updateToLoaded(playlistWithSongs) }
                } catch (e: Exception) {
                    val localFavoritesPlaylist = songRepository.getFavoritesPlaylistWithSongs().first()

                    val playlistWithSongs = localFavoritesPlaylist?.let { playlistWithSongs ->
                        PlaylistWithSongs(
                            playlist = playlistWithSongs.playlist.copy(
                                name = application.getString(R.string.favorites),
                                songsCount = playlistWithSongs.songs.size,
                                isFavorites = true
                            ),
                            songs = playlistWithSongs.songs.map { song ->
                                song.copy(isLiked = true)
                            }
                        )
                    }

                    emit(playlistWithSongs)
                    playlistWithSongs?.let {
                        _uiState.update { state -> state.updateToLoaded(it) }
                    }
                }
            }
        } else {
            flow {
                val localPlaylist = songRepository.getPlaylistWithSongs(id).first()
                _uiState.update { it.updateToLoaded(localPlaylist) }
                emit(localPlaylist)
            }
        }
    }

    private suspend fun getFavoriteSongsLocal(): PlaylistWithSongs? {

        val localFavoritesPlaylist = songRepository.getFavoritesPlaylist().first()

        val playlistWithSongs =
            localFavoritesPlaylist?.let { playlist ->
                PlaylistWithSongs(
                    playlist = Playlist(
                        playlistId = -1L,
                        name = application.getString(R.string.favorites),
                        songsCount = playlist.songsCount,
                    ),
                    songs = playlist.songs ?: emptyList()
                )
            }

        return playlistWithSongs

    }

    private fun syncLikedSongsToFavorites(songs: List<Song>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val likedSongs = songs.filter { it.isLiked == true }
                if (likedSongs.isEmpty()) return@launch

                val favoritesPlaylist = songRepository.getFavoritesPlaylist().first()
                val playlist = favoritesPlaylist ?: run {
                    // Create favorites playlist if it doesn't exist
                    val res = songRepository.customPlaylistToLibrary(
                        PlaylistAction(
                            action = ACTION_ADD,
                            name = "Favorites"
                        )
                    )
                    val newPlaylist = Playlist(
                        name = "Favorites",
                        playlistId = res.playlistId,
                        isFavorites = true,
                    )
                    songRepository.insertPlaylist(newPlaylist)
                    newPlaylist
                }

                // Add all liked songs to favorites
                likedSongs.forEach { song ->
                    addSongToPlaylist(song, playlist)
                }

            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }

//
//    @OptIn(UnstableApi::class)
//    fun refreshPlaylist(id: Long) {
//        _uiState.update { it.updateToLoading() }
//        viewModelScope.launch {
//            try {
//                val playlist = songRepository.getLocalPlaylist(id = id)
//                withContext(Dispatchers.IO) {
////                    songRepository.insertPlaylist(playlist)
//                    playlist.songs?.forEach { song ->
//                        songRepository.insertSong(song)
//                        val crossRef = PlaylistSongCrossRef(playlist.playlistId, song.songId)
//                        songRepository.insertPlaylistSongCrossRef(crossRef)
//                        if (playlist.downloadable) {
//                            downloadTracker.download(song.toMediaItem())
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                _uiState.update { it.updateMessage(e.message) }
//            }
//        }
//    }

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getLocalPlaylists()
    }

    fun addNewPlaylist(name: String) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = songRepository.customPlaylistToLibrary(
                    PlaylistAction(
                        action = ACTION_ADD,
                        name = name
                    )
                )
                withContext(Dispatchers.IO) {
                    songRepository.insertPlaylist(
                        Playlist(
                            name = name,
                            playlistId = res.playlistId,
                        )
                    )
                }
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }


    @OptIn(UnstableApi::class)
    fun addSongToPlaylist(song: Song, playlist: Playlist) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = songRepository.songToPlaylist(
                    PlaylistAction(
                        action = ACTION_ADD,
                        songsId = listOf(song.songId),
                        playlistId = playlist.playlistId
                    )
                )

                withContext(Dispatchers.IO) {
                    songRepository.insertSong(song)
                    val crossRef = PlaylistSongCrossRef(playlist.playlistId, song.songId)
                    songRepository.insertPlaylistSongCrossRef(crossRef)
                }

                if (playlist.downloadable) {
                    downloadTracker.download(song.toMediaItem())
                }
                _uiState.update { it.updateMessage(res.message) }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }


    @OptIn(UnstableApi::class)
    fun updateDownloadStatus(playlist: PlaylistWithSongs, downloadable: Boolean) {

        CoroutineScope(Dispatchers.IO).launch {
            songRepository.updateDownloadStatus(playlist.playlist.playlistId, downloadable)
        }

        if (downloadable) {
            playlist.songs.forEach { song ->
                downloadTracker.download(song.toMediaItem())
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                playlist.songs.forEach { song ->

                    val songPlaylists =
                        songRepository.getSongWithDownloadablePlaylists(song.songId).playlists
                    val downloadablePlaylistCount = songPlaylists.map { it.downloadable }.size

                    if (songPlaylists.size == 1 || downloadablePlaylistCount == 1) {
                        val uri =
                            Preconditions.checkNotNull(song.toMediaItem().localConfiguration).uri
                        val request = downloadTracker.getDownloadRequest(uri)
                        request?.let { it1 ->
                            downloadTracker.deleteDownloadRequest(it1)
                        }
                    }
                }
            }
        }
    }

    fun clearAllDownloadedSongs() {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                // Delete all downloaded files
                downloadTracker.clearAllDownloads()

                // Update all playlists to set downloadable to false
                withContext(Dispatchers.IO) {
                    songRepository.updateAllPlaylistsDownloadStatus(false)
                }

                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage("Error clearing downloads: ${e.message}") }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun deleteSongFromPlaylist(playlist: Playlist, song: Song) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = songRepository.songToPlaylist(
                    PlaylistAction(
                        action = ACTION_DELETE,
                        songsId = listOf(song.songId),
                        playlistId = playlist.playlistId
                    )
                )
                withContext(Dispatchers.IO) {
                    val songPlaylists =
                        songRepository.getSongWithDownloadablePlaylists(song.songId).playlists
                    val downloadablePlaylistCount = songPlaylists.map { it.downloadable }.size

                    if (songPlaylists.size == 1 || downloadablePlaylistCount == 1) {
                        val uri =
                            Preconditions.checkNotNull(song.toMediaItem().localConfiguration).uri
                        val request = downloadTracker.getDownloadRequest(uri)
                        request?.let { it1 ->
                            downloadTracker.deleteDownloadRequest(it1)
                        }
                    }

                    songRepository.deleteSongFromPlaylist(playlist.playlistId, song.songId)
                }
                _uiState.update { it.updateMessage(res.message) }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }


    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

}


