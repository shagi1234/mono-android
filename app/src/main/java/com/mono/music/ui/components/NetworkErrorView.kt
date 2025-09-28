package com.mono.music.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.MusifyTheme
import com.mono.music.ui.theme.WhiteTextColor


@Composable
fun NetworkErrorView(
    modifier : Modifier = Modifier,
    onRetry : () -> Unit
){

    Box(modifier = modifier
        .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.network_error),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 16.sp),
                maxLines = 1,
                fontWeight = FontWeight.Normal
            )


            Spacer(Modifier.height(20.dp))

            CustomButton(
                modifier=  Modifier.fillMaxWidth(0.5f),
                contentColor = MaterialTheme.colorScheme.background,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large,
                text = R.string.refresh

            ) {

                onRetry()
            }
//            Button(
//
//                onClick = { onRetry() },
//                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = MaterialTheme.colorScheme.background,
//                    containerColor = MaterialTheme.colorScheme.primary
//                ),
//                shape = MaterialTheme.shapes.large,
//            ) {
//                Text(
//                    text = stringResource(R.string.refresh),
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = Background
//                )
//
//            }
        }


    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview
fun NetworkErrorViewPreview(){
    MusifyTheme  {
     Scaffold(
     ) {
         NetworkErrorView {  }
     }
    }

}