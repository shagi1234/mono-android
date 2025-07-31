package com.mono.music.ui.components.listview

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.Song
import com.mono.music.presentation.home.HomeViewModel
import com.mono.music.ui.components.SongView
import com.mono.music.ui.theme.SFFontFamily


@Composable
fun SongGridListView(
    homeViewModel: HomeViewModel,
    songs: List<Song>,
    onMoreClicked: (Song) -> Unit,
    onClick: (Song) -> Unit,
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    if (songs.isNotEmpty()) {
        Column {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(id = R.string.hit_songs),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 20.8.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            )

            LazyHorizontalGrid(
                modifier = Modifier.height(300.dp),
                rows = GridCells.Fixed(4),
                contentPadding = PaddingValues(20.dp, 15.dp)
            ) {
                items(songs, key = {song -> song.songId}) { song ->
                    SongView(
                        modifier = Modifier.width(screenWidth * 0.90f),
                        playerController = homeViewModel.getPlayerController(),
                        song = song,
                        downloadTracker = homeViewModel.getDownloadTracker(),
                        onMoreClicked = { onMoreClicked(song) }
                    ) {
                        onClick(song)
                    }
                }
            }
        }
    }
}