package com.mono.music.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface ExoPlayerFactory {
    fun createPlayer(): ExoPlayer
}

@Singleton
class ExoPlayerFactoryImpl @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpDataSourceFactory: CacheDataSource.Factory
) : ExoPlayerFactory {

    @OptIn(UnstableApi::class)
    override fun createPlayer(): ExoPlayer {
        val hlsMediaSource = HlsMediaSource.Factory(httpDataSourceFactory)
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(hlsMediaSource)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {
    @Binds
    abstract fun bindExoPlayerFactory(factory: ExoPlayerFactoryImpl): ExoPlayerFactory
}

