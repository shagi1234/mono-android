package com.mono.music.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Velocity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.legacy.MediaMetadataCompat
import androidx.navigation.NavBackStackEntry
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mono.music.domain.models.Song
import com.mono.music.player.MyPlayer
import com.mono.music.player.PlaybackState
import com.mono.music.player.PlayerStates
import com.mono.music.ui.theme.Background
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections
import kotlin.math.max


fun MutableList<Song>.resetTracks() {
    this.forEach { track ->
        track.isSelected = false
        track.state = PlayerStates.STATE_IDLE
    }
}


@SuppressLint("RestrictedApi")
@UnstableApi
fun List<Song>.toMediaItemList(): MutableList<MediaItem> {
    return this.map {
        val mediaMetaData =
            MediaMetadata.Builder().setArtworkUri(Uri.parse(it.getSongImage())).setTitle(it.name)
                .setDescription(it.getArtistsName())
                .setDurationMs(it.duration)
                .build()

        val trackUri = Uri.parse(it.getSongUrl())
        MediaItem.Builder().setUri(trackUri).setMediaId(it.songId.toString())
            .setMediaMetadata(mediaMetaData).build()

    }.toMutableList()
}

fun CoroutineScope.collectPlayerState(
    myPlayer: MyPlayer, updateState: (PlayerStates) -> Unit
) {

    this.launch {
        myPlayer.playerState.collect {
            updateState(it)
        }
    }
}


fun CoroutineScope.launchPlaybackStateJob(
    playbackStateFlow: MutableStateFlow<PlaybackState>, state: PlayerStates, myPlayer: MyPlayer
) = launch {

    do {
        playbackStateFlow.emit(
            PlaybackState(
                currentPlaybackPosition = myPlayer.currentPlaybackPosition,
                currentBufferedPosition = myPlayer.currentBufferedPosition,
                currentTrackDuration = myPlayer.currentTrackDuration
            )
        )
        delay(1000) // delay for 1 second
    } while ((state == PlayerStates.STATE_PLAYING || state == PlayerStates.STATE_BUFFERING) && isActive)
}


fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private val VerticalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) = available.copy(x = 0f)
    override suspend fun onPreFling(available: Velocity) = available.copy(x = 0f)
}

private val HorizontalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) = available.copy(y = 0f)
    override suspend fun onPreFling(available: Velocity) = available.copy(y = 0f)
}

fun Modifier.disabledVerticalPointerInputScroll(disabled: Boolean = true) =
    if (disabled) this.nestedScroll(VerticalScrollConsumer) else this

fun Modifier.disabledHorizontalPointerInputScroll(disabled: Boolean = true) =
    if (disabled) this.nestedScroll(HorizontalScrollConsumer) else this

fun Bundle.readable() = buildList {
    keySet().forEach {
        add("key=$it, value=${get(it)}")
    }
}.joinToString()


fun isOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun <T> Context.systemService(name: String): T {
    return getSystemService(name) as T
}

fun <T> List<T>.swap(fromIdx: Int, toIdx: Int): List<T> {
    val copy = toMutableList()
    Collections.swap(copy, fromIdx, toIdx)
    return copy
}

fun <T> MutableList<T>.swap(fromIdx: Int, toIdx: Int) {
    Collections.swap(this, fromIdx, toIdx)
}


fun Color.darken(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] = max(0f, hsv[2] * factor) // decrease value to darken
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun Modifier.clickWithoutIndication(onClick: () -> Unit): Modifier {
    return this.clickable(
        interactionSource = MutableInteractionSource(), indication = null
    ) { onClick() }
}

fun timeAddZeros(number: Int?, ifZero: String = ""): String {
    return when (number) {
        0 -> ifZero
        in 1..9 -> "0$number"
        else -> number.toString()
    }
}

fun Long.millisToDuration(): String {
    val seconds = (this / 1000).toInt() % 60
    val minutes = (this / (1000 * 60) % 60).toInt()
    val hours = (this / (1000 * 60 * 60) % 24).toInt()
    "${timeAddZeros(hours)}:${timeAddZeros(minutes, "0")}:${timeAddZeros(seconds, "00")}".apply {
        return if (startsWith(":")) replaceFirst(":", "") else this
    }
}


suspend fun getDominantColorFromImageUrl(context: Context, imageUrl: String): Color {
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(imageUrl).allowHardware(false).build()

        val result = (loader.execute(request) as SuccessResult).drawable
        val bitmap = (result as BitmapDrawable).bitmap

        val palette = withContext(Dispatchers.Default) {
            Palette.from(bitmap).generate()
        }

        return Color(palette.getDominantColor(Background.toArgb()))

    } catch (e: Exception) {
        return Color.Black
    }

}

object ScreenTransition : DestinationStyle.Animated {

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f
        )
    }

//    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
//        return scaleOut(
//            targetScale = 0.8f
//        )
//    }


    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition? {
        return null
    }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition? {
        return null
    }
}