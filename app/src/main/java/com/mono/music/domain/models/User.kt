package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class User(
    @SerializedName("full_name")
    val name: String = "",
    val phone: String = "",
    @SerializedName("birth_day")
    val birthday: String = "",
    @SerializedName("subscription_end_date")
    val validUntil: String = "",
    val gender: String = "male",
    @SerializedName("logged_in_first_time")
    val firstTime: Boolean,
) {

}

