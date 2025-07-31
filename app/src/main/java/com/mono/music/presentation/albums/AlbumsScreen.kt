package com.mono.music.presentation.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.home.HomeViewModel
import com.mono.music.ui.components.AlbumGridView
import com.mono.music.ui.components.AlbumView
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.NotFoundView
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@Composable
@Destination(style = ScreenTransition::class)
fun AlbumsScreen(
    artistId: Long,
    navigator: DestinationsNavigator,
) {

    val albumsViewModel = hiltViewModel<AlbumsViewModel>()

    LaunchedEffect(artistId) {
        albumsViewModel.setArtistId(artistId)
    }

    val albums = albumsViewModel.albums.collectAsLazyPagingItems()

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .background(
            AlbumCoverBlackBG
        ),
        topBar = {

            CollapsingSmallTopAppBar(
                title = stringResource(id = R.string.albums)
            ) {
                navigator.navigateUp()
            }

        }
    ) { padding ->

        if (albums.itemCount == 0 && albums.loadState.refresh == LoadState.Loading) {
            LoadingView(Modifier.fillMaxSize())
        } else if (albums.itemCount == 0 && albums.loadState.refresh is LoadState.Error) {
            NetworkErrorView(Modifier.fillMaxSize()) {
                albums.retry()
                albums.refresh()
            }
        } else if (albums.itemCount == 0) {
            NotFoundView(Modifier.fillMaxSize())
        } else {
            LazyVerticalGrid(
                modifier =Modifier.padding(top = padding.calculateTopPadding()),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp)
            ) {
                items(albums.itemCount) { index ->
                    val album = albums[index]!!

                    AlbumGridView(album = album) {
                        navigator.navigate(
                            PlaylistScreenDestination(album.playlistId, MainActivity.ALBUM)
                        )
                    }

                }


            }
        }
    }
}