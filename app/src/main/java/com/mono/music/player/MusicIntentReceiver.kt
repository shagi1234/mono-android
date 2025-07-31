package com.mono.music.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer



 class MyDeviceCallback(val player: ExoPlayer) : AudioDeviceCallback() {
    override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
        Log.e("LOG_TAG", "onAudioDevicesAdded(): New devices detected")
    }

    override fun onAudioDevicesRemoved(devices: Array<AudioDeviceInfo>) {
        Log.e("LOG_TAG", "onAudioDevicesAdded(): devices removed")
        if (player.isPlaying) player.pause()
    }


}