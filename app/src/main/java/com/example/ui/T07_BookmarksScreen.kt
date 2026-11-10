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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.core.models.BookmarkItem
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * T07 · Favoritos
 * Mostra a lista de favoritos com ações de abrir, editar/excluir, botão flutuante e barra inferior.
 */
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkItem>,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    onAddBookmarkClick: () -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = "termavigator.app",
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

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        text = stringResource(id = R.string.bookmarks),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TermavigatorWhite
                    )
                }
            }

            // Bookmarks Cards List
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
                    items(bookmarks, key = { it.id }) { item ->
                        var showMenu by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE8F2FC))
                                .border(1.dp, Color(0xFFD6E6F7), RoundedCornerShape(16.dp))
                                .clickable { onBookmarkClick(item.url) }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TermavigatorWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.title.take(2).uppercase(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TermavigatorBlueDark
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TermavigatorBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.url.removePrefix("https://").removePrefix("http://"),
                                    fontSize = 12.sp,
                                    color = TermavigatorTextSecondaryLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_more_vert),
                                        contentDescription = null,
                                        tint = TermavigatorBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Abrir") },
                                        onClick = {
                                            showMenu = false
                                            onBookmarkClick(item.url)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Excluir") },
                                        onClick = {
                                            showMenu = false
                                            onDeleteBookmark(item.id)
                                        }
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

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddBookmarkClick,
            shape = CircleShape,
            containerColor = Color.Transparent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = if (isToolbarAtTop) 24.dp else 76.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(ActionGradient)
                .testTag("add_bookmark_fab")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = stringResource(id = R.string.add_to_bookmarks),
                tint = TermavigatorBlack,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
