package com.mono.music.presentation.home

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.offline.Download
import com.google.common.base.Preconditions
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.PlayerController
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.MainScreenData
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_DELETE
import com.mono.music.player.DownloadTracker
import com.mono.music.player.buildRenderersFactory
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
    private val downloadTracker: DownloadTracker,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {


    private val _uiState = MutableStateFlow(BaseUIState<MainScreenData>())
    val uiState: StateFlow<BaseUIState<MainScreenData>> = _uiState.asStateFlow()

    lateinit var selectedSong: Song

    init {
        viewModelScope.launch {
            getMainPageData()
            checkPromoSuccess()
        }
    }

    private suspend fun checkPromoSuccess() {
        // Make this function a suspend function
        try {
            val promoSuccess = preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreConstants.PROMO_SUCCESS_KEY, false
            ).first() // Read only once

            if (promoSuccess) {
                _uiState.update { currentState ->
                    currentState.copy(message = "promo_code")
                }

                // Wait for this operation to complete
                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreConstants.PROMO_SUCCESS_KEY, false
                ) // This ensures we wait for the write to complete
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error checking promo success: ${e.message}")
        }
    }



    fun getDownloadTracker() = downloadTracker


    fun getPlayerController() = playerController


    fun getMainPageData() {
        CoroutineScope(Dispatchers.IO).launch {
            _uiState.update { it.updateToLoading() }
            try {
                val data = songRepository.getMainScreen()
                _uiState.update { it.updateToLoaded(data) }
            } catch (e: Exception) {
                Log.e("TAG", "getMainPageData: "+e.message)
                _uiState.update { it.updateToFailure() }
            }
        }
    }


     fun toggleDownload(song: Song) {
        downloadTracker.download( song.toMediaItem())
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