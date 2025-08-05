package com.mono.music.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mono.music.MainActivity
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.domain.models.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.URL
import javax.inject.Inject

@Suppress("UNUSED_CHANGED_VALUE")
@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var playerController: PlayerController

    private var isHandlingAction = false
    private val actionLock = Any()
    private var lastActionTimes = mutableMapOf<String, Long>()
    private val ACTION_DEBOUNCE_TIME = 300L // milliseconds
    private lateinit var mediaSession: MediaSession
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentSong: Song? = null
    private var currentArtwork: Bitmap? = null
    private var currentArtworkUri: android.net.Uri? = null

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "mono_session_notification_channel_id"
        private val immutableFlag = FLAG_IMMUTABLE
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1234

        // Action keys for notification controls
        const val ACTION_PLAY = "com.mono.music.ACTION_PLAY"
        const val ACTION_PAUSE = "com.mono.music.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.mono.music.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.mono.music.ACTION_NEXT"
        const val ACTION_STOP = "com.mono.music.ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("PlaybackService onCreate")

        initializePlayer()
        initializeMediaSession()
        setupPlayerListener()

        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        setListener(MediaSessionServiceListener())
    }

    private fun initializePlayer() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Timber.d("Media item transition: ${mediaItem?.mediaId}, reason: $reason")
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Timber.d("onIsPlayingChanged: $isPlaying")
                updateNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Timber.d("onPlaybackStateChanged: $playbackState")
                updateNotification()
            }
        })
    }

    private fun initializeMediaSession() {
        // Create session activity intent
        val sessionActivityPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
            getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
        }

        // Create media session
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaLibrarySessionCallback(this, playerController))
            .setSessionActivity(sessionActivityPendingIntent).build()
    }

    private fun setupPlayerListener() {
        // Track the current song
        coroutineScope.launch {
            playerController.tracks.forEach { song ->
                if (song.isSelected) {
                    currentSong = song
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification() {
        if (!::mediaSession.isInitialized) return

        try {
            // Get current media item
            val mediaItem = player.currentMediaItem
            val artworkUri = mediaItem?.mediaMetadata?.artworkUri

            // Check if we need to load new artwork
            if (artworkUri != null && artworkUri != currentArtworkUri) {
                // We need to load on a background thread
                coroutineScope.launch {
                    val bitmap = loadArtworkWithFallback(artworkUri)
                    currentArtwork = bitmap
                    currentArtworkUri = artworkUri

                    // Now create and show notification with the loaded artwork
                    val notification = createNotification()
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            } else {
                // We can use cached artwork or no artwork
                val notification = createNotification()
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating notification")
        }
    }



    private fun createNotification(): Notification {
        ensureNotificationChannel()

        val isPlaying = player.isPlaying
        val mediaItem = player.currentMediaItem
        val title = mediaItem?.mediaMetadata?.title ?: getString(R.string.app_name)
        val artist = mediaItem?.mediaMetadata?.artist ?: ""
        val albumTitle = mediaItem?.mediaMetadata?.albumTitle ?: ""

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(albumTitle)
            .setSmallIcon(R.drawable.playing_on_device_ic)
            .setContentIntent(getContentIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDeleteIntent(getStopIntent())

        if (currentArtwork != null) {
            builder.setLargeIcon(currentArtwork)
        }

        val compactActions = mutableListOf<Int>()
        var actionIndex = 0

        if (isPlaying) {
            builder.addAction(R.drawable.pause, getString(R.string.pause), getPauseIntent())
        } else {
            builder.addAction(R.drawable.play, getString(R.string.play_all), getPlayIntent())
        }
        compactActions.add(actionIndex++) // Всегда показываем play/pause

        if (player.hasPreviousMediaItem()) {
            builder.addAction(
                androidx.media3.session.R.drawable.media3_icon_previous,
                getString(androidx.media3.session.R.string.media3_controls_seek_to_previous_description),
                getPreviousIntent()
            )
            compactActions.add(actionIndex++)
        }

        if (player.hasNextMediaItem()) {
            builder.addAction(
                androidx.media3.session.R.drawable.media3_icon_next,
                getString(R.string.play_next),
                getNextIntent()
            )
            compactActions.add(actionIndex++)
        }

        builder.setStyle(
            MediaStyle(mediaSession).setShowActionsInCompactView(*compactActions.toIntArray())
        )

        return builder.build()
    }

    private fun ensureNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if channel exists
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.media_notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getContentIntent(): PendingIntent {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
            getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
        }
    }

    private fun getPlayIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 1, Intent(this, PlaybackService::class.java).apply {
                action = ACTION_PLAY
                // Add a timestamp to ensure it's treated as a new intent
                putExtra("timestamp", System.currentTimeMillis())
            }, immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    private fun getPauseIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 2, Intent(this, PlaybackService::class.java).apply {
                action = ACTION_PAUSE
                putExtra("timestamp", System.currentTimeMillis())
            }, immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    private fun getNextIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 3, Intent(this, PlaybackService::class.java).apply {
                action = ACTION_NEXT
                putExtra("timestamp", System.currentTimeMillis())
            }, immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    private fun getPreviousIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 4, Intent(this, PlaybackService::class.java).apply {
                action = ACTION_PREVIOUS
                putExtra("timestamp", System.currentTimeMillis())
            }, immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    private fun getStopIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 5, Intent(this, PlaybackService::class.java).apply {
                action = ACTION_STOP
            }, immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                // Use a coroutine to handle actions, allowing debouncing
                coroutineScope.launch {
                    handleActionIntent(action)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun handleActionIntent(action: String) {
        // Debounce rapid actions of the same type
        val currentTime = System.currentTimeMillis()
        val lastTime = lastActionTimes[action] ?: 0L

        if (currentTime - lastTime < ACTION_DEBOUNCE_TIME) {
            Timber.d("Debouncing rapid action: $action")
            return
        }

        lastActionTimes[action] = currentTime

        when (action) {
            ACTION_PLAY -> {
                Timber.d("Handling ACTION_PLAY")
                playerController.onPlayPauseClick()
            }

            ACTION_PAUSE -> {
                Timber.d("Handling ACTION_PAUSE")
                playerController.onPlayPauseClick()
            }

            ACTION_NEXT -> {
                Timber.d("Handling ACTION_NEXT")
                playerController.onNextClick()
            }

            ACTION_PREVIOUS -> {
                Timber.d("Handling ACTION_PREVIOUS")
                playerController.onPreviousClick()
            }

            ACTION_STOP -> {
                Timber.d("Handling ACTION_STOP")
                stopSelf()
            }
        }
    }

    private suspend fun loadArtworkWithFallback(uri: android.net.Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                when {
                    // Content URI
                    uri.scheme == "content" -> {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }
                    // File URI
                    uri.scheme == "file" -> {
                        BitmapFactory.decodeFile(uri.path)
                    }
                    // Network URI (http, https)
                    uri.scheme == "http" || uri.scheme == "https" -> {
                        try {
                            val url = URL(uri.toString())
                            val connection = url.openConnection()
                            connection.connectTimeout = 5000
                            connection.readTimeout = 5000
                            connection.inputStream.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Failed to load network image")
                            null
                        }
                    }
                    // Resource URI
                    uri.scheme == "android.resource" -> {
                        val resId = uri.pathSegments.lastOrNull()?.toIntOrNull()
                        if (resId != null) {
                            BitmapFactory.decodeResource(resources, resId)
                        } else {
                            null
                        }
                    }

                    else -> null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load bitmap from uri: $uri")
                null
            }
        }
    }

    private fun setupMediaItemListener() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                // Preload artwork for the new media item
                mediaItem?.mediaMetadata?.artworkUri?.let { uri ->
                    if (uri != currentArtworkUri) {
                        coroutineScope.launch {
                            currentArtwork = loadArtworkWithFallback(uri)
                            currentArtworkUri = uri
                            updateNotification()
                        }
                    }
                }
            }
        })
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        Timber.d("PlaybackService onDestroy")

        // Release resources in correct order
        mediaSession.release()
        player.release()
        coroutineScope.cancel()
        clearListener()

        // Remove notification when service is destroyed
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        super.onDestroy()
    }

    private inner class MediaSessionServiceListener : Listener {
        @SuppressLint("MissingPermission")
        override fun onForegroundServiceStartNotAllowedException() {
            Timber.w("Foreground service start not allowed exception")
            val notificationManagerCompat = NotificationManagerCompat.from(this@PlaybackService)
            ensureNotificationChannel()

            val pendingIntent = TaskStackBuilder.create(this@PlaybackService).run {
                addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
                getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
            }

            val builder = NotificationCompat.Builder(this@PlaybackService, CHANNEL_ID)
                .setContentIntent(pendingIntent).setSmallIcon(R.drawable.play)
                .setContentTitle(getString(R.string.notification_content_title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)

            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
        }
    }
}

/** A [MediaLibraryService.MediaLibrarySession.Callback] implementation. */
@OptIn(UnstableApi::class)
class MediaLibrarySessionCallback(
    private val context: Context, private val playerController: PlayerController
) : MediaLibraryService.MediaLibrarySession.Callback {

    private val customLayoutCommandButtons: List<CommandButton> = listOf(
        CommandButton.Builder().setDisplayName(context.getString(R.string.shuffle))
            .setSessionCommand(
                SessionCommand(
                    CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY
                )
            ).setIconResId(R.drawable.shuffle).build(),
        CommandButton.Builder().setDisplayName(context.getString(R.string.shuffle))
            .setSessionCommand(
                SessionCommand(
                    CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY
                )
            ).setIconResId(R.drawable.shuffle).build()
    )

    // Define available commands for notification controllers
    private val mediaNotificationSessionCommands =
        MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            .also { builder ->
                // Add custom commands for notification
                customLayoutCommandButtons.forEach { commandButton ->
                    commandButton.sessionCommand?.let { builder.add(it) }
                }
            }.build()

    override fun onConnect(
        session: MediaSession, controller: ControllerInfo
    ): MediaSession.ConnectionResult {
        // Special handling for notification controllers
        if (session.isMediaNotificationController(controller) || session.isAutomotiveController(
                controller
            ) || session.isAutoCompanionController(controller)
        ) {
            // Select shuffle button based on current state
            val customLayout =
                customLayoutCommandButtons[if (session.player.shuffleModeEnabled) 1 else 0]

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(mediaNotificationSessionCommands)
                .setCustomLayout(ImmutableList.of(customLayout)).build()
        }

        // Default commands for other controllers
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON -> {
                playerController.toggleShuffle()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }

            CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF -> {
                playerController.toggleShuffle()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }
        return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
    }

    override fun onGetItem(
        session: MediaLibraryService.MediaLibrarySession, browser: ControllerInfo, mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        // Find the media item by ID
        val tracks = playerController.tracks
        val song = tracks.find { it.songId.toString() == mediaId }

        return if (song != null) {
            val mediaItem = MediaItem.Builder().setMediaId(song.songId.toString()).setMediaMetadata(
                MediaMetadata.Builder().setTitle(song.name).setArtist(song.getArtistsName())
                    .setAlbumTitle(song.albumName).setArtworkUri(song.getSongImage().toUri())
                    .build()
            ).build()
            Futures.immediateFuture(LibraryResult.ofItem(mediaItem, null))
        } else {
            Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
        }
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        // Implement if you need a browsable media library
        return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession, controller: ControllerInfo, mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return Futures.immediateFuture(resolveMediaItems(mediaItems))
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        browser: ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaItemsWithStartPosition> {
        if (mediaItems.size == 1) {
            // Try to expand a single item to a playlist
            maybeExpandSingleItemToPlaylist(mediaItems.first(), startIndex, startPositionMs)?.also {
                return Futures.immediateFuture(it)
            }
        }
        return Futures.immediateFuture(
            MediaItemsWithStartPosition(resolveMediaItems(mediaItems), startIndex, startPositionMs)
        )
    }


    private fun resolveMediaItems(mediaItems: List<MediaItem>): List<MediaItem> {
        // Here you would resolve the media items with full metadata
        val resolvedItems = mutableListOf<MediaItem>()

        for (item in mediaItems) {
            // Find the corresponding song in player controller
            val song = playerController.tracks.find { it.songId.toString() == item.mediaId }

            if (song != null) {
                // Create a new media item with full metadata
                val resolvedItem = item.buildUpon().setMediaMetadata(
                    MediaMetadata.Builder().setTitle(song.name).setArtist(song.getArtistsName())
                        .setAlbumTitle(song.albumName).setArtworkUri(song.getSongImage().toUri())
                        .build()
                ).build()
                resolvedItems.add(resolvedItem)
            } else {
                // If not found, just add the original item
                resolvedItems.add(item)
            }
        }

        return resolvedItems
    }

    private fun maybeExpandSingleItemToPlaylist(
        mediaItem: MediaItem, startIndex: Int, startPositionMs: Long
    ): MediaItemsWithStartPosition? {
        // You could implement playlist expansion logic here
        // For example, if a user selects an album, you could expand it to all tracks
        return null
    }

    companion object {
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON = "com.mono.music.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF = "com.mono.music.SHUFFLE_OFF"
    }
}