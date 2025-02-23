package com.zeuroux.launchly.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.version.Installation
import com.zeuroux.launchly.version.loadInstallationList
import com.zeuroux.launchly.version.saveInstallation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val context = LocalContext.current
    val installations = remember { mutableStateListOf<Installation>() }
    LaunchedEffect(Unit) { loadInstallationList(context).let { installations.addAll(it) } }
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Launchly") },
                actions = {
                    IconButton({ GlobalData.showMoreDialog.value = true })
                    { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                { Text("Add Version") },
                { Icon(Icons.Default.Add, "Add") },
                { GlobalData.showInstallationSetup.value = true }
            )
        }
    ) {
        Surface (Modifier.padding(it).fillMaxSize()) {
            InstallationList(installations)
        }
    }
    InstallationSetup { installation ->
        saveInstallation(context, installation)
        installations.add(installation)
    }
}