package com.mono.music.ui.components.listview

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.domain.models.Artist
import com.mono.music.ui.components.ArtistView
import com.mono.music.ui.theme.SFFontFamily


@Composable
fun ArtistListView(
    @StringRes header: Int,
    artists: List<Artist>,
    onClick: (Artist) -> Unit
) {
    if (artists.isNotEmpty()) {
        Column {

            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(id = header),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 20.8.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            )


            Column(
                Modifier.padding(vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (artist in artists) {
                    ArtistView(artist) { onClick(artist) }
                }
            }
        }
    }
}