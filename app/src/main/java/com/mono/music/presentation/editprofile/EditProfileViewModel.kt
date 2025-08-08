package com.mono.music.presentation.editprofile

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.R
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PLAN_SELECTED_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.REGISTER_COMPLETED_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Nameable
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.UserRepository
import com.mono.music.presentation.otp.VerifyUIState
import com.mono.music.presentation.profile.Gender
import com.mono.music.ui.components.SupportedLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {


    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState

    val name = savedStateHandle.getLiveData<String>(NAME, "")
    val birthday = savedStateHandle.getLiveData<String>(BIRTHDAY, "")

    private val _isTariffActive = MutableStateFlow(false)
    val isTariffActive: StateFlow<Boolean> = _isTariffActive
    init {
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(REGISTER_COMPLETED_KEY, false)
            preferenceDataStoreHelper.putPreference(PLAN_SELECTED_KEY, false)
        }
    }

    private suspend fun saveUserData(user: User) {
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(PreferenceDataStoreConstants.NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(
            PreferenceDataStoreConstants.GENDER_KEY,
            user.gender
        )
        preferenceDataStoreHelper.putPreference(
            PreferenceDataStoreConstants.BIRTDAY_KEY,
            user.birthday
        )
        preferenceDataStoreHelper.putPreference(
            PreferenceDataStoreConstants.VALID_UNTIL_KEY,
            user.validUntil
        )

        checkTariffStatus(user.validUntil)

        setName(user.name)
        setBirthday(user.birthday)
        setGender(if (user.gender == "male") Gender.MALE else Gender.FEMALE)
    }

    fun updateUserData(name: String, birthday: String, gender: String) {

        viewModelScope.launch {
            _uiState.update { it.updateToLoading() }
            try {
                val res = userRepository.profileUpdate(name, birthday, gender)
                _uiState.update { it.updateToSaved() }
                saveUserData(res)
                setIsRegistered()

            } catch (e: Exception) {
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

    private fun setGender(gender: Gender) {
        savedStateHandle[GENDER] = gender.value
    }

    private fun setIsRegistered() {
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(LOGGED_IN_KEY, true)
            preferenceDataStoreHelper.putPreference(REGISTER_COMPLETED_KEY, true)
        }
    }

    private fun checkTariffStatus(validUntil: String?) {
        viewModelScope.launch {
            val isActive = validUntil?.let { checkIsValid(it) } ?: false
            _isTariffActive.value = isActive

            preferenceDataStoreHelper.putPreference(PLAN_SELECTED_KEY, isActive)
        }
    }
    private fun checkIsValid(validUntil:String): Boolean {

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

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    companion object {
        const val NAME = "NAME"
        const val BIRTHDAY = "DAY"
        const val GENDER = "GENDER"
    }


}


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
        return copy(
            success = true,
            loading = false,
            failure = false,
            errorMessage = errorMessage,
            saved = true
        )
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