package com.mono.music.ui.components.bottomsheet

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mono.music.MainViewModel
import com.mono.music.R
import com.mono.music.domain.models.Song
import com.mono.music.presentation.playlist.PlaylistViewModel
import com.mono.music.presentation.settings.findActivity
import com.mono.music.ui.components.ActionsModelView
import com.mono.music.ui.components.CustomButton
import com.mono.music.ui.theme.AlbumCoverBlackBG
import com.mono.music.ui.theme.GrayTextColor
import com.mono.music.ui.theme.SFFontFamily
import com.mono.music.ui.theme.SurfaceSecond
import com.mono.music.ui.theme.WhiteTextColor
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrackBottomSheet(
    deletable: Boolean = false,
    selectedSong: Song,
    songSettingsSheetState: SheetState,
    onAddToPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onNavigateToArtist: () -> Unit,
    onNavigateToAlbum: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit = {},
    onDismiss: () -> Unit
) {

//    val mainViewModel = hiltViewModel<MainViewModel>()

    val context = LocalContext.current
    val activity = context.findActivity_()
    val mainViewModel = hiltViewModel<MainViewModel>(activity)


    val likeState by mainViewModel.likeStates.collectAsState()
    val liked = likeState[selectedSong.songId] ?: selectedSong.isLiked ?: false




    ModalBottomSheet(
        modifier = Modifier,
        containerColor = AlbumCoverBlackBG,
        sheetState = songSettingsSheetState,
        dragHandle = {},
        onDismissRequest = onDismiss,
    ) {


        Column(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {


            Column(
                modifier = Modifier
                    .padding(top = 45.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .width(210.dp)
                        .aspectRatio(1f)
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberAsyncImagePainter(
                            model = selectedSong.getSongImage()
                        ),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                        ),
                    text = selectedSong.name,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WhiteTextColor,
                )
                Text(
                    modifier = Modifier
                        .padding(top = 5.dp),
                    text = selectedSong.getArtistsName(),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = SFFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = GrayTextColor
                )
            }


            val actionText =
                if (!liked) stringResource(id = R.string.add_to_favorites) else stringResource(
                    id = R.string.remove_from_fav
                )
            val actionIcon =
                if (!liked) R.drawable.ic_like else
                    R.drawable.ic_heart_filled

            ActionsModelView(
                expandable = true,
                icon = actionIcon,
                mainText = actionText
            ) {
                mainViewModel.likeSong(
                    songId = selectedSong.songId,
                    liked = liked
                ) {
                    onDismiss()
                }

            }

            ActionsModelView(
                expandable = true,
                icon = R.drawable.library_add,
                mainText = stringResource(id = R.string.add_to_playlist)
            ) {
                onAddToPlaylist()
                onDismiss()

            }


            ActionsModelView(
                expandable = false,
                icon = R.drawable.redo,
                mainText = stringResource(id = R.string.play_next)
            ) {
                onDismiss()
                onPlayNext()
            }


            ActionsModelView(
                expandable = true,
                icon = R.drawable.account_circle,
                mainText = stringResource(id = R.string.see_the_artist)
            ) {
                onDismiss()
                onNavigateToArtist()
            }


            if (selectedSong.albumId != null) {
                ActionsModelView(
                    expandable = true,
                    icon = R.drawable.album,
                    mainText = stringResource(id = R.string.see_the_album)
                ) {
                    onDismiss()
                    onNavigateToAlbum()
                }
            }

            if (deletable) {
                ActionsModelView(
                    expandable = false,
                    icon = R.drawable.ic_trash,
                    mainText = stringResource(id = R.string.delete_from_playlist)
                ) {
                    onDismiss()
                    onDelete()
                }
            }

            ActionsModelView(
                expandable = false,
                icon = R.drawable.share,
                mainText = stringResource(id = R.string.share)
            ) {
                onDismiss()
                onShare()
            }

            CustomButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp),
                text = R.string.cancel,
                onClick = onDismiss,
                containerColor = SurfaceSecond,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.small
            )
        }

    }
}

fun Context.findActivity_(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No Activity found")
}


