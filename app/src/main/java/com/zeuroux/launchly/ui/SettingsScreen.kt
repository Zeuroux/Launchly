package com.zeuroux.launchly.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zeuroux.launchly.globals.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.settings by preferencesDataStore(name = "settings")

@Suppress("unused")
class SettingsManager(private val context: Context, private val scope: CoroutineScope) {

    fun getIntAsFlow(key: String): Flow<Int> = context.settings.data.map { it[intPreferencesKey(key)] ?: 0 }

    fun setInt(key: String, value: Int) {
        scope.launch { context.settings.edit { it[intPreferencesKey(key)] = value } }
    }

    fun getStringAsFlow(key: String): Flow<String> = context.settings.data.map { it[stringPreferencesKey(key)] ?: "" }

    suspend fun getString(key: String): String = getStringAsFlow(key).first()

    fun setString(key: String, value: String) {
        scope.launch { context.settings.edit { it[stringPreferencesKey(key)] = value } }
    }

    fun getBooleanAsFlow(key: String): Flow<Boolean> = context.settings.data.map { it[booleanPreferencesKey(key)] ?: false }

    fun setBoolean(key: String, value: Boolean) {
        scope.launch { context.settings.edit { it[booleanPreferencesKey(key)] = value } }
    }
}

@Composable
fun SettingsScreen() {
    val showScreen = GlobalData.showSettingsScreen
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context, scope) }

    BaseScreen(showScreen.value, { showScreen.value = false }, topBar = { ScreenTopBar("Settings") { showScreen.value = false } }) { padding ->
        Surface(Modifier.padding(padding)) {
            Column {
                AppearanceSettings(settingsManager)
            }
        }
    }
}

@Composable
private fun AppearanceSettings(
    settingsManager: SettingsManager
) {
    val showThemeSelector = remember { mutableStateOf(false) }

    val theme by settingsManager.getStringAsFlow("theme").collectAsState(initial = "system")
    val dynamicColor by settingsManager.getBooleanAsFlow("dynamic_color").collectAsState(initial = true)

    CategoryTitle("Appearance")
    Setting("Theme", "Change the color scheme of the app", {
        Icon(
            when (theme) {
                "system" -> Icons.Default.AutoMode
                "light" -> Icons.Default.LightMode
                "dark" -> Icons.Default.DarkMode
                else -> Icons.Default.AutoMode
            }, contentDescription = "Theme"
        )
    }, {
        val isSystemDark = isSystemInDarkTheme()
        Text(if (theme == "system" || (theme as String?).isNullOrEmpty()) "System (${if (isSystemDark) "Dark" else "Light"})" else theme.replaceFirstChar { it.uppercaseChar() })
    }) {
        showThemeSelector.value = true
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Setting("Dynamic Color", "Automatically change the color scheme based on the wallpaper", { Icon(Icons.Default.Colorize, contentDescription = "Dynamic Color") }, {
            Switch(
                checked = dynamicColor,
                onCheckedChange = { settingsManager.setBoolean("dynamic_color", it) }
            )
        }) { settingsManager.setBoolean("dynamic_color", !dynamicColor) }
    }
    ThemeSelector(showThemeSelector, theme) { settingsManager.setString("theme", it) }
}

@Composable
fun ThemeSelector(show: MutableState<Boolean>, currentTheme: String?, onThemeSelected: (String) -> Unit) {
    val isSystemDark = isSystemInDarkTheme()
    BaseBottomSheet(show.value, { show.value = false }, true) {
        Column {
            Text("Select Theme", Modifier.padding(16.dp, 8.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            ThemeOption("System (${if (isSystemDark) "Dark" else "Light"}) Theme", currentTheme == "system" || currentTheme.isNullOrEmpty()) { onThemeSelected("system") }
            ThemeOption("Light Theme", currentTheme == "light") { onThemeSelected("light") }
            ThemeOption("Dark Theme", currentTheme == "dark") { onThemeSelected("dark") }
        }
    }
}

@Composable
fun ThemeOption(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp, 4.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun Setting(title: String, description: String, icon: @Composable () -> Unit, action: @Composable () -> Unit, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp, 8.dp)
            .defaultMinSize(minHeight = 40.dp),
        Arrangement.Absolute.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f).padding(end = 20.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
            icon()
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
        action()
    }
    HorizontalDivider(Modifier.padding(16.dp, 8.dp))
}

@Composable
fun CategoryTitle(text: String) {
    Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}