package com.mono.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow


@Composable
fun CustomButton(
    modifier: Modifier,
    text: Int,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    shape: Shape = MaterialTheme.shapes.large
) {
    Button(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = containerColor,
            disabledContainerColor = containerColor,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            leadingIcon?.let {
                Icon(
                    painter = painterResource(id = leadingIcon),
                    tint = contentColor,
                    contentDescription = "Shuffle"
                )
            }


            Text(
                text = stringResource(id = text),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontFamily = SFFontFamily
            )

            trailingIcon?.let {
                Icon(
                    painter = painterResource(id = trailingIcon),
                    tint = contentColor,
                    contentDescription = "Shuffle"
                )
            }
        }

    }
}

@Composable
fun CustomMiniButton(
    modifier: Modifier = Modifier,
    text: Int? = null,
    onClick: () -> Unit,
    containerColor: Color = Inactive,
    contentColor: Color = Yellow,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    shape: Shape = MaterialTheme.shapes.extraSmall
) {
    Button(
        modifier = if (text == null) modifier.width(40.dp) else modifier,
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        contentPadding = if (text != null) PaddingValues(
            12.dp,
            0.dp,
            20.dp,
            0.dp
        ) else PaddingValues(0.dp),
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = containerColor,
            disabledContainerColor = containerColor,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            leadingIcon?.let {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    painter = painterResource(id = leadingIcon),
                    tint = contentColor,
                    contentDescription = "Shuffle"
                )
            }

            text?.let {
                Text(
                    text = stringResource(id = text),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFFontFamily
                )
            }
        }
    }
}
