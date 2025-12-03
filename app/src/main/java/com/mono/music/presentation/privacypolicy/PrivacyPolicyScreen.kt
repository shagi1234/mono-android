package com.mono.music.presentation.privacypolicy

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.mono.music.R
import com.mono.music.ui.components.CollapsingSmallTopAppBar
import com.mono.music.ui.components.LoadingView
import com.mono.music.ui.components.NetworkErrorView

import com.mono.music.ui.utils.ScreenTransition
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(style = ScreenTransition::class)
@Composable
fun PrivacyPolicyScreen(
    navigator: DestinationsNavigator,
    viewModel: PrivacyPolicyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CollapsingSmallTopAppBar(
                title = stringResource(id = R.string.privacy_policy)
            ) {
                navigator.navigateUp()
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView(modifier = Modifier.fillMaxSize())
                }
                uiState.isFailure -> {
                    NetworkErrorView(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = { viewModel.getPrivacyPolicy() }
                    )
                }
                uiState.isSuccess && uiState.data != null -> {
                    val privacyPolicy = uiState.data!!

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = false
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                }
                            }
                        },
                        update = { webView ->
                            val htmlContent = convertMarkdownToHtml(privacyPolicy.content)
                            webView.loadDataWithBaseURL(
                                null,
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun convertMarkdownToHtml(markdown: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                    padding: 16px;
                    line-height: 1.6;
                    color: #E0E0E0;
                    background-color: #121212;
                }
                h1 {
                    font-size: 24px;
                    font-weight: bold;
                    margin-top: 24px;
                    margin-bottom: 16px;
                    color: #FFFFFF;
                }
                h2 {
                    font-size: 20px;
                    font-weight: bold;
                    margin-top: 20px;
                    margin-bottom: 12px;
                    color: #FFFFFF;
                }
                h3 {
                    font-size: 18px;
                    font-weight: bold;
                    margin-top: 16px;
                    margin-bottom: 10px;
                    color: #FFFFFF;
                }
                h4 {
                    font-size: 16px;
                    font-weight: bold;
                    margin-top: 14px;
                    margin-bottom: 8px;
                    color: #FFFFFF;
                }
                p {
                    margin-top: 0;
                    margin-bottom: 12px;
                }
                ul, ol {
                    margin-top: 0;
                    margin-bottom: 12px;
                    padding-left: 20px;
                }
                li {
                    margin-bottom: 8px;
                }
                strong {
                    font-weight: bold;
                    color: #FFFFFF;
                }
                a {
                    color: #BB86FC;
                    text-decoration: none;
                }
                code {
                    background-color: #1E1E1E;
                    padding: 2px 6px;
                    border-radius: 3px;
                    font-family: 'Courier New', monospace;
                    color: #FFFFFF;
                }
            </style>
        </head>
        <body>
            ${markdownToHtmlSimple(markdown)}
        </body>
        </html>
    """.trimIndent()
}

private fun markdownToHtmlSimple(markdown: String): String {
    var html = markdown

    // Convert headers
    html = html.replace(Regex("^#### (.+)$", RegexOption.MULTILINE), "<h4>$1</h4>")
    html = html.replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
    html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
    html = html.replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")

    // Convert bold text
    html = html.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")

    // Convert links
    html = html.replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")

    // Convert unordered lists
    val lines = html.split("\n")
    val result = StringBuilder()
    var inList = false

    for (line in lines) {
        when {
            line.trim().startsWith("- ") -> {
                if (!inList) {
                    result.append("<ul>\n")
                    inList = true
                }
                result.append("<li>${line.trim().substring(2)}</li>\n")
            }
            line.trim().isEmpty() && inList -> {
                result.append("</ul>\n")
                inList = false
                result.append("<br>\n")
            }
            else -> {
                if (inList) {
                    result.append("</ul>\n")
                    inList = false
                }
                result.append(line).append("<br>\n")
            }
        }
    }

    if (inList) {
        result.append("</ul>\n")
    }

    return result.toString()
}
