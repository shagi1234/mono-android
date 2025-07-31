package com.mono.music.presentation.tariffs

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.HomeScreenDestination
import com.mono.music.presentation.destinations.WebViewScreenDestination
import com.mono.music.presentation.settings.components.BankSelectionDialog
import com.mono.music.presentation.settings.components.FreeSubscriptionOptionView
import com.mono.music.presentation.settings.components.PromoCodeDialog
import com.mono.music.presentation.settings.components.UpdatedSubscriptionOptionView
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.clearFocusOnKeyboardDismiss
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
//@LoginNavGraph()
@Destination
@Composable
fun TariffsScreen(
    navigator: DestinationsNavigator
) {
    val tariffsViewModel = hiltViewModel<TariffsViewModel>()
    var isSuccess by rememberSaveable { mutableStateOf(false) };

    val uiState by tariffsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val freePlan by tariffsViewModel.freePlan.collectAsState()
    var showCheckPromoCode by rememberSaveable { mutableStateOf(false) }
    var selectedPaymentOption by rememberSaveable { mutableStateOf(0L) }
    val paymentMethods by tariffsViewModel.paymentMethods.collectAsState()
    val snackbarFailMessage = stringResource(id = R.string.error)

    LaunchedEffect(uiState.message) {

        if (uiState.message.isNullOrEmpty()) {
            return@LaunchedEffect;
        }

        if (uiState.succeed) {
            // Navigate to home screen
            navigator.navigate(HomeScreenDestination) {
                popUpTo(HomeScreenDestination.route) {
                    inclusive = true
                }
            }
            tariffsViewModel.updateToDefault()
        } else {
            isSuccess = false
            scope.launch {
                snackbarHostState.showSnackbar(
                    snackbarFailMessage
                )
            }
            tariffsViewModel.clearMessage()
        }


    }

    LaunchedEffect(uiState.isSubscribedToPremium) {
        if (uiState.isSubscribedToPremium) {
            navigator.navigateUp()
            tariffsViewModel.updateToDefault()
        }

    }

    LaunchedEffect(uiState.order) {
        uiState.order?.let { order ->
            scope.launch {
                navigator.navigate(
                    WebViewScreenDestination(
                        order.formUrl, order.orderId
                    )
                ){
                    tariffsViewModel.updateToDefault()
                }

            }
        }
    }


    var code by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box() {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.bg_tariffs),
            contentDescription = "bg_free_plan",
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
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    val contentColor = if (isSuccess) Color.Green else Color.Red
                    Snackbar(
                        containerColor = Surface,
                        contentColor = contentColor,
                        snackbarData = data
                    )
                }
            },
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                    title = {
                        Text(
                            text = stringResource(id = R.string.tariffs),
                            color = WhiteTextColor,
                            fontFamily = SFFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    })
            }

        ) { padding ->

            when {
                uiState.isLoading -> {
                    LoadingView(Modifier.fillMaxSize())
                }

                uiState.isFailure -> {
                    NetworkErrorView(Modifier.fillMaxSize()) {
                        tariffsViewModel.getOptions()
                    }
                }

                uiState.isSuccess -> {
                    uiState.data?.let { options ->

                        LazyColumn(
                            modifier = Modifier
                                .padding(top = padding.calculateTopPadding())
                                .imePadding()
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(options) { option ->

                                if (option.price == 0L){
                                    FreeSubscriptionOptionView(
                                        selected = selectedPaymentOption == option.id,
                                        option = option
                                    ) {
                                        tariffsViewModel.subscribeToFreePlan(option)
                                    }
                                }else{
                                    UpdatedSubscriptionOptionView(
                                        selected = selectedPaymentOption == option.id,
                                        option = option
                                    ) {
                                        selectedPaymentOption = option.id
                                    }
                                }
                            }

                            item {
                                Column(Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = stringResource(R.string.promo_code),
                                        color = WhiteTextColor,
                                        lineHeight = 6.25.em,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = SFFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = code,
                                        onValueChange = {
                                            if (it.length <= 10) {
                                                code = it.uppercase()
                                            }
                                        },

                                        maxLines = 1,
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 16.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = SFFontFamily,
                                            fontWeight = FontWeight(400),
                                            color = WhiteTextColor,
                                        ),
                                        placeholder = {
                                            Text(
                                                text = stringResource(id = R.string.code),
                                                style = TextStyle(
                                                    fontSize = 16.sp,
                                                    lineHeight = 16.sp,
                                                    fontFamily = SFFontFamily,
                                                    fontWeight = FontWeight(700),
                                                    color = Color.DarkGray,
                                                )

                                            )
                                        },
                                        trailingIcon = {
                                            Button(
                                                modifier = Modifier.padding(12.dp),
                                                onClick = {
                                                    tariffsViewModel.checkPromoCode(code)
                                                    code = ""
                                                    keyboardController?.hide()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    disabledContainerColor = Yellow.copy(alpha = 0.5f),
                                                    contentColor = MaterialTheme.colorScheme.background
                                                ),
                                                enabled = code.isNotEmpty(),
                                                shape = MaterialTheme.shapes.small,
                                                contentPadding = PaddingValues(12.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.send),
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        lineHeight = 16.sp,
                                                        fontFamily = SFFontFamily,
                                                        fontWeight = FontWeight(700),
                                                        color = MaterialTheme.colorScheme.background,
                                                    )
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WhiteTextColor,
                                            unfocusedTextColor = WhiteTextColor,
                                            unfocusedContainerColor = Surface,
                                            focusedContainerColor = Surface,
                                            focusedBorderColor = Surface,
                                            unfocusedBorderColor = Surface,
                                            disabledBorderColor = Surface,

                                            ),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Done,
                                            keyboardType = KeyboardType.Text
                                        ),
                                        shape = MaterialTheme.shapes.small,
                                        keyboardActions = KeyboardActions(onDone = {
                                            tariffsViewModel.checkPromoCode(code)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()

                                        }),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedPaymentOption != 0L) {
                Dialog(onDismissRequest = { selectedPaymentOption = 0 }) {
                    BankSelectionDialog(paymentMethods) { bank ->
                        tariffsViewModel.paymentRegister(
                            bank = bank.type,
                            optionId = selectedPaymentOption
                        )
                        selectedPaymentOption = 0

                    }
                }
            }

            if (showCheckPromoCode) {
                Dialog(onDismissRequest = { showCheckPromoCode = false }) {
                    PromoCodeDialog() { code ->
                        showCheckPromoCode = false
                        tariffsViewModel.checkPromoCode(code)
                    }
                }

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
}