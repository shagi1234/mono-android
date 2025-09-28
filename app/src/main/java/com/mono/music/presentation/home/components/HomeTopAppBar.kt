package com.mono.music.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mono.music.R
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.ICON_PRESS
import com.mono.music.ui.utils.scaleIconClickable


@Composable
fun HomeTopAppBar(
    onSettingsClicked: () -> Unit,
    onSearchClicked: () -> Unit,
) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp, top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)

    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_mono_logo),
            contentDescription = "ic_mono_logo",
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.weight(1f))


        IconButton(
            modifier = Modifier.scaleIconClickable (pressValue = ICON_PRESS, onClick = onSearchClicked),
            onClick = {},
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_explore),
                contentDescription = "playlist setting",
                tint = WhiteTextColor
            )
        }

        IconButton(
            modifier = Modifier.scaleIconClickable(onClick = onSettingsClicked),
            onClick = {},
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "playlist setting",
                tint = WhiteTextColor
            )
        }
    }

}