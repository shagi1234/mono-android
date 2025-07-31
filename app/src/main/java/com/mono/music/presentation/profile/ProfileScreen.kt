package com.mono.music.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.components.CustomDatePickerDialog
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.clearFocusOnKeyboardDismiss
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(style = ScreenTransition::class)
@Composable
fun ProfileScreen(
    navigator: DestinationsNavigator
) {

    val profileViewModel = hiltViewModel<ProfileViewModel>()

    val uiState by profileViewModel.uiState.collectAsState()

    val username by profileViewModel.name.observeAsState("")
    val phone by profileViewModel.phone.collectAsState("")
    val selectedGender by profileViewModel.gender.observeAsState()
    val birthday by profileViewModel.birthday.observeAsState("")


    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    var isNameFieldError by remember {
        mutableStateOf(false)
    }
    val isUserNameValid = username.isNotEmpty()

    var isBirthdayFieldError by remember {
        mutableStateOf(false)
    }
    val isBirthdayValid = birthday.isNotEmpty()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved){
            navigator.navigateUp()
        }
    }
    Scaffold { _ ->

        if (uiState.loading) {

            Dialog(
                onDismissRequest = { },
            ) {
                LoadingView(
                    Modifier.fillMaxSize()
                )
            }


        } else if (uiState.loading) {

        } else {
            Column(
                Modifier
                    .padding(top = 20.dp, bottom = 100.dp, start = 20.dp, end = 20.dp)
                    .verticalScroll(rememberScrollState()),
//                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.my_profile),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = SFFontFamily
                )

                Text(
                    text = stringResource(id = R.string.manage_your_profile_easily),
                    color = GrayTextColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    fontFamily = SFFontFamily
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.username),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = SFFontFamily
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .clearFocusOnKeyboardDismiss()
                        .padding(top = 7.dp),
                    value = username,
                    shape = MaterialTheme.shapes.medium,
                    onValueChange = {
                        profileViewModel.setName(it.trim())
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                        disabledContainerColor = Surface,
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
                            text = stringResource(id = R.string.username), style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontFamily = SFFontFamily,
                                color = GrayTextColor,
                            )

                        )
                    },
                    maxLines = 1,
//                keyboardActions = KeyboardActions(
//                    onDone = {
//
//                    },
//                ),
                )

                if (isNameFieldError) {
                    Text(
                        text = stringResource(R.string.must_not_be_empty),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.phone_number),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = SFFontFamily
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .clearFocusOnKeyboardDismiss()
                        .padding(top = 7.dp),
                    value = "+993$phone",
                    shape = MaterialTheme.shapes.medium,
                    enabled = false,
                    onValueChange = {},
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                        disabledContainerColor = Surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Yellow
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Default, keyboardType = KeyboardType.Number

                    ),
                    maxLines = 1,
//                keyboardActions = KeyboardActions(
//                    onDone = {
//
//                    },
//                ),
                )


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.birthday),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = SFFontFamily
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Surface)
                        .clickable {
                            showDialog = true
                        }, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(20.dp), text = birthday, style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontFamily = SFFontFamily,
                            color = GrayTextColor,
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
                Row(Modifier.selectableGroup()) {
                    genders.forEach { gender ->
                        Row(
                            Modifier, verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (gender.value == selectedGender), onClick = {
                                profileViewModel.setGender(gender)
                            })
                            Text(text = stringResource(id = gender.name), fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                CustomButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    text = R.string.save,
                    onClick = {

                        if (!isUserNameValid) {
                            isNameFieldError = true
                        } else if(!isBirthdayValid) {
                            isBirthdayFieldError = true
                        }else{
                            profileViewModel.updateUserData(
                                name = username,
                                gender = selectedGender ?: Gender.MALE.value,
                                birthday = birthday
                            )
                        }

                    },
                    enabled = isUserNameValid && isBirthdayValid ,
                    containerColor = Yellow,
                    contentColor = AlbumCoverBlackBG,
                    shape = MaterialTheme.shapes.small

                )
            }
        }


        if (showDialog) {
            CustomDatePickerDialog(label = stringResource(id = R.string.birthday),
                onDismissRequest = {
                    showDialog = false

                },
                onSelect = { birthday ->
                    profileViewModel.setBirthday(birthday)
                })
        }

    }
}
