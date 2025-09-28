package com.mono.music.presentation.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.LoginTariffsScreenDestination
import com.mono.music.presentation.profile.Gender
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.theme.Surface
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@LoginNavGraph()
@Destination()
@Composable
fun EditProfileScreen(
    navigator: DestinationsNavigator
) {
    val profileViewModel = hiltViewModel<EditProfileViewModel>()

    val uiState by profileViewModel.uiState.collectAsState()

    val username by profileViewModel.name.observeAsState("")
    val birthday by profileViewModel.birthday.observeAsState("")

    val isTariffActive by profileViewModel.isTariffActive.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.failure) {
        if (uiState.failure) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.errorMessage
                )
            }
            profileViewModel.updateToDefault()
        }
    }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            if (!isTariffActive) {
                navigator.navigate(LoginTariffsScreenDestination)
            }
        }
    }

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_login),
        contentDescription = "bg_login",
        contentScale = ContentScale.FillBounds
    )

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Surface,
                    contentColor = Color.Red,
                    snackbarData = data
                )
            }
        }
    ) { _ ->
        EditProfileScreenContent(
            username = username,
            birthday = birthday,
            navigator = navigator,
            onUsernameChange = { newUsername ->
                profileViewModel.setName(newUsername)
            },
            onBirthdayChange = { birthday ->
                profileViewModel.setBirthday(birthday)
            }
        ) {
            profileViewModel.updateUserData(
                name = username,
                gender = Gender.MALE.value,
                birthday = birthday
            )

        }

        if (uiState.loading) {
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




