package com.mono.music.domain.models

data class LatestRelease(
    val song: Song?,
    val album: Playlist?,
) {

    fun isAlbum() = album != null

    fun isSong() = song != null

    fun getName(): String {
       return album?.name ?: song?.name ?: ""
    }

    fun getYear(): String {
        return (album?.year ?: song?.year ?: 2000).toString()
    }

    fun getImage(): String {
        return (album?.image ?: song?.image ?: "").toString()
    }
}
