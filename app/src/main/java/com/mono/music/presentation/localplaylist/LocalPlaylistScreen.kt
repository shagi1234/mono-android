package com.mono.music.presentation.localplaylist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.di.DataModule
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.PlaylistWithSongs
import com.mono.music.domain.models.Song
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.myplaylist.MyPlaylistsViewModel
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.presentation.playlist.PlaylistButtonsRow
import com.mono.music.ui.CollapsibleScaffold
import com.mono.music.ui.TopBar
import com.mono.music.ui.components.ImageGrid
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.SwipeableSongView
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.TransparentColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.ScreenTransition
import com.mono.music.ui.utils.ShareUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination(style = ScreenTransition::class)
fun LocalPlaylistScreen(
    id: Long,
    isFavorites:Boolean,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current

    val localPlaylistViewModel = hiltViewModel<LocalPlaylistViewModel>()

//    val playlist by localPlaylistViewModel.getPlaylist(id, isFavorites).collectAsState(initial = null)


    val playlist by remember(id, isFavorites) {
        localPlaylistViewModel.getPlaylist(id, isFavorites)
    }.collectAsState(initial = null)

    val uiState by localPlaylistViewModel.uiState.collectAsState()


    var settingsClicked by remember {
        mutableStateOf(false)
    }
    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }

    val playlists by localPlaylistViewModel.getAllPlaylists().collectAsState(initial = emptyList())

    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }


    var showLoading by rememberSaveable {
        mutableStateOf(false)
    }


    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addToQueueMessage = stringResource(id = R.string.successfully_added)

    val downloadIcon =
        if (playlist?.playlist?.downloadable != true) R.drawable.ic_download else R.drawable.ic_downloaded

    val listState = rememberLazyListState()

    val configuration = LocalConfiguration.current
    val screenWidth = (configuration.screenWidthDp * 1.1).dp
    LaunchedEffect(Unit) {
        if (!isFavorites)
            localPlaylistViewModel.refreshPlaylist(id)
    }



    LaunchedEffect(uiState.isLoading) {
        showLoading = uiState.isLoading
    }

//    LaunchedEffect(uiState.message) {
//        if (!uiState.message.isNullOrEmpty()) {
//            scope.launch {
//                snackbarHostState.showSnackbar(
//                    uiState.message!!
//                )
//            }
//            localPlaylistViewModel.updateToDefault()
//        }
//    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                AlbumCoverBlackBG
            ), snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive, contentColor = WhiteTextColor, snackbarData = data
                )
            }
        }) { padding ->
        CollapsibleScaffold(state = listState, topBarMaxHeight = screenWidth, snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive,
                    contentColor = WhiteTextColor,
                    snackbarData = data
                )
            }
        }, topBar = {
            TopBar(
                onBack = {
                    navigator.navigateUp()
                },
            ) {
                val fraction = this.fraction
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                ) {
                    ImageGrid(
                        imageUrls = playlist?.getPlaylistImage() ?: emptyList(),
                        fraction = fraction
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        TransparentColor, Background
                                    )
                                )
                            ), contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(
                                    horizontal = 40.dp, vertical = 16.dp
                                )
                                .alpha(if (fraction < 0.27) 0f else fraction),
                            text = playlist?.playlist?.name ?: "",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            modifier = Modifier
                                .padding(
                                    start = 50.dp, top = 16.dp, bottom = 16.dp, end = 16.dp
                                )
                                .align(Alignment.BottomStart)
                                .alpha(if (fraction < 0.1f) 1f else 0f),
                            text = playlist?.playlist?.name ?: "",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            textAlign = TextAlign.Start,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                }
            }
        }) { insets ->
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = insets.calculateTopPadding(), bottom = 100.dp)
            ) {
                item {
                    PlaylistButtonsRow(
                        playlistExists = true,
                        downloadIcon = downloadIcon,
                        onPlay = {
                            playlist?.songs?.let { data ->

                                Log.e("SONGSSSS___", "LocalPlaylistScreen: $data", )
                                if (data.isNotEmpty()) {
                                    localPlaylistViewModel.getPlayerController().init(data[0], data)
                                }

                            }
                        },
                        onAddToLibrary = {
                            playlist?.let { playlist ->
                                localPlaylistViewModel.updateDownloadStatus(
                                    playlist, !playlist.playlist.downloadable
                                )
                            }
                        },
                        onShuffle = {
                            playlist?.songs?.let { data ->
                                if (data.isNotEmpty()) {
                                    localPlaylistViewModel.getPlayerController()
                                        .init(0, data.shuffled())
                                }

                            }
                        })
                }
                playlist?.songs?.let { data ->
                    itemsIndexed(data) { index, song ->
                        SwipeableSongView(
                            index = index,
                            showsOrderNumber = playlist?.playlist?.type == "albums",
                            song = song,
                            playerController = localPlaylistViewModel.getPlayerController(),
                            downloadTracker = localPlaylistViewModel.getDownloadTracker(),
                            onMoreClicked = {
                                localPlaylistViewModel.selectedSong = it
                                settingsClicked = true
                            },
                            onSwipe = {
                                localPlaylistViewModel.getPlayerController().onPlayNext(song)
                                scope.launch {
                                    snackbarHostState.showSnackbar(addToQueueMessage)
                                }
                            }) {
                            localPlaylistViewModel.getPlayerController().init(song, data)

                        }
                    }
                }

            }

            if (settingsClicked) {
                TrackBottomSheet(
                    deletable = playlist?.playlist?.isBuiltin == false,
                    selectedSong = localPlaylistViewModel.selectedSong,
                    songSettingsSheetState = songSettingsSheetState,
                    onAddToPlaylist = {
//                        settingsClicked = false
                        addToPlaylistClicked = true
                    },
                    onNavigateToAlbum = {
                        localPlaylistViewModel.selectedSong.albumId?.let {
                            navigator.navigate(
                                PlaylistScreenDestination(it, MainActivity.ALBUM)
                            )
                        }
                    },
                    onNavigateToArtist = {
                        if (localPlaylistViewModel.selectedSong.artists.size == 1) {
                            localPlaylistViewModel.selectedSong.getArtist().id.let {
                                navigator.navigate(
                                    ArtistScreenDestination(it)
                                )
                            }
                        } else {
                            showArtistDialog = true
                        }
                    },
                    onPlayNext = {
                        localPlaylistViewModel.getPlayerController().selectedTrack?.let {
                            localPlaylistViewModel.getPlayerController().onPlayNext(
                                it
                            )
                        }
                    },
                    onShare = {
                        ShareUtils.shareLink(context = context, link = DataModule.BASE_URL)
                    },
                    onDelete = {
                        scope.launch {
                            delay(1000)
                            playlist?.let { playlist ->
                                localPlaylistViewModel.deleteSongFromPlaylist(
                                    playlist.playlist, localPlaylistViewModel.selectedSong
                                )
                            }
                        }
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
                    selectedSong = localPlaylistViewModel.selectedSong,
                    playlists = playlists,
                    playlistSheetState = playlistSheetState,
                    onCreateNewPlaylist = {
                        showNewPlaylistDialog = true
                    },
                    onSelect = { playlist, song ->
                        localPlaylistViewModel.addSongToPlaylist(
                            localPlaylistViewModel.selectedSong,
                            playlist
                        )
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
                        localPlaylistViewModel.addNewPlaylist(name)
                    }
                }
            }

            if (showArtistDialog) {
                ArtistBottomSheet(
                    selectedSong = localPlaylistViewModel.selectedSong,
                    artists = localPlaylistViewModel.selectedSong.artists,
                    sheetState = artistsSheetState,
                    onSelect = { artist -> navigator.navigate(ArtistScreenDestination(artist.id)) },
                    onDismiss = { showArtistDialog = false }

                )
            }

            if (showLoading){
                Dialog(
                    onDismissRequest = { },
                ) {
                    LoadingView(
                        Modifier.fillMaxSize()
                    )
                }
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
}
