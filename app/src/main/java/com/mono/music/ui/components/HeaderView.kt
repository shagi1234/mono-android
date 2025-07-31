package com.mono.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.clickWithoutIndication


@Composable
fun HeaderView(
    modifier: Modifier = Modifier,
    mainText: String,
    expandable: Boolean,
    trailingIcon: Int = R.drawable.right_arrow,
    onClick: () -> Unit
) {
    Row(modifier = modifier
        .clickWithoutIndication { onClick() }
        .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {

        Text(
            modifier = Modifier.weight(1f),
            text = mainText,
            style = TextStyle(
                fontSize = 20.sp,
                lineHeight = 20.8.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
            )
        )

        if (expandable) {
            Icon(
//                modifier = Modifier.size(24.dp),
                painter = painterResource(id = trailingIcon),
                contentDescription = "See more icon",
                tint = WhiteTextColor
            )
        }
    }
}



