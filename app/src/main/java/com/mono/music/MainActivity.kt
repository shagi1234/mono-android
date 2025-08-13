package com.mono.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.navigation.AppNavGraph
import com.mono.music.ui.theme.MusifyTheme
import dagger.hilt.android.AndroidEntryPoint

private const val REQUEST_CODE_POST_NOTIFICATIONS = 1001


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val ALL = ""
        const val PLAYLIST = "playlists"
        const val TOPS = "tops"
        const val ALBUM = "albums"
        const val ALL_SEARCH = "all"
        const val ARTIST = "artist"
        const val SONG = "song"
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("WakelockTimeout")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
             val mainViewModel =  hiltViewModel<MainViewModel>()

            MusifyTheme {
                val playerBottomSheet = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                )

                Surface {
                    AppNavGraph(
                        mainViewModel = mainViewModel,
                        playerBottomSheet = playerBottomSheet
                    )
                }
            }

        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
            return
        }



    }


}






