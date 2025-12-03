package com.mono.music.domain.repository

import android.util.Log
import androidx.annotation.StringRes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mono.music.R
import com.mono.music.database.AppDatabase
import com.mono.music.domain.models.ActiveSessionsResponse
import com.mono.music.domain.models.CodeVerification
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Nameable
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.Order
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.domain.models.PaymentStatus
import com.mono.music.domain.models.PrivacyPolicy
import com.mono.music.domain.models.Promo
import com.mono.music.domain.models.Token
import com.mono.music.domain.models.User
import com.mono.music.domain.service.UserService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Query
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val userService: UserService,
    private val appDatabase: AppDatabase,

) {
    suspend fun login(number: String) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(PHONE, number)
            .build()
        return userService.login(requestBody)
    }

    suspend fun verifyOTPAndProceed(phone: String, otpCode: String): Token {

        val tokens = userService.verifyOtp(
            CodeVerification(
                phone = phone,
                otp = otpCode
            )
        )
        return tokens

    }

    suspend fun profileUpdate(
        name: String,
        birthday:String,
        gender:String,
    ): User {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(NAME, name)
            .addFormDataPart(GENDER, gender)
            .addFormDataPart(BIRTHDAY, birthday)
            .build()
        return userService.profileUpdate(requestBody)
    }


    suspend fun getProfile(): User {
        return userService.getProfileInfo()
    }

    suspend fun getOptions(): List<Option> {
        return userService.getOptions()
    }

    suspend fun checkPromoCode(
        code: String,
    ) {
        return userService.checkPromo(Promo(code))
    }
    suspend fun paymentRegister(
        id: Long,
        paymentType: String,
    ): Order {
         return userService.paymentRegister(id, paymentType)
    }

    suspend fun getPaymentStatus(
        orderId: Long,
    ): PaymentStatus {
        return userService.getPaymentStatus(orderId)
    }

    suspend fun contactUs(message: String){
        return userService.contactUs(message)
    }

    suspend fun subscribeToFreePlan(): Response<Message> {
        return userService.subscribeToFreePlan()
    }

    suspend fun getFreePlan(): Option {
        return userService.getFreePlan()
    }

    suspend fun getPaymentMethod(): List<PaymentMethod> {
        return userService.getPaymentMethod()
    }

    suspend fun getPrivacyPolicy(): PrivacyPolicy {
        return userService.getPrivacyPolicy()
    }

    suspend fun getActiveSessions(): ActiveSessionsResponse {
        return userService.getActiveSessions()
    }

    suspend fun deleteSession(deviceId: String): Response<Message> {
        return userService.deleteSession(deviceId)
    }

    suspend fun clearDatabase(){
        return appDatabase.clearAllTables()
    }

    companion object {
        const val PHONE = "phone"
        const val NAME = "full_name"
        const val BIRTHDAY = "birth_day"
        const val GENDER = "gender"
    }

}
