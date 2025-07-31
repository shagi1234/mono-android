package com.mono.music.presentation.playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.rememberAsyncImagePainter
import com.mono.music.MainActivity.Companion.ALBUM
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.MainViewModel
import com.mono.music.R
import com.mono.music.di.DataModule
import com.mono.music.domain.models.Song
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.presentation.player.PlayerSourceInfo
import com.mono.music.ui.CollapsibleScaffold
import com.mono.music.ui.TopBar
import com.mono.music.ui.components.CustomMiniButton
import com.mono.music.ui.components.ImageGrid
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.SwipeableSongView
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.components.listview.AlbumListView
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.TransparentColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ScreenTransition
import com.mono.music.ui.utils.ShareUtils
import com.mono.music.ui.utils.clickWithoutIndication
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Destination(style = ScreenTransition::class)
@Composable
fun PlaylistScreen(
    id: Long,
    type: String,
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current

    val playlistViewModel = hiltViewModel<PlaylistViewModel>()

    LaunchedEffect(id, type) {
        playlistViewModel.setPlaylistIdAndType(id, type)
    }


    var settingsClicked by remember {
        mutableStateOf(false)
    }

    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }
    val playlists by playlistViewModel.getAllPlaylists().collectAsState(initial = emptyList())
    val playlistExists by playlistViewModel.playlistExists().collectAsState(initial = false)

    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val uiState by playlistViewModel.uiState.collectAsState()

    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.successfully_added)
    val addToQueueMessage = stringResource(id = R.string.successfully_added)

    val listState = rememberLazyListState()

    val configuration = LocalConfiguration.current
    val screenWidth = (configuration.screenWidthDp * 1.1).dp

    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.message!!
                )
            }
            playlistViewModel.updateToDefault()
        }
    }


    Scaffold(

    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingView(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(Background)
                )

            }

            uiState.isFailure -> {
                NetworkErrorView(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(Background)
                ) {
                    playlistViewModel.getPlaylist(id, type)
                }
            }
        }

        CollapsibleScaffold(
            state = listState,
            topBarMaxHeight = screenWidth,
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
                TopBar(
                    modifier = Modifier.background(Color.Red),
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
                            imageUrls = uiState.data?.getPlaylistImage(true) ?: emptyList(),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 40.dp, vertical = 16.dp
                                        )
                                        .alpha(if (fraction < 0.27) 0f else fraction),
                                    text = uiState.data?.name ?: "",
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
                                        .clickWithoutIndication {
                                            if (uiState.data?.artists?.size == 1) {
                                                uiState.data?.getArtist()?.id?.let {
                                                    navigator.navigate(
                                                        ArtistScreenDestination(it)
                                                    )
                                                }
                                            }
                                        }
                                        .alpha(if (fraction < 0.27) 0f else fraction),
                                    text = uiState.data?.getArtistsName() ?: "",
                                    style = TextStyle(
                                        color = GrayTextColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }


                            Text(
                                modifier = Modifier
                                    .padding(
                                        start = 50.dp,
                                        top = 16.dp,
                                        bottom = 16.dp,
                                        end = 16.dp
                                    )
                                    .align(Alignment.BottomStart)
                                    .alpha(if (fraction < 0.1f) 1f else 0f),
                                text = uiState.data?.name ?: "",
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
                contentPadding = PaddingValues(
                    top = insets.calculateTopPadding(),
                    bottom = 100.dp
                )
            ) {
                if (uiState.isSuccess) {
                    item {
                        PlaylistButtonsRow(playlistExists = playlistExists, onPlay = {
                            uiState.data?.songs?.let { data ->
                                if (data.isNotEmpty()) {

                                    PlayerSourceInfo.setSourceInfo(
                                        id = uiState.data?.playlistId,
                                        type = type,
                                        name = uiState.data?.name,
                                        trackId = data[0].songId,
                                    )

                                    playlistViewModel.getPlayerController()
                                        .init(data[0], data)
                                }
                            }
                        }, onAddToLibrary = {
                            uiState.data?.let { playlistViewModel.savePlaylist(it) }
                        }, onShuffle = {
                            uiState.data?.songs?.let { data ->
                                if (data.isNotEmpty()) {

                                    val shuffledData = data.shuffled()

                                    PlayerSourceInfo.setSourceInfo(
                                        id = uiState.data?.playlistId,
                                        type = type,
                                        name = uiState.data?.name,
                                        trackId = shuffledData[0].songId,
                                    )

                                    playlistViewModel.getPlayerController()
                                        .init(0, shuffledData)
                                }
                            }
                        })
                    }
                }
                uiState.data?.songs?.let { data ->
                    itemsIndexed(
                        data,
                    ) { index, song ->

                        SwipeableSongView(
                            song = song,
                            index = index,
                            showsOrderNumber = type == "albums",
                            playerController = playlistViewModel.getPlayerController(),
                            onMoreClicked = {
                                playlistViewModel.selectedSong = it
                                settingsClicked = true

                            },
                            downloadTracker = playlistViewModel.getDownloadTracker(),

                            onSwipe = {
                                playlistViewModel.getPlayerController().onPlayNext(song)
                                scope.launch {
                                    snackbarHostState.showSnackbar(addToQueueMessage)
                                }
                            },
                            onClick = {
                                // Set source info before playing single song
                                PlayerSourceInfo.setSourceInfo(
                                    id = uiState.data?.playlistId,
                                    type = type,
                                    name = uiState.data?.name,
                                    trackId = song.songId,
                                )

                                // Start playback
                                playlistViewModel.getPlayerController().init(song, data)
                            },
                        )
                    }

                    item {
                        if (type == ALBUM) {
                            val relatedAlbums by playlistViewModel.relatedAlbums.collectAsState()

                            if (relatedAlbums.isNotEmpty()) {
                                val title = stringResource(id = R.string.more_albums)
                                Box(
                                    modifier = Modifier.padding(top = 20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = title,
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                        )

                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        ) {
                                            AlbumListView(
                                                playlists = relatedAlbums,
                                                expandable = false,
                                                showHeader = false,
                                            ) { album ->
                                                navigator.navigate(
                                                    PlaylistScreenDestination(
                                                        album.playlistId, ALBUM
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (type == PLAYLIST) {
                            val relatedPlaylists by playlistViewModel.relatedPlaylist.collectAsState()

                            if (relatedPlaylists.isNotEmpty()) {
                                val title = stringResource(id = R.string.more_playlists)
                                Box(
                                    modifier = Modifier.padding(top = 20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = title,
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                        )

                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        ) {
                                            AlbumListView(
                                                playlists = relatedPlaylists,
                                                expandable = false,
                                                showHeader = false,
                                            ) { album ->
                                                navigator.navigate(
                                                    PlaylistScreenDestination(
                                                        album.playlistId, PLAYLIST
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

            }



            if (settingsClicked) {
                TrackBottomSheet(
                    selectedSong = playlistViewModel.selectedSong,
                    songSettingsSheetState = songSettingsSheetState,
                    onAddToPlaylist = {
                        settingsClicked = false
                        addToPlaylistClicked = true
                    },
                    onNavigateToAlbum = {
                        playlistViewModel.selectedSong.albumId?.let {
                            navigator.navigate(
                                PlaylistScreenDestination(it, ALBUM)
                            )
                        }
                    },
                    onNavigateToArtist = {
                        if (playlistViewModel.selectedSong.artists.size == 1) {
                            playlistViewModel.selectedSong.getArtist().id.let {
                                navigator.navigate(
                                    ArtistScreenDestination(it)
                                )
                            }
                        } else {
                            showArtistDialog = true
                        }
                    },
                    onPlayNext = {
                        playlistViewModel.getPlayerController().selectedTrack?.let {
                            playlistViewModel.getPlayerController().onPlayNext(
                                it
                            )
                        }
                    },
                    onShare = {
                        ShareUtils.shareLink(context = context, link = DataModule.BASE_URL)
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
                    selectedSong = playlistViewModel.selectedSong,
                    playlists = playlists,
                    playlistSheetState = playlistSheetState,
                    onCreateNewPlaylist = {
                        showNewPlaylistDialog = true
                    },
                    onSelect = { playlist, song ->
                        playlistViewModel.addSongToPlaylist(
                            playlistViewModel.selectedSong,
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
                        playlistViewModel.addNewPlaylist(name)
                    }
                }

            }

            if (showArtistDialog) {
                ArtistBottomSheet(
                    selectedSong = playlistViewModel.selectedSong,
                    artists = playlistViewModel.selectedSong.artists,
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
}

@Composable
fun PlaylistButtonsRow(
    playlistExists: Boolean,
    downloadIcon: Int? = null,
    onPlay: () -> Unit,
    onAddToLibrary: () -> Unit,
    onShuffle: () -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        CustomMiniButton(
            text = R.string.play_all,
            onClick = onPlay,
            leadingIcon = R.drawable.play,
        )

        CustomMiniButton(
            onClick = onShuffle,
            contentColor = WhiteTextColor,
            leadingIcon = R.drawable.shuffle,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (downloadIcon != null) {
            CustomMiniButton(
                onClick = onAddToLibrary,
                contentColor = WhiteTextColor,
                leadingIcon = downloadIcon,
            )
        } else {
            if (playlistExists) {
                CustomMiniButton(
                    onClick = {},
                    contentColor = MaterialTheme.colorScheme.primary,
                    leadingIcon = R.drawable.library_add_check,
                )
            } else {
                CustomMiniButton(
                    onClick = onAddToLibrary,
                    contentColor = WhiteTextColor,
                    leadingIcon = R.drawable.library_add,
                )
            }
        }

    }
}

