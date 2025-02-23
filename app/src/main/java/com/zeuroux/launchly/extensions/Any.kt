package com.zeuroux.launchly.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.bitmapConfig
import coil3.toBitmap
import java.io.ByteArrayOutputStream
import java.io.InputStream

suspend fun Any.toBitmap(context: Context): Bitmap? {
    if (this is InputStream) return decodeBitmap(this)
    val loader = context.imageLoader
    val request = ImageRequest.Builder(context)
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        .data(this)
        .build()
    return loader.execute(request).image?.toBitmap()
}

fun decodeBitmap(inputStream: InputStream): Bitmap {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var length: Int
    while ((inputStream.read(buffer).also { length = it }) != -1) {
        byteArrayOutputStream.write(buffer, 0, length)
    }
    val imageBytes = byteArrayOutputStream.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}