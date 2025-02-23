package com.zeuroux.launchly.ui

import android.content.Context
import android.graphics.Bitmap
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zeuroux.launchly.R
import com.zeuroux.launchly.extensions.message
import com.zeuroux.launchly.extensions.toBitmap
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.version.Installation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

val invalidPrefixes = listOf("/data", "/storage/emulated/0/Android/data", "/storage/emulated/0/Android/media", "/storage/emulated/0/Android/obb")
@Composable
fun InstallationSetup(onSetupComplete: (Installation) -> Unit = {}) {
    val context = LocalContext.current
    val versionDb = remember { GlobalData.getVersionDB(context) }
    val versions = versionDb.versions.collectAsState().value
    val latestRelease = versionDb.latestRelease.collectAsState().value
    val showInstallationSetup = GlobalData.showInstallationSetup
    val icon = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(showInstallationSetup.value) {
        icon.value = R.drawable.grass.toBitmap(context)
    }
    val showTypePicker = remember { mutableStateOf(false) }
    if (latestRelease == null) return
    val name = remember { mutableStateOf("Release ${latestRelease.second.name}") }
    var version by remember { mutableStateOf(latestRelease) }
    val directory = remember { mutableStateOf("<Use default directory>") }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            val dir = DocumentsContract.getTreeDocumentId(uri)?.replace("primary:", "/storage/emulated/0/") ?: "<Use default directory>"
            if (invalidPrefixes.none { dir.startsWith(it) } && File(dir).exists()) {
                directory.value = dir
            } else {
                context.message("Invalid directory")
            }
        }
    }
    BaseDialog(showInstallationSetup, { showInstallationSetup.value = false }) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(rememberScrollState()),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterHorizontally
            ) {
                DialogTopBar("Create new Installation") { showInstallationSetup.value = false }
                InstallationIcon(Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showTypePicker.value = true }, icon)
                TextInput(name, "Installation Name")
                TextInput(name, "Version", true, "${version.second.type} ${version.second.name}") {
                    GlobalData.showVersionChooser.value = true
                }
                TextInput(directory, "Game Directory", true, directory.value) {
                    folderPicker.launch(null)
                }
                CancelOrConfirm(confirmText = "Create", onConfirm = {
                    val installation = Installation(
                        installationName = name.value,
                        versionCode = version.first,
                        installationId = UUID.randomUUID().toString(),
                        dateModified = System.currentTimeMillis(),
                        icon = icon.value!!,
                        patches = emptyList()
                    )
                    onSetupComplete(installation)
                    showInstallationSetup.value = false
                })
            }
        }
    }
    TypePickDialog(showTypePicker, { showTypePicker.value = false }) {
        icon.value = it
    }
    VersionChooser {
        val newVersion = versions[it]!!
        if (name.value == "${version.second.type} ${version.second.name}") {
            name.value = "${newVersion.type} ${newVersion.name}"
        }
        version = it to newVersion
    }
}

@Composable//bad lmao
fun TextInput(input: MutableState<String>, label: String, clickMode: Boolean = false, current: String = "", onClick: () -> Unit = {}) {
    val defaults = TextFieldDefaults.colors()
    TextField(
        value = if (clickMode) current else input.value,
        onValueChange = { input.value = it },
        label = { Text(label) },
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .let { if (clickMode) it.clickable { onClick() } else it },
        trailingIcon = if (clickMode) { { Icon(Icons.Default.ExpandMore, "Expand", tint = Color.Gray) } } else null,
        enabled = !clickMode,
        colors = TextFieldDefaults.colors(
            disabledTextColor = defaults.unfocusedTextColor,
            disabledLabelColor = defaults.unfocusedLabelColor,
            disabledIndicatorColor = defaults.unfocusedIndicatorColor,
            disabledTrailingIconColor = defaults.unfocusedTrailingIconColor
        )
    )
}

@Composable
private fun InstallationIcon(modifier: Modifier, installationIcon: MutableState<Bitmap?>) {
    Box(Modifier.requiredSize(80.dp), Alignment.BottomEnd) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(installationIcon.value ?: R.drawable.grass)
                .crossfade(true)
                .build(),
            contentDescription = "Icon",
            modifier = modifier
        )

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Icon",
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
        )
    }
}

fun crop(
    scope: CoroutineScope,
    selected: MutableState<Bitmap?>,
    any: Any,
    context: Context,
    showImageCropper: MutableState<Boolean>
) {
    scope.launch {
        selected.value = squareBitmap(
            scaleBitmap(
                any.toBitmap(context)!!, 1000
            )
        )
        showImageCropper.value = true
    }
}