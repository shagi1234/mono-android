package com.mono.music.domain.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SearchFocusManager {
    private val _shouldFocusSearch = MutableStateFlow(false)
    val shouldFocusSearch = _shouldFocusSearch.asStateFlow()

    fun requestFocus() {
        _shouldFocusSearch.value = true
    }

    fun resetFocus() {
        _shouldFocusSearch.value = false
    }
}