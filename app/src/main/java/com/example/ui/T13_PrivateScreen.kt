package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.models.TabItem
import com.example.ui.components.BrowserBottomBar
import com.example.ui.theme.NightGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorTextSecondaryDark
import com.example.ui.theme.TermavigatorWhite

/**
 * T13 · Modo anônimo
 * Mostra a experiência em tema escuro com aviso no topo de que histórico e cookies não são salvos.
 */
@Composable
fun PrivateScreen(
    currentTab: TabItem?,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onUrlClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBar = @Composable {
        BrowserBottomBar(
            urlDisplay = "termavigator.app",
            tabCount = tabCount,
            canGoBack = false,
            canGoForward = false,
            isPrivate = true,
            onBack = {},
            onForward = {},
            onUrlClick = onUrlClick,
            onTabsClick = onTabsClick,
            onMenuClick = onMenuClick
        )
    }

    Column(modifier = modifier.fillMaxSize().background(TermavigatorBlack)) {
        if (isToolbarAtTop) {
            bottomBar()
        }

        // Top Banner Header in NightGradient with Incognito Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NightGradient)
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_incognito),
                    contentDescription = null,
                    tint = TermavigatorBlueLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.private_mode_banner),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TermavigatorWhite
                )
            }
        }

        // Dark Content View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0F141F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "TECHNOLOGY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Securing Your Digital Footprint",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    color = TermavigatorWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Published · 3 min read",
                    fontSize = 13.sp,
                    color = TermavigatorTextSecondaryDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF1E2A42))
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation. Uta maii ori aripor senmua quis plascre dum, encommodo casit in consemdo conseruat.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFFD1D5DB)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Understanding privacy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TermavigatorWhite
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation. Uta maii ori aripor senmua quis plascre dum, encommodo casit in consentuo, milupii, nonc nudo conseuat.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFFD1D5DB)
                )
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}
