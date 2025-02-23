package com.zeuroux.launchly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.ui.ConfirmationDialog
import com.zeuroux.launchly.ui.LoginScreen
import com.zeuroux.launchly.ui.Main
import com.zeuroux.launchly.ui.ManageAccountScreen
import com.zeuroux.launchly.ui.MoreDialog
import com.zeuroux.launchly.ui.SettingsScreen
import com.zeuroux.launchly.ui.theme.LaunchlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalData.getVersionDB(this).fetchVersions(this)
        GlobalData.updateAuthData(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchlyTheme {
                Main()
                MoreDialog()
                LoginScreen()
                ManageAccountScreen()
                ConfirmationDialog()
                SettingsScreen()
            }
        }
    }
}