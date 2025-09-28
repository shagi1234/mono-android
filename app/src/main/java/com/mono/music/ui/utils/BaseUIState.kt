package com.mono.music.ui.utils


data class BaseUIState<T>(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val isPending: Boolean = false,
    val isFailure: Boolean = false,
    val data: T? = null,
    val message: String? = null,
) {
    fun updateToLoading(): BaseUIState<T> {
        return copy(isLoading = true)
    }

    fun updateToPending(): BaseUIState<T> {
        return copy(isPending = true)
    }

    fun updateToDefault(): BaseUIState<T> {
        return copy(isPending = false, message = null)
    }

    fun updateMessage(message: String?): BaseUIState<T> {
        return copy(
            isPending = false, message = message
        )
    }

    fun updateToLoaded(data: T): BaseUIState<T> {
        return copy(isSuccess = true, isLoading = false, data = data, isFailure = false)
    }

    fun updateToFailure(): BaseUIState<T> {
        return copy(isLoading = false, isFailure = true)
    }
}

data class UIState(
    val success: Boolean = false,
    val loading: Boolean = false,
    val failure: Boolean = false,
    val errorMessage: String = "",
) {
    fun updateToLoading(): UIState {
        return copy(loading = true)
    }

    fun updateToLoaded(errorMessage: String = ""): UIState {
        return copy(success = true, loading = false, failure = false, errorMessage = errorMessage)
    }

    fun updateToFailure(errorMessage: String = ""): UIState {
        return copy(loading = false, failure = true, errorMessage = errorMessage)
    }

    fun updateToDefault(): UIState {
        return copy(
            success = false,
            loading = false,
            failure = false,
        )
    }
}