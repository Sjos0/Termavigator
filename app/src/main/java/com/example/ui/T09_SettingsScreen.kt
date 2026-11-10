package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * T09 · Configurações
 * Mostra os grupos de configuração do app: Geral, Privacidade, Aparência, Memória, Sobre.
 */
@Composable
fun SettingsScreen(
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onGeneralClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onAboutClick: () -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
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
            onMenuClick = {}
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
                    text = stringResource(id = R.string.settings),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorWhite
                )
            }
        }

        // Settings items container
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
                // General
                item {
                    SettingsRowItem(
                        iconRes = R.drawable.ic_settings,
                        title = stringResource(id = R.string.general),
                        subtitle = stringResource(id = R.string.general_desc),
                        testTag = "settings_general",
                        onClick = onGeneralClick
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }

                // Privacy
                item {
                    SettingsRowItem(
                        iconRes = R.drawable.ic_shield,
                        title = stringResource(id = R.string.privacy),
                        subtitle = stringResource(id = R.string.privacy_desc),
                        testTag = "settings_privacy",
                        onClick = onPrivacyClick
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }

                // Appearance
                item {
                    SettingsRowItem(
                        iconRes = R.drawable.ic_palette,
                        title = stringResource(id = R.string.appearance),
                        subtitle = stringResource(id = R.string.appearance_desc),
                        testTag = "settings_appearance",
                        onClick = onAppearanceClick
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }

                // Memory
                item {
                    SettingsRowItem(
                        iconRes = R.drawable.ic_memory,
                        title = stringResource(id = R.string.memory),
                        subtitle = stringResource(id = R.string.memory_desc),
                        testTag = "settings_memory",
                        onClick = onMemoryClick
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }

                // About
                item {
                    SettingsRowItem(
                        iconRes = R.drawable.ic_info,
                        title = stringResource(id = R.string.about),
                        subtitle = stringResource(id = R.string.about_desc),
                        testTag = "settings_about",
                        onClick = onAboutClick
                    )
                }
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}

@Composable
fun SettingsRowItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F2FC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = TermavigatorBlueDark,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TermavigatorBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TermavigatorTextSecondaryLight
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}
