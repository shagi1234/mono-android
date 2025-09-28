package com.mono.music.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.navigation.screen.LoginNavGraph
import com.mono.music.presentation.destinations.LoginScreenDestination
import com.mono.music.presentation.destinations.TariffsScreenDestination
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.NetworkErrorView
import com.mono.music.ui.components.SubsExpiredView
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.FormularFontFamily
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.navigate


@LoginNavGraph(start = true)
@Destination
@Composable
fun OnBoardingScreen(
    navigator: DestinationsNavigator? = null,
) {
//    SubsExpiredView {
////        navController.navigate(TariffsScreenDestination)

    NetworkErrorView {  }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        NetworkErrorView {  }
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.bg_free_plan),
            contentDescription = "bg_free_plan",
            contentScale = ContentScale.FillBounds
        )


        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding() + 30.dp,
                    bottom = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 54.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mono_logo_big),
                contentDescription = "bg_free_plan",
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.enjoy_music).uppercase(),
                fontFamily = FormularFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                overflow = TextOverflow.Ellipsis
            )


            CustomButton(
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth(1f),
                shape = MaterialTheme.shapes.small,
                containerColor = Yellow,
                contentColor = AlbumCoverBlackBG,
                text = R.string.connect_service
            ) {

                navigator?.navigate(LoginScreenDestination)
            }

//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 20.dp),
//                shape = MaterialTheme.shapes.small,
//                onClick = {
//                    navigator?.navigate(LoginScreenDestination)
//                },
//                contentPadding = PaddingValues(all = 16.dp),
//                colors = ButtonColors(
//                    containerColor = Yellow,
//                    contentColor = AlbumCoverBlackBG,
//                    disabledContentColor = AlbumCoverBlackBG,
//                    disabledContainerColor = Yellow,
//                )
//            ) {
//                Text(
//                    text = stringResource(id = R.string.connect_service),
//                    color = AlbumCoverBlackBG,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp,
//                    fontFamily = SFFontFamily
//                )
//
//            }
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnBoardingScreenPreview() {
    OnBoardingScreen()
}