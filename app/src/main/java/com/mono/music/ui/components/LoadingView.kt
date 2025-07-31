package com.mono.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mono.music.R

@Composable
fun LoadingView(
    modifier: Modifier,
) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.data)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        speed = 2f,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier,
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            composition,
            enableMergePaths = true,
            progress = progress,
            modifier = Modifier.size(200.dp)
        )
    }
}