package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorWhite

@Composable
fun BrowserBottomBar(
    urlDisplay: String,
    tabCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isPrivate: Boolean = false,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBackground = if (isPrivate) TermavigatorBlack else MaterialTheme.colorScheme.surfaceVariant
    val iconTint = if (isPrivate) TermavigatorWhite else TermavigatorBlack
    val pillBackground = if (isPrivate) Color(0xFF1E2838) else Color(0xFFEBF4FD)
    val pillBorderColor = if (isPrivate) Color(0xFF2C3E5A) else TermavigatorBlueLight.copy(alpha = 0.7f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(barBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Back
        IconButton(
            onClick = onBack,
            enabled = canGoBack,
            modifier = Modifier
                .size(40.dp)
                .testTag("nav_back_button")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = stringResource(id = R.string.back),
                tint = if (canGoBack) iconTint else iconTint.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Forward
        IconButton(
            onClick = onForward,
            enabled = canGoForward,
            modifier = Modifier
                .size(40.dp)
                .testTag("nav_forward_button")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = stringResource(id = R.string.forward),
                tint = if (canGoForward) iconTint else iconTint.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Center Pill Address Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(CircleShape)
                .background(pillBackground)
                .border(1.5.dp, pillBorderColor, CircleShape)
                .clickable { onUrlClick() }
                .padding(horizontal = 12.dp)
                .testTag("address_bar_pill"),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Lock / Incognito icon
                if (isPrivate) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_incognito),
                        contentDescription = stringResource(id = R.string.private_mode),
                        tint = TermavigatorBlueLight,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Conexão segura",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = if (urlDisplay.isEmpty()) stringResource(id = R.string.search_or_type_url) else urlDisplay,
                    color = if (isPrivate) TermavigatorWhite else TermavigatorBlueDark,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Counter Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, if (isPrivate) Color(0xFF4A5568) else Color(0xFF8692A6), RoundedCornerShape(8.dp))
                .clickable { onTabsClick() }
                .testTag("tabs_counter_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tabCount.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isPrivate) TermavigatorWhite else TermavigatorBlack
            )
        }

        // Menu button
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(40.dp)
                .testTag("nav_menu_button")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert),
                contentDescription = stringResource(id = R.string.settings),
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
