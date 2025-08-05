package com.mono.music.presentation.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.ui.theme.DialogBackground
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.Red
import com.mono.music.ui.theme.Surface


@Composable
fun CharView(
    isTextFieldFocused: Boolean,
    isError: Boolean,
    index: Int,
    text: String
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemWidth = (screenWidth - 80.dp) / 6
    val isFocused = text.length == index  && isTextFieldFocused
    val char = when {
        index == text.length -> ""
        index > text.length -> ""
        else -> text[index].toString()
    }

    val border = if (isError) Red else if (isFocused || char.isNotEmpty() ) MaterialTheme.colorScheme.primary else DialogBackground

    val background =  DialogBackground
    Text(
        modifier = Modifier
            .width(itemWidth)
            .background(
                background, shape = MaterialTheme.shapes.extraSmall
            )
            .border(0.dp, border, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 15.dp, vertical = 20.dp),
        text = char,
        color = GrayTextColor,
        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W400),
        textAlign = TextAlign.Center
    )
}