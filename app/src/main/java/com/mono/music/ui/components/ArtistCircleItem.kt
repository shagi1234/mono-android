package com.mono.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mono.music.domain.models.Artist
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.scaleItemClickable

@Composable
fun ArtistCircleItem(
    artist: Artist,
    modifier: Modifier = Modifier,
    onArtistClick: (Artist) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .scaleItemClickable { onArtistClick(artist) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = artist.getArtistImage()),
            contentDescription = artist.name,
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.run { height(8.dp) })

        Text(
            text = artist.name,
            style = TextStyle(
                color = WhiteTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(80.dp)
        )
    }
}