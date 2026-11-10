package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorOrange
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * T11 · Privacidade
 * Mostra controle de bloqueio de anúncios e rastreadores, opções de limpeza e
 * explicação destacada do modo anônimo.
 */
@Composable
fun PrivacyScreen(
    blockAds: Boolean,
    doNotTrack: Boolean,
    autoHttps: Boolean,
    tabCount: Int,
    isToolbarAtTop: Boolean,
    onBackClick: () -> Unit,
    onBlockAdsChanged: (Boolean) -> Unit,
    onDoNotTrackChanged: (Boolean) -> Unit,
    onAutoHttpsChanged: (Boolean) -> Unit,
    onClearBrowsingData: () -> Unit,
    onClearCookies: () -> Unit,
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
                    text = stringResource(id = R.string.privacy_and_security),
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
                // Web Protection section
                Text(
                    text = stringResource(id = R.string.web_protection),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Block ads
                PrivacyToggleRow(
                    title = stringResource(id = R.string.block_ads_and_trackers),
                    subtitle = stringResource(id = R.string.block_ads_desc),
                    checked = blockAds,
                    onCheckedChange = onBlockAdsChanged,
                    testTag = "toggle_block_ads"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Send Do Not Track
                PrivacyToggleRow(
                    title = stringResource(id = R.string.send_do_not_track),
                    subtitle = stringResource(id = R.string.send_dnt_desc),
                    checked = doNotTrack,
                    onCheckedChange = onDoNotTrackChanged,
                    testTag = "toggle_do_not_track"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Automatic HTTPS
                PrivacyToggleRow(
                    title = stringResource(id = R.string.automatic_https),
                    subtitle = stringResource(id = R.string.auto_https_desc),
                    checked = autoHttps,
                    onCheckedChange = onAutoHttpsChanged,
                    testTag = "toggle_auto_https"
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Clear Data section
                Text(
                    text = stringResource(id = R.string.clear_data),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearBrowsingData,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TermavigatorBlueLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TermavigatorBlueDark),
                        modifier = Modifier.weight(1f).testTag("clear_browsing_data_btn")
                    ) {
                        Text(text = stringResource(id = R.string.clear_browsing_data), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onClearCookies,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TermavigatorBlueLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TermavigatorBlueDark),
                        modifier = Modifier.weight(1f).testTag("clear_cookies_btn")
                    ) {
                        Text(text = stringResource(id = R.string.clear_cookies), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Private Mode section
                Text(
                    text = stringResource(id = R.string.private_mode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Private mode card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_incognito),
                                contentDescription = null,
                                tint = TermavigatorBlack,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(id = R.string.about_private_browsing),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TermavigatorBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(id = R.string.private_mode_desc),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TermavigatorBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!isToolbarAtTop) {
            bottomBar()
        }
    }
}

@Composable
fun PrivacyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
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

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TermavigatorWhite,
                checkedTrackColor = TermavigatorOrange,
                uncheckedThumbColor = TermavigatorWhite,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
