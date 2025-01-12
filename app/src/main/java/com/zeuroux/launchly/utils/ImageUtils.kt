package com.zeuroux.launchly.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun scaleBitmap(bitmap: Bitmap, targetDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val scaleFactor = maxOf(
        targetDimension.toFloat() / width,
        targetDimension.toFloat() / height
    )
    val newWidth = (width * scaleFactor).toInt()
    val newHeight = (height * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun squareBitmap(bitmap: Bitmap, backgroundColor: Int = Color.Transparent.toArgb()): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val squareSize = maxOf(width, height)
    val squareBitmap = Bitmap.createBitmap(squareSize, squareSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(squareBitmap)
    val paint = Paint()
    paint.color = backgroundColor
    canvas.drawRect(0f, 0f, squareSize.toFloat(), squareSize.toFloat(), paint)
    val left = (squareSize - width) / 2f
    val top = (squareSize - height) / 2f
    canvas.drawBitmap(bitmap, left, top, null)
    return squareBitmap
}