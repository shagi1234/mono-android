package com.mono.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.presentation.destinations.LoginScreenDestination
import com.mono.music.presentation.settings.SettingsViewModel
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.FormularFontFamily
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.Red
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun SubsExpiredView(
    onNavigateToTariffs: () -> Unit
) {

    val settingsViewModel = hiltViewModel<SettingsViewModel>()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.BottomCenter,

        ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = "bg_free_plan",
            contentScale = ContentScale.FillBounds
        )


        Column(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 90.dp),
                painter = painterResource(id = R.drawable.ic_mono_logo_big),
                contentDescription = "bg_free_plan",
                contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier
                    .padding(top = 90.dp)
                    .width(240.dp),
                text = stringResource(R.string.subs_has_ended),
                fontFamily = FormularFontFamily,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.choose_subs_desc),
                fontFamily = FormularFontFamily,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
                color = GrayTextColor,
                textAlign = TextAlign.Center
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = MaterialTheme.shapes.small,
                onClick = { onNavigateToTariffs() },
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonColors(
                    containerColor = Yellow,
                    contentColor = AlbumCoverBlackBG,
                    disabledContentColor = AlbumCoverBlackBG,
                    disabledContainerColor = Yellow,
                )
            ) {
                Text(
                    text = stringResource(id = R.string.connect_service),
                    color = AlbumCoverBlackBG,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FormularFontFamily
                )
            }

            Text(
                modifier = Modifier
                    .padding(vertical = 21.dp)
                    .clickable { settingsViewModel.logout() },
                text = stringResource(id = R.string.log_out),
                fontSize = 18.sp,
                fontFamily = FormularFontFamily,
                fontWeight = FontWeight.Bold,
                color = Red
            )

        }
    }
}