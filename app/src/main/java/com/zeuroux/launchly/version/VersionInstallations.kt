package com.zeuroux.launchly.version

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.JsonParser
import com.zeuroux.launchly.extensions.decodeBitmap
import java.io.File

data class Installation(
    val installationName: String,
    val versionCode: String,
    val installationId: String,
    val icon: Bitmap,
    val dateModified: Long,
    val patches: List<String>
)

fun loadInstallationList(context: Context): List<Installation> {
    val installations = mutableListOf<Installation>()
    val installationsDir = File(context.dataDir, "installations")
    if (!installationsDir.exists()) { installationsDir.mkdirs() }
    val installationDirs = installationsDir.listFiles()
    installationDirs?.forEach { installationDir ->
        val jsonFile = File(installationDir, "installation.json")
        val json = JsonParser.parseString(jsonFile.readText()).asJsonObject
        val installation = Installation(
            installationName = json.get("installationName").asString,
            versionCode = json.get("versionCode").asString,
            installationId = json.get("installationId").asString,
            icon = File(installationDir, "icon.png").inputStream().use { decodeBitmap(it) },
            dateModified = json.get("dateModified").asLong,
            patches = json.get("patches").asJsonArray.map { it.asString }
        )
        installations.add(installation)
    }
    return installations
}

fun saveInstallation(context: Context, installation: Installation) {
    val installationsDir = File(context.dataDir, "installations")
    if (!installationsDir.exists()) { installationsDir.mkdirs() }
    val installationDir = File(installationsDir, installation.installationId)
    if (!installationDir.exists()) { installationDir.mkdirs() }
    val jsonFile = File(installationDir, "installation.json")
    jsonFile.writeText(
        """
        {
            "installationName": "${installation.installationName}",
            "versionCode": "${installation.versionCode}",
            "installationId": "${installation.installationId}",
            "dateModified": ${installation.dateModified},
            "patches": ${installation.patches.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
        }
        """.trimIndent()
    )
    val iconFile = File(installationDir, "icon.png")
    installation.icon.compress(Bitmap.CompressFormat.PNG, 100, iconFile.outputStream())

}

fun deleteInstallation(context: Context, installationId: String) {
    val installationsDir = File(context.dataDir, "installations")
    val installationDir = File(installationsDir, installationId)
    installationDir.deleteRecursively()
}