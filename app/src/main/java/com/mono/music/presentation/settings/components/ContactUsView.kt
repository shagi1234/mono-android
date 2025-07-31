package com.mono.music.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow


@Composable
fun ContactUsView(
    onDone: (String) -> Unit,
) {

    var text by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(AlbumCoverBlackBG)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.write_to_us),

            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = Color(0xFFD3D3D3),

                )
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            value = text,
            onValueChange = {
                if (it.length <= 250) {
                    text = it
                }
            },

            maxLines = 4,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight(400),
                color = WhiteTextColor,
            ),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.write_to_us),
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight(700),
                        color = Color.DarkGray,
                    )

                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhiteTextColor,
                unfocusedTextColor = WhiteTextColor,
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface,
                focusedBorderColor = Surface,
                unfocusedBorderColor = Surface,
                disabledBorderColor = Surface,

                ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            shape = MaterialTheme.shapes.small,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()

            }),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onDone(text)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Yellow.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.background
            ),
            enabled = text.isNotEmpty(),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 40.dp)
        ) {
            Text(
                text = stringResource(id = R.string.send),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colorScheme.background,
                )

            )

        }
    }


}