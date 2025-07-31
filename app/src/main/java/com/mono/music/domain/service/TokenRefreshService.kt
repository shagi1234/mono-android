package com.mono.music.domain.service

import com.mono.music.domain.models.Token
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface TokenRefreshService{

    @POST("api/token/refresh/")
    fun refreshTokenSync(
        @Body requestBody: RequestBody
    ): Call<Token>
}