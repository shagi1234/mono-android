package com.mono.music.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.mono.music.R
import com.mono.music.ui.utils.disabledVerticalPointerInputScroll

@Composable
fun PlaceholderImage() {
    Image(
        painter = painterResource(id = R.drawable.placeholder),
        contentDescription = null
    )
}

@Composable
fun ImageGrid(
    imageUrls: List<String?> = emptyList(), columns: Int = 2, fraction: Float, isSmall: Boolean = false
) {
    when (imageUrls.size) {
        0, 1 -> {
            var isLoading by remember { mutableStateOf(true) }
            val alpha by animateFloatAsState(
                targetValue = if (isLoading) 0f else fraction,
                animationSpec = tween(durationMillis = 500), label = "fadeEffect"
            )

            SubcomposeAsyncImage(
                modifier = Modifier
                    .then(if (isSmall) Modifier.size(60.dp) else Modifier.fillMaxSize())
                    .graphicsLayer { this.alpha = alpha },
                model = if (imageUrls.isNotEmpty()) imageUrls.first() else null,
                loading = {
                    isLoading = true
                    AnimatedVisibility(visible = isLoading) {
                        PlaceholderImage()
                    }
                },
                onSuccess = { isLoading = false },
                error = {
                    isLoading = false
                    PlaceholderImage()
                },
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                userScrollEnabled = false,
                modifier = Modifier.then(
                    if (isSmall) Modifier.size(60.dp) else Modifier.fillMaxSize()
                ).disabledVerticalPointerInputScroll(),
            ) {
                items(imageUrls) { imageUrl ->
                    var isLoading by remember { mutableStateOf(true) }
                    val alpha by animateFloatAsState(
                        targetValue = if (isLoading) 0f else fraction,
                        animationSpec = tween(durationMillis = 500), label = "fadeEffect"
                    )

                    SubcomposeAsyncImage(
                        modifier = Modifier.aspectRatio(1f).alpha(alpha),
                        model = imageUrl,
                        loading = {
                            isLoading = true
                            AnimatedVisibility(visible = isLoading) {
                                PlaceholderImage()
                            }
                        },
                        onSuccess = { isLoading = false },
                        error = {
                            isLoading = false
                            PlaceholderImage()
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
