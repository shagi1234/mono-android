package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName

data class PrivacyPolicy(
    val content: String = "",
    @SerializedName("last_updated")
    val lastUpdated: String = ""
)
