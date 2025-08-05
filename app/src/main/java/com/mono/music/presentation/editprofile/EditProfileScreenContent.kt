package com.mono.music.presentation.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.CustomDatePickerDialog
import com.mono.music.ui.components.clearFocusOnKeyboardDismiss
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.DialogBackground
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun EditProfileScreenContent(
    username: String,
    birthday: String,
    navigator: DestinationsNavigator? = null,
    onBirthdayChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSaveButtonClick: () -> Unit

) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isNameFieldError by remember { mutableStateOf(false) }
    var isBirthdayFieldError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.safeDrawingPadding()
    )
    {
        Column(
            Modifier
                .padding(all = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Image(
                modifier = Modifier
                    .padding(top = 74.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.ic_mono_logo_big),
                contentDescription = "ic_mono_logo_big",
                contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(0.8f),
                text = stringResource(id = R.string.finish_register),
                color = WhiteTextColor,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = stringResource(id = R.string.finish_register_desc),
                color = GrayTextColor,
                style = TextStyle(
                    fontFamily = SFFontFamily,
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.8f)
                    .align(alignment = Alignment.Start)
            )


            Text(
                modifier = Modifier.padding(top = 36.dp),
                text = stringResource(R.string.birthday),
                color = WhiteTextColor,
                style = TextStyle(
                    fontFamily = SFFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(DialogBackground)
                    .clickable {
                        isBirthdayFieldError = false
                        showDatePicker = true
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = birthday.ifEmpty { stringResource(R.string.select_your_birthday) },
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight.W400,
                        color = if (birthday.isNotEmpty()) WhiteTextColor else GrayTextColor,
                    )
                )
            }

            if (isBirthdayFieldError) {
                Text(
                    text = stringResource(R.string.must_not_be_empty),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }


            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearFocusOnKeyboardDismiss()
                    .padding(top = 20.dp),
                value = username,
                shape = MaterialTheme.shapes.extraSmall,
                onValueChange = { newValue ->
                    onUsernameChange(newValue.trim())
                    isNameFieldError = false

                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = DialogBackground,
                    focusedContainerColor = DialogBackground,
                    disabledContainerColor = DialogBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    cursorColor = Yellow
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Default, keyboardType = KeyboardType.Text

                ),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.username),
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontFamily = SFFontFamily,
                            fontWeight = FontWeight.W400,
                            color = GrayTextColor,
                        )

                    )
                },
                maxLines = 1,
            )

            if (isNameFieldError) {
                Text(
                    text = stringResource(R.string.must_not_be_empty),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            CustomButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                text = R.string.save,
                onClick = {
                    when {
                        birthday.isEmpty() -> {
                            isBirthdayFieldError = true
                        }

                        username.isEmpty() -> {
                            isNameFieldError = true
                        }

                        else -> {
                            onSaveButtonClick()
                        }
                    }
                },
                containerColor = Yellow,
                contentColor = AlbumCoverBlackBG,
                shape = MaterialTheme.shapes.small

            )
        }

        IconButton(
            modifier = Modifier
                .padding(vertical = 10.dp),
            onClick = {
                navigator?.navigateUp()
            }) {
            Icon(
                tint = Color.White,
                painter = painterResource(id = R.drawable.left_arrow),
                contentDescription = null
            )
        }

        if (showDatePicker) {
            CustomDatePickerDialog(
                label = stringResource(id = R.string.birthday),
                onDismissRequest = {
                    showDatePicker = false
                },
                onSelect = { selectedBirthday ->
                    onBirthdayChange(selectedBirthday)
                    showDatePicker = false
                }
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfilePreview() {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.bg_login),
        contentDescription = "bg_login",
        contentScale = ContentScale.FillBounds
    )
    EditProfileScreenContent(
        username = "John Doe",
        birthday = " ",
        onBirthdayChange = {},
        onUsernameChange = {}
    ) { }
}
