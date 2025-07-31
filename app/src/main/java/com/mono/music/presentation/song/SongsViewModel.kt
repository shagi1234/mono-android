package com.mono.music.presentation.song

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@HiltViewModel
class SongsViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaseUIState<Any>())
    val uiState: StateFlow<BaseUIState<Any>> = _uiState.asStateFlow()

    val artistId = savedStateHandle.getStateFlow("artistId", 0L)
    val isTop = savedStateHandle.getStateFlow("isTop", 0)
    val isSingle = savedStateHandle.getStateFlow("isSingle", 0)

    lateinit var selectedSong: Song

    fun getDownloadTracker() = downloadTracker

    fun getPlayerController() = playerController


    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = combine(artistId, isTop, isSingle) { artistId, isTop, isSingle ->
        Triple(artistId, isTop, isSingle)
    }.flatMapLatest {
        songRepository.getSongs(artistId = it.first, isTop = it.second, it.third)
    }.cachedIn(viewModelScope)


    fun setArtistId(artistId: Long) {
        savedStateHandle["artistId"] = artistId
    }

    fun setIsTop(isTop: Int) {
        savedStateHandle["isTop"] = isTop
    }

    fun setIsSingle(isSingle: Int) {
        savedStateHandle["isSingle"] = isSingle
    }

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getLocalPlaylists()
    }

    fun addNewPlaylist(name:String){
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = songRepository.customPlaylistToLibrary(PlaylistAction(action = ACTION_ADD, name = name))
                withContext(Dispatchers.IO){
                    songRepository.insertPlaylist(Playlist(name = name, playlistId = res.playlistId))
                }
                _uiState.update { it.updateToDefault() }

            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }

    }


    fun addSongToPlaylist(song: Song, playlist: Playlist){
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

                if (playlist.downloadable){
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
