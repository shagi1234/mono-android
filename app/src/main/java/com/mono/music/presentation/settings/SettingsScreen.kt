package com.mono.music.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.BuildConfig
import com.mono.music.R
import com.mono.music.presentation.destinations.DevicesScreenDestination
import com.mono.music.presentation.destinations.LegalScreenDestination
import com.mono.music.presentation.destinations.ProfileScreenDestination
import com.mono.music.presentation.destinations.TariffsScreenDestination
import com.mono.music.presentation.destinations.WebViewScreenDestination
import com.mono.music.presentation.localplaylist.LocalPlaylistViewModel
import com.mono.music.presentation.settings.components.BankSelectionDialog
import com.mono.music.presentation.settings.components.ContactUsView
import com.mono.music.presentation.settings.components.PromoCodeDialog
import com.mono.music.presentation.settings.components.SettingsElements
import com.mono.music.presentation.settings.components.SettingsTopAppBar
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.LanguageSelectionView
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.LocaleHelper
import com.mono.music.ui.utils.ScreenTransition
import com.mono.music.ui.utils.scaleButtonClickable
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination(style = ScreenTransition::class)
fun SettingsScreen(
    navigator: DestinationsNavigator
) {


    val settingsViewModel = hiltViewModel<SettingsViewModel>()
    val playListViewModel = hiltViewModel<LocalPlaylistViewModel>()

    var isSuccess by rememberSaveable { mutableStateOf(false) };

    val uiState by settingsViewModel.uiState.collectAsState()

    val currentLanguage by settingsViewModel.currentLanguage.collectAsState()

    val paymentMethods by settingsViewModel.paymentMethods.collectAsState()
    val name by settingsViewModel.name.collectAsState("")
    val validUntil by settingsViewModel.validUntil.collectAsState("")

    var showCheckPromoCode by rememberSaveable {
        mutableStateOf(false)
    }
    val languageSheetState = rememberModalBottomSheetState()

    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var showContactUs by rememberSaveable { mutableStateOf(false) }
    var selectedPaymentOption by rememberSaveable { mutableStateOf(0L) }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val snackbarSuccessMessage = stringResource(id = R.string.subscription_extended)

    LaunchedEffect(uiState.order) {
        uiState.order?.let { order ->
            scope.launch {
                navigator.navigate(
                    WebViewScreenDestination(
                        order.formUrl, order.orderId
                    )
                ) {
                    settingsViewModel.updateToDefault()
                }

            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            isSuccess = false
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.errorMessage
                )
            }
            settingsViewModel.updateToDefault()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.success) {
            isSuccess = true
            if (uiState.successMessage.isNotEmpty()) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        snackbarSuccessMessage
                    )
                }
                settingsViewModel.updateToDefault()
            }
        }
    }

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_tariffs),
        contentDescription = "bg_login",
        contentScale = ContentScale.FillBounds
    )

    Scaffold(
        containerColor = Color.Transparent,
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
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp)
                .imePadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            SettingsTopAppBar(
                name = name,
                validUntil = validUntil
            ) {
            }

            Text(
                modifier = Modifier
                    .padding(top = 32.dp),
                text = stringResource(R.string.general_settings),
                color = WhiteTextColor,
                lineHeight = 6.25.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold
                ),
            )

            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .background(DarkGray, MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
            ) {

                SettingsElements(
                    icon = R.drawable.account_circle,
                    text = stringResource(id = R.string.my_account),
                    expandable = true
                ) {
                    navigator.navigate(ProfileScreenDestination)
                }
                SettingsElements(
                    icon = R.drawable.ic_library,
                    text = stringResource(id = R.string.playlists_menu),
                    expandable = true
                ) {
                    navigator.navigate(DevicesScreenDestination)
                }
                SettingsElements(
                    icon = R.drawable.comments,
                    text = stringResource(id = R.string.write_to_us),
                    expandable = true
                ) {
                    showContactUs = true
                }
                SettingsElements(
                    icon = R.drawable.version,
                    text = stringResource(id = R.string.legal),
                    expandable = true
                ) {
                    navigator.navigate(LegalScreenDestination)
                }
                SettingsElements(
                    icon = R.drawable.version,
                    text = stringResource(id = R.string.version),
                    expandable = false,
                    endText = BuildConfig.VERSION_NAME
                ) {

                }
                SettingsElements(
                    icon = R.drawable.language,
                    text = stringResource(id = R.string.language),
                    expandable = false,
                    endText = currentLanguage.toString()
                ) {
                    showLanguage = true
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .scaleButtonClickable {
                        (navigator.navigate(
                            TariffsScreenDestination
                        ))
                    }
                    .padding(12.dp, 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.tariffs),
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            fontSize = 20.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 4.dp),
                        text = stringResource(id = R.string.tariffs_desc),
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.background
                    )

                }

                Icon(
                    tint = Color.Black,
                    painter = painterResource(id = R.drawable.right_arrow),
                    contentDescription = null
                )

            }

            Column(Modifier.padding(top = 32.dp)) {
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
                            modifier = Modifier
                                .padding(12.dp)
                                .scaleIconClickable(enabled = code.isNotEmpty()) {
                                    settingsViewModel.checkPromoCode(code)
                                    code = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                            onClick = {

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
                        settingsViewModel.checkPromoCode(code)
                        focusManager.clearFocus()
                        keyboardController?.hide()

                    }),
                )
            }
//            SubscriptionOptionsView(
//                options = options,
//                showPromoCodeDialog = {
//                    showCheckPromoCode = true
//                },
//                onSelectOption = { option ->
//                    selectedPaymentOption = option
//                }
//            )


            Column {
                Row(
                    modifier = Modifier
                        .scaleItemClickable { settingsViewModel.logout(); playListViewModel.clearAllDownloadedSongs() }.fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.leave),
                        contentDescription = stringResource(id = R.string.log_out),
                        tint = WhiteTextColor
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        text = stringResource(id = R.string.log_out),
                        fontSize = 16.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = WhiteTextColor
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showCheckPromoCode) {
            Dialog(onDismissRequest = { showCheckPromoCode = false }) {
                PromoCodeDialog() { code ->
                    showCheckPromoCode = false
                    settingsViewModel.checkPromoCode(code)
                }
            }

        }

        if (showContactUs) {
            Dialog(onDismissRequest = { showContactUs = false }) {
                ContactUsView() { text ->
                    showContactUs = false
                    settingsViewModel.contactUs(text)
                }
            }

        }


        if (selectedPaymentOption != 0L) {
            Dialog(onDismissRequest = { selectedPaymentOption = 0 }) {
                BankSelectionDialog(paymentMethods) { bank ->
                    settingsViewModel.paymentRegister(
                        bank = bank.type,
                        optionId = selectedPaymentOption
                    )
                    selectedPaymentOption = 0

                }
            }
        }

        if (showLanguage) {
            LanguageSelectionView(
                selectedLanguage = currentLanguage,
                sheetState = languageSheetState,
                onDismissRequest = { showLanguage = false },
                onSelect = { language ->
                    showLanguage = false
                    LocaleHelper.setLocale(
                        context, language
                    )
                    restartActivity(context.findActivity())

                },
            )
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun restartActivity(activity: Activity?) {
    val intent = activity?.getIntent()
    activity?.finish()
    activity?.startActivity(intent)
}
