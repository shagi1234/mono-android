package com.mono.music.ui.components

import android.provider.SyncStateContract.Columns
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.domain.models.Song
import com.mono.music.player.PlaybackState
import com.mono.music.player.PlayerStates
import com.mono.music.presentation.player.pulsatingEffect
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.Black
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.darken
import com.mono.music.ui.utils.getDominantColorFromImageUrl
import com.mono.music.ui.utils.scaleButtonClickable
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    song: Song,
    playerController: PlayerController,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
) {

    val playbackStateValue = playerController.playbackState.collectAsState(
        initial = PlaybackState(0L, 0L, 0L)
    ).value
    var currentMediaProgress = playbackStateValue.currentPlaybackPosition.toFloat()
    var currentPosTemp by rememberSaveable { mutableStateOf(0f) }

    val pagerState =
        androidx.compose.foundation.pager.rememberPagerState(
            playerController.selectedTrackIndex,
            pageCount = { playerController.tracks.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(playerController.selectedTrackIndex) {

        if (playerController.selectedTrackIndex >= 0 && pagerState.currentPage != playerController.selectedTrackIndex) {
            delay(300)
            pagerState.scrollToPage(playerController.selectedTrackIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {

        if (playerController.selectedTrackIndex >= 0 && playerController.selectedTrackIndex < playerController.tracks.size && pagerState.currentPage != playerController.selectedTrackIndex) {
            delay(400)
            playerController.onTrackClick(playerController.tracks[pagerState.currentPage])
        }

    }


    val dominantColor  = remember { Animatable(Color.Black) }

    LaunchedEffect(playerController.selectedTrack?.image) {
        coroutineScope.launch {
            dominantColor.animateTo(getDominantColorFromImageUrl( context = context, playerController.selectedTrack?.image ?: ""), animationSpec = tween(500))
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .clip(MaterialTheme.shapes.large)
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scaleItemClickable { onClick() }
                .background(dominantColor.value.darken(0.5f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f),
//                contentPadding = PaddingValues(horizontal = 10.dp)
            ) { page ->
                val song = playerController.tracks[page]
                val songImagePainter = rememberAsyncImagePainter(
                    model = song.getSongImage(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape = MaterialTheme.shapes.small)
                            .background(DarkGray),
                        painter = songImagePainter,
                        contentDescription = "artist image",
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .basicMarquee(
                                    // Animate forever.
                                    iterations = Int.MAX_VALUE,
                                ),
                            text = song.name,
                            fontFamily = SFFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WhiteTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.getArtistsName(),
                            fontFamily = SFFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = GrayTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }



            IconButton(
                modifier = Modifier
                    .scaleIconClickable { onPlayPauseClick() }
                    .padding(end = 5.dp)
                    .size(35.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Yellow
                ),
                onClick = {  },
            ) {
                Icon(
                    painter = if (song.isPlaying()) painterResource(id = R.drawable.pause) else painterResource(
                        id = R.drawable.play
                    ),
                    contentDescription = "see more",
                    tint = Black
                )

            }

        }
        val interactionSource = remember { MutableInteractionSource() }
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Slider(
                value = if (currentPosTemp == 0f) currentMediaProgress else currentPosTemp,
                onValueChange = {},
                onValueChangeFinished = {},
                enabled = false,
                valueRange = 0f..playbackStateValue.currentTrackDuration.toFloat(),
                modifier = Modifier
                    .padding(0.dp)
                    .height(0.dp)
                    .defaultMinSize(1.dp, 5.dp)
                    .fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = GrayTextColor,
                ),
                thumb = {
                    SliderDefaults.Thumb(
                        modifier = Modifier
                            .offset(x = 0.dp)
                            .pulsatingEffect(
                                if (currentPosTemp == 0f) currentMediaProgress else currentPosTemp,
                                isVisible = playerController.selectedTrack?.isBuffering() == true,
                                color = MaterialTheme.colorScheme.primary.copy(0.5f)
                            ),
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(0.dp, 0.dp)
                    )
                },
                track = {
                    SliderDefaults.Track(modifier = Modifier
                        .height(3.dp)
                        .padding(0.dp)
                        .defaultMinSize(1.dp, 1.dp),
                        sliderState = it,
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = {})
                }

            )
        }

    }
}