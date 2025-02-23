package com.zeuroux.launchly.extensions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

fun Context.browse(url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(this, url.toUri())
}

fun Context.getRealPathFromURI(uri: Uri): String? {
    (contentResolver.query(uri, null, null, null, null) ?: return null).use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        val fileName = name.substringAfterLast("/").replace("..", "_").replace("/", "_")
        val file = File(filesDir, "temp/$fileName")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(input.available().coerceAtMost(1024 * 1024))
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
        }
        return file.path
    }
}

fun Context.message(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}