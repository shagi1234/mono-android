package com.mono.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.domain.models.Playlist
import com.mono.music.presentation.localplaylist.LocalPlaylistViewModel
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable


@Composable
fun LocalPlayListView(
    playlist: Playlist,
    count: Int,
    isFavorites: Boolean = false,
    selectPlaylist: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit
) {
    val localPlaylistViewModel = hiltViewModel<LocalPlaylistViewModel>()

    val play by localPlaylistViewModel.getPlaylist(playlist.playlistId, isFavorites)
        .collectAsState(initial = null)

    var expanded by remember {
        mutableStateOf(false)
    }

    val type =
        if (playlist.isAlbum) stringResource(id = R.string.album) else stringResource(id = R.string.playlist)

    Row(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.small)
            .scaleItemClickable { selectPlaylist() }
            .padding(vertical = 5.dp)
            .padding(start = 20.dp, end = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically) {

        if (isFavorites)
            Image(
                painter = painterResource(id = R.drawable.fav_playlist_imag),
                contentDescription = "Favorites image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop // how the image is scaled
            )
        else

            ImageGrid(
                imageUrls = play?.getPlaylistImage() ?: emptyList(),
                fraction = 1f,
                isSmall = true
            )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
            Text(
                text = playlist.name,
                fontFamily = SFFontFamily,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.library_add_check),
                    contentDescription = "playlist",
                    tint = if (playlist.downloadable) Yellow else Color.Unspecified
                )


                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = type,
                    fontFamily = SFFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(3.dp)
                        .background(GrayTextColor, CircleShape)
                )

                Text(
                    text = stringResource(id = R.string.song_count, count.toString()),
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
        if (!isFavorites)
            Box {

                IconButton(
                    modifier = Modifier.scaleIconClickable { expanded = true },
                    onClick = { },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.song_setting),
                        contentDescription = "playlist setting",
                        tint = WhiteTextColor
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    if (!playlist.isBuiltin) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.edit)) },
                            onClick = {
                                onEdit()
                                expanded = false
                            })

                    }
                    DropdownMenuItem(

                        text = { Text(text = stringResource(R.string.delete)) },
                        onClick = {
                            onDelete()
                            expanded = false
                        })
                }
            }

    }
}