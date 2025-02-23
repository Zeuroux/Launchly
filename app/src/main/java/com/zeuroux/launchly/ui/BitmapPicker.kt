package com.zeuroux.launchly.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun BitmapPicker(showPicker: MutableState<Boolean>, bitmaps: Map<String, Bitmap>, onDismiss: () -> Unit, onPick: (Bitmap) -> Unit) {
    val searchQuery = remember { mutableStateOf("") }
    val dummy = remember { mutableStateOf("") }
    BaseDialog(showPicker, onDismiss) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .height(500.dp)
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(bottom = 8.dp),
                Arrangement.spacedBy(4.dp),
            ) {
                DialogTopBar("Choose Icon") { showPicker.value = false }
                SearchBar(searchQuery)
                BitmapList(
                    bitmaps.filter { it.key.contains(searchQuery.value, ignoreCase = true) },
                    onPick,
                    onDismiss)
            }
        }
    }
}

@Composable
private fun BitmapList(
    bitmaps: Map<String, Bitmap>,
    onPick: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn {
        items(bitmaps.count()) { index ->
            val bitmap = bitmaps.values.elementAt(index)
            val path = bitmaps.keys.elementAt(index)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPick(bitmap); onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bitmap)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(path, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}