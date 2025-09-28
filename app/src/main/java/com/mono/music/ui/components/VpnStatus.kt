package com.mono.music.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import com.mono.music.R
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.SecondaryBackground
import com.mono.music.ui.theme.SurfaceSecond
import com.mono.music.ui.theme.WhiteTextColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HasUpdateBottomSheet(
    onUpdateClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })

    ModalBottomSheet(
        containerColor = SecondaryBackground,
        sheetState = sheetState,
        onDismissRequest = { /* пусто -> нельзя свайпнуть/тапнуть */ },
        dragHandle = null, // Убираем drag handle чтобы нельзя было свайпнуть
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "\uD83D\uDE80",
                        modifier = Modifier.padding(all = 26.dp),
                        fontSize = 40.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.W400,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                CustomMiniButton(
                    modifier = Modifier.size(30.dp),
                    contentColor = GrayTextColor,
                    containerColor = Background,
                    leadingIcon = R.drawable.ic_close,
                    shape = CircleShape
                ) {
                    scope.launch {
                        sheetState.hide()
                        onCloseClick() // родитель может убрать Composable
                    }
                }
            }

            Text(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = stringResource(R.string.update_available),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
            )

            Text(
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp),
                text = stringResource(R.string.update_available_message),
                fontSize = 15.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Normal,
                color = GrayTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                modifier = Modifier.fillMaxWidth(),
                text = R.string.update_available,
                onClick = onUpdateClick,
                shape = MaterialTheme.shapes.small
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnStatus() {

    val context = LocalContext.current

    var isVpnConnected by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        isVpnConnected = checkVpnStatus(context = context)
    }


    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })


    if (isVpnConnected) {
        ModalBottomSheet(
            containerColor = SecondaryBackground,
            sheetState = sheetState,
            onDismissRequest = {},
            dragHandle = null,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(26.dp),
                            text = "\uD83D\uDE30",
                            fontSize = 40.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = WhiteTextColor,
                        )

                    }



                    Spacer(modifier = Modifier.weight(1f))

                    CustomMiniButton(
                        modifier = Modifier.size(30.dp),
                        contentColor = GrayTextColor,
                        containerColor = Background,
                        leadingIcon = R.drawable.ic_close,
                        shape = CircleShape
                    ) {
                        scope.launch {
                            sheetState.hide()
                            isVpnConnected = false
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                        ),
                    text = stringResource(R.string.error),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WhiteTextColor,
                )
                Text(
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 10.dp),
                    text = stringResource(R.string.vpn_usage_message),
                    fontSize = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = GrayTextColor
                )



                Spacer(modifier = Modifier.height(20.dp))


                CustomButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = R.string.reconnect,
                    onClick = {
                        isVpnConnected = checkVpnStatus(context = context)
                    },
                    shape = MaterialTheme.shapes.small
                )
            }
        }
    }


}

fun checkVpnStatus(context: Context): Boolean {
    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork
    val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
    return (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true)
}

