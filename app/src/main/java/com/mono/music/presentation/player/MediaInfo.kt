package com.mono.music.presentation.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.domain.models.Artist
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.clickWithoutIndication

@Composable
fun MediaInfo(
    playerController: PlayerController,
    navigateToArtist: (Artist) -> Unit,
    onShowArtistDialog: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    isInPlaylist: Boolean = false
) {

    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .padding(end = 10.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.basicMarquee(
                    // Animate forever.
                    iterations = Int.MAX_VALUE,
                ),
                text = playerController.selectedTrack?.name ?: "",
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickWithoutIndication {
                        if (playerController.selectedTrack?.artists?.size == 1) {
                            playerController.selectedTrack
                                ?.getArtist()
                                ?.let { navigateToArtist(it) }
                        } else {
                            onShowArtistDialog()
                        }
                    },
                text = playerController.selectedTrack?.getArtistsName() ?: "",
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                fontFamily = SFFontFamily,
                fontWeight = FontWeight.Normal,
                color = WhiteTextColor
            )
        }
        IconButton(onClick = {
            onAddToPlaylistClick()
        }) {
            Icon(
                modifier = Modifier.size(24.dp),
                tint = if (isInPlaylist) {
                    Yellow
                } else {
                    WhiteTextColor
                },
                painter = painterResource(
                    id = if (isInPlaylist) {
                        R.drawable.library_add_check // Icon for already in playlist
                    } else {
                        R.drawable.library_add // Icon for not in playlist
                    }
                ),
                contentDescription = null
            )
        }
    }
}