package com.mono.music.domain.service

import com.mono.music.domain.models.ActiveSessionsResponse
import com.mono.music.domain.models.CodeVerification
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.Order
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.domain.models.PaymentStatus
import com.mono.music.domain.models.PrivacyPolicy
import com.mono.music.domain.models.Promo
import com.mono.music.domain.models.Token
import com.mono.music.domain.models.User
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface UserService {


    @POST("api/login/")
    suspend fun login(@Body requestBody: RequestBody)

    @POST("api/otp-verify/")
    suspend fun verifyOtp(@Body body: CodeVerification): Token

    @GET("api/profile")
    suspend fun getProfileInfo(): User

    @POST("api/profile-update")
    suspend fun profileUpdate(@Body requestBody: RequestBody): User

    @GET("api/subscriptions")
    suspend fun getOptions(): List<Option>

    @POST("api/check-promo-codes")
    suspend fun checkPromo(@Body promo: Promo)

    @GET("api/payment-register")
    suspend fun paymentRegister(
        @Query("subscription_id") id: Long,
        @Query("payment_type") paymentType: String,
    ): Order

    @GET("api/payment-status")
    suspend fun getPaymentStatus(
        @Query("order_id") orderId: Long,
    ): PaymentStatus

    @POST("api/contact-us")
    suspend fun contactUs(@Query("message") message: String)

    @GET("/api/subscribe-to-free-plan")
    suspend fun subscribeToFreePlan() : Response<Message>

    @GET("/api/free-plan")
    suspend fun getFreePlan():Option

    @GET("api/payment-methods/")
    suspend fun getPaymentMethod():List<PaymentMethod>

    @GET("api/privacy-policy/")
    suspend fun getPrivacyPolicy(): PrivacyPolicy

    @GET("api/sessions/active/")
    suspend fun getActiveSessions(): ActiveSessionsResponse

    @DELETE("api/sessions/{device_id}/")
    suspend fun deleteSession(@Path("device_id") deviceId: String): Response<Message>

}