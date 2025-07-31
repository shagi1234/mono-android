package com.mono.music




import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreConstants.BIRTDAY_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FIRST_TIME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.NAME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PHONE_KEY
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
import com.mono.music.domain.repository.UserRepository
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaseUIState<Any>())
    val uiState: StateFlow<BaseUIState<Any>> = _uiState.asStateFlow()

    val validUntil = preferenceDataStoreHelper.getPreference(VALID_UNTIL_KEY, "")
    val isFirstTime = preferenceDataStoreHelper.getPreference(FIRST_TIME_KEY, "")

    var option = MutableStateFlow(Option())

    init {
        getUserData()
    }

    fun getPlayerController() = playerController

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return songRepository.getLocalPlaylists()
    }

    val token = preferenceDataStoreHelper.getPreference(LOGGED_IN_KEY, false)

    fun getLocale(): String {
        return  runBlocking { preferenceDataStoreHelper.getFirstPreference(
            PreferenceDataStoreConstants.LANGUAGE_KEY, "tk")}
    }

    fun addNewPlaylist(name:String){
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
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    fun getFreePlan() {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.getFreePlan()
                option.update {res}
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    fun subscribeToFreePlan( option: Option) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.subscribeToFreePlan()
                if (res.isSuccessful){
                    updateValidUntil(option)
                    _uiState.update { it.updateToDefault() }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<Message>() {}.type
                    var errorResponse: Message? = gson.fromJson(res.errorBody()!!.charStream(), type)
                    _uiState.update { it.updateMessage(errorResponse?.message?: "Error") }
                }
            } catch (e: Exception) {
                _uiState.update { it.updateMessage(e.message) }
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    fun updateValidUntil(option: Option ) {
        val currentDate = LocalDate.now()
        val newDate = currentDate.plusDays(option.days)
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, newDate.toString())
        }
    }

    suspend fun saveUserData(user: User){
        preferenceDataStoreHelper.putPreference(PHONE_KEY, "+993"+user.phone)
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

    fun checkIsValid(validUntil:String): Boolean {

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