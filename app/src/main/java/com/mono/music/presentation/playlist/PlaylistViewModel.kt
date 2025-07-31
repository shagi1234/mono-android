package com.mono.music.presentation.playlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.MainActivity.Companion.ALBUM
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.PlayerController
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _uiState = MutableStateFlow(BaseUIState<Playlist>())

    val uiState = _uiState

    fun getDownloadTracker() = downloadTracker

    fun getPlayerController() = playerController

    var type = savedStateHandle.getStateFlow<String>("type", "playlist")

    var id = savedStateHandle.getStateFlow<Long>("id", 0L)

    private val _relatedAlbums = MutableStateFlow<List<Playlist>>(emptyList())
    val relatedAlbums = _relatedAlbums.asStateFlow()

    private val _relatedPlaylist = MutableStateFlow<List<Playlist>>(emptyList())
    val relatedPlaylist = _relatedPlaylist.asStateFlow()

    lateinit var selectedSong: Song

    init {
        getPlaylist(id.value, type.value)
    }

    fun getPlaylist(id: Long, type: String) {
        viewModelScope.launch {
            _uiState.update { it.updateToLoading() }
            try {
                val data = songRepository.getPlaylist(id, type)

                if (type == ALBUM && data.artists!!.isNotEmpty()) {
                    val artistId = data.getArtist()?.id
                    if (artistId != null) {
                        // Get artist to access their albums
                        val artist = songRepository.getArtist(artistId)

                        // Filter out the current album
                        val relatedAlbums = artist.albums.filter { it.playlistId != data.playlistId }

                        // Store related albums in the view model for later use
                        _relatedAlbums.value = relatedAlbums
                    }
                } else if (type == PLAYLIST) {
                    val playlists = songRepository.getSimilarPlaylist(id)

                    _relatedPlaylist.value = playlists
                }

                _uiState.update { it.updateToLoaded(data) }
            } catch (e: Exception) {
                _uiState.update { it.updateToFailure() }
            }
        }


    }

    fun setPlaylistIdAndType(id: Long, type: String) {

        savedStateHandle["type"] = type
        savedStateHandle["id"] = id
    }

    fun savePlaylist(playlist: Playlist) {
        playlist.type = if (type.value == ALBUM) ALBUM else PLAYLIST
        playlist.isBuiltin = true
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = if (playlist.type == PLAYLIST) {
                    songRepository.playlistToLibrary(
                        PlaylistAction(
                            action = ACTION_ADD,
                            playlistId = playlist.playlistId
                        )
                    )
                } else {
                    songRepository.albumToLibrary(
                        PlaylistAction(
                            action = ACTION_ADD,
                            albumId = playlist.playlistId
                        )
                    )
                }
                withContext(Dispatchers.IO) {
                    songRepository.insertPlaylist(playlist = playlist)
                    if (playlist.songs == null) return@withContext

                    for (song in playlist.songs.reversed()) {
                        songRepository.insertSong(song)
                        val crossRef = PlaylistSongCrossRef(playlist.playlistId, song.songId)
                        songRepository.insertPlaylistSongCrossRef(crossRef)
                    }
                }
                _uiState.update { it.updateMessage(res.message) }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }

    }


    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getLocalPlaylists()
    }


    fun playlistExists(): Flow<Boolean> {
        return songRepository.playlistExists(id.value)
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

    fun isSongInAnyPlaylist(songId: Long?): Flow<Boolean> {
        return if (songId != null) {
            songRepository.isSongInAnyPlaylist(songId)
        } else {
            flowOf(false)
        }
    }


    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    fun toggleDownload(song: Song) {
        downloadTracker.download(song.toMediaItem())
    }
}
