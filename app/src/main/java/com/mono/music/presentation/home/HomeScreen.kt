package com.mono.music.presentation.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.MainActivity
import com.mono.music.R
import com.mono.music.di.DataModule
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ShareUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.mono.music.presentation.destinations.SearchScreenDestination
import com.mono.music.presentation.destinations.SettingsScreenDestination
import com.mono.music.presentation.home.components.HomeTopAppBar
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.HasUpdateBottomSheet
import com.mono.music.ui.components.listview.AlbumListView
import com.mono.music.ui.components.listview.ArtistListView
import com.mono.music.ui.components.listview.PlaylistListView
import com.mono.music.ui.components.listview.SongGridListView
import com.mono.music.ui.utils.HapticType
import com.mono.music.ui.utils.rememberHaptic
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.math.truncate
import androidx.core.net.toUri
import com.mono.music.MainViewModel
import com.mono.music.domain.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator
) {

    val context = LocalContext.current

    val homeViewModel = hiltViewModel<HomeViewModel>()
    val mainViewModel = hiltViewModel<MainViewModel>()

    val uiState by homeViewModel.uiState.collectAsState()


    var settingsClicked by remember {
        mutableStateOf(false)
    }


    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }
    val playlists by homeViewModel.getAllPlaylists().collectAsState(initial = emptyList())

    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }


    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarSuccessMessage = stringResource(id = R.string.subscription_extended)

    LaunchedEffect(uiState) {
        Log.e("TAG___STATE", "HomeScreen: $uiState")
    }



    LaunchedEffect(Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateAvailable ->
            if (updateAvailable.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                showUpdateSheet = true
            }
        }.addOnFailureListener { exception ->
            Timber.tag("UpdateCheck").e("Failure on check update: ${exception.message}")

        }

    }

    LaunchedEffect(uiState.message) {
        if (uiState.message.isNullOrEmpty()) {
            return@LaunchedEffect
        }

        scope.launch {
            (if (uiState.message == "promo_code") snackbarSuccessMessage else uiState.message)?.let {
                snackbarHostState.showSnackbar(
                    it
                )
            }
        }
        homeViewModel.updateToDefault()

    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive,
                    contentColor = WhiteTextColor,
                    snackbarData = data
                )
            }
        },
    ) { _ ->

        LazyColumn(
            Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            item {
                HomeTopAppBar(
                    onSearchClicked = { navigator.navigate(SearchScreenDestination) },
                    onSettingsClicked = { navigator.navigate(SettingsScreenDestination) }
                )

            }


            if (uiState.isSuccess) {
                uiState.data?.let { mainScreenData ->

                    item(
                        key = "hits"
                    ) {
                        SongGridListView(
                            homeViewModel = homeViewModel,
                            songs = mainScreenData.songs,
                            onMoreClicked = {
                                homeViewModel.selectedSong = it
                                settingsClicked = true
                            }
                        ) { song ->
                            homeViewModel.getPlayerController().init(song, mainScreenData.songs)
                        }
                    }

                    item(
                        key = "top"
                    ) {
                        PlaylistListView(
                            title = stringResource(id = R.string.top),
                            playlists = mainScreenData.tops
                        ) { playlist ->
                            navigator.navigate(
                                PlaylistScreenDestination(playlist.playlistId, MainActivity.TOPS)
                            )
                        }
                    }

                    item(
                        key = "artists"
                    ) {
                        ArtistListView(
                            header = R.string.artists_of_the_week, mainScreenData.artists
                        ) { artist ->
                            navigator.navigate(
                                ArtistScreenDestination(artist.id)
                            )
                        }
                    }

                    item(
                        key = "albums"
                    ) {
                        AlbumListView(
                            title = stringResource(id = R.string.albums),
                            mainScreenData.albums
                        ) { album ->
                            navigator.navigate(
                                PlaylistScreenDestination(album.playlistId, MainActivity.ALBUM)
                            )
                        }
                    }



                    items(
                        mainScreenData.playlistCategory,
                        key = { playlistCategory -> playlistCategory.id }) { playlistCategory ->
                        PlaylistListView(
                            title = playlistCategory.name,
                            playlists = playlistCategory.playlists
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
        when {

            uiState.isLoading -> {
                LoadingView(Modifier.fillMaxSize(1f))
            }

            uiState.isFailure -> {

                NetworkErrorView(Modifier.fillMaxSize(1f)) {
                    homeViewModel.getMainPageData()
                }


            }
        }
        if (settingsClicked) {
            TrackBottomSheet(
                selectedSong = homeViewModel.selectedSong,
                songSettingsSheetState = songSettingsSheetState,
                onAddToPlaylist = {
                    addToPlaylistClicked = true
                },
                onNavigateToAlbum = {
                    homeViewModel.selectedSong.albumId?.let {
                        navigator.navigate(
                            PlaylistScreenDestination(it, MainActivity.ALBUM)
                        )
                    }
                },
                onNavigateToArtist = {
                    if (homeViewModel.selectedSong.artists.size == 1) {
                        homeViewModel.selectedSong.getArtist().id.let {
                            navigator.navigate(
                                ArtistScreenDestination(it)
                            )
                        }
                    } else {
                        showArtistDialog = true
                    }
                },
                onPlayNext = {
                    homeViewModel.getPlayerController().selectedTrack?.let {
                        homeViewModel.getPlayerController().onPlayNext(
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
                selectedSong = homeViewModel.selectedSong,
                playlists = playlists,
                playlistSheetState = playlistSheetState,
                onCreateNewPlaylist = {
                    showNewPlaylistDialog = true
                },
                onSelect = { playlist, song ->
                    homeViewModel.addSongToPlaylist(homeViewModel.selectedSong, playlist)

                    scope.launch {
                        snackbarHostState.showSnackbar("hhhhhh")
                    }
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
                    scope.launch {
                        showNewPlaylistDialog = false
                        homeViewModel.addNewPlaylist(name)
                    }
                }
            }
        }

        if (showArtistDialog) {
            ArtistBottomSheet(
                selectedSong = homeViewModel.selectedSong,
                artists = homeViewModel.selectedSong.artists,
                sheetState = artistsSheetState,
                onSelect = { artist -> navigator.navigate(ArtistScreenDestination(artist.id)) },
                onDismiss = { showArtistDialog = false }
            )
        }

        if (showUpdateSheet) {
            HasUpdateBottomSheet(
                onUpdateClick = { navigateToPlayMarket(context = context) },
                onCloseClick = {
                    showUpdateSheet = false
                })

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

fun navigateToPlayMarket(context: Context, packageName: String = context.packageName) {
    try {
        // Пытаемся открыть через Play Market приложение
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=$packageName".toUri()
            setPackage("com.android.vending")
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Если Play Market не установлен, открываем через браузер
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://play.google.com/store/apps/details?id=$packageName".toUri()
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Если и браузер недоступен
            e.printStackTrace()
        }
    }
}

