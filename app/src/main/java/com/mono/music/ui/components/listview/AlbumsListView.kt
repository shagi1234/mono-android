package com.mono.music.ui.components.listview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.Playlist
import com.mono.music.ui.components.AlbumView
import com.mono.music.ui.components.HeaderView
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.SFFontFamily


@Composable
fun AlbumListView(
    title: String = stringResource(id = R.string.albums),
    playlists: List<Playlist>,
    expandable:Boolean = false,
    showHeader: Boolean = true,
    navigateToAlbums: () -> Unit = {},
    onClick: (Playlist) -> Unit,
) {
    if (playlists.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Background)
        ) {

            if (showHeader) {
                HeaderView(
                    modifier = Modifier.padding(top = 10.dp),
                    mainText = title,
                    expandable = expandable
                ){
                    navigateToAlbums()
                }
            }

            LazyRow(
                contentPadding = PaddingValues(20.dp, 15.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists) { playlist ->
                    AlbumView(playlist) { onClick(playlist) }
                }
            }
        }


    }
}
