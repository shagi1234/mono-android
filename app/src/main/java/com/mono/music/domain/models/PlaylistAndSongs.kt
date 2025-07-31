package com.mono.music.domain.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation


data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<Song>
) {
    fun getPlaylistImage(isBuiltin: Boolean = false): List<String?> {
        if (isBuiltin || this.playlist.isBuiltin) {
            return listOf(this.playlist.image);
        }

        if (this.songs.isEmpty()) return emptyList()

        return if (this.songs.size >= 4) {
            this.songs.subList(0, 4).map { it.getSongImage() }
        } else {
            listOf(this.songs[0].getSongImage())
        }
    }
}

data class SongWithPlaylists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "songId",
        entityColumn = "playlistId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val playlists: List<Playlist>
)