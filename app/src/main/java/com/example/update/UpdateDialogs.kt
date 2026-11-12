package com.example.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import java.io.File

sealed class UpdateStep {
    object Idle : UpdateStep()
    object Checking : UpdateStep()
    data class Available(val info: UpdateInfo) : UpdateStep()
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateStep()
    data class ReadyToInstall(val file: File) : UpdateStep()
    object UpToDate : UpdateStep()
    data class Error(val message: String) : UpdateStep()
}

class UpdateController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    var step: UpdateStep by mutableStateOf(UpdateStep.Idle)
        private set

    fun check() {
        scope.launch {
            step = UpdateStep.Checking
            step = when (val result = withContext(Dispatchers.IO) { AppUpdater.checkForUpdate() }) {
                UpdateCheck.UpToDate -> UpdateStep.UpToDate
                is UpdateCheck.Available -> UpdateStep.Available(result.info)
                is UpdateCheck.Error -> UpdateStep.Error(result.message)
            }
        }
    }

    fun download(info: UpdateInfo) {
        scope.launch {
            step = UpdateStep.Downloading(info, 0f)
            try {
                val file = withContext(Dispatchers.IO) {
                    AppUpdater.download(context, info) { progress ->
                        scope.launch { step = UpdateStep.Downloading(info, progress) }
                    }
                }
                step = UpdateStep.ReadyToInstall(file)
            } catch (e: Exception) {
                step = UpdateStep.Error("Falha no download. Verifique a conexão e tente de novo.")
            }
        }
    }

    fun install(file: File) {
        if (AppUpdater.install(context, file)) {
            step = UpdateStep.Idle
        }
    }

    fun dismiss() {
        step = UpdateStep.Idle
    }
}

@Composable
fun rememberUpdateController(): UpdateController {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    return remember { UpdateController(context, scope) }
}

/** Pop-ups do fluxo: verificar, oferecer, baixar com progresso e confirmar a instalação. */
@Composable
fun UpdateDialogs(controller: UpdateController) {
    when (val s = controller.step) {
        UpdateStep.Idle -> Unit

        UpdateStep.Checking -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.update_checking)) },
            text = { CircularProgressIndicator() },
            confirmButton = {}
        )

        is UpdateStep.Available -> AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = { Text(stringResource(R.string.update_available_text, s.info.versionName)) },
            confirmButton = {
                TextButton(onClick = { controller.download(s.info) }) {
                    Text(stringResource(R.string.update_download))
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismiss) {
                    Text(stringResource(R.string.update_later))
                }
            }
        )

        is UpdateStep.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.update_downloading_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { s.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.update_percent, (s.progress * 100).toInt()))
                }
            },
            confirmButton = {}
        )

        is UpdateStep.ReadyToInstall -> AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text(stringResource(R.string.update_install_title)) },
            text = { Text(stringResource(R.string.update_install_text)) },
            confirmButton = {
                TextButton(onClick = { controller.install(s.file) }) {
                    Text(stringResource(R.string.update_install))
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismiss) {
                    Text(stringResource(R.string.update_later))
                }
            }
        )

        UpdateStep.UpToDate -> AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text(stringResource(R.string.update_title)) },
            text = { Text(stringResource(R.string.update_uptodate, BuildConfig.VERSION_NAME)) },
            confirmButton = {
                TextButton(onClick = controller::dismiss) { Text(stringResource(R.string.update_ok)) }
            }
        )

        is UpdateStep.Error -> AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text(stringResource(R.string.update_title)) },
            text = { Text(s.message) },
            confirmButton = {
                TextButton(onClick = controller::dismiss) { Text(stringResource(R.string.update_ok)) }
            }
        )
    }
}
