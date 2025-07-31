package com.mono.music

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.mono.music.player.PlaybackService
import com.mono.music.ui.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp



@HiltAndroidApp
class MyApplication : Application(){


    override fun onCreate() {
        super.onCreate()

    }

    override fun attachBaseContext(base: Context?) {
        val config = Resources.getSystem().configuration
        val locale = config.locales.get(0)
        base?.let {
            when (locale.language) {
                "ru" -> {
                    super.attachBaseContext(LocaleHelper.onAttach(base, "ru"))
                }
                "en" -> {
                    super.attachBaseContext(LocaleHelper.onAttach(base, "en"))
                }
                else -> {
                    super.attachBaseContext(LocaleHelper.onAttach(base, "tk"))
                }
            }
        }

    }
}