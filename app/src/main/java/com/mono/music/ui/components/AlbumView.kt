package com.mono.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mono.music.domain.models.Playlist
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.DarkGray
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.TransparentColor
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleItemClickable


@Composable
fun AlbumView(
    album: Playlist,
    onClick: ()->Unit
) {

    val albumImagePainter = rememberAsyncImagePainter(
        model = album.getPlaylistImage(true)[0],
    )

    Row(modifier = Modifier
        .height(250.dp)
        .aspectRatio(1f)
    ) {
        Box(modifier = Modifier
            .scaleItemClickable { onClick() }
            .clip(shape = MaterialTheme.shapes.large)
        ){

            Image(
                modifier  = Modifier.fillMaxSize(),
                painter = albumImagePainter,
                contentDescription = "Album Image",
                contentScale = ContentScale.Crop
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TransparentColor, AlbumCoverBlackBG
                        )
                    )
                )
            )
            Row(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.padding(end = 10.dp).weight(1f)
                   ) {
                    Text(
                        text = album.name,
                        fontFamily = SFFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.getArtistsName(),
                        fontFamily = SFFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                Text(
                    text = album.year.toString(),
                    fontFamily = SFFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

}


@Composable
fun AlbumGridView(
    album: Playlist,
    onClick: ()->Unit
) {

    val albumImagePainter = rememberAsyncImagePainter(
        model = album.getPlaylistImage(true)[0],
    )

    Column(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable {
                onClick()
            }
    ) {
        Box(modifier = Modifier
            .clip(shape = MaterialTheme.shapes.large)
            .background(DarkGray)
            .fillMaxWidth()
            .aspectRatio(1f)
        ){

            Image(
                modifier  = Modifier.fillMaxSize(),
                painter = albumImagePainter,
                contentDescription = "Album Image",
                contentScale = ContentScale.Crop
            )
        }

        Text(
            modifier  = Modifier.padding(top = 10.dp),
            text = album.name,
            fontFamily = SFFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.getArtistsName(),
            fontFamily = SFFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrayTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}

