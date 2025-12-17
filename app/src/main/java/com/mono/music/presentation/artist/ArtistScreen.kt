package com.mono.music.presentation.artist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.di.DataModule
import com.mono.music.domain.models.Song
import com.mono.music.presentation.artist.components.LatestReleaseView
import com.mono.music.presentation.destinations.AlbumsScreenDestination
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.destinations.SongsScreenDestination
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.ui.CollapsibleScaffold
import com.mono.music.ui.TopBar
import com.mono.music.ui.components.ArtistCircleItem
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.HeaderView
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.SwipeableSongView
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.components.listview.AlbumListView
import com.mono.music.ui.components.toolbar.rememberCollapsingToolbarScaffoldState
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.TransparentColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ScreenTransition
import com.mono.music.ui.utils.ShareUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Destination(style = ScreenTransition::class)
fun ArtistScreen(
    id: Long, navigator: DestinationsNavigator
) {

    val artistViewModel = hiltViewModel<ArtistViewModel>()

    val context = LocalContext.current

    LaunchedEffect(id) {
        artistViewModel.setID(id)
    }


    val uiState by artistViewModel.uiState.collectAsState()
    val similarArtists by artistViewModel.similarArtists.collectAsState()

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)

    val playlists by artistViewModel.getAllPlaylists().collectAsState(initial = emptyList())
    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var settingsClicked by remember {
        mutableStateOf(false)
    }

    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }


    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.getTop(density)


    val toolbarHeight = 100.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

    // our offset to collapse toolbar
    val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.value + delta
                toolbarOffsetHeightPx.value =
                    newOffset.coerceIn(-(2 * statusBarTop + toolbarHeightPx), 0f)
                return Offset.Zero
            }
        }
    }
    val state = rememberCollapsingToolbarScaffoldState()
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
            artistViewModel.updateToDefault()
        }
    }

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
                artistViewModel.getArtistDetail(id)
            }
        }
    }


    CollapsibleScaffold(state = listState, topBarMaxHeight = screenWidth, snackbarHost = {
        SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                containerColor = Inactive, contentColor = WhiteTextColor, snackbarData = data
            )
        }
    }, topBar = {
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
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = fraction
                        },
                    painter = rememberAsyncImagePainter(model = uiState.data?.getArtistImage()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = fraction
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                horizontal = 16.dp, vertical = 16.dp
                            )
                            .alpha(if (fraction < 0.27) 0f else fraction),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.data?.name ?: "",
                            style = TextStyle(
                                color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            textAlign = TextAlign.Start,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val isSubscribed = uiState.data?.isSubscribed == true
                        CustomButton(
                            text = if (isSubscribed) R.string.unsubscribe else R.string.subscribe,
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            onClick = {
                                artistViewModel.toggleSubscription()
                            }
                        )
                    }

                    Text(
                        modifier = Modifier
                            .padding(
                                start = 50.dp, top = 16.dp, bottom = 16.dp, end = 16.dp
                            )
                            .align(Alignment.BottomStart)
                            .alpha(if (fraction < 0.1f) 1f else 0f),
                        text = uiState.data?.name ?: "",
                        style = TextStyle(
                            color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                uiState.data?.let { data ->
                    if (data.songs.isNotEmpty()) {
                        CustomButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(60.dp)
                                .alpha(if (fraction < 0.3f) 0f else fraction),
                            shape = CircleShape,
                            contentColor = Color.Black,
                            containerColor = Color(0xFFFFA500), // Orange color
                            leadingIcon = R.drawable.play,
                        ) {
                            artistViewModel.getPlayerController().init(data.songs[0], data.songs)
                        }
//                        FloatingActionButton(
//                            onClick = {
//                                // Play all songs from the artist
//                                artistViewModel.getPlayerController().init(data.songs[0], data.songs)
//                            },
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
//                                .padding(16.dp)
//                                .size(60.dp)
//                                .alpha(if (fraction < 0.3f) 0f else fraction),
//                            shape = CircleShape,
//                            containerColor = Color(0xFFFFA500), // Orange color
//                            contentColor = Color.Black
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.PlayArrow,
//                                contentDescription = "Play All",
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
                    }
                }

            }
        }
    }) { insets ->

        LazyColumn(
            state = listState, contentPadding = PaddingValues(
                top = insets.calculateTopPadding(), bottom = 100.dp
            )
        ) {

            uiState.data?.let { data ->

                item {
                    if (data.hasLatestRelease()) {
                        LatestReleaseView(
                            latestRelease = data.latestRelease,
                            navigateToAlbum = { album ->
                                navigator.navigate(
                                    PlaylistScreenDestination(
                                        album.playlistId, MainActivity.ALBUM
                                    )
                                )
                            },
                            playSong = { song ->
                                val list = listOf(song)
                                artistViewModel.getPlayerController().init(list[0], list)
                            })
                    }

                }

                item {
                    if (data.songs.isNotEmpty()) {
                        val title = stringResource(id = R.string.top_songs)
                        HeaderView(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp
                            ), mainText = title, expandable = true
                        ) {
                            navigator.navigate(
                                SongsScreenDestination(
                                    title = title, artistId = data.id, isTop = 1, isSingle = 0
                                )
                            )
                        }

                    }

                }

                items(data.songs) { song ->
                    SwipeableSongView(
                        song = song,
                        playerController = artistViewModel.getPlayerController(),
                        onMoreClicked = {
                            artistViewModel.selectedSong = it
                            settingsClicked = true
                        },
                        downloadTracker = artistViewModel.getDownloadTracker(),

                        onSwipe = {
                            artistViewModel.getPlayerController().onPlayNext(song)
                            scope.launch {
                                snackbarHostState.showSnackbar(addToQueueMessage)
                            }
                        }) {
                        artistViewModel.getPlayerController().init(song, data.songs)
                    }

                }

                item {
                    Box(
                        modifier = Modifier.padding(top = 20.dp),
                    ) {
                        AlbumListView(
                            playlists = data.albums,
                            expandable = true,
                            navigateToAlbums = {
                                navigator.navigate(
                                    AlbumsScreenDestination(id)
                                )
                            }) { album ->
                            navigator.navigate(
                                PlaylistScreenDestination(
                                    album.playlistId, MainActivity.ALBUM
                                )
                            )
                        }
                    }

                }

                item {
                    if (data.singles.isNotEmpty()) {
                        val title = stringResource(id = R.string.singles)
                        HeaderView(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp
                            ), mainText = title, expandable = true
                        ) {
                            navigator.navigate(
                                SongsScreenDestination(
                                    title = title, artistId = data.id, isTop = 0, isSingle = 1
                                )
                            )
                        }
                    }

                }

                items(data.singles) { song ->
                    SwipeableSongView(
                        song = song,
                        onMoreClicked = {
                            artistViewModel.selectedSong = it
                            settingsClicked = true
                        },
                        playerController = artistViewModel.getPlayerController(),
                        downloadTracker = artistViewModel.getDownloadTracker(),

                        onSwipe = {
                            artistViewModel.getPlayerController().onPlayNext(song)
                            scope.launch {
                                snackbarHostState.showSnackbar(addToQueueMessage)
                            }
                        }) {
                        artistViewModel.getPlayerController().init(song, data.singles)
                    }

                }

                item {
                    if (similarArtists.isNotEmpty()) {
                        val title = stringResource(id = R.string.more_artists)
                        HeaderView(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp
                            ),
                            mainText = title,
                            expandable = false
                        ) {
                            //empty for now
                        }

                        LazyRow(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(similarArtists.take(10)) { artist ->
                                ArtistCircleItem(
                                    artist = artist,
                                    onArtistClick = { selectedArtist ->
                                        navigator.navigate(
                                            ArtistScreenDestination(selectedArtist.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

            }

        }


        if (settingsClicked) {
            TrackBottomSheet(
                selectedSong = artistViewModel.selectedSong,
                songSettingsSheetState = songSettingsSheetState,
                onAddToPlaylist = {
                    settingsClicked = false
                    addToPlaylistClicked = true
                },
                onNavigateToAlbum = {
                    artistViewModel.selectedSong.albumId?.let {
                        navigator.navigate(
                            PlaylistScreenDestination(it, MainActivity.ALBUM)
                        )
                    }
                },
                onNavigateToArtist = {
                    if (artistViewModel.selectedSong.artists.size == 1) {
                        artistViewModel.selectedSong.getArtist().id.let {
                            navigator.navigate(
                                ArtistScreenDestination(it)
                            )
                        }
                    } else {
                        showArtistDialog = true
                    }
                },
                onPlayNext = {
                    artistViewModel.getPlayerController().selectedTrack?.let {
                        artistViewModel.getPlayerController().onPlayNext(
                            it
                        )
                    }
                },
                onShare = {
                    ShareUtils.shareLink(context = context, link = DataModule.BASE_URL)
                },
                onDelete = {},
            ) {
                scope.launch {
                    songSettingsSheetState.hide()
                    settingsClicked = false
                }
            }
        }


        if (addToPlaylistClicked) {
            AddToPlaylistBottomSheet(
                selectedSong = artistViewModel.selectedSong,
                playlists = playlists,
                playlistSheetState = playlistSheetState,
                onCreateNewPlaylist = {
                    showNewPlaylistDialog = true
                },
                onSelect = { playlist, song ->
                    artistViewModel.addSongToPlaylist(artistViewModel.selectedSong, playlist)
                }) {
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
                    artistViewModel.addNewPlaylist(name)
                }
            }
        }

        if (showArtistDialog) {
            ArtistBottomSheet(
                selectedSong = artistViewModel.selectedSong,
                artists = artistViewModel.selectedSong.artists,
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