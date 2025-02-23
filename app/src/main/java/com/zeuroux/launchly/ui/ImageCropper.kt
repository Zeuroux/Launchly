package com.zeuroux.launchly.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageView

@Composable
fun ImageCropper(showCropper: MutableState<Boolean>, bitmap: Bitmap, onConfirmation: (Bitmap) -> Unit) {
    var croppedImageView by remember { mutableStateOf<CropImageView?>(null) }

    BaseDialog(showCropper) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(bottom = 8.dp),
                Arrangement.spacedBy(4.dp),
            ) {
                DialogTopBar("Crop image", {
                    IconButton({ croppedImageView?.rotateImage(90) }, Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Rotate90DegreesCw,"Rotate", Modifier.size(24.dp))
                    }
                }) { showCropper.value = false }
                CropImageViewCompose(
                    Modifier
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    setImageBitmap(bitmap)
                    setAspectRatio(1, 1)
                    setFixedAspectRatio(true)
                    croppedImageView = this
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    TextButton({ showCropper.value = false }) {
                        Text("Cancel")
                    }
                    TextButton({ croppedImageView?.getCroppedImage()?.let { onConfirmation(it); showCropper.value = false } }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

fun scaleBitmap(bitmap: Bitmap, targetDimension: Int): Bitmap {
    val scaleFactor = minOf(
        targetDimension.toFloat() / bitmap.width,
        targetDimension.toFloat() / bitmap.height
    )
    val newWidth = (bitmap.width * scaleFactor).toInt()
    val newHeight = (bitmap.height * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
}

fun squareBitmap(bitmap: Bitmap, backgroundColor: Int = Color.TRANSPARENT): Bitmap {
    val size = maxOf(bitmap.width, bitmap.height)
    val squareBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(squareBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    val left = (size - bitmap.width) / 2f
    val top = (size - bitmap.height) / 2f
    canvas.drawBitmap(bitmap, left, top, null)

    return squareBitmap
}
