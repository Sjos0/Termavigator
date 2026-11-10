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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorOrange
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite
import com.example.ui.theme.TermavigatorYellow

/**
 * T12 · Memória
 * Mostra controle do limite de abas ativas, consumo de RAM do app e opção de pausar abas inativas.
 */
@Composable
fun MemoryScreen(
    maxActiveTabs: Int,
    pauseInactiveTabs: Boolean,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onMaxActiveTabsChanged: (Int) -> Unit,
    onPauseInactiveTabsChanged: (Boolean) -> Unit,
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
                    text = stringResource(id = R.string.memory_settings),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorWhite
                )
            }
        }

        // Content
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
                // Memory in use section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.memory_in_use),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TermavigatorBlueDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "40%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TermavigatorBlack
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE8D1))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.40f)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(ActionGradient)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(id = R.string.memory_usage_desc, "1.2 GB", "3.0 GB"),
                    fontSize = 14.sp,
                    color = TermavigatorTextSecondaryLight,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Active tabs section
                Text(
                    text = stringResource(id = R.string.active_tabs),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.active_tabs_desc),
                    fontSize = 14.sp,
                    color = TermavigatorTextSecondaryLight,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Current tabs badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2EDF9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = maxActiveTabs.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TermavigatorBlueDark
                        )
                    }
                }

                Slider(
                    value = maxActiveTabs.toFloat(),
                    onValueChange = { onMaxActiveTabsChanged(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = TermavigatorOrange,
                        activeTrackColor = TermavigatorYellow,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("memory_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..10) {
                        Text(
                            text = i.toString(),
                            fontSize = 13.sp,
                            fontWeight = if (i == maxActiveTabs) FontWeight.Bold else FontWeight.Normal,
                            color = if (i == maxActiveTabs) TermavigatorBlack else TermavigatorTextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Pause inactive tabs toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.pause_inactive_tabs),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TermavigatorBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.pause_inactive_desc),
                            fontSize = 13.sp,
                            color = TermavigatorTextSecondaryLight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = pauseInactiveTabs,
                        onCheckedChange = onPauseInactiveTabsChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TermavigatorWhite,
                            checkedTrackColor = Color(0xFF3B82F6),
                            uncheckedThumbColor = TermavigatorWhite,
                            uncheckedTrackColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.testTag("toggle_pause_inactive")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Never pause these sites
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.never_pause_sites),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TermavigatorBlack,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}
