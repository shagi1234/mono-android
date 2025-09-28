package com.mono.music.presentation.myplaylist

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.MainActivity.Companion.ALBUM
import com.mono.music.MainActivity.Companion.ALL
import com.mono.music.MainActivity.Companion.ALL_SEARCH
import com.mono.music.MainActivity.Companion.ARTIST
import com.mono.music.MainActivity.Companion.PLAYLIST
import com.mono.music.MainActivity.Companion.SONG
import com.mono.music.R
import com.mono.music.domain.models.Playlist
import com.mono.music.presentation.destinations.LocalPlaylistScreenDestination
import com.mono.music.presentation.player.NewPlaylistDialog
import com.mono.music.ui.components.LocalPlayListView
import com.mono.music.ui.theme.Inactive
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.Surface
import com.mono.music.ui.theme.WhiteTextColor
import com.mono.music.ui.theme.Yellow
import com.mono.music.ui.utils.HapticType
import com.mono.music.ui.utils.scaleIconClickable
import com.mono.music.ui.utils.scaleItemClickable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@Destination
@Composable
fun MyPlaylistsScreen(
    navigator: DestinationsNavigator
) {
    val myPlaylistsViewModel = hiltViewModel<MyPlaylistsViewModel>()
    val uiState by myPlaylistsViewModel.uiState.collectAsState()

    var showNewPlaylistDialog by remember {
        mutableStateOf(false)
    }

    var selectedPlaylist by remember {
        mutableStateOf<Playlist?>(null)
    }
    val playlists by myPlaylistsViewModel.playlists.collectAsState(initial = emptyList())

    val type by myPlaylistsViewModel.type.collectAsState("")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sections = listOf(
        LibrarySection.All,
        LibrarySection.Playlist,
        LibrarySection.Album,
        LibrarySection.Downloads
    )

    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    uiState.message!!
                )
            }
            myPlaylistsViewModel.updateToDefault()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.my_playlists),
                    fontSize = 22.sp,
                    color = WhiteTextColor,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    modifier = Modifier.scaleIconClickable {  showNewPlaylistDialog = true },
                    onClick = { },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.library_add),
                        contentDescription = "new playlist",
                        tint = WhiteTextColor
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Inactive,
                    contentColor = WhiteTextColor,
                    snackbarData = data
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            LibraryContentChipsView(
                sections = sections,
                selected = type
            ) { type ->
                myPlaylistsViewModel.setType(type)
            }
            LazyColumn(
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
            ) {
                items(playlists) { playlist ->
                    LocalPlayListView(playlist = playlist, count = playlist.songsCount, onEdit = {
                        showNewPlaylistDialog = true
                        selectedPlaylist = playlist
                    },
                        onDelete = {
                        myPlaylistsViewModel.deletePlaylist(playlist)
                    }, selectPlaylist = {
                        navigator.navigate(LocalPlaylistScreenDestination(playlist.playlistId))
                    })


                }
            }
        }





        if (showNewPlaylistDialog) {
            Dialog(onDismissRequest = {
                showNewPlaylistDialog = false
                selectedPlaylist = null
            }) {
                NewPlaylistDialog(
                    playlist = selectedPlaylist,
                    onEdit = { playlist ->
                        showNewPlaylistDialog = false
                        myPlaylistsViewModel.updatePlaylist(playlist)
                        selectedPlaylist = null
                    },

                    ) { name ->
                    showNewPlaylistDialog = false
                    myPlaylistsViewModel.addNewPlaylist(name)


                }
            }

        }
    }


}

@Composable
fun LibraryContentChipsView(
    sections: List<LibrarySection>,
    selected: String,
    onClick: (String) -> Unit
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {

        itemsIndexed(sections) { index, section ->
            val containerColor = if (selected == section.value) Yellow else Surface
            val contentColor =
                if (selected == section.value) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground


            Text(
                modifier = Modifier
                    .scaleItemClickable(hapticType = HapticType.LIGHT) {
                        onClick(section.value)
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                    .clip(MaterialTheme.shapes.extraSmall)

                    .background(containerColor)
                    .padding(20.dp, 10.dp),
                text = stringResource(id = section.title),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(500),
                    fontFamily = SFFontFamily,
                    color = contentColor

                )

            )
        }

    }
}

sealed class LibrarySection(
    @StringRes val title: Int,
    val value: String,
) {
    object All : LibrarySection(
        R.string.all, ALL
    )

    object Playlist : LibrarySection(
        R.string.playlist, PLAYLIST
    )


    object Album : LibrarySection(
        R.string.album, ALBUM
    )

    object Song : LibrarySection(
        R.string.song, SONG
    )

    object Artist : LibrarySection(
        R.string.artist, ARTIST
    )

    object Downloads : LibrarySection(
        R.string.downloads, "DOWNLOADS"
    )
}

sealed class SearchLibrarySection(
    @StringRes val title: Int,
    val value: String,
) {
    object AllSearch : LibrarySection(
        R.string.all, ALL_SEARCH
    )


    object Playlist : LibrarySection(
        R.string.playlist, "playlist"
    )


    object Album : LibrarySection(
        R.string.album, "album"
    )


    object Song : LibrarySection(
        R.string.song, SONG
    )

    object Artist : LibrarySection(
        R.string.artist, ARTIST
    )
}
