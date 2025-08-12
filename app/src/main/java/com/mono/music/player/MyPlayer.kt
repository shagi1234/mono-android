package com.mono.music.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.mono.music.player.PlayerStates.STATE_ERROR
import com.mono.music.player.PlayerStates.STATE_IDLE
import com.mono.music.player.PlayerStates.STATE_PAUSE
import com.mono.music.player.PlayerStates.STATE_PLAYING
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

class MyPlayer @Inject constructor(
    private val player: ExoPlayer,
    private val context: Context
) : Player.Listener, AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "MyPlayer"
    }

    private val focusLock = Any()
    var isBuffering = false
    var playbackDelayed = false
    var resumeOnFocusGain = false
    var playbackNowAuthorized = false

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MediaWakelock")

    val playerState = MutableStateFlow(STATE_IDLE)
    val hasPrev = MutableStateFlow(false)
    val hasNext = MutableStateFlow(false)
    val currentPlaybackPosition: Long get() = player.currentPosition.coerceAtLeast(0L)
    val currentBufferedPosition: Long get() = player.bufferedPosition.coerceAtLeast(0L)
    val currentTrackDuration: Long get() = player.duration.coerceAtLeast(0L)

    fun iniPlayer(trackList: MutableList<MediaItem>) {
        requestAudioFocus()

        // Make sure MyDeviceCallback is correctly implemented.
        audioManager.registerAudioDeviceCallback(MyDeviceCallback(player), handler)

        // Remove any existing listeners to avoid duplicates
        player.removeListener(this)
        player.addListener(this)

        // Set shuffle mode to false before setting media items to ensure consistent playback
        val wasShuffled = player.shuffleModeEnabled
        player.shuffleModeEnabled = false

        player.setMediaItems(trackList)
        player.prepare()

        // Restore shuffle state if it was enabled
        if (wasShuffled) {
            player.shuffleModeEnabled = true
        }

        if (!player.isPlaying) player.play()

        // Explicitly emit the state to ensure UI updates
        playerState.tryEmit(STATE_PLAYING)
    }

    fun addMediaItem(index: Int, track: MediaItem) {
        player.addMediaItem(index, track)
    }

    fun reOrder(from: Int, to: Int, track: MediaItem) {
        player.removeMediaItem(from)
        player.addMediaItem(to, track)
    }

    fun setRepeatMode(mode: Int) {
        player.repeatMode = mode
    }

    fun getRepeatMode(): Int = player.repeatMode
    fun getShuffleMode(): Boolean = player.shuffleModeEnabled

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun getPlayer(): ExoPlayer = player

    fun setUpTrack(index: Int, isTrackPlay: Boolean) {
        try {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()

            // Temporarily disable shuffle when directly selecting a track
            val wasShuffleEnabled = player.shuffleModeEnabled
            if (wasShuffleEnabled) {
                // Temporarily disable shuffle to ensure we play exactly the requested track
                player.shuffleModeEnabled = false
            }

            player.seekTo(index, 0)

            // Restore shuffle state if it was enabled
            if (wasShuffleEnabled) {
                player.shuffleModeEnabled = true
            }

            if (isTrackPlay) player.playWhenReady = true
        } catch (e: Exception) {
            Timber.e(e, "Error setting up track at index $index")
        }
    }

    fun playPause() {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.isPlaying) player.pause() else player.play()
    }

    fun play() {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.play()
    }

    fun pause() {
        if (player.isPlaying) player.pause()
    }

    fun releasePlayer() {
        player.release()
    }

    fun seekToPosition(position: Long) {
        player.seekTo(position)
    }

    fun emitPlaying() {
        playerState.tryEmit(STATE_PLAYING)
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "onPlayerError: ${error.message}")
        playerState.tryEmit(STATE_ERROR)
    }

//    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//        // Always emit the appropriate state, regardless of the current playback state
//        if (playWhenReady) {
//            playerState.tryEmit(STATE_PLAYING)
//        } else {
//            playerState.tryEmit(STATE_PAUSE)
//        }
//    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        // Immediately emit the state change
        playerState.tryEmit(PlayerStates.STATE_MEDIA_ITEM_TRANSITION)

        // If the player is set to play when ready and we've transitioned to a new item,
        // also emit the playing state to ensure UI updates
//        if (player.playWhenReady) {
//            handler.postDelayed({
//                playerState.tryEmit(STATE_PLAYING)
//            }, 50) // Small delay to ensure transition state is processed first
//        }
    }

//    override fun onPlaybackStateChanged(playbackState: Int) {
//        if (playbackState != Player.STATE_ENDED) return
//
//        player.seekTo(0, 0)
//
//        if (player.repeatMode == Player.REPEAT_MODE_ALL) {
//            playerState.tryEmit(STATE_PLAYING)
//            player.play()
//        } else {
//            Handler(Looper.getMainLooper()).postDelayed({
//                playerState.tryEmit(STATE_PAUSE)
//                player.pause()
//            }, 10)
//        }
//    }

    fun onPlayerPaused() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        hasNext.tryEmit(player.hasNextMediaItem())
        hasPrev.tryEmit(player.hasPreviousMediaItem())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> synchronized(focusLock) {
                if (playbackDelayed || resumeOnFocusGain) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                    play()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> synchronized(focusLock) {
                resumeOnFocusGain = false
                playbackDelayed = false
            }.also { pause() }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> synchronized(focusLock) {
                resumeOnFocusGain = player.isPlaying
                playbackDelayed = false
            }.also { pause() }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Optionally lower volume instead of pausing.
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this, handler)
                .build()

            val res = audioManager.requestAudioFocus(focusRequest)
            synchronized(focusLock) {
                playbackNowAuthorized = when (res) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio focus request failed", e)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                isBuffering = false
                playerState.tryEmit(STATE_IDLE)
            }
            Player.STATE_BUFFERING -> {
                isBuffering = true
                playerState.tryEmit(PlayerStates.STATE_BUFFERING)
            }
            Player.STATE_READY -> {
                isBuffering = false
                // Проверяем, что плеер реально играет
                if (player.playWhenReady && player.isPlaying) {
                    playerState.tryEmit(STATE_PLAYING)
                } else {
                    playerState.tryEmit(STATE_PAUSE)
                }
            }
            Player.STATE_ENDED -> {
                isBuffering = false
                player.seekTo(0, 0)

                if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                    playerState.tryEmit(STATE_PLAYING)
                    player.play()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        playerState.tryEmit(STATE_PAUSE)
                        player.pause()
                    }, 10)
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            isBuffering = false
            playerState.tryEmit(STATE_PLAYING)
        } else {
            // Только если не буферизуется
            if (!isBuffering && player.playbackState == Player.STATE_READY) {
                playerState.tryEmit(STATE_PAUSE)
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        // Обновляем состояние только если плеер готов
        if (player.playbackState == Player.STATE_READY) {
            if (playWhenReady && player.isPlaying) {
                playerState.tryEmit(STATE_PLAYING)
            } else if (!playWhenReady) {
                playerState.tryEmit(STATE_PAUSE)
            }
        }
    }
}
