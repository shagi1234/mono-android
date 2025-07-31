package com.mono.music.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mono.music.domain.utils.SearchFocusManager
import com.mono.music.navigation.screen.BottomBarDestination
import com.mono.music.presentation.NavGraphs
import com.mono.music.presentation.appCurrentDestinationAsState
import com.mono.music.presentation.destinations.Destination
import com.mono.music.presentation.destinations.HomeScreenDestination
import com.mono.music.presentation.destinations.MyPlaylistsScreenDestination
import com.mono.music.presentation.destinations.SearchScreenDestination
import com.mono.music.presentation.startAppDestination
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.utils.isRouteOnBackStack

@Composable
fun BottomNavigationBar(
    navController: NavController,
) {
    val currentDestination: Destination =
        navController.appCurrentDestinationAsState().value ?: NavGraphs.root.startAppDestination
    var selectedDestination by remember { mutableStateOf(HomeScreenDestination.route) }
    val view = LocalView.current

    val destinations = listOf(
        HomeScreenDestination.route,
        SearchScreenDestination.route,
        MyPlaylistsScreenDestination.route
    )

    LaunchedEffect(currentDestination) {
        if (destinations.contains(currentDestination.route)) {
            selectedDestination = currentDestination.route
        }
    }

    NavigationBar(
        containerColor = AlbumCoverBlackBG,
        contentColor = Color.Black
    ) {
        BottomBarDestination.entries.forEach { destination ->
            val selected = selectedDestination == destination.direction.route
            val isCurrentDestOnBackStack = navController.isRouteOnBackStack(destination.direction)

            val scale = remember { Animatable(1f) }

            LaunchedEffect(selected) {
                if (selected) {
                    scale.animateTo(1.15f, animationSpec = spring(dampingRatio = 1.5f))
                    scale.animateTo(1f, animationSpec = spring(dampingRatio = 1.5f))
                }
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = if (selected) destination.activeIcon else destination.icon),
                        contentDescription = null,
                        tint = if (selected) Yellow else Color.White,
                        modifier = Modifier.scale(scale.value)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = destination.label),
                        color = if (selected) Yellow else Color.White,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                },
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    // Perform haptic feedback
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                    if (destination.direction.route == SearchScreenDestination.route) {
                        SearchFocusManager.requestFocus()
                    }

                    if (selected && isCurrentDestOnBackStack) {
                        navController.popBackStack(destination.direction, false)
                        return@NavigationBarItem
                    }

                    selectedDestination = destination.direction.route
                    navController.navigate(destination.direction) {
                        launchSingleTop = true
                        restoreState = true
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Yellow,
                    selectedTextColor = Yellow,
                    indicatorColor = Background
                )
            )
        }
    }
}