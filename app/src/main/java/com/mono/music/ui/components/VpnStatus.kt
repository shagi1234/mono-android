package com.mono.music.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import coil.compose.rememberAsyncImagePainter
import com.mono.music.R
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.SurfaceSecond
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnStatus() {

    val context = LocalContext.current

    var isVpnConnected by remember { mutableStateOf(false) }

    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork
    val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)

    LaunchedEffect(Unit) {
        if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            isVpnConnected = true
        }
    }


    val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    if (isVpnConnected){
        ModalBottomSheet(
            containerColor = AlbumCoverBlackBG,
            sheetState = playlistSheetState,
            onDismissRequest = {
                isVpnConnected = false
            },
            dragHandle = {}
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                        ),
                    text = "\uD83D\uDE30",
                    fontSize = 40.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WhiteTextColor,
                )

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
                    text = R.string.cancel,
                    onClick = {  isVpnConnected = false },
                    containerColor = SurfaceSecond,
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.small
                )
            }
        }
    }
}

