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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageProgress by remember { mutableFloatStateOf(0.65f) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val activeUrl = currentTab?.url.orEmpty()
    val isDemoArticle = activeUrl.contains("termavigator.app/minimal-interfaces") || activeUrl.isEmpty()

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

        TopProgressBar(progress = pageProgress, isLoading = isLoading || isDemoArticle)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isDemoArticle) {
                // High fidelity reproduction of reference article view shown in T01 screenshot
                ArticleDemoContent(modifier = Modifier.fillMaxSize())
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
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

@Composable
fun ArticleDemoContent(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "TECHNOLOGY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TermavigatorTextSecondaryLight,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "The Evolution of Minimal Digital Interfaces",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            color = TermavigatorBlueDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Published · 5 min read",
            fontSize = 13.sp,
            color = TermavigatorTextSecondaryLight
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE2EDF9))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation. Uta maii ori aripor senmua quis plascre dum. encommodo casit in consemdo conseruat.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Designing for the user",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TermavigatorBlueDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation. Uta maii ori aripor senmua quis plascre dum. encommodo casit in consentuo, milupii, nonc nudo conseuat.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
