package com.mono.music.presentation.otp

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.EditProfileScreenDestination
import com.mono.music.presentation.destinations.LoginTariffsScreenDestination
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.clickWithoutIndication
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch


@LoginNavGraph
@Destination
@Composable
fun OTPScreen(
    phone: String,
    navigator: DestinationsNavigator
) {
    val verificationViewModel = hiltViewModel<VerificationViewModel>()

    val uiState by verificationViewModel.uiState.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val (code, onCodeChange) = remember {
        mutableStateOf("")
    }
    val (isFocused, onFocusChange) = remember {
        mutableStateOf(false)
    }
    val (isError, onErrorChange) = remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val resendAllowed = verificationViewModel.resendAllowed
    val resendTime = verificationViewModel.timeLeftToResend


    var isNotValid by remember { mutableStateOf(false) }

    val isTariffActive by verificationViewModel.isTariffActive.collectAsState()


    LaunchedEffect(uiState.failure) {
        if (uiState.failure) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.errorMessage
                )
            }
            verificationViewModel.updateToDefault()
        }

    }

    //if is first time
    LaunchedEffect(uiState.isVerifiedToDetails) {
        if (uiState.isVerifiedToDetails) {
            navigator.navigate(EditProfileScreenDestination)
        }
    }

    //if is not first time
    LaunchedEffect(uiState.isVerifiedToApp) {
        if (uiState.isVerifiedToApp) {
            if (!isTariffActive)
                navigator.navigate(LoginTariffsScreenDestination)
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

        ) { padding ->
        Box(
            modifier = Modifier
                .safeDrawingPadding()
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {


                Image(
                    modifier = Modifier
                        .padding(top = 74.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.ic_mono_logo_big),
                    contentDescription = "ic_mono_logo_big",
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    modifier = Modifier
                        .padding(top = 60.dp)
                        .fillMaxWidth(0.7f),
                    text = stringResource(R.string.enter_verification_code),
                    color = WhiteTextColor,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = stringResource(R.string.enter_verification_code_desc),
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
                    text = stringResource(R.string.code),
                    color = WhiteTextColor,
                    style = TextStyle(
                        fontFamily = SFFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 56.dp)
                )

                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp)
                        .onFocusChanged {
                            onFocusChange(it.isFocused)
                        },
                    value = code,
                    onValueChange = {
                        if (it.length > 6) return@BasicTextField
                        onCodeChange(it)
                        isNotValid = false
                    },
                    decorationBox = {
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(6) { index ->
                                CharView(
                                    isTextFieldFocused = isFocused,
                                    isError = isError,
                                    index = index,
                                    text = code
                                )
                                if (index != 6) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                    },

                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                    }),
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

                if (isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = stringResource(R.string.invalid_code_desc),
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 16.8.sp,
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
                    text = R.string.confirm,
                    onClick = {
                        if (code.length == 6) {
                            scope.launch {
                                verificationViewModel.verify(phone, code)
                            }
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        } else isNotValid = true
                    },
                    containerColor = Yellow,
                    contentColor = AlbumCoverBlackBG,
                    shape = MaterialTheme.shapes.small

                )

                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        modifier = Modifier.clickWithoutIndication {
                            if (resendAllowed) verificationViewModel.resendCode(
                                phone
                            )
                        },
                        text = stringResource(R.string.resend),
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 16.8.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight(700),
                            color = MaterialTheme.colorScheme.primary,

                            textAlign = TextAlign.Center,
                        )
                    )

                    if (!resendAllowed) {
                        Text(
                            text = resendTime,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = SFFontFamily,
                                fontWeight = FontWeight(700),
                                textAlign = TextAlign.Center,
                            ),
                            color = White
                        )
                    } else {
                        Icon(
                            modifier = Modifier.clickWithoutIndication {
                                verificationViewModel.resendCode(
                                    phone
                                )
                            },
                            tint = MaterialTheme.colorScheme.primary,
                            painter = painterResource(id = R.drawable.ic_restart),
                            contentDescription = null
                        )
                    }
                }
            }

            IconButton(
                modifier = Modifier
                    .padding(vertical = 10.dp),
                onClick = {
                    navigator.navigateUp()
                }) {
                Icon(
                    tint = White,
                    painter = painterResource(id = R.drawable.left_arrow),
                    contentDescription = null
                )
            }
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