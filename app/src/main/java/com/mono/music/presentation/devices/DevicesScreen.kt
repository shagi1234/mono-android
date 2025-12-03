package com.mono.music.presentation.devices

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.domain.models.DeviceSession
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination(style = ScreenTransition::class)
fun DevicesScreen(
    navigator: DestinationsNavigator
) {
    val viewModel = hiltViewModel<DevicesViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    var deviceToRemove by rememberSaveable { mutableStateOf<DeviceSession?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.errorMessage)
            }
            viewModel.updateToDefault()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.successMessage)
            }
            viewModel.updateToDefault()
        }
    }

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_tariffs),
        contentDescription = "bg_devices",
        contentScale = ContentScale.FillBounds
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Surface,
                    contentColor = if (uiState.success) Color.Green else Color.Red,
                    snackbarData = data
                )
            }
        },
        topBar = {
            CollapsingSmallTopAppBar(
                title = stringResource(id = R.string.devices)
            ) {
                navigator.navigateUp()
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.loading && uiState.sessionsData == null) {
                LoadingView(modifier = Modifier.fillMaxSize())
            } else {
                uiState.sessionsData?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        // Current Device Section
                        val currentDevice = data.activeSessions.find { it.isCurrentSession }
                        if (currentDevice != null) {
                            Text(
                                text = stringResource(R.string.this_device),
                                color = WhiteTextColor,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = SFFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            DeviceItem(
                                device = currentDevice,
                                onRemoveClick = null // Current device can't be removed
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Other Devices Section
                        val otherDevices = data.activeSessions.filter { !it.isCurrentSession }
                        if (otherDevices.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.other_devices),
                                color = WhiteTextColor,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = SFFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkGray, MaterialTheme.shapes.medium)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                otherDevices.forEachIndexed { index, device ->
                                    DeviceItem(
                                        device = device,
                                        onRemoveClick = { deviceToRemove = device }
                                    )
                                    if (index < otherDevices.size - 1) {
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color.Gray.copy(alpha = 0.3f))
                                                .padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Remove Device Confirmation Dialog
    if (deviceToRemove != null) {
        AlertDialog(
            onDismissRequest = { deviceToRemove = null },
            title = {
                Text(
                    text = stringResource(R.string.remove_device_confirmation),
                    color = WhiteTextColor,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deviceToRemove?.let { device ->
                            viewModel.deleteSession(device.deviceId)
                        }
                        deviceToRemove = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.remove),
                        color = Color.Red,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToRemove = null }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = WhiteTextColor,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DeviceItem(
    device: DeviceSession,
    onRemoveClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Device Icon
            val iconRes = when (device.deviceType.lowercase()) {
                "mobile" -> R.drawable.account_circle // Replace with actual mobile icon
                "web" -> R.drawable.account_circle // Replace with actual web icon
                "tablet" -> R.drawable.account_circle // Replace with actual tablet icon
                "desktop" -> R.drawable.account_circle // Replace with actual desktop icon
                else -> R.drawable.account_circle
            }

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = device.deviceType,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = device.deviceName,
                    color = WhiteTextColor,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = device.platform + if (device.appVersion.isNotEmpty()) " ${device.appVersion}" else "",
                    color = WhiteTextColor.copy(alpha = 0.7f),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        // Remove button (only for other devices)
        if (onRemoveClick != null) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.leave),
                    contentDescription = stringResource(R.string.remove),
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
