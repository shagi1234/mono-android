package com.mono.music.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.mono.music.MainActivity
import com.mono.music.MainViewModel
import com.mono.music.presentation.NavGraphs
import com.mono.music.presentation.destinations.ArtistScreenDestination
import com.mono.music.presentation.destinations.HomeScreenDestination
import com.mono.music.presentation.destinations.LoginScreenDestination
import com.mono.music.presentation.destinations.OnBoardingScreenDestination
import com.mono.music.presentation.destinations.PlaylistScreenDestination
import com.mono.music.presentation.destinations.ProfileScreenDestination
import com.mono.music.presentation.destinations.SettingsScreenDestination
import com.mono.music.presentation.destinations.TariffsScreenDestination
import com.mono.music.presentation.destinations.WebViewScreenDestination
import com.mono.music.presentation.player.PlayerScreen
import com.mono.music.ui.components.MiniPlayer
import com.mono.music.ui.components.SubsExpiredView
import com.mono.music.ui.components.VpnStatus
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun AppNavGraph(
    mainViewModel: MainViewModel,
    playerBottomSheet: SheetState
) {

    val navController = rememberNavController()

    val scope = rememberCoroutineScope()

    var isNowPlayingScreenVisible by remember { mutableStateOf(false) }

    val loggedIn by mainViewModel.isLoggedIn.collectAsState(initial = null)
    val token by mainViewModel.token.collectAsState(initial = null)
    val validUntil by mainViewModel.validUntil.collectAsState(initial = null)
    val isFirstTime by mainViewModel.isFirstTime.collectAsState(initial = null)
    val isRegisterCompleted by mainViewModel.isRegisterCompleted.collectAsState(initial = null)
    val planSelected by mainViewModel.planSelected.collectAsState(initial = null)


    val startRoute = if (loggedIn == true) NavGraphs.root.startRoute else LoginScreenDestination
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val destination = navBackStackEntry?.destination?.route ?: startRoute.route

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var showSubscribe by remember { mutableStateOf(false) }

    val uiState by mainViewModel.uiState.collectAsState()

    val onBoardingNavHostEngine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) +
                        fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) +
                        fadeOut(animationSpec = tween(500))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) +
                        fadeOut(animationSpec = tween(500))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) +
                        fadeIn(animationSpec = tween(500))
            },
        )
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loggedIn) {
        if (loggedIn == true) {
            mainViewModel.getFreePlan()
        }
    }

    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.message!!
                )
            }
            mainViewModel.updateToDefault()
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        if (loggedIn == true && isRegisterCompleted == true && planSelected == true) {
            LaunchedEffect(validUntil, isFirstTime) {
                if (validUntil == null || isFirstTime == null || isFirstTime == "") {
                    mainViewModel.getUserData()
                    return@LaunchedEffect
                }
                val isNotValid = validUntil?.let {
                    !mainViewModel.checkIsValid(it)
                } ?: false
                showSubscribe = isNotValid
            }

            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                Scaffold(bottomBar = {
                    if (destination.shouldShowScaffoldElements()) {
                        BottomNavigationBar(navController = navController)
                    }
                }) { paddingValues ->

                    DestinationsNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        navGraph = NavGraphs.root,
                        startRoute = HomeScreenDestination,
                        engine = rememberAnimatedNavHostEngine()
                    )

                    BackHandler(isNowPlayingScreenVisible) {
                        isNowPlayingScreenVisible = false
                    }

                }

                if (destination.shouldShowScaffoldElements()) {
                    AnimatedContent(
                        modifier = Modifier,
                        targetState = isNowPlayingScreenVisible,
                        transitionSpec = {
                            slideInVertically(animationSpec = tween()) + fadeIn() with shrinkOut(
                                animationSpec = tween(
                                    durationMillis = 500
                                )
                            ) + fadeOut(
                                spring(stiffness = Spring.StiffnessMedium)
                            )
                        }
                    ) { isFullScreenVisible ->
                        if (isFullScreenVisible) {
                            if (mainViewModel.getPlayerController().selectedTrack != null /*&& showPlayer*/) {

                                Box(
                                    Modifier
                                        .offset {
                                            IntOffset(
                                                offsetX.roundToInt(),
                                                offsetY.roundToInt()
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()

                                                val (x, y) = dragAmount
                                                if (y > 0) {
                                                    isNowPlayingScreenVisible = false
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                }
                                            }
                                        }) {
                                    PlayerScreen(
                                        mainViewModel = mainViewModel,
                                        playerController = mainViewModel.getPlayerController(),
                                        playerBottomSheet = playerBottomSheet,
                                        isFullScreenVisible = isFullScreenVisible,
                                        navigateToArtist = { artist ->
                                            isNowPlayingScreenVisible = false
                                            navController.navigate(ArtistScreenDestination(artist.id))
                                        },
                                        navigateToAlbum = { album ->
                                            isNowPlayingScreenVisible = false
                                            navController.navigate(
                                                PlaylistScreenDestination(
                                                    album, MainActivity.ALBUM
                                                )
                                            )
                                        },
                                        navigateToPlaylist = { playlist ->
                                            isNowPlayingScreenVisible = false
                                            navController.navigate(
                                                PlaylistScreenDestination(
                                                    playlist, MainActivity.PLAYLIST
                                                )
                                            )
                                        },
                                        navigateToTops = { tops ->
                                            isNowPlayingScreenVisible = false
                                            navController.navigate(
                                                PlaylistScreenDestination(
                                                    tops, MainActivity.TOPS
                                                )
                                            )
                                        },
                                    ) {
                                        isNowPlayingScreenVisible = false
                                    }

                                }
                            }
                        } else {
                            mainViewModel.getPlayerController().selectedTrack?.let { song ->
                                MiniPlayer(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .padding(
                                            bottom = if (destination.shouldShowScaffoldElements()
                                            ) 80.dp else 20.dp
                                        ),
                                    song = song,
                                    playerController = mainViewModel.getPlayerController(),
                                    onClick = {
                                        scope.launch() {
                                            isNowPlayingScreenVisible = true
                                        }
                                    },
                                    onPlayPauseClick = mainViewModel.getPlayerController()::onPlayPauseClick

                                )

                            }
                        }
                    }
                }


            }


            if (showSubscribe && destination.shouldShowScaffoldElements()) {
                SubsExpiredView {
                    navController.navigate(TariffsScreenDestination)
                }
            }
        } else if (loggedIn == false || isRegisterCompleted == false || planSelected == false) {
            DestinationsNavHost(
                navController = navController,
                navGraph = NavGraphs.login,
                modifier = Modifier,
                startRoute = OnBoardingScreenDestination,
                engine = onBoardingNavHostEngine
            )
        }

        VpnStatus()

    }

}


private fun String.shouldShowScaffoldElements(): Boolean {
    return this != SettingsScreenDestination.route && this != WebViewScreenDestination.route && this != ProfileScreenDestination.route && this != TariffsScreenDestination.route
}


