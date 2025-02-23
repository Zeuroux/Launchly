package com.zeuroux.launchly.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.version.Installation
import com.zeuroux.launchly.version.VersionData
import com.zeuroux.launchly.version.deleteInstallation
import kotlinx.coroutines.delay


lateinit var versions: Map<String, VersionData>
@Composable
fun InstallationList(installations: MutableList<Installation>) {
    versions = GlobalData.getVersionDB(LocalContext.current).versions.collectAsState().value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val visibleInstallations = remember { mutableStateListOf<Installation>() }
    val pendingRemoval = remember { mutableStateListOf<Installation>() }
    LaunchedEffect(installations.size) {
        installations.forEach { installation ->
            if (!visibleInstallations.contains(installation) && !pendingRemoval.contains(installation)) {
                delay(100)
                visibleInstallations.add(installation)
            }
        }
    }
    LazyColumn {
        items(installations) { installation ->
            AnimatedVisibility(
                visible = visibleInstallations.contains(installation) && !pendingRemoval.contains(installation),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                InstallationCard(installation) {
                    deleteInstallation(context, installation.installationId)
                    pendingRemoval.add(installation)
                }
            }
        }
    }
}

@Composable
fun InstallationCard(installation: Installation, onRemove: () -> Unit) {
    val progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(progress)
    val versionInfo = versions[installation.versionCode]!!
    ProgressBox(
        animatedProgress,
        Modifier.padding(6.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            Modifier
                .height(70.dp)
                .fillMaxWidth()
                .clickable { onRemove() }
                .padding(8.dp),
            Arrangement.spacedBy(8.dp),
            Alignment.CenterVertically
        ) {
            InstallationIcon(installation)
            Column(Modifier.fillMaxHeight().weight(1f)) {
                Text(installation.installationName, style = MaterialTheme.typography.titleLarge)
                Info("Version: ${versionInfo.name}, Code: ${installation.versionCode}")
                Info("Type: ${versionInfo.type}, Architecture: ${versionInfo.architecture}")
            }
        }
    }
}
@Composable
private fun InstallationIcon(installation: Installation) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(installation.icon)
            .crossfade(true)
            .build(),
        contentDescription = "Icon",
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.Gray)
    )
}

@Composable
fun Info(text : String) = Text(text, style = MaterialTheme.typography.bodySmall, overflow = TextOverflow.Ellipsis)