package com.mono.music

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mono.music.data.datastore.PreferenceDataStoreConstants.ACCESS_TOKEN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.BIRTDAY_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FIRST_TIME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.NAME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PHONE_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PLAN_SELECTED_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.REGISTER_COMPLETED_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.VALID_UNTIL_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.PlaylistSongCrossRef
import com.mono.music.domain.models.Song
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.SongRepository
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_ADD
import com.mono.music.domain.repository.SongRepository.Companion.ACTION_DELETE
import com.mono.music.domain.repository.UserRepository
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalDate

@HiltViewModel
class MainViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val downloadTracker: DownloadTracker,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaseUIState<Any>())
    val uiState: StateFlow<BaseUIState<Any>> = _uiState.asStateFlow()


    val validUntil = preferenceDataStoreHelper.getPreference(VALID_UNTIL_KEY, "")
    val token = preferenceDataStoreHelper.getPreference(ACCESS_TOKEN_KEY, "")
    val planSelected = preferenceDataStoreHelper.getPreference(PLAN_SELECTED_KEY, false)
    val isFirstTime = preferenceDataStoreHelper.getPreference(FIRST_TIME_KEY, "")
    val isRegisterCompleted = preferenceDataStoreHelper.getPreference(REGISTER_COMPLETED_KEY, false)

    val isLoggedIn = preferenceDataStoreHelper.getPreference(LOGGED_IN_KEY, false)

    var option = MutableStateFlow(Option())


    init {
        getUserData()
    }

    fun getPlayerController() = playerController

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getLocalPlaylists()
    }

    private val _likeState = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val likeStates = _likeState.asStateFlow()


    fun likeSong(songId: Long, liked: Boolean) {
        viewModelScope.launch {
            try {
                val result = songRepository.likeSong(songId, liked)
                val isLiked = result.liked ?: false

                _likeState.value = _likeState.value.toMutableMap().apply {
                    put(songId, isLiked)
                }

                if (isLiked) {
                    playerController.selectedTrack?.let { song ->
                        addToFavorites(song.copy(isLiked = true))
                    }
                } else {
                    playerController.selectedTrack?.let { song ->
                        removeFromFavorites(song)
                    }
                }

            } catch (e: Exception) {
                _likeState.value = _likeState.value.toMutableMap().apply {
                    put(songId, false)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun removeFromFavorites(song: Song) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            val playlist = songRepository.getFavoritesPlaylist().first()!!
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


    fun addToFavorites(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favoritesPlaylist = songRepository.getFavoritesPlaylist().first()

                val hasFavoritesPlaylist =
                    favoritesPlaylist?.let { songRepository.playlistExists(it.playlistId).first() }

                if (hasFavoritesPlaylist == true) {
                    addSongToPlaylist(song, favoritesPlaylist)
                } else {
                    val res = songRepository.customPlaylistToLibrary(
                        PlaylistAction(
                            action = ACTION_ADD,
                            name = context.getString(R.string.favorites)
                        )
                    )

                    val newPlaylist = Playlist(
                        name = context.getString(R.string.favorites),
                        playlistId = res.playlistId,
                        isFavorites = true,
                    )

                    songRepository.insertPlaylist(newPlaylist)
                    addSongToPlaylist(song, newPlaylist)
                }

            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
            }
        }
    }

    fun addNewPlaylist(name: String) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = songRepository.customPlaylistToLibrary(
                    PlaylistAction(
                        action = ACTION_ADD,
                        name = name
                    )
                )
                songRepository.insertPlaylist(
                    Playlist(
                        name = name,
                        playlistId = res.playlistId,
                    )
                )
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

    fun getUserData() {
        viewModelScope.launch {
            try {
                val res = userRepository.getProfile()
                saveUserData(res)
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: " + e.message)
            }
        }
    }


    fun getFreePlan() {

        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.getFreePlan()
                option.update { res }
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: " + e.message)
            }
        }
    }

    fun subscribeToFreePlan(option: Option) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.subscribeToFreePlan()
                if (res.isSuccessful) {
                    updateValidUntil(option)
                    _uiState.update { it.updateToDefault() }
                } else {
                    val gson = Gson()
                    val type = object : TypeToken<Message>() {}.type
                    var errorResponse: Message? =
                        gson.fromJson(res.errorBody()!!.charStream(), type)
                    _uiState.update { it.updateMessage(errorResponse?.message ?: "Error") }
                }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
                Log.e("TAG", "getUserData: " + e.message)
            }
        }
    }

    fun updateValidUntil(option: Option) {
        val currentDate = LocalDate.now()
        val newDate = currentDate.plusDays(option.days)
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, newDate.toString())
        }
    }

    private suspend fun saveUserData(user: User) {
        preferenceDataStoreHelper.putPreference(PHONE_KEY, "+993" + user.phone)
        preferenceDataStoreHelper.putPreference(NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(BIRTDAY_KEY, user.birthday)
        preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, user.validUntil)
        preferenceDataStoreHelper.putPreference(FIRST_TIME_KEY, user.firstTime.toString())
        println("DEBG CHANGE")
    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    fun checkIsValid(validUntil: String): Boolean {

        if (validUntil == "") return false
        val currentDate = LocalDate.now()
        val futureDate = LocalDate.parse(validUntil)

        return if (currentDate < futureDate) {
            true
        } else if (currentDate > futureDate) {
            false
        } else {
            false
        }
    }

}