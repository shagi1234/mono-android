@file:OptIn(ExperimentalMaterial3Api::class)

package com.mono.music.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.OTPScreenDestination
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.CustomTextField
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.clearFocusOnKeyboardDismiss
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@LoginNavGraph()
@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator
) {

    val loginViewModel = hiltViewModel<LoginViewModel>()

    val uiState by loginViewModel.uiState.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    var isNotValid by remember { mutableStateOf(false) }

    val phone by loginViewModel.phone.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.error)

    LaunchedEffect(uiState.failure) {
        if (uiState.failure) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
            }

            loginViewModel.updateToDefault()
        }

    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navigator.navigate(OTPScreenDestination("+993$phone"))
            loginViewModel.updateToDefault()

        }

    }

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_login),
        contentDescription = "bg_login",
        contentScale = ContentScale.FillBounds
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Surface,
                    contentColor = Color.Red,
                    snackbarData = data
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },

    ) { padding ->


        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Image(
                modifier = Modifier
                    .padding(top = 70.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.ic_mono_logo_big),
                contentDescription = "ic_mono_logo_big",
                contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .fillMaxWidth(0.7f),
                text = stringResource(R.string.welcome),
                color = WhiteTextColor,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = stringResource(R.string.enter_phone_number_to_continue),
                color = GrayTextColor,
                style = TextStyle(
                    fontFamily = SFFontFamily,
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.7f)
                    .align(alignment = Alignment.Start)
            )

            Text(
                text = stringResource(R.string.phone_number),
                color = WhiteTextColor,
                lineHeight = 6.25.em,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(top = 60.dp)
            )
            CustomTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .clearFocusOnKeyboardDismiss()
                    .padding(top = 8.dp),

                value = phone,
                shape = MaterialTheme.shapes.extraSmall,
                onValueChange = {
                    isNotValid = false
                    if (it.length < 9) {
                        loginViewModel.setPhone(it)
                    }
                },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_phone),
                        contentDescription = "song setting",
                        tint = GrayTextColor
                    )
                },
                leadingIcon = {

                    Row(
                        Modifier
                            .padding(start = 12.dp)
                            .height(IntrinsicSize.Min)
                    ) {

                        Text(
                            text = stringResource(R.string._993),
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontFamily = SFFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = GrayTextColor,
                            )
                        )
                        VerticalDivider(
                            color = GrayTextColor,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                    disabledContainerColor = Surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Yellow
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Default, keyboardType = KeyboardType.Phone

                ),
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (phone.trim().length == 8){
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            loginViewModel.loginUser(phone.trim())
                        }else{
                            isNotValid = true
                        }

                    },
                ),
            )
            if (isNotValid) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.must_not_be_empty),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight(400),
                        color = Red,

                        textAlign = TextAlign.Center,
                    )
                )
            }

            CustomButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                text = R.string.continue_string,
                onClick = {
                    if (phone.trim().length == 8){
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        loginViewModel.loginUser(phone.trim())
                    }else{
                        isNotValid = true
                    }
                },
                containerColor = Yellow,
                contentColor = AlbumCoverBlackBG,
                shape = MaterialTheme.shapes.small

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
