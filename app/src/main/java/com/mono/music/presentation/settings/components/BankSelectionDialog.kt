package com.mono.music.presentation.settings.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.Nameable
import com.mono.music.domain.models.PaymentMethod
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Border
import com.mono.music.ui.theme.DialogBackground
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily


@Composable
fun BankSelectionDialog(
    paymentMethods: List<PaymentMethod>,
    onDone: (PaymentMethod) -> Unit,
) {

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(DialogBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = stringResource(id = R.string.select_bank),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontFamily = SFFontFamily,
            fontSize = 20.sp
        )

        Text(
            modifier = Modifier
                .padding(top = 8.dp),
            text = stringResource(id = R.string.select_bank_desc),
            color = GrayTextColor,
                fontWeight = FontWeight.Normal,
            fontFamily = SFFontFamily,
            fontSize = 15.sp
        )


        Spacer(
            modifier = Modifier
                .height(20.dp)
        )

        Column(
            modifier = Modifier

        ) {
            for (bank in paymentMethods) {

                BankView(
                    bankName = bank.title,
                    bankDesc = bank.description,
                    onSelect = {
                        onDone(bank)
                    }
                )

                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )

            }
        }

    }


}


@Composable
fun BankView(
    bankName: String,
    bankDesc: String,
    onSelect: () -> Unit
) {

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .border(1.dp, Border, MaterialTheme.shapes.extraSmall)
            .clickable { onSelect() }
            .padding(12.dp, 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bankName,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 16.8.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                )
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = bankDesc,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = GrayTextColor,
                    textAlign = TextAlign.Start
                )
            )
        }


        Icon(
            painter = painterResource(id = R.drawable.ic_next),
            contentDescription = "song setting",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }

}

sealed class Bank(val value: String, @StringRes name: Int) : Nameable(name) {
    object ALTYN_ASYR : Bank("altyn_asyr", R.string.altyn_asyr)
    object RYSGAL : Bank("rysgal", R.string.rysgal)
    object SENAGAT : Bank("senagat", R.string.senagat)

}

val supportedBanks = listOf(
    Bank.ALTYN_ASYR,
    Bank.RYSGAL,
    Bank.SENAGAT,
)