package com.mono.music.presentation.tariffs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mono.music.data.datastore.PreferenceDataStoreConstants
import com.mono.music.data.datastore.PreferenceDataStoreConstants.BIRTDAY_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.FIRST_TIME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.NAME_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.PHONE_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.VALID_UNTIL_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.Order
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.domain.models.User
import com.mono.music.domain.repository.SettingsRepository
import com.mono.music.domain.repository.UserRepository
import com.mono.music.presentation.settings.SettingsUIState
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TariffsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TariffsUIState())
    val uiState: StateFlow<TariffsUIState> = _uiState

    var freePlan = MutableStateFlow<Option?>(null)
    val paymentMethods = MutableStateFlow(emptyList<PaymentMethod>())


    init {
        getOptions()
    }

     fun getOptions() {
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val res = userRepository.getOptions()
                val resMethods = userRepository.getPaymentMethod()
                paymentMethods.update {resMethods}
                _uiState.update { it.updateToLoaded(res) }
            } catch (e: Exception) {
                Log.e("TAG", "getOptions: " + e.message)
            }
        }
    }


    fun checkPromoCode(code: String) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                userRepository.checkPromoCode(code = code)
                getUserData()

                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreConstants.PROMO_SUCCESS_KEY, true
                )

                // Set plan as selected when promo code is successful
                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreConstants.PLAN_SELECTED_KEY, true
                )

                _uiState.update { it.updateSuccessCheckPromoCode("Succeed") }
            } catch (e: Exception) {
                Log.e("TAG", "getOptions: " + e.message)
                _uiState.update { it.updateFailCheckPromoCode(e.message.toString()) }
            }
        }
    }

    fun onPaymentSuccess(option: Option) {
        viewModelScope.launch {
            updateValidUntil(option)
            // Set plan as selected when payment is successful
            preferenceDataStoreHelper.putPreference(
                PreferenceDataStoreConstants.PLAN_SELECTED_KEY, true
            )
            _uiState.update { it.updateToSubscribedToPremium() }
        }
    }

    fun getFreePlan() {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.getFreePlan()
                freePlan.update {res}
                _uiState.update { it.updateToDefault() }
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    private fun getUserData() {
        viewModelScope.launch {
            try {
                val res = userRepository.getProfile()
                saveUserData(res)
            } catch (e: Exception) {
                Log.e("TAG", "getUserData: "+e.message )
            }
        }
    }

    private suspend fun saveUserData(user: User){
        preferenceDataStoreHelper.putPreference(PHONE_KEY, "+993"+user.phone)
        preferenceDataStoreHelper.putPreference(NAME_KEY, user.name)
        preferenceDataStoreHelper.putPreference(PHONE_KEY, user.phone)
        preferenceDataStoreHelper.putPreference(BIRTDAY_KEY, user.birthday)
        preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, user.validUntil)
        preferenceDataStoreHelper.putPreference(FIRST_TIME_KEY, user.firstTime.toString())

        // Check if the user has a valid tariff and update plan selection accordingly
        val isValidTariff = checkIsValid(user.validUntil ?: "")
        preferenceDataStoreHelper.putPreference(
            PreferenceDataStoreConstants.PLAN_SELECTED_KEY, isValidTariff
        )
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


    fun paymentRegister(optionId: Long, bank: String) {
        _uiState.update { it.updateToPending() }
        viewModelScope.launch {
            try {
                val res = userRepository.paymentRegister(optionId, bank)
                _uiState.update { it.updateToFormUrlLoaded(res) }
            } catch (e: Exception) {
                Log.e("TAG", "paymentRegister: " + e.message)
                _uiState.update { it.updateMessage(e.message.toString()) }
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

                    preferenceDataStoreHelper.putPreference(
                        PreferenceDataStoreConstants.PLAN_SELECTED_KEY, true
                    )
                    _uiState.update { it.updateToSubscribedToPremium() }
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

    private fun updateValidUntil(option: Option ) {
        val currentDate = LocalDate.now()
        val newDate = currentDate.plusDays(option.days)
        viewModelScope.launch {
            preferenceDataStoreHelper.putPreference(VALID_UNTIL_KEY, newDate.toString())
        }
    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }

    fun clearMessage() {
        _uiState.update { currentState ->
            currentState.copy(message = null)
        }
    }


}

data class TariffsUIState(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val isPending: Boolean = false,
    val isFailure: Boolean = false,
    val succeed: Boolean = false,
    val isSubscribedToPremium: Boolean = false,
    val message: String? = null,
    val order: Order? = null,
    val data: List<Option>? = null
) {

    fun updateSuccessCheckPromoCode(message: String?): TariffsUIState {
        return copy(
            isSuccess = true,
            isLoading = false,
            isFailure = false,
            succeed = true,
            message = message,
            isSubscribedToPremium = true,
        )
    }

    fun updateFailCheckPromoCode(message: String?): TariffsUIState {
        return copy(
            isPending = false,
            succeed = false,
            message = message,
        )
    }

    fun updateToPending(): TariffsUIState {
        return copy(isPending = true)
    }

    fun updateToSuccessful(): TariffsUIState {
        return copy(isSuccess = true, isLoading = false, isFailure = false)
    }

    fun updateToLoading(): TariffsUIState {
        return copy(isLoading = true)
    }

    fun updateToLoaded( data:List<Option>,): TariffsUIState {
        return copy(isSuccess = true,  data = data,isLoading = false, isFailure = false)
    }

    fun updateToFormUrlLoaded(order: Order): TariffsUIState {
        return copy(isSuccess = true, isLoading = false, isFailure = false, order = order)
    }

    fun updateToSubscribedToPremium(): TariffsUIState {
        return copy(isSuccess = true, isLoading = false, isFailure = false, isSubscribedToPremium = true)
    }

    fun updateToFailure(errorMessage: String = ""): TariffsUIState {
        return copy(isLoading = false, isFailure = true, message = errorMessage)
    }
    fun updateMessage(message: String?): TariffsUIState {
        return copy(
            isPending = false, message = message
        )
    }
    fun updateToDefault(): TariffsUIState {
        return copy(
            isPending = false,
            message = null,
            isSuccess = true,
            isLoading = false,
            isFailure = false,
            isSubscribedToPremium = false,
            order = null,
        )
    }
}