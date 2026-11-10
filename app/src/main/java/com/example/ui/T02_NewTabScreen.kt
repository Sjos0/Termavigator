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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.core.models.BookmarkItem
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorCardBorderLight
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

data class ShortcutItem(
    val title: String,
    val url: String,
    val bgColor: Color,
    val iconEmoji: String
)

/**
 * T02 · Nova aba
 * Mostra o cabeçalho em gradient.brand com o campo de busca em destaque,
 * a grade de atalhos em 8 posições (Social, Vídeo, etc.) e a seção de favoritos recentes.
 */
@Composable
fun NewTabScreen(
    tabCount: Int,
    recentBookmarks: List<BookmarkItem>,
    isToolbarAtTop: Boolean,
    onSearchClick: () -> Unit,
    onNavigateUrl: (String) -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcuts = listOf(
        ShortcutItem("Social", "https://x.com", Color(0xFFE8F1FC), "💬"),
        ShortcutItem("Vídeo", "https://youtube.com", Color(0xFFFDE8E8), "▶️"),
        ShortcutItem("Compras", "https://amazon.com.br", Color(0xFFFFF7E6), "🛍️"),
        ShortcutItem("Notícias", "https://g1.globo.com", Color(0xFFE6F7FF), "📰"),
        ShortcutItem("E-mail", "https://mail.google.com", Color(0xFFEFF4FE), "✉️"),
        ShortcutItem("Mapas", "https://maps.google.com", Color(0xFFEDF8EE), "📍"),
        ShortcutItem("Jogos", "https://poki.com", Color(0xFFF2EBFD), "🎮"),
        ShortcutItem("Tecnologia", "https://news.ycombinator.com", Color(0xFFEBF6FA), "💻")
    )

    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = "termavigator.app",
            tabCount = tabCount,
            canGoBack = false,
            canGoForward = false,
            isPrivate = false,
            onBack = {},
            onForward = {},
            onUrlClick = onSearchClick,
            onTabsClick = onTabsClick,
            onMenuClick = onMenuClick
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isToolbarAtTop) {
            bottomBar()
        }

        // Header with Brand Gradient and Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandGradient)
                .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 28.dp)
        ) {
            // Search Input Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(TermavigatorWhite)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp)
                    .testTag("new_tab_search_bar"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Buscar",
                        tint = TermavigatorTextSecondaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.search_or_type_url),
                        fontSize = 15.sp,
                        color = TermavigatorTextSecondaryLight,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Voz",
                        tint = TermavigatorTextSecondaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Imagem",
                        tint = TermavigatorTextSecondaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Main White Content Container with rounded top
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
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Shortcuts Grid 4x2
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..3) {
                                    val index = row * 4 + col
                                    val item = shortcuts[index]
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(72.dp)
                                            .clickable { onNavigateUrl(item.url) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(item.bgColor)
                                                .border(1.dp, Color(0xFFE5EDF7), RoundedCornerShape(16.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = item.iconEmoji, fontSize = 24.sp)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(id = R.string.recent_favorites),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TermavigatorBlueDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Recent Favorites list
                items(recentBookmarks.take(4)) { bookmark ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateUrl(bookmark.url) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEBF3FC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bookmark.title.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = TermavigatorBlueLight,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Text(
                                text = bookmark.url.removePrefix("https://").removePrefix("http://"),
                                fontSize = 13.sp,
                                color = TermavigatorTextSecondaryLight,
                                maxLines = 1
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F4FA), thickness = 1.dp)
                }
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}
