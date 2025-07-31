package com.mono.music.di

import com.google.gson.GsonBuilder
import com.mono.music.BuildConfig
import com.mono.music.data.datastore.interceptor.LanguageInterceptor
import com.mono.music.data.datastore.interceptor.SynchronizedAuthenticator
import com.mono.music.domain.service.ApiService
import com.mono.music.domain.service.TokenRefreshService
import com.mono.music.domain.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OtherInterceptorOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    companion object {
        const val BASE_URL = "https://mono.com.tm"
//        const val BASE_URL = "http://mono.com.tm:8000"
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): Interceptor {
        return if (BuildConfig.DEBUG) {
            Interceptor { chain ->
                val request = chain.request()
                val startTime = System.nanoTime()

                println("🌐 --> ${request.method} ${request.url}")
                request.headers.forEach { (name, value) ->
                    println("🌐 --> $name: $value")
                }

                val response = chain.proceed(request)
                val endTime = System.nanoTime()

                println("🌐 <-- ${response.code} ${response.message} ${request.url} (${(endTime - startTime) / 1e6}ms)")
                response.headers.forEach { (name, value) ->
                    println("🌐 <-- $name: $value")
                }

                response
            }
        } else {
            Interceptor { chain -> chain.proceed(chain.request()) }
        }
    }

    @AuthInterceptorOkHttpClient
    @Provides
    fun provideOkHttpClientWithAuth(
        synchronizedAuthenticator: SynchronizedAuthenticator,
        languageInterceptor: LanguageInterceptor,
        httpLoggingInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(languageInterceptor)
            .addInterceptor(synchronizedAuthenticator)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @OtherInterceptorOkHttpClient
    @Provides
    fun provideOkHttpClient(
        languageInterceptor: LanguageInterceptor,
        httpLoggingInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(languageInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    fun buildTokenRefreshApi(
        @OtherInterceptorOkHttpClient okHttpClient: OkHttpClient
    ): TokenRefreshService {
        val DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss"
        val gson = GsonBuilder().setDateFormat(DATE_FORMAT).create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TokenRefreshService::class.java)
    }

    @Provides
    fun provideRetrofitServiceBuilder(@AuthInterceptorOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        //date format for getting Date object from json
        val DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss"
        val gson = GsonBuilder().setDateFormat(DATE_FORMAT).create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }
}