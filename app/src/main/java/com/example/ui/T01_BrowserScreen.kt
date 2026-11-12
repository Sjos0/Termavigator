package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blocking.ContentBlocker
import com.example.core.models.TabItem
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.TopProgressBar
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorTextSecondaryLight
import java.io.ByteArrayInputStream

/**
 * T01 · Navegação
 * Mostra a área da página renderizada pelo WebView (ou conteúdo do artigo minimalista quando em demo),
 * o indicador de progresso no topo em gradiente amarelo para laranja, e a barra inferior na base.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    currentTab: TabItem?,
    tabCount: Int,
    contentBlocker: ContentBlocker,
    isToolbarAtTop: Boolean,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    onPageLoaded: (url: String, title: String) -> Unit,
    onDownloadRequested: (url: String, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageProgress by remember { mutableFloatStateOf(0.65f) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val activeUrl = currentTab?.url.orEmpty()
    val hasPage = activeUrl.isNotEmpty()
    val bgArgb = MaterialTheme.colorScheme.background.toArgb()

    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = if (activeUrl.isEmpty()) "termavigator.app" else activeUrl.removePrefix("https://").removePrefix("http://"),
            tabCount = tabCount,
            canGoBack = canGoBack || webViewRef?.canGoBack() == true,
            canGoForward = canGoForward || webViewRef?.canGoForward() == true,
            isPrivate = currentTab?.isPrivate == true,
            onBack = { webViewRef?.goBack() },
            onForward = { webViewRef?.goForward() },
            onUrlClick = onUrlClick,
            onTabsClick = onTabsClick,
            onMenuClick = onMenuClick
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isToolbarAtTop) {
            bottomBar()
        }

        TopProgressBar(progress = pageProgress, isLoading = isLoading)

        Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            if (!hasPage) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(bgArgb)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                                onDownloadRequested(url, userAgent, contentDisposition, mimeType, contentLength)
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val reqUrl = request?.url?.toString()
                                    if (reqUrl != null && contentBlocker.shouldBlock(reqUrl)) {
                                        // Blocked ad/tracker
                                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    pageProgress = 0.2f
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    pageProgress = 1f
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                    if (url != null) {
                                        onPageLoaded(url, view?.title ?: url)
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                    if (newProgress == 100) isLoading = false
                                }
                            }

                            loadUrl(activeUrl)
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        if (view.url != activeUrl && activeUrl.isNotEmpty()) {
                            view.loadUrl(activeUrl)
                        }
                        webViewRef = view
                    }
                )
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }

    DisposableEffect(currentTab?.id) {
        onDispose {
            // When tab changes, keep memory clean
            if (currentTab?.isPrivate == true) {
                webViewRef?.clearHistory()
                webViewRef?.clearCache(true)
            }
        }
    }
}
