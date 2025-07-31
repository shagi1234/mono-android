package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("order_id")
    val orderId: Long,
    @SerializedName("form_url")
    val formUrl: String,

) {
}
data class PaymentStatus(
    @SerializedName("order_id")
    val orderId: Long,
    val status: String,

) {
}
