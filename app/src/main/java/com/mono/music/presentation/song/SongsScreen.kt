package com.mono.music.presentation.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.di.DataModule.Companion.BASE_URL
import com.mono.music.domain.models.Song
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.NotFoundView
import com.mono.music.ui.components.SwipeableSongView
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.ScreenTransition
import com.mono.music.ui.utils.ShareUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Destination(style = ScreenTransition::class)
@Composable
fun SongsScreen(
    title:String? = null,
    artistId: Long,
    isTop: Int,
    isSingle: Int,
    navigator: DestinationsNavigator
) {

    val songsViewModel = hiltViewModel<SongsViewModel>()
    val context = LocalContext.current
    val uiState by songsViewModel.uiState.collectAsState(BaseUIState())

    LaunchedEffect(artistId) {
        songsViewModel.setArtistId(artistId)
        songsViewModel.setIsSingle(isSingle)
        songsViewModel.setIsTop(isTop)
    }


    var settingsClicked by remember {
        mutableStateOf(false)
    }

    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }
    val playlists by songsViewModel.getAllPlaylists().collectAsState(initial = emptyList())

    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.successfully_added)
    val addToQueueMessage = stringResource(id = R.string.successfully_added)

    val songs = songsViewModel.songs.collectAsLazyPagingItems()

    LaunchedEffect(uiState.message){
        if (!uiState.message.isNullOrEmpty()){
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.message!!
                )
            }
            songsViewModel.updateToDefault()
        }
    }


    Scaffold(modifier = Modifier
        .fillMaxSize()
        .background(
            AlbumCoverBlackBG
        ),

        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive,
                    contentColor = WhiteTextColor,
                    snackbarData = data
                )
            }
        },
        topBar = {

            CollapsingSmallTopAppBar(
                title = title ?: stringResource(id = R.string.songs)
            ) {
                navigator.navigateUp()
            }

        }
    ) { padding ->

        if (songs.itemCount == 0 && songs.loadState.refresh == LoadState.Loading) {
            LoadingView(Modifier.fillMaxSize())
        } else if (songs.itemCount == 0 && songs.loadState.refresh is LoadState.Error) {
            NetworkErrorView(Modifier.fillMaxSize()) {
                songs.retry()
                songs.refresh()
            }
        } else if (songs.itemCount == 0) {
            NotFoundView(Modifier.fillMaxSize())
        } else {

            LazyColumn(modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(songs) { song ->

                    SwipeableSongView(song = song!!,
                        playerController = songsViewModel.getPlayerController(),
                        onMoreClicked = {
                            songsViewModel.selectedSong = it
                            settingsClicked = true
                        },
                        downloadTracker = songsViewModel.getDownloadTracker(),

                        onSwipe = {
                            songsViewModel.getPlayerController().onPlayNext(song)
                            scope.launch {
                                snackbarHostState.showSnackbar(addToQueueMessage)
                            }
                        }
                    ) {
                        songsViewModel.getPlayerController().init(song, songs.itemSnapshotList.items)
                    }
                }
            }

        }


        if (settingsClicked) {
            TrackBottomSheet(
                selectedSong = songsViewModel.selectedSong,
                songSettingsSheetState = songSettingsSheetState,
                onAddToPlaylist = {
                    settingsClicked = false
                    addToPlaylistClicked = true
                },
                onNavigateToAlbum = {
                    songsViewModel.selectedSong.albumId?.let {
                        navigator.navigate(
                            PlaylistScreenDestination(it, MainActivity.ALBUM)
                        )
                    }
                },
                onNavigateToArtist = {
                    if ( songsViewModel.selectedSong.artists.size == 1) {
                        songsViewModel.selectedSong.getArtist().id.let { navigator.navigate(
                            ArtistScreenDestination(it)
                        )}
                    } else {
                        showArtistDialog = true
                    }
                },
                onPlayNext = {
                    songsViewModel.getPlayerController().selectedTrack?.let {
                        songsViewModel.getPlayerController().onPlayNext(
                            it
                        )
                    }
                },
                onShare = {
                    ShareUtils.shareLink(context = context, link = BASE_URL )
                },
            ) {
                scope.launch {
                    songSettingsSheetState.hide()
                    settingsClicked = false
                }
            }
        }




        if (addToPlaylistClicked) {
            AddToPlaylistBottomSheet(
                selectedSong =  songsViewModel.selectedSong,
                playlists = playlists,
                playlistSheetState = playlistSheetState,
                onCreateNewPlaylist = {
                    showNewPlaylistDialog = true
                },
                onSelect = {  playlist, song ->
                    songsViewModel.addSongToPlaylist( songsViewModel.selectedSong, playlist)
                }
            ) {
                scope.launch {
                    playlistSheetState.hide()
                    addToPlaylistClicked = false
                }
            }
        }

        if (showNewPlaylistDialog) {
            Dialog(onDismissRequest = { showNewPlaylistDialog = false }) {
                NewPlaylistDialog() { name ->
                    showNewPlaylistDialog = false
                    songsViewModel.addNewPlaylist(name)
                }
            }

        }

        if (showArtistDialog) {
            ArtistBottomSheet(
                selectedSong =  songsViewModel.selectedSong,
                artists =  songsViewModel.selectedSong.artists,
                sheetState = artistsSheetState,
                onSelect = { artist -> navigator.navigate(ArtistScreenDestination(artist.id)) },
                onDismiss = { showArtistDialog = false }

            )

        }

        if (uiState.isPending) {
            Dialog(
                onDismissRequest = { },
            ) {
                LoadingView(
                    Modifier.fillMaxSize()
                )
            }
        }
    }
}