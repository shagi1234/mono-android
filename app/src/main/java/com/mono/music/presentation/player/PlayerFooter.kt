package com.mono.music.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mono.music.R
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleIconClickable

@Composable
fun PlayerFooter(onShowPlaylistBottomSheet: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier.scaleIconClickable { },
            onClick = { }
        ) {
            Icon(
                tint = WhiteTextColor,
                painter = painterResource(id = R.drawable.ic_cast),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }

        IconButton(
            modifier = Modifier.scaleIconClickable { onShowPlaylistBottomSheet() },
            onClick = {}
        ) {
            Icon(
                tint = WhiteTextColor,
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }
    }
}
