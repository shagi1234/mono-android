package com.mono.music.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.Search
import com.mono.music.di.DataModule
import com.mono.music.domain.models.Song
import com.mono.music.domain.utils.SearchFocusManager
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.myplaylist.LibraryContentChipsView
import com.mono.music.presentation.myplaylist.SearchLibrarySection
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.presentation.search.components.SearchKeysView
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.SearchBar
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.components.listview.AlbumListView
import com.mono.music.ui.components.listview.ArtistListView
import com.mono.music.ui.components.listview.PlaylistListView
import com.mono.music.ui.components.listview.SongListView
import com.mono.music.ui.components.toolbar.CollapsingToolbarScaffoldScopeInstance.align
import com.mono.music.ui.components.toolbar.ExperimentalToolbarApi
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.BaseUIState
import com.mono.music.ui.utils.ShareUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalToolbarApi::class)
@Composable
@Destination
fun SearchScreen(
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current

    val searchViewModel = hiltViewModel<SearchViewModel>()


    var settingsClicked by remember {
        mutableStateOf(false)
    }
    val playlists by searchViewModel.getAllPlaylists().collectAsState(initial = emptyList())

    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Add this to observe the focus requests
    val shouldFocusSearch by SearchFocusManager.shouldFocusSearch.collectAsState()

    // Add this LaunchedEffect to handle focus requests
    LaunchedEffect(shouldFocusSearch) {
        if (shouldFocusSearch) {
            delay(100) // Small delay for UI to be ready
            focusRequester.requestFocus()
            keyboardController?.show()
            SearchFocusManager.resetFocus()
        }
    }

    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by searchViewModel.uiState.collectAsState(BaseUIState())


    val searchStr by searchViewModel.searchStr.collectAsState("")
    val keys = searchViewModel.keys.collectAsState(initial = Search.getDefaultInstance())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.successfully_added)
    val addToQueueMessage = stringResource(id = R.string.successfully_added)

    val type by searchViewModel.type.collectAsState("")

    val sections = listOf(
        SearchLibrarySection.AllSearch,
        SearchLibrarySection.Artist,
        SearchLibrarySection.Playlist,
        SearchLibrarySection.Album,
        SearchLibrarySection.Song,
    )


    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.message!!
                )
            }
            searchViewModel.updateToDefault()
        }
    }

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive,
                    contentColor = WhiteTextColor,
                    snackbarData = data
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (uiState.isSuccess) 100.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            SearchBar(focusRequester = focusRequester,

                searchStr = searchStr,
                enabled = true,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    focusRequester.requestFocus()
                },
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    searchViewModel.saveSearchStr(searchStr.trim())
                },
                onClear = {
                    searchViewModel.setSearch("")
                }
            ) {
                searchViewModel.setSearch(it)
            }

            if (searchStr.isEmpty() && keys.value.messageList.isNotEmpty()) {
                SearchKeysView(keys = keys.value.messageList,
                    onDelete = { searchViewModel.clearSearchStr() },
                    onSearch = {
                        searchViewModel.setSearch(it)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
            }

            LibraryContentChipsView(
                sections = sections,
                selected = type
            ) { type ->
                searchViewModel.setType(type)
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingView(
                            Modifier
                                .fillMaxSize()
                        )
                    }
                }

                uiState.isFailure -> {
                    NetworkErrorView(Modifier.weight(1f)) {
                        searchViewModel.retryLastQuery()
                    }
                }

                uiState.isSuccess -> {
                    uiState.data?.let { mainScreenData ->
                        SongListView(mainScreenData.songs,
                            playerController = searchViewModel.getPlayerController(),
                            downloadTracker = searchViewModel.getDownloadTracker(),
                            onMoreClicked = {
                                searchViewModel.selectedSong = it
                                settingsClicked = true
                            },
                            onSwipe = { song ->
                                searchViewModel.getPlayerController().onPlayNext(song)
                                scope.launch {
                                    snackbarHostState.showSnackbar(addToQueueMessage)
                                }
                            }) { song ->
                            searchViewModel.getPlayerController()
                                .init(song, mainScreenData.songs)

                        }

                        ArtistListView(
                            header = R.string.artists, mainScreenData.artists
                        ) { artist ->
                            navigator.navigate(
                                ArtistScreenDestination(artist.id)
                            )
                        }

                        AlbumListView(
                            playlists = mainScreenData.albums
                        ) { album ->
                            navigator.navigate(
                                PlaylistScreenDestination(
                                    album.playlistId,
                                    MainActivity.ALBUM
                                )
                            )
                        }

                        PlaylistListView(
                            title = stringResource(id = R.string.playlists),
                            playlists = mainScreenData.playlists
                        ) { playlist ->
                            navigator.navigate(
                                PlaylistScreenDestination(
                                    playlist.playlistId,
                                    MainActivity.PLAYLIST
                                )
                            )
                        }

                    }
                }
            }


        }

        if (settingsClicked) {
            TrackBottomSheet(
                selectedSong = searchViewModel.selectedSong,
                songSettingsSheetState = songSettingsSheetState,
                onAddToPlaylist = {
                    settingsClicked = false
                    addToPlaylistClicked = true
                },
                onNavigateToAlbum = {
                    searchViewModel.selectedSong.albumId?.let {
                        navigator.navigate(
                            PlaylistScreenDestination(it, MainActivity.ALBUM)
                        )
                    }
                },
                onNavigateToArtist = {
                    if (searchViewModel.selectedSong.artists.size == 1) {
                        searchViewModel.selectedSong.getArtist().id.let {
                            navigator.navigate(
                                ArtistScreenDestination(it)
                            )
                        }
                    } else {
                        showArtistDialog = true
                    }
                },
                onPlayNext = {
                    searchViewModel.getPlayerController().selectedTrack?.let {
                        searchViewModel.getPlayerController().onPlayNext(
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
                selectedSong = searchViewModel.selectedSong,
                playlists = playlists,
                playlistSheetState = playlistSheetState,
                onCreateNewPlaylist = {
                    showNewPlaylistDialog = true
                },
                onSelect = { playlist, song ->
                    searchViewModel.addSongToPlaylist(searchViewModel.selectedSong, playlist)

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
                NewPlaylistDialog { name ->
                    showNewPlaylistDialog = false
                    searchViewModel.addNewPlaylist(name)
                }
            }

        }

        if (showArtistDialog) {
            ArtistBottomSheet(
                selectedSong = searchViewModel.selectedSong,
                artists = searchViewModel.selectedSong.artists ?: emptyList(),
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

