package com.mono.music.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.Option
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.TransparentColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow

@Composable
fun SubscriptionOptionView(
    option: Option,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(15.dp, 13.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_three_circle),
            contentDescription = "button",
            tint = WhiteTextColor
        )


        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = option.name,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                text = stringResource(id = R.string.manats, option.price),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary
            )
        }



        Icon(
            modifier = Modifier
                .size(14.dp),
            painter = painterResource(id = R.drawable.right_arrow),
            contentDescription = "See more icon",
            tint = WhiteTextColor
        )


    }
}


@Composable
fun UpdatedSubscriptionOptionView(
    selected: Boolean,
    option: Option,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, if (selected) Yellow else TransparentColor, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top) {

        Icon(
            modifier = Modifier.size(24.dp),
            painter = if (selected) painterResource(id = R.drawable.ic_check_circle_active) else painterResource(
                id = R.drawable.ic_check_circle
            ),
            contentDescription = "button",
            tint = Color.Unspecified
        )


        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Text(
                text = option.name,
                fontSize = 20.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = R.string.days, option.days),
                fontSize = 15.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Normal,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = GrayTextColor
            )

            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = stringResource(id = R.string.manats, option.price),
                fontSize = 20.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary
            )
        }


    }
}


@Composable
fun FreeSubscriptionOptionView(
    selected: Boolean,
    option: Option,
    onClick: () -> Unit
) {
    Box {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.small),
            painter = painterResource(id = R.drawable.bg_gift_box),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, if (selected) Yellow else TransparentColor, MaterialTheme.shapes.medium)
                .clickable { onClick() }
                .padding(12.dp, 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top) {

            Icon(
                modifier = Modifier.size(24.dp),
                painter = if (selected) painterResource(id = R.drawable.ic_check_circle_active) else painterResource(
                    id = R.drawable.ic_check_circle
                ),
                contentDescription = "button",
                tint = MaterialTheme.colorScheme.background

            )


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.name,
                    fontSize = 20.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(id = R.string.days, option.days),
                    fontSize = 15.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Normal,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.background
                )

            }


        }
    }

}

@Preview
@Composable
fun PaymentItemPreview() {
    val mockOption = Option(
        id = 0L,
        name = "Premium 1 months",
        image = "", days = 30,
        price = 15
    )
    FreeSubscriptionOptionView(
        option = mockOption, selected = true

    ) {}

}