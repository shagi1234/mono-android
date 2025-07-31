package com.mono.music.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.MainActivity.Companion.ALL_SEARCH
import com.mono.music.PlayerController
import com.mono.music.di.DataStoreUtil
import com.mono.music.di.SearchDataStore
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.SearchData
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val SEARCH_DEBOUNCE: Long = 400

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker,
    private val dataStoreUtil: DataStoreUtil
) : ViewModel() {

    private val searchDataStore: SearchDataStore =
        SearchDataStore.getInstance(dataStoreUtil.dataStore)

    lateinit var selectedSong: Song

    private val _uiState = MutableStateFlow(BaseUIState<SearchData>())

    val uiState = _uiState

    private val searchQuery = MutableStateFlow(arrayOf("", ALL_SEARCH))

    val type = savedStateHandle.getStateFlow("type", ALL_SEARCH)
    val searchStr = savedStateHandle.getStateFlow("SEARCH", "")

    fun retryLastQuery() {
        searchQuery.value = arrayOf(searchStr.value, type.value)
    }

    init {
        combine(searchStr, type) { s, t ->
            arrayOf(s, t)
        }.onEach { searchQuery.value = it }
            .launchIn(viewModelScope)

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        searchQuery
            .onEach { _uiState.update { it.updateToLoading() } }
            .debounce(SEARCH_DEBOUNCE)
            .mapLatest { query ->
                _uiState.update { it.updateToLoading() }
                try {
                    val data = songRepository.search(query[0], query[1])
                    _uiState.update { it.updateToLoaded(data) }
                } catch (e: Exception) {
                    _uiState.update { it.updateToFailure() }
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    val keys = searchDataStore.searchFlow

    fun getPlayerController() = playerController
    fun getDownloadTracker() = downloadTracker

    fun saveSearchStr(s: String) {
        viewModelScope.launch {
            searchDataStore.saveNewMsg(s)
        }
    }

    fun clearSearchStr() {
        viewModelScope.launch {
            searchDataStore.clearAllMyMessages()
        }
    }

    fun setSearch(s: String) {
        savedStateHandle["SEARCH"] = s

    }

    fun setType(type: String) {
        savedStateHandle["type"] = type
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

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

}

