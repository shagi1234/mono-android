package com.mono.music.presentation.artist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.PlayerController
import com.mono.music.domain.models.Artist
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject




@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BaseUIState<Artist>())
    private val _similarArtists : MutableStateFlow<List<Artist>> = MutableStateFlow(emptyList())
    
    val similarArtists = _similarArtists
    val uiState = _uiState
    var id = savedStateHandle.getStateFlow("id", 0L)

    lateinit var selectedSong: Song

    init {
        getArtistDetail(id.value)
        getSimilarArtists(id.value)
    }

    private fun getSimilarArtists(id: Long) {
        viewModelScope.launch {
            try {
                val data = songRepository.getSimilarArtists(id)
                _similarArtists.update { data }
            } catch (e: Exception) {
                Log.e("TAG", "similar artists: " + e.message)
            }
        }
    }

    fun getPlayerController() = playerController
    fun getDownloadTracker() = downloadTracker


    fun getArtistDetail(id:Long) {

        viewModelScope.launch {
            _uiState.update { it.updateToLoading() }

            try {
                    val data = songRepository.getArtist(id)
                    _uiState.update { it.updateToLoaded(data) }
                } catch (e: Exception) {
                    Log.e("TAG", "getMainPageData: " + e.message)
                    _uiState.update { it.updateToFailure() }
                }
            }


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

    fun setID(id: Long) {
        savedStateHandle["id"] = id

    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    fun toggleSubscription() {
        val currentData = _uiState.value.data ?: return
        val currentStatus = currentData.isSubscribed ?: false
        val newStatus = !currentStatus
        val action = if (newStatus) SongRepository.ACTION_ADD else SongRepository.ACTION_DELETE

        // Optimistic Update
        _uiState.update {
            it.updateToLoaded(currentData.copy(isSubscribed = newStatus))
        }

        viewModelScope.launch {
            try {
                val res = songRepository.postArtistToLibrary(currentData.id, action)
                // Optional: Update with server response if needed, but optimistic is sufficient for now
                // Just update message if needed or silent success
            } catch (e: Exception) {
                // Revert on failure
                _uiState.update {
                    it.updateToLoaded(currentData.copy(isSubscribed = currentStatus))
                }
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }

}