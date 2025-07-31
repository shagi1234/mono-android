package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName


data class CodeVerification (
    val phone: String,
    val otp: String
)