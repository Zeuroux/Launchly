package com.zeuroux.launchly.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun LaunchlyTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)

    val isDarkTheme = isSystemInDarkTheme()
    val darkTheme = remember {
        mutableStateOf(
            sharedPreferences.getBoolean("dark_theme", isDarkTheme).also { defaultValue ->
                if (!sharedPreferences.contains("dark_theme")) {
                    sharedPreferences.edit().putBoolean("dark_theme", defaultValue).apply()
                }
            }
        )
    }

    val dynamicColor = remember {
        mutableStateOf(
            sharedPreferences.getBoolean("dynamic_color", true).also { defaultValue ->
                if (!sharedPreferences.contains("dynamic_color")) {
                    sharedPreferences.edit().putBoolean("dynamic_color", defaultValue).apply()
                }
            }
        )
    }

    DisposableEffect(sharedPreferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                "dark_theme" -> darkTheme.value = prefs.getBoolean("dark_theme", isDarkTheme)
                "dynamic_color" -> dynamicColor.value = prefs.getBoolean("dynamic_color", true)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val colorScheme = when {
        dynamicColor.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme.value) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme.value -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
