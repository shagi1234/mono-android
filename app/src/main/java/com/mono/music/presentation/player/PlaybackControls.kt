package com.mono.music.presentation.player

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.player.PlaybackState
import com.mono.music.player.PlayerStates
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.HEAVY_PRESS
import com.mono.music.ui.utils.formatTime
import com.mono.music.ui.utils.scaleButtonClickable
import com.mono.music.ui.utils.scaleIconClickable
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    modifier: Modifier,
    playerController: PlayerController,
    playerState: PlayerStates,
    isPlaying: Boolean
) {
    val haptic = LocalHapticFeedback.current


    val progressState by playerController.playbackState.collectAsState(
        initial = PlaybackState(0L, 0L, 0L)
    )

    var shuffleEnabled by rememberSaveable {
        mutableStateOf(playerController.getShuffleMode())
    }

    var repeatMode by rememberSaveable {
        mutableStateOf(playerController.getRepeatMode())
    }

    val hasPrev = playerController.hasPrev.collectAsState().value
    val hasNext = playerController.hasNext.collectAsState().value

    var draggingProgress by remember { mutableStateOf<Float?>(null) }

    var currentMediaProgress = progressState.currentPlaybackPosition.toFloat()

    LaunchedEffect(currentMediaProgress) {
        if (draggingProgress != null) {
            val difference = abs(currentMediaProgress - draggingProgress!!)
            if (difference < 1000) {
                draggingProgress = null
            }
        }
    }

    Column(modifier = modifier) {
        Slider(
            value = draggingProgress ?: currentMediaProgress,
            onValueChange = { value ->
                draggingProgress = value
            },
            onValueChangeFinished = {
                draggingProgress?.toLong()?.let {
                    playerController.onSeekBarPositionChanged(it)
                }
            },

            valueRange = 0f..progressState.currentTrackDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = GrayTextColor,
            ),
            thumb = {
                SliderDefaults.Thumb(
                    modifier = Modifier
                        .padding(vertical = 5.dp),
                    interactionSource = remember { MutableInteractionSource() },
                    thumbSize = DpSize(14.dp, 14.dp)
                )
            },
            track = {
                SliderDefaults.Track(
                    modifier = Modifier
                        .height(5.dp)
                        .padding(0.dp)
                        .defaultMinSize(1.dp, 1.dp)
                        .pulsatingEffect(
                            draggingProgress ?: currentMediaProgress,
                            isVisible = playerState == PlayerStates.STATE_BUFFERING,
                            color = MaterialTheme.colorScheme.primary.copy(0.5f)
                        ),
                    sliderState = it,
                    thumbTrackGapSize = 0.dp,
                    drawStopIndicator = {})
            })

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier,
                text = progressState.currentPlaybackPosition.formatTime(),
                fontSize = 14.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Medium,
                color = WhiteTextColor
            )
            Text(
                modifier = Modifier,
                text = progressState.currentTrackDuration.formatTime(),
                fontSize = 14.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Medium,
                color = WhiteTextColor
            )
        }


        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                modifier = Modifier
                    .weight(.8f)
                    .scaleIconClickable {
                        playerController.toggleShuffle()
                        shuffleEnabled = playerController.getShuffleMode()
                    },
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shuffle),
                    contentDescription = stringResource(id = R.string.shuffle),
                    tint = if (shuffleEnabled) Yellow else GrayTextColor
                )
            }
            IconButton(
                enabled = hasPrev,
                modifier = Modifier
                    .weight(.8f)
                    .scaleIconClickable(onClick = playerController::onPreviousClick),
                onClick = {}
            ) {
                Icon(
                    modifier = Modifier.size(44.dp),
                    painter = painterResource(id = R.drawable.media_skip_backward),
                    contentDescription = stringResource(id = R.string.shuffle),
                    tint = if (hasPrev) WhiteTextColor else GrayTextColor
                )
            }

            FloatingActionButton(
                modifier = Modifier
                    .size(74.dp)
                    .scaleIconClickable {
                        if (playerState == PlayerStates.STATE_PLAYING || playerState == PlayerStates.STATE_PAUSE) {
                            playerController.onPlayPauseClick()
                        }
                    }
                    .clip(shape = CircleShape),
                onClick = {},
                containerColor = Yellow,
                contentColor = AlbumCoverBlackBG,
            ) {

                if (playerState == PlayerStates.STATE_PLAYING || playerState == PlayerStates.STATE_PAUSE) {
                    Icon(
                        modifier = Modifier.size(46.dp),
                        painter = if (isPlaying)
                            painterResource(id = R.drawable.pause)
                        else
                            painterResource(id = R.drawable.play),
                        contentDescription = stringResource(id = R.string.pause),
                        tint = Background
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Background,
                        strokeWidth = 3.dp
                    )
                }
            }
            IconButton(
                enabled = hasNext,
                modifier = Modifier
                    .weight(.8f)
                    .scaleIconClickable(onClick = playerController::onNextClick),
                onClick = {}) {
                Icon(
                    modifier = Modifier.size(44.dp),
                    painter = painterResource(id = R.drawable.media_skip_forward),
                    contentDescription = stringResource(id = R.string.shuffle),
                    tint = if (hasNext) WhiteTextColor else GrayTextColor
                )
            }
            val painter = if (repeatMode == REPEAT_MODE_ONE) R.drawable.repeat_one
            else R.drawable.repeat

            IconButton(
                modifier = Modifier
                    .weight(.8f)
                    .scaleIconClickable(
                        onClick =
                            {
                                when (playerController.getRepeatMode()) {
                                    REPEAT_MODE_OFF -> {
                                        playerController.setRepeatMode(REPEAT_MODE_ALL)
                                    }

                                    REPEAT_MODE_ALL -> {
                                        playerController.setRepeatMode(REPEAT_MODE_ONE)

                                    }

                                    REPEAT_MODE_ONE -> {
                                        playerController.setRepeatMode(REPEAT_MODE_OFF)

                                    }
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                repeatMode = playerController.getRepeatMode()
                            }
                    ),
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(id = painter),
                    contentDescription = stringResource(id = R.string.repeat),
                    tint = if (repeatMode != REPEAT_MODE_OFF) Yellow else GrayTextColor
                )
            }

        }
    }
}

fun Modifier.pulsatingEffect(
    currentValue: Float,
    isVisible: Boolean,
    color: Color = Color.Gray,
): Modifier = composed {
    var trackWidth by remember { mutableFloatStateOf(0f) }
    val thumbX by remember(currentValue) {
        mutableFloatStateOf(trackWidth * currentValue)
    }
    val transition = rememberInfiniteTransition(label = "trackAnimation")
    val animationProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                delayMillis = 200,
            )
        ), label = "width"
    )
    this then Modifier
        .onGloballyPositioned { coordinates ->
            trackWidth = coordinates.size.width.toFloat()
        }
        .drawWithContent {
            drawContent()
            val strokeWidth = size.height
            val y = size.height / 2f
            val startOffset = thumbX
            val endOffset = thumbX + animationProgress * (trackWidth - thumbX)
            val dynamicAlpha = (1f - animationProgress).coerceIn(0f, 1f)
            if (isVisible) {
                drawLine(
                    color = color.copy(alpha = dynamicAlpha),
                    start = Offset(startOffset, y),
                    end = Offset(endOffset, y),
                    cap = StrokeCap.Round,
                    strokeWidth = strokeWidth
                )
            }
        }
}