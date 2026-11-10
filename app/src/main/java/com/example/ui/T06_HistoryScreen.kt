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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.models.HistoryItem
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorCardBorderLight
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * T06 · Histórico
 * Mostra a lista de histórico de navegação agrupada por data, com campo de busca e botão de limpar.
 */
@Composable
fun HistoryScreen(
    historyList: List<HistoryItem>,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = if (searchQuery.isBlank()) {
        historyList
    } else {
        historyList.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.url.contains(searchQuery, ignoreCase = true)
        }
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

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

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isToolbarAtTop) {
            bottomBar()
        }

        // Top Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F6FB))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = TermavigatorTextSecondaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = TermavigatorBlack),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.search_history),
                                    fontSize = 15.sp,
                                    color = TermavigatorTextSecondaryLight
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f).testTag("history_search_input")
                    )
                }
            }
        }

        // Title and Clear Button Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.history),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TermavigatorBlueDark
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onClearHistory) {
                Text(
                    text = stringResource(id = R.string.clear),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TermavigatorBlueDark
                )
            }
        }

        // List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.today),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TermavigatorTextSecondaryLight,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(filteredList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F2FC))
                        .border(1.dp, Color(0xFFD6E6F7), RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.url) }
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
                    Spacer(modifier = Modifier.width(12.dp))
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeFormat.format(Date(item.timestamp)),
                        fontSize = 12.sp,
                        color = TermavigatorTextSecondaryLight
                    )
                }
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}
