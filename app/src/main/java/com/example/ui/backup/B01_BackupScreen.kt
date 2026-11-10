package com.example.ui.backup

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ActionGradient
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorOrange
import com.example.ui.theme.TermavigatorTextSecondaryLight
import com.example.ui.theme.TermavigatorWhite

/**
 * B01 · Backup na Conta Google (v0.2, inativa nesta geração)
 * Não executa chamadas de rede. Estrutura de UI pronta.
 */
@Composable
fun BackupScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBackupEnabled by remember { mutableStateOf(false) }
    var syncTabs by remember { mutableStateOf(true) }
    var syncBookmarks by remember { mutableStateOf(true) }
    var syncSettings by remember { mutableStateOf(true) }
    var syncHistory by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
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
                    text = "Backup no Google Drive",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorWhite
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ativar backup criptografado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TermavigatorBlack
                        )
                        Text(
                            text = "Backup na pasta privada do seu Google Drive",
                            fontSize = 13.sp,
                            color = TermavigatorTextSecondaryLight
                        )
                    }
                    Switch(
                        checked = isBackupEnabled,
                        onCheckedChange = { isBackupEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TermavigatorWhite,
                            checkedTrackColor = TermavigatorOrange
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Categorias para incluir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermavigatorBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                BackupCategoryRow("Abas abertas e arquivadas", syncTabs) { syncTabs = it }
                BackupCategoryRow("Favoritos", syncBookmarks) { syncBookmarks = it }
                BackupCategoryRow("Configurações do app", syncSettings) { syncSettings = it }
                BackupCategoryRow("Histórico de navegação (opcional)", syncHistory) { syncHistory = it }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { /* Inativo na v0.1 */ },
                    enabled = isBackupEnabled,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(if (isBackupEnabled) ActionGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)))
                ) {
                    Text(
                        text = "Fazer backup agora",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TermavigatorBlack
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Último backup: Nenhum registro (recurso desligado)",
                    fontSize = 13.sp,
                    color = TermavigatorTextSecondaryLight
                )
            }
        }
    }
}

@Composable
fun BackupCategoryRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = TermavigatorOrange)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 15.sp, color = TermavigatorBlack)
    }
}
