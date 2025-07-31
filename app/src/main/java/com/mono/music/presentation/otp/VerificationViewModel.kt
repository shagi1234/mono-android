package com.mono.music.presentation.otp

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.data.datastore.PreferenceDataStoreConstants.ACCESS_TOKEN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.BIRTDAY_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FIRST_TIME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.NAME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PHONE_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.REFRESH_TOKEN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.VALID_UNTIL_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Token
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.UserRepository
import com.mono.music.ui.utils.getFormattedCounTimeShort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUIState())
    val uiState: StateFlow<VerifyUIState> = _uiState

    var timer = createTimer()
    var timeLeftToResend by mutableStateOf("00:00")
    var resendAllowed by mutableStateOf(false)
    var retryCount by mutableStateOf(0)

    init {
        startTimer()
    }

    fun loginUser(phone: String) {
        incRetryCount()
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                userRepository.login(phone)
                startTimer()
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun verify(phone: String, code: String) {

        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val res = userRepository.verifyOTPAndProceed(phone, code)
                saveTokens(res)
                if (res.user?.firstTime == true) {
                    _uiState.update { it.updateToIsVerifiedToDetails() }
                }else{
                    res.user?.let { saveUserData(it) }
                    _uiState.update { it.updateToIsVerifiedToApp() }
                }
            } catch (e: Exception) {
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    suspend fun saveUserData(user: User){
        preferenceDataStoreHelper.putPreference(PHONE_KEY, "+993"+user.phone)
        preferenceDataStoreHelper.putPreference(NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(BIRTDAY_KEY, user.birthday)
        preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, user.validUntil)
        preferenceDataStoreHelper.putPreference(FIRST_TIME_KEY, user.firstTime.toString())
    }

    fun saveTokens(token: Token) {
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(REFRESH_TOKEN_KEY, token.refresh)
            preferenceDataStoreHelper.putPreference(ACCESS_TOKEN_KEY, token.access)
        }
    }

    fun shownTheError() {
        _uiState.value = VerifyUIState()
    }

    fun loggedInTheUser() {
        _uiState.value = VerifyUIState()
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(LOGGED_IN_KEY, true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }

    fun startTimer(){
        resendAllowed = false
        timer.start()
    }

    fun cancelTimer(){
        timer.cancel()
        resendAllowed = true
    }

    fun createTimer(): CountDownTimer {
        val time = Calendar.getInstance()
        time.add(Calendar.SECOND, TIME)
        val timeInMillis = time.timeInMillis
        val timer = object : CountDownTimer(timeInMillis - Calendar.getInstance().timeInMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeftToResend = getFormattedCounTimeShort(millisUntilFinished)
            }

            override fun onFinish() {
                resendAllowed = true
            }

        }
        return timer
    }


    private fun incRetryCount(){
        retryCount += 1
    }

    private fun resetRetryCount(){
        retryCount = 0
    }

    companion object {
        const val TIME = 60
        const val TIME_DEV = 10
    }
}

data class VerifyUIState(
    val isVerifiedToApp: Boolean = false,
    val isVerifiedToDetails: Boolean = false,
    val isVerifiedToTariffs: Boolean = false,
    val success: Boolean = false,
    val loading: Boolean = false,
    val failure: Boolean = false,
    val errorMessage: String = "",
) {
    fun updateToLoading(): VerifyUIState {
        return copy(loading = true)
    }

    fun updateToIsVerifiedToApp(): VerifyUIState {
        return copy(isVerifiedToApp = true)
    }
    fun updateToIsVerifiedToDetails(): VerifyUIState {
        return copy(isVerifiedToDetails = true)
    }

    fun updateToIsVerifiedToTariffs(): VerifyUIState {
        return copy(isVerifiedToTariffs = true)
    }

    fun updateToFailure(errorMessage: String = ""): VerifyUIState {
        return copy(loading = false, failure = true, errorMessage = errorMessage)
    }

    fun updateToDefault(): VerifyUIState {
        return copy(
            success = false,
            loading = false,
            failure = false,
        )
    }
}