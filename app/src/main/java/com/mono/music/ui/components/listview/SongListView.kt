package com.mono.music.ui.components.listview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.PlayerController
import com.mono.music.R
import com.mono.music.domain.models.Song
import com.mono.music.player.DownloadTracker
import com.mono.music.ui.components.SongView
import com.mono.music.ui.components.SwipeableSongView
import com.mono.music.ui.components.swipe.SwipeAction
import com.mono.music.ui.components.swipe.SwipeableActionsBox
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.Background
import com.mono.music.ui.theme.SFFontFamily


@Composable
fun SongListView(
    songs: List<Song>,
    playerController: PlayerController,
    downloadTracker: DownloadTracker? = null,
    onMoreClicked: (Song) -> Unit,
    onSwipe: (Song) -> Unit,
    onClick: (Song) -> Unit,
) {

    if (songs.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().background(Background)) {

            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(id = R.string.songs),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            )

            Column(
                Modifier.padding(vertical = 15.dp),
            ) {
                songs.forEach { song ->

                    SwipeableSongView(
                        song = song,
                        playerController = playerController,
                        onMoreClicked = { onMoreClicked(song) },
                        downloadTracker = downloadTracker,

                        onSwipe = { onSwipe(song) }
                    ) {
                        onClick(song)
                    }
                }
            }
        }
    }
}