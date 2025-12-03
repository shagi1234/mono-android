package com.mono.music.presentation.privacypolicy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.domain.models.PrivacyPolicy
import com.mono.music.domain.repository.UserRepository
import com.mono.music.ui.utils.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaseUIState<PrivacyPolicy>())
    val uiState: StateFlow<BaseUIState<PrivacyPolicy>> = _uiState

    init {
        getPrivacyPolicy()
    }

    fun getPrivacyPolicy() {
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                val privacyPolicy = userRepository.getPrivacyPolicy()
                _uiState.update { it.updateToLoaded(privacyPolicy) }
            } catch (e: Exception) {
                Log.e("PrivacyPolicyViewModel", "getPrivacyPolicy: ${e.message}")
                _uiState.update { it.updateToFailure() }
            }
        }
    }

    fun updateToDefault() {
        _uiState.update { BaseUIState() }
    }
}
