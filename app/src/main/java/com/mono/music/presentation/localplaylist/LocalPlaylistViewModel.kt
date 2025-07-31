package com.mono.music.presentation.localplaylist

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.google.common.base.Preconditions
import com.mono.music.PlayerController
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class LocalPlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker
) : ViewModel() {


    private val _uiState = MutableStateFlow(BaseUIState<PlaylistWithSongs>())

    val uiState = _uiState

    lateinit var selectedSong: Song

    init {

    }

    fun getPlayerController() = playerController

    fun getDownloadTracker() = downloadTracker

    fun getPlaylist(id: Long): Flow<PlaylistWithSongs> {
        return songRepository.getPlaylistWithSongs(id)
//            .map { playlistWithSongs ->
//            val sortedSongs = playlistWithSongs.songs.sortedByDescending { song -> song.dateAdded }
//            PlaylistWithSongs(playlistWithSongs.playlist, sortedSongs)
//        }
    }

    fun refreshPlaylist(id: Long) {
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val playlist = songRepository.getLocalPlaylist(id = id)
                withContext(Dispatchers.IO) {
//                    songRepository.insertPlaylist(playlist)
                    playlist.songs?.forEach { song ->
                        songRepository.insertSong(song)
                        val crossRef = PlaylistSongCrossRef(playlist.playlistId, song.songId)
                        songRepository.insertPlaylistSongCrossRef(crossRef)
                        if (playlist.downloadable) {
                            downloadTracker.download(song.toMediaItem())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "refreshPlaylist: " + e.message)
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }

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
