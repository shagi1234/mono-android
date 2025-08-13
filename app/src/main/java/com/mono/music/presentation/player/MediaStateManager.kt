package com.mono.music.presentation.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.mono.music.player.PlaybackService
import com.mono.music.player.PlayerStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

class MediaStateManager @Inject constructor(
    private val context: Context
) {
    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    // Основные потоки состояния
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem = _currentMediaItem.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerStates.STATE_IDLE)
    val playerState = _playerState.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                setupControllerListener()

                // Инициализируем текущие значения
                updateCurrentState()
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect MediaController")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupControllerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaItem.value = mediaItem
                _currentIndex.value = mediaController?.currentMediaItemIndex ?: -1

                Timber.d("Media item transition: ${mediaItem?.mediaId}, index: ${_currentIndex.value}")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _playerState.value = PlayerStates.STATE_PLAYING
                } else if (mediaController?.playbackState == Player.STATE_READY) {
                    _playerState.value = PlayerStates.STATE_PAUSE
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (mediaController?.playbackState == Player.STATE_READY) {
                    if (playWhenReady && mediaController?.isPlaying == true) {
                        _playerState.value = PlayerStates.STATE_PLAYING
                    } else if (!playWhenReady) {
                        _playerState.value = PlayerStates.STATE_PAUSE
                    }
                }
            }
        })
    }

    private fun updateCurrentState() {
        mediaController?.let { controller ->
            _currentMediaItem.value = controller.currentMediaItem
            _currentIndex.value = controller.currentMediaItemIndex
            _isPlaying.value = controller.isPlaying
        }
    }

    private fun updatePlayerState(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> _playerState.value = PlayerStates.STATE_IDLE
            Player.STATE_BUFFERING -> _playerState.value = PlayerStates.STATE_BUFFERING
            Player.STATE_READY -> {
                if (mediaController?.playWhenReady == true && mediaController?.isPlaying == true) {
                    _playerState.value = PlayerStates.STATE_PLAYING
                } else {
                    _playerState.value = PlayerStates.STATE_PAUSE
                }
            }
            Player.STATE_ENDED -> {
                _playerState.value = PlayerStates.STATE_PAUSE
            }
        }
    }

    // Методы управления
    fun playPause() {
        mediaController?.let { controller ->
            if (controller.playbackState == Player.STATE_IDLE) controller.prepare()
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun seekToNext() {
        mediaController?.seekToNext()
    }

    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun setMediaItems(items: List<MediaItem>) {
        mediaController?.setMediaItems(items)
        mediaController?.prepare()
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    fun setRepeatMode(mode: Int) {
        mediaController?.repeatMode = mode
    }

    // Геттеры для дополнительной информации
    val currentPosition: Long
        get() = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L

    val duration: Long
        get() = mediaController?.duration?.coerceAtLeast(0L) ?: 0L

    val bufferedPosition: Long
        get() = mediaController?.bufferedPosition?.coerceAtLeast(0L) ?: 0L

    fun release() {
        mediaController?.release()
    }
}