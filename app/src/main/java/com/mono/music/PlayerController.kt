package com.mono.music

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.util.UnstableApi
import com.google.gson.Gson
import com.mono.music.domain.models.Song
import com.mono.music.domain.repository.SongRepository
import com.mono.music.player.MyPlayer
import com.mono.music.player.PlaybackService
import com.mono.music.player.PlaybackState
import com.mono.music.player.PlayerEvents
import com.mono.music.player.PlayerStates
import com.mono.music.presentation.player.MediaStateManager
import com.mono.music.ui.utils.collectPlayerState
import com.mono.music.ui.utils.launchPlaybackStateJob
import com.mono.music.ui.utils.resetTracks
import com.mono.music.ui.utils.swap
import com.mono.music.ui.utils.toMediaItemList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import androidx.core.content.edit
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// Data class to track listening progress
data class ListeningProgress(
    val songId: Long,
    val startTime: Long,
    val maxProgress: Float = 0f,
    val hasTriggeredAPI: Boolean = false
)

@OptIn(UnstableApi::class)
class PlayerController @Inject constructor(
    private val myPlayer: MyPlayer,
    private val context: Context,
    private val songRepository: SongRepository,
) : PlayerEvents {

    val playerState = myPlayer.playerState

    private val _tracks = mutableStateListOf<Song>()
    val tracks: List<Song> get() = _tracks

    private val navigationLock = Any()
    private var isNavigating = false
    private var lastTransitionTime = 0L
    private val TRANSITION_DEBOUNCE_TIME = 300L // milliseconds
    private var isProcessingStateUpdate = false


    private var isTrackPlay: Boolean = false


    var selectedTrack: Song? by mutableStateOf(null)
        private set

    var selectedTrackIndex: Int by mutableIntStateOf(-1)

    private var playbackStateJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState(0L, 0L, 0L))

    val playbackState: StateFlow<PlaybackState> get() = _playbackState

    val hasPrev = myPlayer.hasPrev
    val hasNext = myPlayer.hasNext

//    private var isAuto: Boolean = false

    private var currentListeningProgress: ListeningProgress? = null
    private val listenedSongs = mutableSetOf<Long>()

    // Threshold for "under 30%" listening
    private val LISTENING_THRESHOLD = 0.3f // 30%



    init {
        myPlayer.setOnMediaItemTransitionCallback { newIndex ->
            handleMediaItemTransitionFromPlayer(newIndex)
        }
    }


    private fun handleMediaItemTransitionFromPlayer(newIndex: Int) {
        synchronized(navigationLock) {
            if (isNavigating || isProcessingStateUpdate) {
                Timber.d("Already processing, skipping external transition")
                return
            }

            isProcessingStateUpdate = true
            try {
                val previousSongId = selectedTrack?.songId
                handleSongTransition(previousSongId)

                if (newIndex >= 0 && newIndex < _tracks.size) {
                    if (selectedTrackIndex >= 0 && selectedTrackIndex < _tracks.size) {
                        _tracks[selectedTrackIndex].isSelected = false
                        _tracks[selectedTrackIndex].state = PlayerStates.STATE_IDLE
                    }

                    selectedTrackIndex = newIndex
                    selectedTrack = tracks[selectedTrackIndex]


                    _tracks.resetTracks()
                    _tracks[selectedTrackIndex].isSelected = true
                    _tracks[selectedTrackIndex].state = PlayerStates.STATE_PLAYING

                    currentListeningProgress = ListeningProgress(
                        songId = selectedTrack!!.songId,
                        startTime = System.currentTimeMillis(),
                        maxProgress = 0f,
                        hasTriggeredAPI = false
                    )

//                    isAuto = true

                    Timber.d("Updated UI for track transition to index: $newIndex, track: ${selectedTrack?.name}")
                }
            } finally {
                isProcessingStateUpdate = false
            }
        }
    }

    fun init(track: Song, songs: List<Song>) {
        observePlayerState()
        synchronized(navigationLock) {
            if (isNavigating) {
                Timber.d("Already handling navigation, skipping init")
                return
            }

            isNavigating = true
            try {
                // Check if it's the exact same track AND the player is already playing
                if (track == selectedTrack && myPlayer.playerState.value == PlayerStates.STATE_PLAYING) {
//                    onPlayPauseClick()
                    return
                }

                // Check if the songs list is the same but we want to play a different track in that list
                if (songs == tracks && track != selectedTrack) {
                    val trackIndex = tracks.indexOf(track)
                    if (trackIndex >= 0) {
                        myPlayer.getPlayer().seekToDefaultPosition(trackIndex)
                        onTrackSelected(trackIndex)
                        return
                    }
                }

                // Otherwise, initialize with the new list of songs
                myPlayer.iniPlayer(songs.toMediaItemList())

                if (!_tracks.isEmpty()) {
                    _tracks.removeRange(0, tracks.size)
                }
                _tracks.addAll(songs)

                // Service will be started when user selects a track to play
                onTrackClick(song = track)
            } finally {
                isNavigating = false
            }
        }
    }

    fun init(track: Int, songs: List<Song>) {
        myPlayer.iniPlayer(songs.toMediaItemList())
        observePlayerState()

        _tracks.clear()
        _tracks.addAll(songs)

        // Don't start service here - only start it when track is selected
        onTrackClick(song = track)
    }

    private fun startPlaybackService(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.putExtra(
                "FOREGROUND_SERVICE_TYPE",
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }

        ContextCompat.startForegroundService(context, intent)
    }

    fun onReorder(from: Int, to: Int) {
        selectedTrackIndex = _tracks.indexOf(selectedTrack)
    }

    fun onMove(from: Int, to: Int) {
        val song = tracks[from]
        _tracks.swap(from, to)

        when {
            selectedTrackIndex == from -> selectedTrackIndex = to
            selectedTrackIndex in (minOf(from, to)..maxOf(from, to)) -> {
                selectedTrackIndex =
                    if (from < to) selectedTrackIndex - 1 else selectedTrackIndex + 1
            }
        }
        myPlayer.reOrder(from, to, song.toMediaItem())


    }

    fun setRepeatMode(mode: Int) {
        return myPlayer.setRepeatMode(mode)
    }

    fun getRepeatMode(): Int {
        return myPlayer.getRepeatMode()
    }

    fun getShuffleMode(): Boolean {
        return myPlayer.getShuffleMode()
    }

    fun toggleShuffle() {
        return myPlayer.toggleShuffle()
    }

    fun playerCurrentTime(): Long {
        return myPlayer.currentPlaybackPosition
    }

    fun playerTrackDuration(): Long {
        return myPlayer.currentTrackDuration
    }

    // Method to make the API call for under-listened songs
    private fun reportUnderListenedSong(songId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                songRepository.listenedSong(songId)
            } catch (e: Exception) {
                Timber.e("SONG_TRACKER", "❌ Error reporting under-listened song: $songId", e)
            }
        }
    }

    private fun checkListeningProgress() {
        val currentProgress = currentListeningProgress ?: return
        val duration = playerTrackDuration()
        val position = playerCurrentTime()

        if (duration > 0) {
            val progressPercent = position.toFloat() / duration.toFloat()

            if (progressPercent > currentProgress.maxProgress) {
                currentListeningProgress = currentProgress.copy(maxProgress = progressPercent)
            }
        }
    }

    // Method to handle song change/end
    private fun handleSongTransition(previousSongId: Long?) {
        previousSongId?.let { songId ->
            val progress = currentListeningProgress
            if (progress != null &&
                progress.songId == songId &&
                progress.maxProgress < LISTENING_THRESHOLD &&
                !progress.hasTriggeredAPI &&
                !listenedSongs.contains(songId)
            ) {

                reportUnderListenedSong(songId)
                listenedSongs.add(songId)

                currentListeningProgress = progress.copy(hasTriggeredAPI = true)
            } else if (progress != null && progress.songId == songId) {
                Timber.d(
                    "SONG_TRACKER",
                    "✅ Song $songId listened adequately: ${(progress.maxProgress * 100).toInt()}%"
                )
            }
        }
    }

    private fun onTrackSelected(index: Int) {
        val previousSongId = selectedTrack?.songId
        handleSongTransition(previousSongId)

        if (selectedTrackIndex == -1) isTrackPlay = true

        _tracks.resetTracks()
        selectedTrackIndex = index
        selectedTrack = tracks[selectedTrackIndex]


        // Initialize tracking for the new song
        selectedTrack?.let { song ->
            currentListeningProgress = ListeningProgress(
                songId = song.songId,
                startTime = System.currentTimeMillis(),
                maxProgress = 0f,
                hasTriggeredAPI = false
            )
        }

        startPlaybackService(context)

        setUpTrack()

        myPlayer.play()

    }
//
//    private fun setUpTrack() {
//        if (!isAuto) myPlayer.setUpTrack(selectedTrackIndex, isTrackPlay)
//        isAuto = false
//    }

    private fun setUpTrack() {
        myPlayer.setUpTrack(selectedTrackIndex, isTrackPlay)

//        Log.e("TRACK_SELECTED", "setUpTrack: isAuto=$isAuto, selectedTrackIndex=$selectedTrackIndex")
//        if (!isAuto) {
//            Log.e("TRACK_SELECTED", "Вызываем myPlayer.setUpTrack")
//            myPlayer.setUpTrack(selectedTrackIndex, isTrackPlay)
//        } else {
//            Log.e("TRACK_SELECTED", "Пропускаем myPlayer.setUpTrack из-за isAuto=true")
//        }
//        isAuto = false
    }

    private fun updateState(state: PlayerStates) {
        if (isProcessingStateUpdate) {
            Timber.d("Skipping nested state update: $state")
            return
        }

        isProcessingStateUpdate = true
        try {
            if (selectedTrackIndex != -1 && selectedTrackIndex < _tracks.size) {
                isTrackPlay = state == PlayerStates.STATE_PLAYING
                _tracks[selectedTrackIndex].state = state
                _tracks[selectedTrackIndex].isSelected = true
                selectedTrack = tracks[selectedTrackIndex]  // Don't set to null first

                if (state == PlayerStates.STATE_NEXT_TRACK) {
                    onNextClick()
                    myPlayer.emitPlaying()
                }

                if (state == PlayerStates.STATE_MEDIA_ITEM_TRANSITION) {
                    onPlayerItemTransition()
                }

                updatePlaybackState(state)

                if (state == PlayerStates.STATE_END && myPlayer.getRepeatMode() == REPEAT_MODE_ALL) {
                    onTrackSelected(0)
                }

            }
        } finally {
            isProcessingStateUpdate = false
        }
    }

    private fun observePlayerState() {
        CoroutineScope(Dispatchers.Main).collectPlayerState(myPlayer, ::updateState)
    }

    private fun updatePlaybackState(state: PlayerStates) {
        playbackStateJob?.cancel()
        playbackStateJob = CoroutineScope(Dispatchers.Main).launch {
            launchPlaybackStateJob(_playbackState, state, myPlayer)

            if (state == PlayerStates.STATE_PLAYING) {
                checkListeningProgress()
            }
        }
    }

    override fun onPreviousClick() {
        synchronized(navigationLock) {
            if (isNavigating) {
                Timber.d("Already handling navigation, skipping")
                return
            }

            isNavigating = true
            try {
                if (myPlayer.getPlayer().hasPreviousMediaItem()) {
                    if (myPlayer.currentPlaybackPosition > 3000) {
                        myPlayer.getPlayer().seekTo(0)
                    } else {
                        myPlayer.getPlayer().seekToPrevious()
                        selectedTrackIndex = myPlayer.getPlayer().currentMediaItemIndex
                        onTrackSelected(selectedTrackIndex)
                    }
                } else {
                    myPlayer.getPlayer().seekTo(0)
                }
            } finally {
                isNavigating = false
            }
        }
    }

    override fun onNextClick() {
        synchronized(navigationLock) {
            if (isNavigating) {
                Timber.d("Already handling navigation, skipping")
                return
            }

            isNavigating = true
            try {
                if (myPlayer.getPlayer().hasNextMediaItem()) {
                    myPlayer.getPlayer().seekToNext()
                    selectedTrackIndex = myPlayer.getPlayer().currentMediaItemIndex
                    onTrackSelected(selectedTrackIndex)
                }
            } finally {
                isNavigating = false
            }
        }
    }

    override fun onPlayPauseClick() {
        myPlayer.playPause()
    }

    override fun onTrackClick(song: Song) {
        onTrackSelected(tracks.indexOf(song))
    }

    override fun onTrackClick(song: Int) {
        onTrackSelected(song)
    }

    override fun onPlayNext(song: Song) {
        if (_tracks.contains(song)) {
            if (selectedTrackIndex == _tracks.indexOf(song)) return
            onMove(_tracks.indexOf(song), selectedTrackIndex + 1)
        } else {
            _tracks.add(selectedTrackIndex + 1, song)
            myPlayer.addMediaItem(selectedTrackIndex + 1, song.toMediaItem())
        }
    }

    override fun onSeekBarPositionChanged(position: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            myPlayer.seekToPosition(position)

        }
    }

    override fun onPlayerItemTransition() {
        val currentPlayerIndex = myPlayer.getPlayer().currentMediaItemIndex
        val currentMediaItem = myPlayer.getPlayer().currentMediaItem

        val previousSongId = selectedTrack?.songId

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTransitionTime < TRANSITION_DEBOUNCE_TIME) {
            Timber.d("Ignoring rapid transition - debounced")
            return
        }

        lastTransitionTime = currentTime

        currentMediaItem ?: return

        val currentSong = tracks.find { it.songId.toString() == currentMediaItem.mediaId }
        if (currentSong != null && currentPlayerIndex != selectedTrackIndex) {
            handleSongTransition(previousSongId)
            if (!isProcessingStateUpdate) {
                synchronized(navigationLock) {
                    if (selectedTrackIndex != -1 && selectedTrackIndex < _tracks.size) {
                        _tracks.resetTracks()
                    }

                    selectedTrackIndex = currentPlayerIndex
                    selectedTrack = currentSong

                    if (selectedTrackIndex < _tracks.size) {
                        _tracks[selectedTrackIndex].isSelected = true
                    }

                    currentListeningProgress = ListeningProgress(
                        songId = currentSong.songId,
                        startTime = System.currentTimeMillis(),
                        maxProgress = 0f,
                        hasTriggeredAPI = false
                    )

//                    isAuto = true
                }
            }
        }
    }


}