package com.mono.music.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * iOS-like haptic feedback types
 */
enum class HapticType {
    LIGHT,      // Light tap - like iOS impactOccurred(.light)
    MEDIUM,     // Medium tap - like iOS impactOccurred(.medium)
    HEAVY,      // Heavy tap - like iOS impactOccurred(.heavy)
    SUCCESS,    // Success notification - like iOS notificationOccurred(.success)
    WARNING,    // Warning notification - like iOS notificationOccurred(.warning)
    ERROR,      // Error notification - like iOS notificationOccurred(.error)
    SELECTION   // Selection change - like iOS selectionChanged()
}

/**
 * Universal haptic feedback class that works on all Android versions
 */
class UniversalHaptic(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }


    fun performHaptic(type: HapticType) {
        if (vibrator?.hasVibrator() != true) return
        performModernHaptic(type)
    }

    private fun performModernHaptic(type: HapticType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.WARNING -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.ERROR -> createErrorPattern()
                HapticType.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            }
            vibrator?.vibrate(effect)
        }
    }



    @Suppress("DEPRECATION")
    private fun performLegacyVibration(type: HapticType) {
        val duration = when (type) {
            HapticType.LIGHT -> 25L
            HapticType.MEDIUM -> 50L
            HapticType.HEAVY -> 100L
            HapticType.SUCCESS -> 75L
            HapticType.WARNING -> 100L
            HapticType.ERROR -> {
                // Double vibration for error
                vibrator?.vibrate(longArrayOf(0, 50, 50, 50), -1)
                return
            }
            HapticType.SELECTION -> 20L
        }
        vibrator?.vibrate(duration)
    }

    private fun createErrorPattern(): VibrationEffect {
        return VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    private fun createErrorPatternLegacy(): VibrationEffect {
        return  VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
    }
}

/**
 * Compose hook for universal haptic feedback
 */
@Composable
fun rememberUniversalHaptic(): UniversalHaptic {
    val context = LocalContext.current
    return remember { UniversalHaptic(context) }
}

/**
 * Simple extension function for easy one-liner usage
 */
@Composable
fun rememberHaptic(): (HapticType) -> Unit {
    val haptic = rememberUniversalHaptic()
    return remember { { type -> haptic.performHaptic(type) } }
}