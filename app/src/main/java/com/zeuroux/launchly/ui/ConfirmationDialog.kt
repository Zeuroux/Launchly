package com.zeuroux.launchly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zeuroux.launchly.globals.GlobalData

@Composable
fun ConfirmationDialog() {
    val showScreen = GlobalData.showConfirmationDialog
    val dialogData = GlobalData.confirmationDialogData.value ?: return
    BaseDialog(showScreen, { showScreen.value = false; dialogData.onCancel() }) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = dialogData.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = dialogData.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(8.dp)
                        .defaultMinSize(minHeight = 30.dp)
                )
                CancelOrConfirm({
                    dialogData.onCancel()
                    GlobalData.showConfirmationDialog.value = false
                }, dialogData.cancelText, {
                    dialogData.onConfirm()
                    GlobalData.showConfirmationDialog.value = false
                }, dialogData.confirmText)//lmao
            }
        }
    }
}

@Composable
fun CancelOrConfirm(
    onCancel: () -> Unit = {},
    cancelText: String = "",
    onConfirm: () -> Unit = {},
    confirmText: String = ""
) {
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.SpaceBetween
    ) {
        TextButton(onCancel, enabled = cancelText.isNotEmpty()) {
            Text(text = cancelText)
        }

        TextButton(onConfirm, enabled = confirmText.isNotEmpty()) {
            Text(text = confirmText)
        }

    }
}