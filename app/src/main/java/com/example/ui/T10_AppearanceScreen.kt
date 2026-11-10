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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.settings.AppThemeMode
import com.example.settings.ToolbarPosition
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorOrange
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite
import com.example.ui.theme.TermavigatorYellow

/**
 * T10 · Aparência
 * Mostra a personalização visual do app: seleção de tema, cor de destaque,
 * posição da barra de endereço e prévia ao vivo.
 */
@Composable
fun AppearanceScreen(
    currentTheme: AppThemeMode,
    currentAccentIndex: Int,
    currentToolbarPosition: ToolbarPosition,
    tabCount: Int,
    onBackClick: () -> Unit,
    onThemeChanged: (AppThemeMode) -> Unit,
    onAccentChanged: (Int) -> Unit,
    onToolbarPositionChanged: (ToolbarPosition) -> Unit,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColors = listOf(
        TermavigatorYellow,
        TermavigatorOrange,
        TermavigatorBlueLight,
        TermavigatorBlueDark,
        TermavigatorBlack
    )

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
        if (currentToolbarPosition == ToolbarPosition.TOP) {
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
                    text = stringResource(id = R.string.appearance),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorWhite
                )
            }
        }

        // Settings Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Preview Section
                Text(
                    text = stringResource(id = R.string.preview),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Miniature Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F2FC))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TermavigatorWhite)
                            .border(1.dp, Color(0xFFD6E6F7), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "TECHNOLOGY", fontSize = 8.sp, color = TermavigatorTextSecondaryLight, fontWeight = FontWeight.Bold)
                            Text(text = "The Evolution of Minimal Digital Interfaces", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(modifier = Modifier.weight(1f))
                            // Mini bottom bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEBF4FD))
                                    .border(1.dp, TermavigatorBlueLight, CircleShape)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = "🔒 termavigator.app", fontSize = 9.sp, color = TermavigatorBlueDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Theme Section
                Text(
                    text = stringResource(id = R.string.theme),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Theme Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F6FB))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeOptionButton(
                        title = stringResource(id = R.string.theme_light),
                        isSelected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { onThemeChanged(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f).testTag("theme_light_btn")
                    )
                    ThemeOptionButton(
                        title = stringResource(id = R.string.theme_dark),
                        isSelected = currentTheme == AppThemeMode.DARK,
                        onClick = { onThemeChanged(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f).testTag("theme_dark_btn")
                    )
                    ThemeOptionButton(
                        title = stringResource(id = R.string.theme_auto),
                        isSelected = currentTheme == AppThemeMode.AUTO,
                        onClick = { onThemeChanged(AppThemeMode.AUTO) },
                        modifier = Modifier.weight(1f).testTag("theme_auto_btn")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Accent Color Section
                Text(
                    text = stringResource(id = R.string.accent_color),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    accentColors.forEachIndexed { index, color ->
                        val isSelected = currentAccentIndex == index
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, color, CircleShape)
                                    } else Modifier
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onAccentChanged(index) }
                                .testTag("accent_color_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Toolbar Position Section
                Text(
                    text = stringResource(id = R.string.toolbar_position),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F6FB))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeOptionButton(
                        title = stringResource(id = R.string.toolbar_top),
                        isSelected = currentToolbarPosition == ToolbarPosition.TOP,
                        onClick = { onToolbarPositionChanged(ToolbarPosition.TOP) },
                        modifier = Modifier.weight(1f).testTag("toolbar_top_btn")
                    )
                    ThemeOptionButton(
                        title = stringResource(id = R.string.toolbar_bottom),
                        isSelected = currentToolbarPosition == ToolbarPosition.BOTTOM,
                        onClick = { onToolbarPositionChanged(ToolbarPosition.BOTTOM) },
                        modifier = Modifier.weight(1f).testTag("toolbar_bottom_btn")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (currentToolbarPosition == ToolbarPosition.BOTTOM) {
            bottomBar()
        }
    }
}

@Composable
fun ThemeOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFFE8F2FC) else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(1.5.dp, TermavigatorBlueLight, CircleShape) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TermavigatorBlueDark else TermavigatorTextSecondaryLight
        )
    }
}
