package com.zeuroux.launchly.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsDialog(showDialog: Boolean, onDismissRequest: () -> Unit) {
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiPrefs = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    val isSystemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(uiPrefs.getBoolean("dark_theme", isSystemDarkTheme)) }
    val isDynamicColor = remember { mutableStateOf(uiPrefs.getBoolean("dynamic_color", true)) }
    val isFullscreenVersionSelector = remember { mutableStateOf(uiPrefs.getBoolean("fullscreen_version_selector", false)) }
    val versionSelectorHeight = remember { mutableIntStateOf(uiPrefs.getInt("version_selector_height", 250)) }

    val versionListPrefs = context.getSharedPreferences("version_list_prefs", Context.MODE_PRIVATE)
    val showVersionCode = remember { mutableStateOf(versionListPrefs.getBoolean("show_version_code", true)) }
    val showVersionType = remember { mutableStateOf(versionListPrefs.getBoolean("show_version_type", false)) }
    val showDividers = remember { mutableStateOf(versionListPrefs.getBoolean("show_dividers", true)) }
    val combineVersions = remember { mutableStateOf(versionListPrefs.getBoolean("combine_versions", false)) }


    val showInputDialog = remember { mutableStateOf(false) }
    val inputDialogTitle = remember { mutableStateOf("") }
    val inputDialogLabel = remember { mutableStateOf("") }
    val inputDialogInitialValue = remember { mutableStateOf("") }
    val inputDialogFilters = remember { mutableStateOf(Regex("")) }
    val inputDialogKeyboardOptions = remember { mutableStateOf(KeyboardOptions.Default) }
    val inputDialogOnDismissRequest: MutableState<(String) -> Unit> = remember { mutableStateOf({}) }

    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            animateIn = false
            onDismissRequest()
        }
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
                Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 0.dp)) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(24.dp, 0.dp))
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier.fillMaxHeight(.7f)
                    ) {
                        Column( Modifier.verticalScroll(rememberScrollState())) {
                            Text("User Interface", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp, 0.dp))
                            Option(
                                title = "Theme",
                                description = if (isDarkTheme.value) "Dark" else "Light",
                                state = isDarkTheme.value,
                                onCheckedChange = {
                                    isDarkTheme.value = it
                                    uiPrefs.edit().putBoolean("dark_theme", it).apply()
                                }
                            )
                            Option(
                                title = "Dynamic Color",
                                description = "Theme color based on wallpaper",
                                state = isDynamicColor.value,
                                onCheckedChange = {
                                    isDynamicColor.value = it
                                    uiPrefs.edit().putBoolean("dynamic_color", it).apply()
                                }
                            )
                            Option(
                                title = "Fullscreen Version Selector",
                                description = "Show version selector in fullscreen",
                                state = isFullscreenVersionSelector.value,
                                onCheckedChange = {
                                    isFullscreenVersionSelector.value = it
                                    uiPrefs.edit().putBoolean("fullscreen_version_selector", it).apply()
                                }
                            )
                            if (!isFullscreenVersionSelector.value) {
                                Option(
                                    title = "Version Selector Height",
                                    description = "${versionSelectorHeight.intValue} dp",
                                    modifier = Modifier.clickable {
                                        inputDialogTitle.value = "Version Selector Height"
                                        inputDialogLabel.value = "Height (dp)"
                                        inputDialogInitialValue.value = versionSelectorHeight.intValue.toString()
                                        inputDialogFilters.value = Regex("[0-9]{1,4}")
                                        inputDialogKeyboardOptions.value = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        inputDialogOnDismissRequest.value = {
                                            versionSelectorHeight.intValue = it.toInt()
                                            uiPrefs.edit().putInt("version_selector_height", it.toInt()).apply()
                                        }
                                        showInputDialog.value = true
                                    }
                                )
                            }
                            Text("Version List", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp, 24.dp, 24.dp, 0.dp))
                            Option(
                                title = "Show Version Code",
                                description = "Show version code in version list",
                                state = showVersionCode.value,
                                onCheckedChange = {
                                    showVersionCode.value = it
                                    versionListPrefs.edit().putBoolean("show_version_code", it).apply()
                                }
                            )
                            Option(
                                title = "Show Version Type",
                                description = "Show version type in version list",
                                state = showVersionType.value,
                                onCheckedChange = {
                                    showVersionType.value = it
                                    versionListPrefs.edit().putBoolean("show_version_type", it).apply()
                                }
                            )
                            Option(
                                title = "Show Dividers",
                                description = "Show dividers between versions",
                                state = showDividers.value,
                                onCheckedChange = {
                                    showDividers.value = it
                                    versionListPrefs.edit().putBoolean("show_dividers", it).apply()
                                }
                            )
                            Option(
                                title = "Combine Versions",
                                description = "Combine versions with the same name",
                                state = combineVersions.value,
                                onCheckedChange = {
                                    combineVersions.value = it
                                    versionListPrefs.edit().putBoolean("combine_versions", it).apply()
                                }
                            )
                        }
                    }
                }
                InputDialog(
                    showDialog = showInputDialog.value,
                    title = inputDialogTitle.value,
                    label = inputDialogLabel.value,
                    initialValue = inputDialogInitialValue.value,
                    filters = inputDialogFilters.value,
                    keyboardOptions = inputDialogKeyboardOptions.value,
                    onDismissRequest = {
                        inputDialogOnDismissRequest.value(it)
                        showInputDialog.value = false
                    }
                )
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
fun Option(
    title: String,
    description: String,
    state: Boolean? = null,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(24.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state != null) {
            VerticalDivider(
                Modifier
                    .height(30.dp)
                    .padding(16.dp, 0.dp))
            Switch(
                checked = state,
                onCheckedChange = { bool ->
                    onCheckedChange(bool)
                },
                thumbContent = {
                    if (title == "Theme") {
                        if (state) {
                            Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode")
                        } else {
                            Icon(Icons.Default.LightMode, contentDescription = "Light Mode")
                        }
                    }
                }
            )
        }
    }
}