package com.mono.music.player

import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.mono.music.player.PlayerStates.STATE_ERROR
import com.mono.music.player.PlayerStates.STATE_IDLE
import com.mono.music.player.PlayerStates.STATE_PAUSE
import com.mono.music.player.PlayerStates.STATE_PLAYING
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

class MyPlayer @Inject constructor(
    player: ExoPlayer,
    private val context: Context
) : Player.Listener, AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "MyPlayer"
    }

    private lateinit var mediaController: MediaController
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val focusLock = Any()
    private var isBuffering = false
    private var playbackDelayed = false
    private var resumeOnFocusGain = false
    private var playbackNowAuthorized = false

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MediaWakelock")

    val playerState = MutableStateFlow(STATE_IDLE)
    val hasPrev = MutableStateFlow(false)
    val hasNext = MutableStateFlow(false)

    val currentPlaybackPosition: Long
        get() = if (::mediaController.isInitialized) mediaController.currentPosition.coerceAtLeast(
            0L
        ) else 0L
    val currentBufferedPosition: Long
        get() = if (::mediaController.isInitialized) mediaController.bufferedPosition.coerceAtLeast(
            0L
        ) else 0L
    val currentTrackDuration: Long
        get() = if (::mediaController.isInitialized) mediaController.duration.coerceAtLeast(
            0L
        ) else 0L

    private var onMediaItemTransitionCallback: ((Int) -> Unit)? = null

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                Timber.d("MediaController connected and listener added")
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect MediaController")
            }
        }, ContextCompat.getMainExecutor(context))
    }


    fun iniPlayer(trackList: MutableList<MediaItem>) {
        requestAudioFocus()

        // Make sure MyDeviceCallback is correctly implemented.
        audioManager.registerAudioDeviceCallback(MyDeviceCallback(mediaController), handler)

        // Remove any existing listeners to avoid duplicates
        mediaController.removeListener(this)
        mediaController.addListener(this)

        // Set shuffle mode to false before setting media items to ensure consistent playback
        val wasShuffled = mediaController.shuffleModeEnabled
        mediaController.shuffleModeEnabled = false

        mediaController.setMediaItems(trackList)
        mediaController.prepare()

        // Restore shuffle state if it was enabled
        if (wasShuffled) {
            mediaController.shuffleModeEnabled = true
        }

        if (!mediaController.isPlaying) mediaController.play()

        // Explicitly emit the state to ensure UI updates
        playerState.tryEmit(STATE_PLAYING)
    }

    fun addMediaItem(index: Int, track: MediaItem) {
        mediaController.addMediaItem(index, track)
    }

    fun reOrder(from: Int, to: Int, track: MediaItem) {
        mediaController.removeMediaItem(from)
        mediaController.addMediaItem(to, track)
    }

    fun setRepeatMode(mode: Int) {
        mediaController.repeatMode = mode
    }

    fun getRepeatMode(): Int = mediaController.repeatMode
    fun getShuffleMode(): Boolean = mediaController.shuffleModeEnabled

    fun toggleShuffle() {
        mediaController.shuffleModeEnabled = !mediaController.shuffleModeEnabled
    }

    fun getPlayer() = mediaController

    fun setUpTrack(index: Int, isTrackPlay: Boolean) {
        try {
            if (mediaController.playbackState == Player.STATE_IDLE) mediaController.prepare()

            val wasShuffleEnabled = mediaController.shuffleModeEnabled
            if (wasShuffleEnabled) {
                mediaController.shuffleModeEnabled = false
            }

            mediaController.seekTo(index, 0)

            if (wasShuffleEnabled) {
                mediaController.shuffleModeEnabled = true
            }

            if (isTrackPlay) mediaController.playWhenReady = true
        } catch (e: Exception) {
            Timber.e(e, "Error setting up track at index $index")
        }
    }

    fun playPause() {
        if (mediaController.playbackState == Player.STATE_IDLE) mediaController.prepare()
        if (mediaController.isPlaying) mediaController.pause() else mediaController.play()
    }

    fun play() {
        if (mediaController.playbackState == Player.STATE_IDLE) mediaController.prepare()
        mediaController.play()
    }

    fun pause() {
        if (mediaController.isPlaying) mediaController.pause()
    }

    fun seekToPosition(position: Long) {
        mediaController.seekTo(position)
    }

    fun emitPlaying() {
        playerState.tryEmit(STATE_PLAYING)
    }

    override fun onPlayerError(error: PlaybackException) {
        Timber.e(TAG, "onPlayerError: ${error.message}")
        playerState.tryEmit(STATE_ERROR)
    }

    fun setOnMediaItemTransitionCallback(callback: (Int) -> Unit) {
        onMediaItemTransitionCallback = callback
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

        playerState.tryEmit(PlayerStates.STATE_MEDIA_ITEM_TRANSITION)

        val currentIndex = mediaController.currentMediaItemIndex
        onMediaItemTransitionCallback?.invoke(currentIndex)


        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
            reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
        ) {
            handler.postDelayed({
                if (mediaController.isPlaying) {
                    playerState.tryEmit(STATE_PLAYING)
                }
            }, 100)
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
                resumeOnFocusGain = mediaController.isPlaying
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
            Timber.e("Audio focus request failed", e)
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
                if (mediaController.playWhenReady && mediaController.isPlaying) {
                    playerState.tryEmit(STATE_PLAYING)
                } else {
                    playerState.tryEmit(STATE_PAUSE)
                }
            }

            Player.STATE_ENDED -> {
                isBuffering = false
                mediaController.seekTo(0, 0)

                if (mediaController.repeatMode == Player.REPEAT_MODE_ALL) {
                    playerState.tryEmit(STATE_PLAYING)
                    mediaController.play()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        playerState.tryEmit(STATE_PAUSE)
                        mediaController.pause()
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
            if (!isBuffering && mediaController.playbackState == Player.STATE_READY) {
                playerState.tryEmit(STATE_PAUSE)
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (mediaController.playbackState == Player.STATE_READY) {
            if (playWhenReady && mediaController.isPlaying) {
                playerState.tryEmit(STATE_PLAYING)
            } else if (!playWhenReady) {
                playerState.tryEmit(STATE_PAUSE)
            }
        }
    }
}
