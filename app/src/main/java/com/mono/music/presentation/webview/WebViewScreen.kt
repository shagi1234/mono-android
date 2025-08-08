package com.mono.music.presentation.webview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.mono.music.R
import com.mono.music.presentation.settings.findActivity
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@Destination(style = ScreenTransition::class)
@Composable
fun WebViewScreen(
    url: String,
    orderId: Long,
    navigator: DestinationsNavigator
) {

    LaunchedEffect(Unit) {

    }
    Scaffold { _ ->
        Column {
            CollapsingSmallTopAppBar(
                title = stringResource(id = R.string.payment)
            ) {
                navigator.navigateUp()
            }
            AndroidView(factory = {
                WebView(it).apply {
                    this.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    getSettings().loadWithOverviewMode = true
                    getSettings().useWideViewPort = true
                    getSettings().javaScriptEnabled = true
                    this.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (url.contains("mono.com.tm") || url.contains("finish.html") || url.contains("OnCompletion")) {
                                restartActivity(context.findActivity())
                                return false
                            }
                            return false // Используется стандартная обработка
                        }
                    }
                    loadUrl(url)
                }
            })
        }

    }

}

fun restartActivity(activity: Activity?) {
    val intent = activity?.getIntent()
    activity?.finish()
    activity?.startActivity(intent)
}