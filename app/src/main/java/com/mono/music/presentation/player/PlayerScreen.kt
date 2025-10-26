package com.mono.music.presentation.player

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.MainActivity.Companion.ALBUM
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.MainActivity.Companion.TOPS
import com.mono.music.MainViewModel
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.di.DataModule
import com.mono.music.domain.models.Artist
import com.mono.music.domain.models.Playlist
import com.mono.music.player.PlayerStates
import com.mono.music.player.components.PlayerTopAppBar
import com.mono.music.presentation.playlist.PlaylistViewModel
import com.mono.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import com.mono.music.ui.components.bottomsheet.ArtistBottomSheet
import com.mono.music.ui.components.bottomsheet.TrackBottomSheet
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ShareUtils
import com.mono.music.ui.utils.darken
import com.mono.music.ui.utils.getDominantColorFromImageUrl
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    mainViewModel: MainViewModel,
    playerController: PlayerController,
    playerBottomSheet: SheetState,
    isFullScreenVisible: Boolean,
    navigateToArtist: (Artist) -> Unit,
    navigateToAlbum: (Long) -> Unit,
    navigateToPlaylist: (Long) -> Unit,
    navigateToTops: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val playlists = mainViewModel.getAllPlaylists().collectAsState(initial = emptyList())
    val playerState by playerController.playerState.collectAsState()
    val isPlaying = playerState != PlayerStates.STATE_PAUSE

    val sourceId = PlayerSourceInfo.getSourceId()
    val sourceType = PlayerSourceInfo.getSourceType()
    val sourceName = PlayerSourceInfo.getSourceName()

    var songExists by rememberSaveable {
        mutableStateOf(false)
    }
    var settingsClicked by remember {
        mutableStateOf(false)
    }
    var showNewPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var addToPlaylistClicked by rememberSaveable {
        mutableStateOf(false)
    }

    var showPlaylistBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val songSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val playlistBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarMessage = stringResource(id = R.string.successfully_added)

    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val isInPlaylist by playlistViewModel.isSongInAnyPlaylist(
        playerController.selectedTrack?.songId
    ).collectAsState(initial = false)

    val likeState by mainViewModel.likeStates.collectAsState()
    val liked = playerController.selectedTrack?.songId?.let { songId ->
        likeState[songId] ?: playerController.selectedTrack?.isLiked ?: false
    } ?: false

    val snackbarHostState = remember { SnackbarHostState() }

    val addToQueueMessage = stringResource(id = R.string.successfully_added)

    val dominantColor = remember { Animatable(Color.Black) }

    LaunchedEffect(playerController.selectedTrack?.image) {
        coroutineScope.launch {
            dominantColor.animateTo(
                getDominantColorFromImageUrl(
                    context = context,
                    playerController.selectedTrack?.image ?: ""
                ), animationSpec = tween(500)
            )
        }
    }

    LaunchedEffect(playerController.selectedTrack?.songId) {
        if (playerController.selectedTrack != null) {
            PlayerSourceInfo.checkAndClearIfNeeded(playerController.selectedTrack?.songId)
        }
    }


    LaunchedEffect(mainViewModel.uiState) {
        snapshotFlow { mainViewModel.uiState.value }
            .collect { state ->
                if (state.message != null) {
                    snackbarHostState.showSnackbar(state.message)

                    mainViewModel.updateToDefault()
                }
            }
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
                .background(dominantColor.value.darken(0.5f))
        ) {

            PlayerTopAppBar(
                playerController.selectedTrack,
                onMoreClicked = {
                    settingsClicked = true
                },
                onPlaylistClicked = {
                    sourceId?.let { id ->
                        when (sourceType) {
                            ALBUM -> navigateToAlbum(id)
                            PLAYLIST -> navigateToPlaylist(id)
                            TOPS -> navigateToTops(id)
                            else -> playerController.selectedTrack?.albumId?.let {
                                navigateToPlaylist(
                                    it
                                )
                            }
                        }
                    } ?: run {
                        // Fallback to album navigation if sourceId is null
                        playerController.selectedTrack?.albumId?.let { navigateToAlbum(it) }
                    }
                },
                goBack = {
                    onDismiss()
                },
                sourceId = sourceId,
                sourceType = sourceType,
                sourceName = sourceName
            )

            AlbumCoverPager(
                playerController,
                isPlaying,
                isFullScreenVisible
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                MediaInfo(
                    playerController,
                    navigateToArtist,
                    onShowArtistDialog = { showArtistDialog = true },
                    onAddToPlaylistClick = { addToPlaylistClicked = true },
                    onAddFavoritesClick = {
                        mainViewModel.likeSong(
                            songId = playerController.selectedTrack?.songId ?: 0,
                            liked = liked
                        )

                    },
                    isInPlaylist = isInPlaylist,
                    liked = liked
                )
                Spacer(modifier = Modifier.height(20.dp))
                PlaybackControls(
                    modifier = Modifier.weight(1f),
                    playerController,
                    playerState,
                    isPlaying
                )
                Spacer(modifier = Modifier.height(10.dp))
                PlayerFooter(onShowPlaylistBottomSheet = { showPlaylistBottomSheet = true })
            }
        }
    }

    if (showPlaylistBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier,
            containerColor = AlbumCoverBlackBG,
            sheetState = playlistBottomSheet,
            onDismissRequest = {
                scope.launch {
                    playlistBottomSheet.hide()
                    showPlaylistBottomSheet = false
                }
            }
        ) {
            PlaylistBottomSheet(
                sheetState = playlistBottomSheet,
                playerController = playerController
            )
        }

    }

    if (settingsClicked && playerController.selectedTrack != null) {
        TrackBottomSheet(
            selectedSong = playerController.selectedTrack!!,
            songSettingsSheetState = songSettingsSheetState,
            onAddToPlaylist = {
                scope.launch {
                    songSettingsSheetState.hide()
                    settingsClicked = false
                }
                addToPlaylistClicked = true
            },
            onNavigateToAlbum = {
                playerController.selectedTrack?.albumId?.let { navigateToAlbum(it) }
            },
            onNavigateToArtist = {
                if (playerController.selectedTrack?.artists?.size == 1) {
                    playerController.selectedTrack?.getArtist()?.let { navigateToArtist(it) }
                } else {
                    showArtistDialog = true
                }
            },
            onPlayNext = {
                playerController.selectedTrack?.let {
                    playerController.onPlayNext(
                        it
                    )
                }
                scope.launch {
                    snackbarHostState.showSnackbar(addToQueueMessage)
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

    if (addToPlaylistClicked && playerController.selectedTrack != null) {
        AddToPlaylistBottomSheet(
            selectedSong = playerController.selectedTrack!!,
            playlists = playlists.value,
            playlistSheetState = playlistSheetState,
            onCreateNewPlaylist = {
                showNewPlaylistDialog = true
            },
            onSelect = { playlist, song ->
                playerController.selectedTrack?.let {
                    mainViewModel.addSongToPlaylist(it, playlist)
                }

                scope.launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }

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
                mainViewModel.addNewPlaylist(name)
            }
        }
    }

    if (showArtistDialog && playerController.selectedTrack != null) {
        ArtistBottomSheet(
            selectedSong = playerController.selectedTrack!!,
            artists = playerController.selectedTrack?.artists ?: emptyList(),
            sheetState = artistsSheetState,
            onSelect = { artist -> navigateToArtist(artist) },
            onDismiss = { showArtistDialog = false }

        )
    }
}

