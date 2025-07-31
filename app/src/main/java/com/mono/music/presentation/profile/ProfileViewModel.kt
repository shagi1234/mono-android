package com.mono.music.presentation.profile

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.R
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Nameable
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.UserRepository
import com.mono.music.presentation.otp.VerifyUIState
import com.mono.music.ui.components.SupportedLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {


    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState

    val name = savedStateHandle.getLiveData<String>(NAME, "")
    val birthday = savedStateHandle.getLiveData<String>(BIRTHDAY, "")
    val gender = savedStateHandle.getLiveData<String>(GENDER, Gender.MALE.value)
    val phone = preferenceDataStoreHelper.getPreference(PreferenceDataStoreConstants.PHONE_KEY, "")

    init {
        getLocalUserData()
        getUserData()
    }

    fun getUserData() {
        viewModelScope.launch {
            delay(1000)
            try {
                val res = userRepository.getProfile()
                saveUserData(res)
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    fun getLocalUserData() {

        viewModelScope.launch {
            _uiState.update { it.updateToLoading() }
            val name = runBlocking { preferenceDataStoreHelper.getFirstPreference(PreferenceDataStoreConstants.NAME_KEY, "")}
            val gender = runBlocking { preferenceDataStoreHelper.getFirstPreference(PreferenceDataStoreConstants.GENDER_KEY,"")}
            val birthday = runBlocking { preferenceDataStoreHelper.getFirstPreference(PreferenceDataStoreConstants.BIRTDAY_KEY, "")}
            setName(name)
            setBirthday(birthday)
            setGender( if (gender == "male" )Gender.MALE else Gender.FEMALE)
            _uiState.update { it.updateToLoaded() }

        }
    }

    suspend fun saveUserData(user: User){
        setName(user.name)
        setBirthday(user.birthday)
        setGender( if (user.gender == "male" )Gender.MALE else Gender.FEMALE)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.GENDER_KEY, user.gender)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.BIRTDAY_KEY, user.birthday)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.VALID_UNTIL_KEY, user.validUntil)


    }

    fun updateUserData(name: String, birthday:String, gender:String) {

        viewModelScope.launch {
            _uiState.update { it.updateToLoading() }
            try {
                val res = userRepository.profileUpdate(name, birthday, gender)
                Log.e("TAG", "updateUserData: "+res )
                _uiState.update { it.updateToSaved() }
                saveUserData(res)
            } catch (e: Exception) {
                Log.e("TAG", "updateUserData: "+e.message )
                _uiState.update { it.updateToFailure("") }
            }
        }
    }

    fun setName(name: String) {
        savedStateHandle[NAME] = name
    }
    fun setBirthday(day: String) {
        savedStateHandle[BIRTHDAY] = day
    }
    fun setGender(gender: Gender) {
        savedStateHandle[GENDER] = gender.value
    }

    fun loggedInTheUser() {
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(LOGGED_IN_KEY, true)
        }
    }

    companion object {
        const val NAME = "NAME"
        const val BIRTHDAY = "DAY"
        const val GENDER = "GENDER"
    }


}

sealed class Gender(val value: String, @StringRes name: Int): Nameable(name) {
    object MALE : Gender("male", R.string.male)
    object FEMALE: Gender("female", R.string.female)

}

val genders = listOf(
    Gender.MALE,
    Gender.FEMALE,
)
data class ProfileUIState(
    val success: Boolean = false,
    val loading: Boolean = false,
    val failure: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String = "",
) {
    fun updateToLoading(): ProfileUIState {
        return copy(loading = true)
    }

    fun updateToLoaded(errorMessage: String = ""): ProfileUIState {
        return copy(success = true, loading = false, failure = false, errorMessage = errorMessage)
    }

    fun updateToSaved(errorMessage: String = ""): ProfileUIState {
        return copy(success = true, loading = false, failure = false, errorMessage = errorMessage, saved = true)
    }

    fun updateToFailure(errorMessage: String = ""): ProfileUIState {
        return copy(loading = false, failure = true, errorMessage = errorMessage)
    }

    fun updateToDefault(): ProfileUIState {
        return copy(
            success = false,
            loading = false,
            failure = false,
        )
    }
}