package com.zeuroux.launchly.extensions

import android.content.Context
import android.graphics.Bitmap
import com.zeuroux.launchly.ui.scaleBitmap
import net.lingala.zip4j.ZipFile

suspend fun ZipFile.getImages(context: Context): MutableMap<String, Bitmap> {
    val images = mutableMapOf<String, Bitmap>()
    this.fileHeaders.forEach {
        if (it.fileName.matches(Regex(".*\\.(png|jpg|jpeg)$"))) {
            val image = this.getInputStream(it).use { img -> img.toBitmap(context) }
            val fileName = it.fileName.substringAfterLast('/')
            if (image != null) images[fileName] = scaleBitmap(image, 64)
        }
    }
    if (images.isEmpty()) context.message("No images found in zip file")
    return images
}