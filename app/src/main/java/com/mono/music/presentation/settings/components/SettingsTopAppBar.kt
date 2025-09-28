package com.mono.music.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.User
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleItemClickable


@Composable
fun SettingsTopAppBar (
    name:String,
    validUntil:String,
    onClick: () ->Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGray, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .scaleItemClickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier,
                text = name,
                lineHeight = 20.sp,
                fontSize = 20.sp,
                color = WhiteTextColor,
                fontWeight = FontWeight.Bold
            )
            Row (verticalAlignment = Alignment.CenterVertically){
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.premium_user),
                    lineHeight = 14.sp,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = validUntil,
                    lineHeight = 12.sp,
                    fontSize = 12.sp,
                    color = GrayTextColor,
                    fontWeight = FontWeight.Normal
                )
            }

        }


    }

}