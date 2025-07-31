package com.mono.music.presentation.login

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mono.music.domain.repository.UserRepository
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    var phone = savedStateHandle.getStateFlow("phone", "")


    fun loginUser(phone: String){
        _uiState.update { it.updateToLoading() }
        viewModelScope.launch {
            try {
                userRepository.login("+993$phone")
                _uiState.update { it.updateToLoaded() }
            }catch (e:Exception){
                Log.e("TAG", "loginUser: "+e.message )
                _uiState.update { it.updateToFailure(e.message.toString()) }
            }
        }
    }


    fun updateToDefault(){
        _uiState.update { it.updateToDefault() }
    }

    fun setPhone(s:String){
        savedStateHandle["phone"] = s

    }



}

