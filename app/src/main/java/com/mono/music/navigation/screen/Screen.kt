package com.mono.music.navigation.screen


sealed class Screen(
    val route: String
) {

    object Home : Screen(
        "home"
    )


    object Search : Screen(
        "search"
    )

    object MyPlaylist : Screen(
        "my_playlist"
    )


    object Login : Screen(
        "login"
    )

    object Artist : Screen(
        "artist"
    )


    object Playlist : Screen(
        "playlist"
    )

    object LocalPlaylist : Screen(
        "local_playlist"
    )


    object Settings : Screen(
        "playlist"
    )

    object Songs : Screen(
        "songs"
    )

    object Albums : Screen(
        "albums"
    )

}