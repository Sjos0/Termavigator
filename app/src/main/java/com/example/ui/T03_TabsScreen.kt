package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.models.TabItem
import com.example.core.models.TabState
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorCardBorderLight
import com.example.ui.theme.TermavigatorSurfaceLight
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * T03 · Gerenciador de abas
 * Mostra a lista de abas abertas e pausadas com botões de arquivar e fechar,
 * botão flutuante de nova aba em gradient.action e a barra inferior.
 */
@Composable
fun TabsScreen(
    tabs: List<TabItem>,
    currentTabId: String?,
    isToolbarAtTop: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onArchiveTab: (String) -> Unit,
    onNewTabClick: () -> Unit,
    onUrlClick: () -> Unit,
    onMenuClick: () -> Unit,
    onArchivedTabsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTabs = tabs.filter { it.state == TabState.ACTIVE && !it.isPrivate }
    val pausedTabs = tabs.filter { it.state == TabState.PAUSED && !it.isPrivate }

    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = "termavigator.app",
            tabCount = tabs.size,
            canGoBack = false,
            canGoForward = false,
            isPrivate = false,
            onBack = {},
            onForward = {},
            onUrlClick = onUrlClick,
            onTabsClick = {},
            onMenuClick = onMenuClick
        )
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isToolbarAtTop) {
                bottomBar()
            }

            // Top Header decorative band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(BrandGradient),
                contentAlignment = Alignment.TopCenter
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(TermavigatorWhite.copy(alpha = 0.6f))
                )
            }

            // Content Container with rounded top
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Open Tabs Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.open_tabs),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TermavigatorBlueDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEBF3FC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeTabs.size.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TermavigatorBlueDark
                                )
                            }
                        }
                    }

                    // Active Tabs Cards
                    items(activeTabs, key = { it.id }) { tab ->
                        TabCardItem(
                            tab = tab,
                            isCurrent = tab.id == currentTabId,
                            isPaused = false,
                            onSelect = { onSelectTab(tab.id) },
                            onArchive = { onArchiveTab(tab.id) },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }

                    // Paused Tabs Section (if any)
                    if (pausedTabs.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.paused_tabs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TermavigatorTextSecondaryLight,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(pausedTabs, key = { it.id }) { tab ->
                            TabCardItem(
                                tab = tab,
                                isCurrent = false,
                                isPaused = true,
                                onSelect = { onSelectTab(tab.id) },
                                onArchive = { onArchiveTab(tab.id) },
                                onClose = { onCloseTab(tab.id) }
                            )
                        }
                    }

                    // Link to Archived Tabs
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onArchivedTabsClick() }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_archive),
                                contentDescription = null,
                                tint = TermavigatorBlueDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.archived_tabs),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TermavigatorBlueDark
                            )
                        }
                    }
                }
            }

            if (!isToolbarAtTop) {
                bottomBar()
            }
        }

        // Action FAB for New Tab in Yellow-Orange ActionGradient
        FloatingActionButton(
            onClick = onNewTabClick,
            shape = CircleShape,
            containerColor = Color.Transparent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = if (isToolbarAtTop) 24.dp else 76.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(ActionGradient)
                .testTag("new_tab_fab")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = stringResource(id = R.string.new_tab),
                tint = TermavigatorBlack,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TabCardItem(
    tab: TabItem,
    isCurrent: Boolean,
    isPaused: Boolean,
    onSelect: () -> Unit,
    onArchive: () -> Unit,
    onClose: () -> Unit
) {
    val cardBg = if (isPaused) Color(0xFFF1F6FB) else Color(0xFFE8F2FC)
    val borderColor = if (isCurrent) Color(0xFF5BB8F5) else Color(0xFFD6E6F7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini favicon or badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TermavigatorWhite),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tab.title.take(2).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaused) TermavigatorTextSecondaryLight else TermavigatorBlueDark
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and URL
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tab.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPaused) TermavigatorTextSecondaryLight else TermavigatorBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.url.removePrefix("https://").removePrefix("http://"),
                fontSize = 12.sp,
                color = TermavigatorTextSecondaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Archive Action
        IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_archive),
                contentDescription = stringResource(id = R.string.archive),
                tint = TermavigatorBlack,
                modifier = Modifier.size(18.dp)
            )
        }

        // Close Action
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.close),
                tint = TermavigatorBlack,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
