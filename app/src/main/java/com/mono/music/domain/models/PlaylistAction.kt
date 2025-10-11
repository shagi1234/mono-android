package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName

data class PlaylistAction(
    @SerializedName("playlist_id")
    var playlistId: Long? = null,
    @SerializedName("album_id")
    var albumId: Long? = null,
    @SerializedName("songs_id")
    var songsId: List<Long>? = null,
    val id: Long? = null,
    val action: String? = null,
    val name: String? = null,
) {
}

data class RequestLikeSong(
    val song: Long,
    val action: String?
)