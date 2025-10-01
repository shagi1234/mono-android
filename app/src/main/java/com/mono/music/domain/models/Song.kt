package com.mono.music.domain.models

import android.net.Uri
import android.util.Log
import androidx.annotation.Keep
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mono.music.di.DataModule.Companion.BASE_URL
import com.mono.music.player.PlayerStates
import java.lang.Exception
import java.util.Date
import javax.annotation.concurrent.Immutable
import androidx.core.net.toUri

@Entity
@Immutable
data class Song(
    @SerializedName("id")
    @PrimaryKey val songId: Long,
    val name: String,
    val artists: List<Artist> = emptyList(),
    val image: String = "",
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val duration: Long = 0L,
    @SerializedName("album_id")
    val albumId: Long? = null,
    @SerializedName("album_name")
    val albumName: String? = "",
    @SerializedName("album_year")
    val albumYear: Long? = 1999,
    val year: Long = 1999,
    val audio: String = "",
    var dateAdded: Long = 0,
    var isSelected: Boolean = false,
    var isDownloaded: Boolean = false,
    var state: PlayerStates? = PlayerStates.STATE_IDLE,
) {

    fun getArtistsName(): String {
        val names = artists.map { it.name }.toMutableList()
        return names.joinToString(separator = ", ")
    }

    fun getArtist(): Artist {

        return artists[0]
    }

    fun getSongImage(): String {
        return image
    }

    fun getSongUrl(): String {
        var hslUrl = audio
        try{
            hslUrl = audio.removeSuffix(".mp3") + ".m3u8"
        }catch (e:Exception){
            Log.e("TAG", "getSongUrl: "+e.message )
        }

        return hslUrl
    }


    fun isPlaying(): Boolean {
        return state == PlayerStates.STATE_READY
                || state == PlayerStates.STATE_BUFFERING
                || state == PlayerStates.STATE_PLAYING


    }

    fun isBuffering(): Boolean {
        return state == PlayerStates.STATE_IDLE
                || state == PlayerStates.STATE_BUFFERING
                || state == PlayerStates.STATE_READY


    }

    @UnstableApi
    fun toMediaItem(): MediaItem {
        val mediaMetaData = MediaMetadata.Builder()
            .setArtworkUri(getSongImage().toUri())
            .setTitle(name)
            .setDescription(getArtistsName())
            .setAlbumArtist(getArtistsName())
            .setArtist(getArtistsName())
            .setDurationMs(duration)
            .build()

        val trackUri = getSongUrl().toUri()
        return MediaItem.Builder()
            .setUri(trackUri)
            .setMediaId(songId.toString())
            .setMediaMetadata(mediaMetaData)
            .build()
    }


}

