package com.mono.music.domain.service

import android.provider.ContactsContract.Profile
import com.mono.music.domain.models.Artist
import com.mono.music.domain.models.CodeVerification
import com.mono.music.domain.models.MainScreenData
import com.mono.music.domain.models.Message
import com.mono.music.domain.models.Paging
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistAction
import com.mono.music.domain.models.RequestLikeSong
import com.mono.music.domain.models.ResponseSong
import com.mono.music.domain.models.SearchData
import com.mono.music.domain.models.Song
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/main/")
    suspend fun getMainScreenData(): MainScreenData

    @GET("api/search/")
    suspend fun search(
        @Query("s") s: String,
        @Query("search_type") type: String
    ): SearchData

    @GET("api/artists/{id}/")
    suspend fun getArtist(
        @Path("id") id: Long
    ): Artist

    @GET("api/{type}/{id}/")
    suspend fun getPlaylist(
        @Path("type") type: String,
        @Path("id") id: Long
    ): Playlist

    @GET("api/my-playlists/{id}/")
    suspend fun getLocalPlaylist(
        @Path("id") id: Long
    ): Playlist

    @GET("/api/albums/")
    suspend fun getAlbums(
        @Query("page") page: Int,
        @Query("artist") artistId: Long
    ): Paging<Playlist>

    @GET("/api/songs/")
    suspend fun getSongs(
        @Query("page") page: Int,
        @Query("artist") artistId: Long,
        @Query("is_top") isTop: Int,
        @Query("is_single") isSingle: Int,
    ): Paging<Song>


    @GET("api/liked-songs")
    suspend fun favoriteSongs(): Paging<ResponseSong>

    @POST("api/like-song")
    suspend fun likeSong(
        @Body body: RequestLikeSong
    ): Message

    @POST("api/playlist-to-library/")
    suspend fun playlistToLibrary(
        @Body body: PlaylistAction
    ): Message

    @POST("api/album-to-library/")
    suspend fun albumToLibrary(
        @Body body: PlaylistAction
    ): Message


    @POST("api/custom-playlist-to-library/")
    suspend fun customPlaylistToLibrary(
        @Body body: PlaylistAction
    ): Playlist

    @POST("api/song-to-playlist/")
    suspend fun songToPlaylist(
        @Body body: PlaylistAction
    ): Message

    @GET("/api/my-playlists")
    suspend fun getMyPlaylists(
        @Query("page") page: Int,
    ): Paging<Playlist>

    @GET("api/similar-playlists/{id}/")
    suspend fun getSimilarPlaylist(
        @Path("id") id: Long,
    ): List<Playlist>

    @GET("api/similar-artists/{id}/")
    suspend fun getSimilarArtists(
        @Path("id") id: Long,
    ): List<Artist>

    @GET("api/similar-albums/{id}/")
    suspend fun getSimilarAlbums(
        @Path("id") id: Long,
    ): List<Playlist>

    @GET("api/listened-song")
    suspend fun listenedSong(
        @Query("song_id") songId: Long
    ): Message


}