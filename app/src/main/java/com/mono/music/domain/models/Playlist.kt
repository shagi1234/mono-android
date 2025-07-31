package com.mono.music.domain.models

import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.annotations.SerializedName
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.di.DataModule
import javax.annotation.concurrent.Immutable

@Entity
@Immutable
data class Playlist(
    @SerializedName("id")
    @PrimaryKey val playlistId: Long = 0L,
    val name: String,
    val artists: List<Artist>? = emptyList(),
    val songs: List<Song>? = emptyList(),
    val image: String? = "",
    @SerializedName("songs_count")
    val songsCount: Int = 0,
    val year: Int = 0,
    var type: String = PLAYLIST,
    val downloadable: Boolean = false,
    @SerializedName("is_album")
    var isAlbum: Boolean = false,
    @SerializedName("is_builtin_playlist")
    var isBuiltin: Boolean = false,
    var dateAdded: Long = 0,
) {
    fun getPlaylistImage(isBuiltin: Boolean = false): List<String?> {
        if (isBuiltin || this.isBuiltin) {
            return listOf(image);
        }

        if (this.songs.isNullOrEmpty()) return emptyList()

        return if (this.songs.size >= 4) {
            this.songs.subList(0, 4).map { it.getSongImage() }
        } else {
            listOf(this.songs[0].getSongImage())
        }
    }

    fun getArtistsName(): String {
        if (artists == null) return ""
        val names = artists.map { it.name }.toMutableList()
        return names.joinToString(separator = ", ")
    }

    fun getArtist(): Artist? {
        if (artists?.size == 1) return artists[0]
        else return null
    }

}

