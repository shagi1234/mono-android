package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName


data class Paging<T>(
    val next: Int?,
    val previous: Int?,
    val total: Int?,
    val results: List<T>,
    @SerializedName("withdraw_text")
    val withdrawText: String?,

    )

data class ResponseSong(
    val song : Song
)