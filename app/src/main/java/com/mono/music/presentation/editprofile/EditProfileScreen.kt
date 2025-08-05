package com.mono.music.presentation.editprofile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.protobuf.Internal.BooleanList
import com.mono.music.R
import com.mono.music.domain.models.User
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.profile.Gender
import com.mono.music.presentation.profile.genders
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.CustomDatePickerDialog
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.clearFocusOnKeyboardDismiss
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.DialogBackground
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.UIState
import com.mono.music.ui.utils.clickWithoutIndication
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




