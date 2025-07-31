package com.mono.music.data.datastore.interceptor

import android.util.Log
import com.mono.music.data.datastore.PreferenceDataStoreConstants.ACCESS_TOKEN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreConstants.REFRESH_TOKEN_KEY
import com.mono.music.data.datastore.PreferenceDataStoreHelper
import com.mono.music.domain.models.Token
import com.mono.music.domain.service.TokenRefreshService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject


class SynchronizedAuthenticator @Inject constructor(
    private val tokenRefreshService: TokenRefreshService,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authenticatedReq = originalRequest.signedRequest()
        val initResponse = chain.proceed(authenticatedReq)
        when (initResponse.code){
            403, 401 -> {
                synchronized(this){
                    val refreshToken = refreshTokenWithCode()
                    return when {
                        refreshToken != null -> {
                            val newAuthReq = originalRequest
                                .newBuilder()
                                .addHeader("Authorization", "Bearer $refreshToken")
                                .build()
                            initResponse.close()
                            chain.proceed(newAuthReq)
                        }
                        else -> {
                            initResponse
                        }
                    }
                }
            }
            else -> return initResponse
        }
    }

    private fun Request.signedRequest(): Request {
        val accessToken = getAccessToken()
        return if(accessToken.isNotEmpty())
            newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        else
            newBuilder().build()
    }


    private fun refreshTokenWithCode() : String? {
        val refreshToken = getRefreshToken()
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("refresh", refreshToken)
                .build()
            val call = tokenRefreshService.refreshTokenSync(requestBody)
            val response = call.execute()
            return when {
                response.isSuccessful -> {
                    val tokens = response.body()
                    if (tokens != null) {
                        putPreference(tokens)
                    }
                    tokens?.access
                }

                response.code() in 401..403 -> {
                    clearAllPreference()
                    null
                }
                else -> { null }
            }
        } catch (e: Exception){
            Timber.e("refreshTokenWithCode: " + e.message)
            return null
        }
    }

    private fun getAccessToken(): String {
        return runBlocking { preferenceDataStoreHelper.getFirstPreference(ACCESS_TOKEN_KEY,"") }
    }

    private fun getRefreshToken(): String {
        return runBlocking { preferenceDataStoreHelper.getFirstPreference(REFRESH_TOKEN_KEY,"") }
    }

    private fun putPreference(token: Token) {
        CoroutineScope(Dispatchers.IO).launch {
            preferenceDataStoreHelper.putPreference(ACCESS_TOKEN_KEY, token.access)
            preferenceDataStoreHelper.putPreference(REFRESH_TOKEN_KEY, token.refresh)
        }
    }

    private fun clearAllPreference() {
        CoroutineScope(Dispatchers.IO).launch {
            preferenceDataStoreHelper.clearAllPreference()
        }
    }

}
