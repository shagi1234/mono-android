package com.mono.music.presentation.tariffs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.domain.models.Option
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.HomeScreenDestination
import com.mono.music.presentation.destinations.WebViewScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@LoginNavGraph()
@Destination()
@Composable
fun LoginTariffsScreen(
    navigator: DestinationsNavigator
) {
    Box(
        modifier = Modifier.safeDrawingPadding()
    ) {
        TariffsScreen(navigator)
    }
}

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
    var selectedPaymentOption by rememberSaveable { mutableStateOf(0L) }
    val paymentMethods by tariffsViewModel.paymentMethods.collectAsState()
    val snackbarFailMessage = stringResource(id = R.string.error)
    var code by remember { mutableStateOf("") }


    LaunchedEffect(uiState.message) {

        if (uiState.message.isNullOrEmpty()) {
            return@LaunchedEffect;
        }

        if (uiState.succeed) {
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
                ) {
                    tariffsViewModel.updateToDefault()
                }

            }
        }
    }

    TariffsScreenContent(
        uiState = uiState,
        code = code,
        selectedPaymentOption = selectedPaymentOption,
        paymentMethods = paymentMethods,
        isSuccess = isSuccess,
        snackbarHostState = snackbarHostState,
        onCodeChange = { newCode -> code = newCode },
        onSelectedPaymentOptionChange = { option -> selectedPaymentOption = option },
        onRetry = { tariffsViewModel.getOptions() },
        onSubscribeToFreePlan = { option -> tariffsViewModel.subscribeToFreePlan(option) },
        onCheckPromoCode = { promoCode -> tariffsViewModel.checkPromoCode(promoCode) },
        onPaymentRegister = { bank, optionId ->
            tariffsViewModel.paymentRegister(bank = bank, optionId = optionId)
        },
        onClearCode = { code = " " },
        navigator = navigator
    )
}

