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

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var playerFactory: ExoPlayerFactory

    @Inject
    lateinit var playerController: PlayerController

    private lateinit var player: ExoPlayer

    private lateinit var mediaSession: MediaSession

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "mono_session_notification_channel_id"
    }

    // ========== LIFECYCLE METHODS ==========

    override fun onCreate() {
        super.onCreate()
        Timber.d("PlaybackService onCreate")

        initializePlayer()
        initializeMediaSession()
        createNotificationChannel()

    }


    override fun onDestroy() {
        Timber.d("PlaybackService onDestroy")
        // Освобождаем ресурсы в правильном порядке
        coroutineScope.cancel()
        mediaSession.release()
        player.release()

        super.onDestroy()
    }

    // ========== REQUIRED MediaSessionService METHODS ==========

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Останавливаем сервис если музыка не играет
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    // ========== INITIALIZATION METHODS ==========

    private fun initializePlayer() {
        player = playerFactory.createPlayer()
    }

    private fun initializeMediaSession() {
        val sessionActivityPendingIntent = createSessionActivityPendingIntent()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaLibrarySessionCallback(this, playerController))
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    private fun createSessionActivityPendingIntent(): PendingIntent {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
            getPendingIntent(
                0,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )
        }
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.media_notification_channel_description)
                setShowBadge(false)
                setSound(null, null) // Отключаем звуки уведомлений
            }
            notificationManager.createNotificationChannel(channel)
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


