package com.mono.music.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.presentation.destinations.PrivacyPolicyScreenDestination
import com.mono.music.presentation.settings.components.SettingsElements
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(style = ScreenTransition::class)
@Composable
fun LegalScreen(
    navigator: DestinationsNavigator
) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_tariffs),
        contentDescription = "bg_legal",
        contentScale = ContentScale.FillBounds
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CollapsingSmallTopAppBar(
                title = stringResource(id = R.string.legal)
            ) {
                navigator.navigateUp()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGray, MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                SettingsElements(
                    icon = R.drawable.account_circle,
                    text = stringResource(id = R.string.privacy_policy),
                    expandable = true
                ) {
                    navigator.navigate(PrivacyPolicyScreenDestination)
                }

                SettingsElements(
                    icon = R.drawable.comments,
                    text = stringResource(id = R.string.terms_of_use),
                    expandable = true
                ) {
                    navigator.navigate(PrivacyPolicyScreenDestination)
                }

                SettingsElements(
                    icon = R.drawable.version,
                    text = stringResource(id = R.string.copyright_holders),
                    expandable = true
                ) {
                    navigator.navigate(PrivacyPolicyScreenDestination)
                }
            }
        }
    }
}
