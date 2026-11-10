package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorWhite

/**
 * T05 · Menu
 * Mostra folha deslizante com opções de navegação rápida, destaque para aba anônima,
 * histórico, favoritos, downloads e configurações.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onHistoryClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onAddToBookmarks: () -> Unit,
    onShare: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = TermavigatorWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // New tab row
            MenuItemRow(
                iconRes = R.drawable.ic_add,
                title = stringResource(id = R.string.new_tab),
                testTag = "menu_new_tab",
                onClick = {
                    onDismiss()
                    onNewTab()
                }
            )

            // New private tab - Black pill highlight (PRD Design requirement)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(TermavigatorBlack)
                    .clickable {
                        onDismiss()
                        onNewPrivateTab()
                    }
                    .padding(horizontal = 18.dp)
                    .testTag("menu_new_private_tab"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_incognito),
                        contentDescription = null,
                        tint = TermavigatorWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.new_private_tab),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TermavigatorWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(2.dp))

            // History
            MenuItemRow(
                iconRes = R.drawable.ic_history,
                title = stringResource(id = R.string.history),
                testTag = "menu_history",
                onClick = {
                    onDismiss()
                    onHistoryClick()
                }
            )

            // Bookmarks
            MenuItemRow(
                iconRes = R.drawable.ic_bookmark_border,
                title = stringResource(id = R.string.bookmarks),
                testTag = "menu_bookmarks",
                onClick = {
                    onDismiss()
                    onBookmarksClick()
                }
            )

            // Downloads
            MenuItemRow(
                iconRes = R.drawable.ic_file_download,
                title = stringResource(id = R.string.downloads),
                testTag = "menu_downloads",
                onClick = {
                    onDismiss()
                    onDownloadsClick()
                }
            )

            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(2.dp))

            // Add to bookmarks
            MenuItemRow(
                iconRes = R.drawable.ic_bookmark_border,
                title = stringResource(id = R.string.add_to_bookmarks),
                testTag = "menu_add_bookmark",
                onClick = {
                    onDismiss()
                    onAddToBookmarks()
                }
            )

            // Share
            MenuItemRow(
                iconRes = R.drawable.ic_share,
                title = stringResource(id = R.string.share),
                testTag = "menu_share",
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            // Settings
            MenuItemRow(
                iconRes = R.drawable.ic_settings,
                title = stringResource(id = R.string.settings),
                testTag = "menu_settings",
                onClick = {
                    onDismiss()
                    onSettingsClick()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MenuItemRow(
    iconRes: Int,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = TermavigatorBlack,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TermavigatorBlack
        )
    }
}
