package com.zeuroux.launchly.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zeuroux.launchly.extensions.getImages
import com.zeuroux.launchly.extensions.getRealPathFromURI
import com.zeuroux.launchly.extensions.message
import com.zeuroux.launchly.extensions.toBitmap
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile

@Composable
fun TypePickDialog(showPicker: MutableState<Boolean>, onDismiss: () -> Unit, onPick: (Bitmap?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showImageCropper = remember { mutableStateOf(false) }
    val selected = remember { mutableStateOf<Bitmap?>(null) }
    val showBitmapPicker = remember { mutableStateOf(false) }
    var bitmaps by remember { mutableStateOf(mapOf<String, Bitmap>()) }
    val showUrlDialog = remember { mutableStateOf(false) }
    val pickMediaLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            crop(scope, selected, uri, context, showImageCropper)
        } else context.message("No image selected")
    }
    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
        if (fileUri != null) {
            val path = context.getRealPathFromURI(fileUri)
            if (path?.matches(Regex(".*\\.(mcpack|zip|mcaddon)$")) == true) {
                scope.launch {
                    bitmaps = ZipFile(path).getImages(context)
                    showBitmapPicker.value = true
                }
            } else context.message("Invalid file type")
        } else context.message("No file selected")
    }
    BaseDialog(showPicker, onDismiss) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(bottom = 8.dp),
                Arrangement.spacedBy(4.dp),
            ) {
                DialogTopBar("Choose From") { onDismiss() }
                TypeOption(Icons.Default.Image, "Gallery") {
                    pickMediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
                TypeOption(Icons.Default.Link, "Image Url") {
                    showUrlDialog.value = true
                }
                TypeOption(Icons.Default.FolderZip, "Resource pack") {
                    pickFileLauncher.launch("*/*")
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                TypeOption(Icons.Default.Restore, "Restore to default") {
                    onPick(null)
                    onDismiss()
                }
            }
        }
    }
    if (selected.value != null) {
        ImageCropper(showImageCropper, selected.value!!) { onPick(it); onDismiss() }
    }

    if (bitmaps.isNotEmpty()) {
        BitmapPicker(showBitmapPicker, bitmaps, { showBitmapPicker.value = false }) {
            crop(scope, selected, it, context, showImageCropper)
        }
    }

    EnterURLDialog(showUrlDialog, {showUrlDialog.value = false}) {url ->
        scope.launch {
            val bitmap = url.toBitmap(context)
            if (bitmap != null) {
                selected.value = scaleBitmap(bitmap, 2080)
                showImageCropper.value = true
            } else context.message("Invalid URL")
        }
    }
}

@Composable
fun TypeOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row (
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        Arrangement.spacedBy(4.dp),
        Alignment.CenterVertically
    ) {
        Icon(icon, title)
        Text(title)
    }
}