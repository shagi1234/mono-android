package com.mono.music.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.common.base.Preconditions
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.domain.models.Song
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.components.swipe.SwipeAction
import com.mono.music.ui.components.swipe.SwipeableActionsBox
import com.mono.music.ui.theme.FormularFontFamily
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.ICON_PRESS
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SongView(
    modifier: Modifier? = Modifier,
    playerController: PlayerController,
    song: Song,
    reorderable: Boolean = false,
    downloadTracker: DownloadTracker? = null,
    onMoreClicked: (Song) -> Unit,
    onClick: () -> Unit,
) {


    val isDownloaded = downloadTracker?.isDownloaded(song.toMediaItem()) ?: false

    val isPlaying = song.songId == playerController.selectedTrack?.songId

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.equalizer)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )

    Row(
        modifier = modifier?.scaleItemClickable(onClick = onClick)

            ?.padding(5.dp)
            ?: Modifier
                .scaleItemClickable(onClick = onClick)
                .padding(start = 20.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .size(50.dp)
                .clip(shape = MaterialTheme.shapes.small)
                .aspectRatio(1f)
                .background(Surface),
            model = song.getSongImage(),
            contentScale = ContentScale.Crop,
            contentDescription = "",
            alignment = Alignment.Center

        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (isPlaying) {
                        LottieAnimation(
                            composition,
                            { progress },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = song.name,
                        fontFamily = SFFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else WhiteTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (isDownloaded) {
                        Icon(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(end = 2.dp),
                            painter = painterResource(id = R.drawable.ic_check_circle_active),
                            contentDescription = "song setting",
                            tint = Color.Unspecified
                        )
                    }
                    Text(
                        text = song.getArtistsName(),
                        fontFamily = SFFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

            }

            if (reorderable) {

                Icon(
                    modifier = Modifier.padding(20.dp),
                    painter = painterResource(id = R.drawable.two_lines),
                    contentDescription = "song setting",
                    tint = WhiteTextColor
                )

            } else {
//                CustomMiniButton(
//                    modifier = Modifier.padding(2.dp),
//                    containerColor = Background,
//                    contentColor = WhiteTextColor,
//                    leadingIcon = R.drawable.song_setting,
//                    onClick = { onMoreClicked(song) })
                IconButton(
                    onClick = {},
                    modifier = Modifier.scaleIconClickable(
                        pressValue = ICON_PRESS,
                        onClick = { onMoreClicked(song) }
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.song_setting),
                        contentDescription = "song setting",
                        tint = WhiteTextColor
                    )
                }
            }
        }


    }


}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SwipeableSongView(
    @SuppressLint("ModifierParameter") modifier: Modifier? = null,
    playerController: PlayerController,
    showsOrderNumber: Boolean = false,
    song: Song,
    index: Int = 0,
    reorderable: Boolean = false,
    downloadTracker: DownloadTracker? = null,
    onMoreClicked: (Song) -> Unit,
    onSwipe: () -> Unit,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val playNext = SwipeAction(
        icon = painterResource(id = R.drawable.redo),
        background = MaterialTheme.colorScheme.primary,
        isUndo = false,
        onSwipe = {
            onSwipe()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.equalizer)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    val isDownloaded = downloadTracker?.isDownloaded(song.toMediaItem()) ?: false
    val isDownloading = downloadTracker?.isDownloading(song.toMediaItem()) ?: false

    val endActions = listOf(playNext)

    val isPlaying = song.songId == playerController.selectedTrack?.songId


    SwipeableActionsBox(
        endActions = endActions, swipeThreshold = 40.dp
    ) {
        // Swipeable content goes here.
        Row(modifier = modifier?.scaleItemClickable { onClick() } ?: Modifier
            .scaleItemClickable { onClick() }
            .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(if (showsOrderNumber) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                if (!showsOrderNumber) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape = MaterialTheme.shapes.small)
                            .aspectRatio(1f)
                            .background(Surface),
                        model = song.getSongImage(),
                        contentScale = ContentScale.Crop,
                        contentDescription = "",
                        alignment = Alignment.Center
                    )
                } else {
                    Text(
                        text = index.plus(1).toString(),
                        modifier = Modifier.width(20.dp),
                        color = if (isPlaying) Yellow else WhiteTextColor,
                        fontFamily = FormularFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .size(50.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(25.dp),
                            strokeWidth = 1.dp
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "song setting",
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        if (isPlaying) {
                            LottieAnimation(
                                composition,
                                { progress },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = song.name,
                            fontFamily = SFFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) MaterialTheme.colorScheme.primary else WhiteTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDownloaded) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(12.dp),
                                painter = painterResource(id = R.drawable.ic_check_circle_active),
                                contentDescription = "song setting",
                                tint = Color.Unspecified
                            )
                        }
                        Text(
                            text = song.getArtistsName(),
                            fontFamily = SFFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrayTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                    }
                }

                if (reorderable) {

                    Icon(
                        modifier = Modifier.padding(20.dp),
                        painter = painterResource(id = R.drawable.two_lines),
                        contentDescription = "song setting",
                        tint = WhiteTextColor
                    )

                } else {
                    IconButton(
                        modifier = Modifier.scaleIconClickable(onClick = { onMoreClicked(song) }),
                        onClick = { },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.song_setting),
                            contentDescription = "song setting",
                            tint = WhiteTextColor
                        )
                    }
                }
            }


        }

    }
}


