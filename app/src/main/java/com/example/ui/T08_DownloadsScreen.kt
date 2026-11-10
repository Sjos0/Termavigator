package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.models.DownloadItem
import com.example.core.models.DownloadStatus
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * T08 · Downloads
 * Mostra a lista de downloads com status (em andamento, concluído, falhou) e ação de abrir.
 */
@Composable
fun DownloadsScreen(
    downloads: List<DownloadItem>,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onOpenFile: (DownloadItem) -> Unit,
    onRetryDownload: (DownloadItem) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = "termavigator.app/downloads",
            tabCount = tabCount,
            canGoBack = true,
            canGoForward = false,
            isPrivate = false,
            onBack = onBackClick,
            onForward = {},
            onUrlClick = onUrlClick,
            onTabsClick = onTabsClick,
            onMenuClick = onMenuClick
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isToolbarAtTop) {
            bottomBar()
        }

        // Header with Brand Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandGradient)
                .padding(start = 12.dp, end = 20.dp, top = 28.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(id = R.string.back),
                        tint = TermavigatorWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.downloads),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorWhite
                )
            }
        }

        // List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(downloads, key = { it.id }) { item ->
                    val fileIcon = when {
                        item.name.endsWith(".zip", ignoreCase = true) -> R.drawable.ic_folder_zip
                        item.name.endsWith(".pdf", ignoreCase = true) -> R.drawable.ic_picture_as_pdf
                        else -> R.drawable.ic_image
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8F2FC))
                            .border(1.dp, Color(0xFFD6E6F7), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TermavigatorWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = fileIcon),
                                    contentDescription = null,
                                    tint = TermavigatorBlueDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TermavigatorBlack,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                when (item.status) {
                                    DownloadStatus.IN_PROGRESS -> {
                                        Text(
                                            text = "18.4 MB de 45.2 MB",
                                            fontSize = 13.sp,
                                            color = TermavigatorTextSecondaryLight
                                        )
                                    }
                                    DownloadStatus.COMPLETED -> {
                                        Text(
                                            text = "4.8 MB",
                                            fontSize = 13.sp,
                                            color = TermavigatorTextSecondaryLight
                                        )
                                    }
                                    DownloadStatus.FAILED -> {
                                        Text(
                                            text = "22.1 MB · Falhou",
                                            fontSize = 13.sp,
                                            color = TermavigatorTextSecondaryLight
                                        )
                                    }
                                }
                            }

                            when (item.status) {
                                DownloadStatus.IN_PROGRESS -> {
                                    IconButton(
                                        onClick = { onCancelDownload(item.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_close),
                                            contentDescription = stringResource(id = R.string.cancel),
                                            tint = TermavigatorTextSecondaryLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                DownloadStatus.COMPLETED -> {
                                    Button(
                                        onClick = { onOpenFile(item) },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TermavigatorBlueLight,
                                            contentColor = TermavigatorWhite
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp).testTag("open_download_btn")
                                    ) {
                                        Text(text = stringResource(id = R.string.open), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                DownloadStatus.FAILED -> {
                                    IconButton(
                                        onClick = { onRetryDownload(item) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_refresh),
                                            contentDescription = stringResource(id = R.string.retry),
                                            tint = Color(0xFFFF8A1F),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Progress bar for active downloads
                        if (item.status == DownloadStatus.IN_PROGRESS) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFE0B2))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .height(4.dp)
                                        .background(ActionGradient)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}
