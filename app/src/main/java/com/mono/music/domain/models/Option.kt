package com.mono.music.domain.models

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Option(
    val id: Long = 0L,
    val name: String = "",
    val image: String = "",
    val days: Long = 0L,
    val price: Long = 0L,
){

}