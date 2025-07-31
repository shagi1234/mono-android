package com.mono.music.domain.models

data class PaymentMethod(
    val type: String,
    val title: String,
    val description: String
)