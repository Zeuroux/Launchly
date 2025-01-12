package com.zeuroux.launchly.screens

import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zeuroux.launchly.utils.Architecture
import com.zeuroux.launchly.utils.VersionData
import com.zeuroux.launchly.utils.VersionType
import com.zeuroux.launchly.utils.VersionsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionSelector(
    showDialog: Boolean,
    versionsViewModels: List<VersionsViewModel>,
    onConfirmation: (version: VersionData?) -> Unit
) {
    val archs = (Build.SUPPORTED_ABIS.map { Architecture.fromString(it).arch }).filter { it != "unknown" }
    var animateIn by remember { mutableStateOf(false) }
    var selectedArch by remember { mutableStateOf(Architecture.ARM64) }
    var selectedVersion by remember { mutableStateOf<VersionData?>(null) }
    var type by remember { mutableStateOf(VersionType.BETA) }
    var searchQuery by remember { mutableStateOf("") }
    var showAnimatedDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiPrefs = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    val fullScreen = uiPrefs.getBoolean("fullscreen_version_selector", false)
    val versionSelectorHeight = (uiPrefs.getInt("version_selector_height", 250))

    val versionListPrefs = context.getSharedPreferences("version_list_prefs", Context.MODE_PRIVATE)
    val showVersionCode = versionListPrefs.getBoolean("show_version_code", true)
    val showVersionType = versionListPrefs.getBoolean("show_version_type", false)
    val showDividers = versionListPrefs.getBoolean("show_dividers", true)
    val combineVersions = versionListPrefs.getBoolean("combine_versions", false)

    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            selectedArch = Architecture.ARM64
            selectedVersion = null
            type = VersionType.RELEASE
            searchQuery = ""
            onConfirmation(null)
            animateIn = false
        },
        properties = DialogProperties(usePlatformDefaultWidth = !fullScreen)
    ) {
        LaunchedEffect(Unit) {
            animateIn = true
        }
        AnimatedVisibility(
            visible = animateIn,
            enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(
                initialScale = .8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically { it / 8 } + fadeOut() + scaleOut(targetScale = .95f)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                    Text(
                        "Select Minecraft Version",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Version") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                        if (!combineVersions) {
                            Row(
                                Modifier.padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Beta")
                                Checkbox(checked = type == VersionType.BETA, onCheckedChange = {
                                    type = if (it) VersionType.BETA else VersionType.RELEASE
                                })
                            }
                        }
                    }
                    TabRow(selectedTabIndex = selectedArch.ordinal) {
                        archs.forEachIndexed { index, title ->
                            Tab(
                                selected = index == selectedArch.ordinal,
                                onClick = { selectedArch = Architecture.entries.toTypedArray()[index] },
                                text = { Text(title) }
                            )
                        }
                    }
                    Box(
                        Modifier.then(
                            if (fullScreen) Modifier
                                .fillMaxSize()
                                .weight(1f) else Modifier.height(
                                versionSelectorHeight.dp
                            )
                        )
                    ) {
                        val viewModel = versionsViewModels[selectedArch.ordinal]
                        val versionData by viewModel.versionData.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()
                        val error by viewModel.error.collectAsState()

                        when {
                            isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                            error != null -> Text("Error: $error", Modifier.align(Alignment.Center))
                            else -> {
                                val filteredVersions = versionData.filter {
                                    (combineVersions || it.type == type) &&
                                            (it.name.contains(
                                                searchQuery,
                                                ignoreCase = true
                                            ) ||
                                                    it.code.toString().contains(searchQuery))
                                }
                                LazyColumn {
                                    items(
                                        items = filteredVersions,
                                        key = { it.code }
                                    ) { version ->
                                        VersionRow(
                                            version = version,
                                            selectedVersion = selectedVersion,
                                            onClick = { selectedVersion = version },
                                            showVersionCode = showVersionCode,
                                            showVersionType = showVersionType,
                                        )
                                        if (showDividers) HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            onConfirmation(selectedVersion)
                            selectedArch = Architecture.ARM64
                            selectedVersion = null
                            type = VersionType.RELEASE
                            searchQuery = ""
                            animateIn = false
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text(selectedVersion?.name?.let { "Confirm ($it)" } ?: "Close")
                    }
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    showAnimatedDialog = false
                }
            }
        }
    }
}

@Composable
fun VersionRow(
    version: VersionData,
    selectedVersion: VersionData?,
    onClick: () -> Unit,
    showVersionCode: Boolean,
    showVersionType: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(version.name, style = MaterialTheme.typography.bodyLarge)
            if (showVersionCode) Text(
                "Version Code: ${version.code}",
                style = MaterialTheme.typography.bodySmall
            )
            if (showVersionType) Text(
                "Version Type: ${if (version.type == VersionType.BETA) "Beta" else "Release"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.weight(1f))
        RadioButton(
            selected = selectedVersion?.name == version.name,
            onClick = onClick
        )
    }
}
