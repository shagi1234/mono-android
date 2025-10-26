package com.mono.music.presentation.myplaylist

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.common.base.Preconditions
import com.mono.music.MainActivity.Companion.ALBUM
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FAVORITES_COUNT
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.MainScreenData
import com.mono.music.domain.models.Paging
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.PlaylistWithSongs
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_DELETE
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_UPDATE
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.collectEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences
import javax.inject.Inject


@HiltViewModel
class MyPlaylistsViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val downloadTracker: DownloadTracker,
    private val savedStateHandle: SavedStateHandle,
    preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {


    private val _uiState = MutableStateFlow(BaseUIState<List<Playlist>>())
    val uiState: StateFlow<BaseUIState<List<Playlist>>> = _uiState.asStateFlow()

    val type = savedStateHandle.getStateFlow("type", "")

    val favoritesPlaylist = songRepository.getFavoritesPlaylist()


    private var currentPage = 1

    init {
        loadMorePlaylists()

    }


    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getAllPlaylists("")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists = type
        .flatMapLatest { type ->
            if (type == LibrarySection.Downloads.value) {
                songRepository.getAllDownloadedPlaylists()
            } else {
                loadMorePlaylists()
                songRepository.getAllPlaylists(type)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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
                            playlistId = res.playlistId
                        )
                    )
                }
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = songRepository.customPlaylistToLibrary(
                    PlaylistAction(
                        action = ACTION_UPDATE,
                        name = playlist.name,
                        id = playlist.playlistId
                    )
                )
                withContext(Dispatchers.IO) {
                    songRepository.updatePlaylist(playlist)
                }
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }


    fun setType(type: String) {
        savedStateHandle["type"] = type
    }

    fun deletePlaylist(playlist: Playlist) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                if (playlist.isBuiltin) {
                    songRepository.playlistToLibrary(
                        PlaylistAction(
                            action = ACTION_DELETE,
                            playlistId = playlist.playlistId
                        )
                    )
                } else {
                    songRepository.customPlaylistToLibrary(
                        PlaylistAction(
                            action = ACTION_DELETE,
                            id = playlist.playlistId
                        )
                    )
                }
                withContext(Dispatchers.IO) {
                    deletePlaylistWithSongsFromLibrary(playlist)
                }
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }

    }

    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun deletePlaylistWithSongsFromLibrary(playlist: Playlist) {
        val songs = songRepository.getPlaylistSongs(playlist.playlistId).songs

        songs.forEach { song ->
            val songPlaylists =
                songRepository.getSongWithDownloadablePlaylists(song.songId).playlists
            val downloadablePlaylistCount = songPlaylists.map { it.downloadable }.size

            if (songPlaylists.size == 1 || downloadablePlaylistCount == 1) {
                val uri = Preconditions.checkNotNull(song.toMediaItem().localConfiguration).uri
                val request = downloadTracker.getDownloadRequest(uri)
                request?.let { it1 ->
                    downloadTracker.deleteDownloadRequest(it1)
                }
            }
        }
        songRepository.deletePlaylist(playlist)
    }


    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    fun loadMorePlaylists() {
        viewModelScope.launch {
            try {
                val data = songRepository.getMyPlaylists(currentPage)
                importPlaylists(data.results)
                if (data.next != null) {
                    currentPage++
                    loadMorePlaylists()
                }
            } catch (e: Exception) {
                Log.e("TAG", "loadMorePlaylists: " + e.message)

            }
        }
    }


    suspend fun importPlaylists(playlists: List<Playlist>) {
        withContext(Dispatchers.IO) {
            playlists.forEach { playlist ->
                songRepository.insertPlaylist(playlist = playlist.apply {
                    if (isAlbum) type = ALBUM else type = PLAYLIST
                })
                if (playlist.songs != null) {
                    for (song in playlist.songs.reversed()) {
                        songRepository.insertSong(song)
                        val crossRef = PlaylistSongCrossRef(playlist.playlistId, song.songId)
                        songRepository.insertPlaylistSongCrossRef(crossRef)
                    }
                }
            }
        }
    }
}