package com.mono.music.ui.components

import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.HapticType
import com.mono.music.ui.utils.LIGHT_PRESS
import com.mono.music.ui.utils.MEDIUM_PRESS
import com.mono.music.ui.utils.clickWithoutIndication
import com.mono.music.ui.utils.rememberHaptic
import com.mono.music.ui.utils.scaleButtonClickable
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//@Composable
//fun CustomButton(
//    modifier: Modifier,
//    text: Int?=null,
//    containerColor: Color= Yellow,
//    contentColor: Color= AlbumCoverBlackBG,
//    enabled: Boolean = true,
//    leadingIcon: Int? = null,
//    trailingIcon: Int? = null,
//    shape: Shape = MaterialTheme.shapes.large,
//    onClick: () -> Unit,
//
//    ) {
//    val interactionSource = remember { MutableInteractionSource() }
//
//    val isPressed by interactionSource.collectIsPressedAsState()
//
//
//    val scale by animateFloatAsState(
//        targetValue = if (isPressed) 0.95f else 1f, // например, уменьшаем до 95%
//        animationSpec = tween(durationMillis = 120), // скорость анимации
//        label = "scale"
//    )
//    val haptic=  rememberHaptic()
//
//
//
//    Button(
//        modifier = modifier.scale(scale),
//        shape = shape,
//        onClick = {
//            haptic(HapticType.MEDIUM)
//            onClick()
//        },
//        enabled = enabled,
//        contentPadding = PaddingValues(vertical = 16.dp),
//        interactionSource = interactionSource,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = containerColor,
//            contentColor = contentColor,
//            disabledContentColor = contentColor.copy(alpha = 0.6f),
//            disabledContainerColor = containerColor,
//        )
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//
//            leadingIcon?.let {
//                Icon(
//                    painter = painterResource(id = leadingIcon),
//                    tint = contentColor,
//                    contentDescription = "Shuffle"
//                )
//            }
//
//
//        text?.let {
//            Text(
//                text = stringResource(id = text),
//                color = contentColor,
//                fontWeight = FontWeight.Bold,
//                fontFamily = SFFontFamily
//            )
//
//        }
//            trailingIcon?.let {
//                Icon(
//                    painter = painterResource(id = trailingIcon),
//                    tint = contentColor,
//                    contentDescription = "Shuffle"
//                )
//            }
//        }
//
//    }
//}

@Composable
fun CustomMiniButton(
    modifier: Modifier = Modifier,
    text: Int? = null,
    containerColor: Color = Inactive,
    contentColor: Color = Yellow,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    onClick: () -> Unit,
) {
    Button(
        modifier = if (text == null) modifier
            .scaleIconClickable(onClick = onClick)
            .width(40.dp) else modifier.scaleIconClickable(onClick = onClick),
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        contentPadding = if (text != null) PaddingValues(
            12.dp,
            0.dp,
            20.dp,
            0.dp
        ) else PaddingValues(0.dp),
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = containerColor,
            disabledContainerColor = containerColor,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            leadingIcon?.let {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    painter = painterResource(id = leadingIcon),
                    tint = contentColor,
                    contentDescription = "Shuffle"
                )
            }

            text?.let {
                Text(
                    text = stringResource(id = text),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFFontFamily
                )
            }
        }
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    text: Int? = null,
    containerColor: Color = Yellow,
    contentColor: Color = AlbumCoverBlackBG,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    shape: Shape = MaterialTheme.shapes.large,
    hapticType: HapticType = HapticType.MEDIUM,
    scaleValue: Float = MEDIUM_PRESS,
    onClick: () -> Unit,
) {

    val haptic = rememberHaptic()

    var isManualPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val automaticPressed by interactionSource.collectIsPressedAsState()

    val isPressed = automaticPressed || isManualPressed


    val scaleAnimatable = remember { Animatable(1f) }

    val animatedContainerColor by animateColorAsState(
        targetValue = when {
            !enabled -> containerColor.copy(alpha = 0.6f)
            isPressed -> {
                // More subtle press effect
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(containerColor.toArgb(), hsv)
                hsv[2] *= 0.92f // Less dramatic darkening
                Color(android.graphics.Color.HSVToColor(hsv))
            }
            else -> containerColor
        },
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "container_color"
    )

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnimatable.animateTo(
                targetValue = scaleValue,
                animationSpec =  tween(durationMillis = 100, easing = EaseInCubic)
            )
        } else {
            scaleAnimatable.animateTo(
                targetValue = 1f,
                animationSpec =  tween(durationMillis = 200, easing = EaseOutCubic)
            )
            // Выполняем действие ПОСЛЕ завершения анимации
            pendingAction?.invoke()
            pendingAction = null
        }
    }






    Button(
        onClick = {
            haptic(hapticType)
            isManualPressed = true
            haptic(hapticType)

            pendingAction = onClick
            kotlinx.coroutines.MainScope().launch {
                delay(150)
                isManualPressed = false
            }
        },
        modifier = modifier
            .scale(scaleAnimatable.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isManualPressed = true
                        tryAwaitRelease()
                        isManualPressed = false
                    }
                )
            },
        shape = shape,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = contentColor,
            disabledContainerColor = animatedContainerColor,
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            leadingIcon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = contentColor
                )
            }

            text?.let {
                Text(
                    text = stringResource(it),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFFontFamily
                )
            }

            trailingIcon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}

//
////@OptIn(ExperimentalComposeUiApi::class)
////@Composable
////fun CustomMiniButton(
////    modifier: Modifier = Modifier,
////    text: Int? = null,
////    containerColor: Color = Inactive,
////    contentColor: Color = Yellow,
////    enabled: Boolean = true,
////    leadingIcon: Int? = null,
////    shape: Shape = MaterialTheme.shapes.extraSmall,
////    hapticType: HapticType = HapticType.LIGHT, // Default to lighter haptic for mini buttons
////    onClick: () -> Unit,
////
////    ) {
////    val isPressed = remember { mutableStateOf(false) }
////    val haptic = rememberHaptic()
//
//    // Scale animation
//    val scale = animateFloatAsState(
//        targetValue = if (isPressed.value) 0.95f else 1f, // Slightly more scale for mini buttons
//        label = "scale"
//    )
//
//    // HSV darkening animation
//    val animatedContainerColor = animateColorAsState(
//        targetValue = if (isPressed.value && enabled) {
//            val hsv = FloatArray(3)
//            android.graphics.Color.colorToHSV(containerColor.toArgb(), hsv)
//            hsv[2] = hsv[2] * 0.85f // Slightly more darkening for mini buttons
//            Color(android.graphics.Color.HSVToColor(hsv))
//        } else if (!enabled) {
//            containerColor.copy(alpha = 0.6f)
//        } else {
//            containerColor
//        },
//        label = "container_color"
//    )
//
//    Button(
//        modifier = (if (text == null) modifier.width(40.dp) else modifier)
//            .scale(scale.value)
//            .pointerInput(enabled) {
//                detectTapGestures(
//                    onPress = {
//                        if (enabled) {
//                            isPressed.value = true
//                            haptic(hapticType)
//                            tryAwaitRelease()
//                            isPressed.value = false
//                        }
//                    }
//                )
//            },
//        shape = shape,
//        onClick = onClick,
//        enabled = enabled,
//        contentPadding = if (text != null) PaddingValues(
//            12.dp,
//            0.dp,
//            20.dp,
//            0.dp
//        ) else PaddingValues(0.dp),
//        colors = ButtonColors(
//            containerColor = animatedContainerColor.value,
//            contentColor = contentColor,
//            disabledContentColor = contentColor.copy(alpha = 0.6f),
//            disabledContainerColor = animatedContainerColor.value,
//        ),
//        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 0.dp,
//            pressedElevation = 0.dp
//        )
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//
//            leadingIcon?.let {
//                Icon(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(id = leadingIcon),
//                    tint = contentColor,
//                    contentDescription = "Icon"
//                )
//            }
//
//            text?.let {
//                Text(
//                    text = stringResource(id = text),
//                    color = contentColor,
//                    fontWeight = FontWeight.Bold,
//                    fontFamily = SFFontFamily
//                )
//            }
//        }
//    }
//}
