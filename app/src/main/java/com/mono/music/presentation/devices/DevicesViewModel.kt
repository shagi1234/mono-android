package com.mono.music.presentation.devices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.domain.models.ActiveSessionsResponse
import com.mono.music.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUIState())
    val uiState: StateFlow<DevicesUIState> = _uiState

    init {
        getActiveSessions()
    }

    fun getActiveSessions() {
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val response = userRepository.getActiveSessions()
                _uiState.update { it.updateToLoaded(response) }
            } catch (e: Exception) {
                Log.e("DevicesViewModel", "getActiveSessions: ${e.message}")
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun deleteSession(deviceId: String) {
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val response = userRepository.deleteSession(deviceId)
                if (response.isSuccessful) {
                    getActiveSessions() // Refresh the list
                    _uiState.update { it.updateToSuccess("Session removed successfully") }
                } else {
                    _uiState.update { it.updateToFailure("Failed to remove session") }
                }
            } catch (e: Exception) {
                Log.e("DevicesViewModel", "deleteSession: ${e.message}")
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }

    fun updateToDefault() {
        _uiState.update { it.updateToDefault() }
    }
}

data class DevicesUIState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val failure: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val sessionsData: ActiveSessionsResponse? = null
) {
    fun updateToLoading(): DevicesUIState {
        return copy(loading = true, success = false, failure = false)
    }

    fun updateToLoaded(data: ActiveSessionsResponse): DevicesUIState {
        return copy(
            loading = false,
            success = false,
            failure = false,
            sessionsData = data,
            errorMessage = "",
            successMessage = ""
        )
    }

    fun updateToSuccess(message: String): DevicesUIState {
        return copy(
            loading = false,
            success = true,
            failure = false,
            successMessage = message
        )
    }

    fun updateToFailure(errorMessage: String): DevicesUIState {
        return copy(
            loading = false,
            success = false,
            failure = true,
            errorMessage = errorMessage
        )
    }

    fun updateToDefault(): DevicesUIState {
        return copy(
            loading = false,
            success = false,
            failure = false,
            errorMessage = "",
            successMessage = ""
        )
    }
}
