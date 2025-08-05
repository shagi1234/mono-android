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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mono.music.R
import com.mono.music.domain.models.Option
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.presentation.settings.components.BankSelectionDialog
import com.mono.music.presentation.settings.components.FreeSubscriptionOptionView
import com.mono.music.presentation.settings.components.UpdatedSubscriptionOptionView
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.theme.DialogBackground
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffsScreenContent(
    uiState: TariffsUIState,
    code: String,
    isSuccess: Boolean,
    selectedPaymentOption: Long,
    paymentMethods: List<PaymentMethod>,
    snackbarHostState: SnackbarHostState,
    onCodeChange: (String) -> Unit,
    onSelectedPaymentOptionChange: (Long) -> Unit,
    onRetry: () -> Unit,
    onSubscribeToFreePlan: (option: Option) -> Unit,
    onCheckPromoCode: (String) -> Unit,
    onPaymentRegister: (bank: String, optionId: Long) -> Unit,
    onClearCode: () -> Unit,
    navigator: DestinationsNavigator? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box {
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
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier
                                .padding(vertical = 10.dp),
                            onClick = {
                                navigator?.navigateUp()
                            }) {
                            Icon(
                                tint = Color.White,
                                painter = painterResource(id = R.drawable.left_arrow),
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { padding ->

            when {
                uiState.isLoading -> {
                    LoadingView(Modifier.fillMaxSize())
                }

                uiState.isFailure -> {
                    NetworkErrorView(Modifier.fillMaxSize()) {
                        onRetry()
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            items(options) { option ->
                                if (option.price == 0L) {
                                    FreeSubscriptionOptionView(
                                        selected = selectedPaymentOption == option.id,
                                        option = option
                                    ) {
                                        onSubscribeToFreePlan(option)
                                    }
                                } else {
                                    UpdatedSubscriptionOptionView(
                                        selected = selectedPaymentOption == option.id,
                                        option = option
                                    ) {
                                        onSelectedPaymentOptionChange(option.id)
                                    }
                                }
                            }

                            item {
                                PromoCodeSection(
                                    code = code,
                                    onCodeChange = onCodeChange,
                                    onCheckPromoCode = {
                                        onCheckPromoCode(code)
                                        onClearCode()
                                    },
                                    onKeyboardDone = {
                                        onCheckPromoCode(code)
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment Selection Dialog
        if (selectedPaymentOption != 0L) {
            Dialog(onDismissRequest = { onSelectedPaymentOptionChange(0) }) {
                BankSelectionDialog(paymentMethods) { bank ->
                    onPaymentRegister(bank.type, selectedPaymentOption)
                    onSelectedPaymentOptionChange(0)
                }
            }
        }

        // Loading Dialog
        if (uiState.isPending) {
            Dialog(onDismissRequest = { }) {
                LoadingView(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PromoCodeSection(
    code: String,
    onCodeChange: (String) -> Unit,
    onCheckPromoCode: () -> Unit,
    onKeyboardDone: () -> Unit
) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(
            text = stringResource(R.string.promo_code),
            color = WhiteTextColor,
            lineHeight = 6.25.em,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = code,
            onValueChange = { newCode ->
                if (newCode.length <= 10) {
                    onCodeChange(newCode.uppercase())
                }
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight(400),
                color = WhiteTextColor,
            ),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.code),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight(400),
                        color = GrayTextColor,
                    )
                )
            },
            trailingIcon = {
                Button(
                    modifier = Modifier.padding(12.dp),
                    onClick = onCheckPromoCode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Yellow.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    enabled = code.isNotEmpty(),
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.continue_string),
                        style = TextStyle(
                            fontSize = 14.sp,
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
                unfocusedContainerColor = DialogBackground,
                focusedContainerColor = DialogBackground,
                focusedBorderColor = DialogBackground,
                unfocusedBorderColor = DialogBackground,
                disabledBorderColor = DialogBackground,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            shape = MaterialTheme.shapes.extraSmall,
            keyboardActions = KeyboardActions(onDone = { onKeyboardDone() }),
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TariffsContentPreview() {
    val mockOptions = listOf(
        Option(
            price = 0L,
            name = "Premium 1 months",
            image = "", days = 30,
        ),

                Option(
                name = "Premium 1 months",
        image = "", days = 30,
        price = 15
    )

    )

    val mockUiState = TariffsUIState(
        isLoading = false,
        isSuccess = true,
        isFailure = false,
        data = mockOptions,
        isPending = false
    )

    TariffsScreenContent(
        uiState = mockUiState,
        code = "",
        selectedPaymentOption = 0L,
        paymentMethods = emptyList(),
        isSuccess = false,
        snackbarHostState = remember { SnackbarHostState() },
        onCodeChange = { },
        onSelectedPaymentOptionChange = { },
        onRetry = { },
        onSubscribeToFreePlan = { },
        onCheckPromoCode = { },
        onPaymentRegister = { _, _ -> },
        onClearCode = { },
    )
}