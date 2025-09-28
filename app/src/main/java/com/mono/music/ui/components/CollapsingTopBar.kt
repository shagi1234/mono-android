package com.mono.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleIconClickable


@Composable
fun CollapsingSmallTopAppBar(
    title: String,
    trailingIcon: Int? = null,
    navigationIcon: Int = R.drawable.left_arrow,
    onIconClick: () -> Unit = {},
    goBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth().background(Color.Transparent).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.scaleIconClickable {
                goBack()
            },
            onClick = { }) {
            Icon(
                tint = WhiteTextColor,
                painter = painterResource(id = navigationIcon),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = WhiteTextColor,
            fontFamily = SFFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (trailingIcon != null) {
            IconButton(
                modifier = Modifier.padding(5.dp, 20.dp).scaleIconClickable { onIconClick() },
                onClick = {  },
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = trailingIcon),
                    tint = WhiteTextColor,
                    contentDescription = ""
                )
            }
        }


    }


}