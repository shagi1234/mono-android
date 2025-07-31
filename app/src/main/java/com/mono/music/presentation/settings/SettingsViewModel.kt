package com.mono.music.presentation.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.R
import com.mono.music.data.datastore.PreferenceDataStoreConstants.BIRTDAY_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FIRST_TIME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.LOGGED_IN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.NAME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PHONE_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.VALID_UNTIL_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.Order
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.domain.models.SearchData
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.SettingsRepository
import com.mono.music.domain.repository.UserRepository
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.LocaleHelper
import com.mono.music.ui.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> = _uiState

    var currentLanguage = settingsRepository.locale

    val validUntil = preferenceDataStoreHelper.getPreference(VALID_UNTIL_KEY, "")
    val name = preferenceDataStoreHelper.getPreference(NAME_KEY, "")
    val options = MutableStateFlow(emptyList<Option>())
    val paymentMethods = MutableStateFlow(emptyList<PaymentMethod>())

    init {
        getUserData()
        getOptions()
    }

    private fun getOptions(){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val resOptions = userRepository.getOptions()
                val resMethods = userRepository.getPaymentMethod()
                options.update {resOptions}
                paymentMethods.update {resMethods}
                _uiState.update { it.updateToLoaded() }
            }catch (e:Exception){
                Log.e("TAG", "getOptions: "+e.message )
            }
        }
    }


    private fun getPaymentMethod(){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val res = userRepository.getPaymentMethod()
                paymentMethods.update {res}
                _uiState.update { it.updateToLoaded() }
            }catch (e:Exception){
                Log.e("TAG", "getOptions: "+e.message )
            }
        }
    }


    fun checkPromoCode(code:String){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
               userRepository.checkPromoCode(code = code)
                getUserData()
                _uiState.update { it.updateToLoaded("Promo code applied successfully!") }

            }catch (e:Exception){
                Log.e("TAG", "getOptions: "+e.message )
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun paymentRegister(optionId:Long, bank:String){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val res = userRepository.paymentRegister(optionId, bank)
                _uiState.update { it.updateToFormUrlLoaded(res) }
            }catch (e:Exception){
                Log.e("TAG", "paymentRegister: "+e.message )
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }


    fun contactUs(text:String){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                userRepository.contactUs(message = text)
                _uiState.update { it.updateToLoaded() }
            }catch (e:Exception){
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
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

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                preferenceDataStoreHelper.putPreference(LOGGED_IN_KEY, false)
                preferenceDataStoreHelper.clearAllPreference()
                userRepository.clearDatabase()
            }
        }
    }


    suspend fun saveUserData(user: User){
        preferenceDataStoreHelper.putPreference(PHONE_KEY, "+993"+user.phone)
        preferenceDataStoreHelper.putPreference(NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(BIRTDAY_KEY, user.birthday)
        preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, user.validUntil)
        preferenceDataStoreHelper.putPreference(FIRST_TIME_KEY, user.firstTime.toString())
    }
}

data class SettingsUIState(
    val success: Boolean = false,
    val loading: Boolean = false,
    val failure: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val order: Order? = null
) {
    fun updateToLoading(): SettingsUIState {
        return copy(loading = true)
    }

    fun updateToFormUrlLoaded(order: Order): SettingsUIState {
        return copy(success = true, loading = false, failure = false, order = order)
    }

    fun updateToFailure(errorMessage: String = ""): SettingsUIState {
        return copy(loading = false, failure = true, errorMessage = errorMessage)
    }

    fun updateToLoaded(successMessage: String = ""): SettingsUIState {
        return copy(success = true, loading = false, failure = false, successMessage = successMessage)
    }

    // Add this to the updateToDefault function
    fun updateToDefault(): SettingsUIState {
        return copy(
            success = false,
            loading = false,
            failure = false,
            order = null,
            errorMessage = "",
            successMessage = ""
        )
    }
}