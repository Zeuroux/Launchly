package com.zeuroux.launchly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun EnterURLDialog(showDialog: MutableState<Boolean>, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val url = remember { mutableStateOf("") }
    BaseDialog(showDialog, onDismiss) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(bottom = 8.dp),
                Arrangement.spacedBy(4.dp),
            ) {
                DialogTopBar("Image URL") { onDismiss() }
                TextField(
                    value = url.value,
                    onValueChange = { url.value = it },
                    label = { Text("Enter URL") },
                    modifier = Modifier.padding(8.dp).fillMaxWidth()
                )
                CancelOrConfirm(confirmText = "Confirm", onConfirm = { onConfirm(url.value); onDismiss() })
            }
        }
    }
}

