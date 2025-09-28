package com.mono.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mono.music.R
import com.mono.music.domain.models.Playlist
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleItemClickable

@Composable
fun PlaylistView(
    playlist: Playlist,
    onClick: () -> Unit
) {

    val playlistImagePainter = rememberAsyncImagePainter(
        model = playlist.getPlaylistImage(true)[0],
    )

    Column(
        modifier = Modifier
            .width(170.dp)
            .scaleItemClickable { onClick() }
            .clip(shape = MaterialTheme.shapes.medium)
            .background(AlbumCoverBlackBG)
    ) {

        Image(
            modifier = Modifier
                .size(170.dp)
                .background(DarkGray),
            painter = playlistImagePainter,
            contentDescription = "Album Cover",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = playlist.name,
                fontFamily = SFFontFamily,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.song_count, playlist.songsCount.toString()),
                fontFamily = SFFontFamily,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GrayTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }

}


