package com.mono.music.player.components

import android.view.Display.Mode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.music.R
import com.mono.music.domain.models.Playlist
import com.mono.music.domain.models.Song
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.utils.clickWithoutIndication
import com.mono.music.ui.utils.scaleIconClickable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopAppBar(
    selectedSong: Song?,
    onMoreClicked: () -> Unit,
    onPlaylistClicked: () -> Unit,
    goBack: () -> Unit,
    sourceId: Long? = null,
    sourceType: String? = null,
    sourceName: String? = null
) {


    Row( modifier = Modifier
        .fillMaxWidth().statusBarsPadding().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier.scaleIconClickable { goBack() },
            onClick = {}) {
            Icon(
                tint = WhiteTextColor,
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }

        selectedSong?.let {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f)
                    .clickWithoutIndication { onPlaylistClicked() },
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sourceName ?: selectedSong.albumName ?: "",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight(700),
                    ),maxLines = 1,
                    overflow = TextOverflow.Ellipsis,

                    )

                Text(
                    text = when {
                        sourceType != null -> ""
                        selectedSong.albumYear != null && selectedSong.albumYear != 0L ->
                            selectedSong.albumYear.toString()
                        else -> ""
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        fontFamily = SFFontFamily,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF828282),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }

        IconButton(
            modifier = Modifier.scaleIconClickable { onMoreClicked() },
            onClick = {}) {
            Icon(
                tint = WhiteTextColor,
                painter = painterResource(id = R.drawable.ic_more_hor),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }
    }



}