package com.mono.music.presentation.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.rememberAsyncImagePainter
import com.mono.music.PlayerController
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun AlbumCoverPager(
    playerController: PlayerController,
    isPlaying: Boolean,
    isFullScreenVisible: Boolean
) {
    val pagerState =
        androidx.compose.foundation.pager.rememberPagerState(playerController.selectedTrackIndex,
            pageCount = { playerController.tracks.size })

    LaunchedEffect(playerController.selectedTrackIndex) {
        if (playerController.selectedTrackIndex >= 0 &&
            playerController.selectedTrackIndex < playerController.tracks.size &&
            pagerState.currentPage != playerController.selectedTrackIndex
        ) {

            delay(300)
            if (!isFullScreenVisible) {
                pagerState.scrollToPage(playerController.selectedTrackIndex)
            } else {
                pagerState.animateScrollToPage(playerController.selectedTrackIndex)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {

        if (isFullScreenVisible) {
            if (playerController.selectedTrackIndex >= 0 && playerController.selectedTrackIndex < playerController.tracks.size && pagerState.currentPage != playerController.selectedTrackIndex) {
                delay(500)
                playerController.onTrackClick(playerController.tracks[pagerState.currentPage])
            }
        }

    }

    HorizontalPager(
        pageSpacing = 2.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = Modifier.fillMaxWidth(),
        state = pagerState
    ) { page ->
        // Create a transition for animating the scale
        val transition = updateTransition(
            targetState = isPlaying,
            label = "PlayingTransition"
        )

        // Define the scale animation
        val scale by transition.animateFloat(
            transitionSpec = {
                tween(durationMillis = 300, easing = FastOutSlowInEasing)
            },
            label = "ScaleAnimation"
        ) { playing ->
            if (playing) 1f else 0.95f
        }

        Card(shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    val pageOffset =
                        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                    // Calculate the base scale based on whether it's the current page or not
                    val baseScale = lerp(
                        start = 0.95f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    // Apply the animated scale only to the current page
                    // Other pages follow the normal pager scaling
                    val targetScale = if (page == pagerState.currentPage) {
                        baseScale * scale
                    } else {
                        baseScale
                    }

                    // Apply the calculated scale
                    scaleX = targetScale
                    scaleY = targetScale

                    // Keep the alpha calculation the same
                    alpha = lerp(
                        start = 0.95f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberAsyncImagePainter(
                    model = playerController.tracks[page].getSongImage() ?: ""
                ),
                contentDescription = "",
                contentScale = ContentScale.Crop,
            )
        }
    }
}