package com.zeuroux.launchly.activities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.SwipeableState
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.zeuroux.launchly.broadcast_receivers.PackageStatusReceiver
import com.zeuroux.launchly.screens.Onboarding
import com.zeuroux.launchly.screens.SettingsDialog
import com.zeuroux.launchly.screens.VersionOptions
import com.zeuroux.launchly.screens.VersionSelector
import com.zeuroux.launchly.ui.theme.LaunchlyTheme
import com.zeuroux.launchly.utils.ActionButton
import com.zeuroux.launchly.utils.Category
import com.zeuroux.launchly.utils.Downloader
import com.zeuroux.launchly.utils.DropDown
import com.zeuroux.launchly.utils.GPlayAPI
import com.zeuroux.launchly.utils.GPlayAPI.getApks
import com.zeuroux.launchly.utils.Info
import com.zeuroux.launchly.utils.OptionMode
import com.zeuroux.launchly.utils.ProgressBox
import com.zeuroux.launchly.utils.SavedVersionData
import com.zeuroux.launchly.utils.SessionResult
import com.zeuroux.launchly.utils.Status
import com.zeuroux.launchly.utils.VersionDatabaseHelper
import com.zeuroux.launchly.utils.VersionDatabaseViewModel
import com.zeuroux.launchly.utils.VersionType
import com.zeuroux.launchly.utils.VersionsViewModel
import com.zeuroux.launchly.utils.f
import com.zeuroux.launchly.utils.getPackageArch
import com.zeuroux.launchly.utils.getPackageInfo
import com.zeuroux.launchly.utils.getPackageInstallSource
import com.zeuroux.launchly.utils.getVersionTypeByCode
import com.zeuroux.launchly.utils.installPackage
import com.zeuroux.launchly.utils.saveIconToFile
import com.zeuroux.launchly.utils.uninstallApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        filesDir.walk().forEach {
            if (it.isFile && it.name.endsWith(".temp")) {
                it.delete()
            }
        }

        val sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE)
        val accountPreferences = getSharedPreferences("accountData", Context.MODE_PRIVATE)
        val onboardingComplete = sharedPreferences.getBoolean("onboarding_complete", false)
        setContent {
            LaunchlyTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (onboardingComplete) "main" else "onboarding"
                ) {
                    composable("onboarding") {
                        if (!onboardingComplete) {
                            val removes = mutableListOf<String>()
                            if (!packageManager.canRequestPackageInstalls()) {
                                removes.add("Permissions")
                            }
                            if (!accountPreferences.contains("accountName")) {
                                removes.add("SignIn")
                            }
                            if (sharedPreferences.getBoolean("first_run", true)) {
                                removes.add("Start")
                            }
                            Onboarding(
                                onFinish = {
                                    sharedPreferences.edit().putBoolean("onboarding_complete", true)
                                        .apply()
                                    sharedPreferences.edit().putBoolean("first_run", false).apply()
                                    navController.navigate("main") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                },
                                removes = removes,
                            )
                        } else {
                            navController.navigate("main") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }
                    composable("main") {
                        Main()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val context = LocalContext.current
    val versionDbHelper = VersionDatabaseHelper(context)
    val versionDbViewModel: VersionDatabaseViewModel = viewModel()
    LaunchedEffect(Unit) {
        versionDbViewModel.loadSavedVersions(context)
    }

    val downloadedVersions = versionDbViewModel.downloadedVersions
    val showVersionSelector = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    val showOptions = remember { mutableStateOf(false) }

    val versionsViewModels = remember { Build.SUPPORTED_ABIS.map { VersionsViewModel(it) } }
    var userProfileInfo by remember { mutableStateOf(listOf<String?>()) }
    var savedVersionData by remember { mutableStateOf<SavedVersionData?>(null) }
    var optionMode by remember { mutableStateOf(OptionMode.ADD) }

    LaunchedEffect(Unit) {
        userProfileInfo = GPlayAPI.getUserProfileInfo(context)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                { Text("Add Version") },
                { Icon(Icons.Default.Add, "Add") },
                { showVersionSelector.value = true }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Launchly") },
                actions = {
                    IconButton(onClick = {
                        showSettings.value = true
                    }) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        }
    ) { innerPadding ->
        var isRefreshing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        DownloadedVersions(
            items = downloadedVersions,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                versionDbViewModel.loadSavedVersions(context)
                coroutineScope.launch {
                    delay(500)
                    isRefreshing = false
                }
            },
            onEditVersion = { installationId ->
                savedVersionData = downloadedVersions.find { it.installationId == installationId }
                optionMode = OptionMode.EDIT
                showOptions.value = true
            },
            onRemoveVersion = {
                versionDbHelper.removeVersion(it)
                val fileToDelete = File(context.filesDir, "versions/$it/")
                if (fileToDelete.exists()) fileToDelete.deleteRecursively()
                downloadedVersions.remove(downloadedVersions.find { version -> version.installationId == it })
            },
            modifier = Modifier.padding(innerPadding)
        )

        VersionSelector(
            versionsViewModels = versionsViewModels,
            showDialog = showVersionSelector.value,
            onConfirmation = { version ->
                showVersionSelector.value = false
                if (version != null) {
                    savedVersionData = SavedVersionData(
                        UUID.randomUUID().toString(),
                        "Minecraft ${version.name}",
                        version.code.toString(),
                        version.name,
                        version.type,
                        version.architecture,
                        Status.NOT_DOWNLOADED,
                        listOf(),
                        false,
                        System.currentTimeMillis()
                    )
                    optionMode = OptionMode.ADD
                    showOptions.value = true
                }
            }
        )
        if (savedVersionData != null) {
            VersionOptions(
                showDialog = showOptions.value,
                savedVersionData = savedVersionData!!,
                onConfirmation = { version ->
                    showOptions.value = false
                    if (version != null) {
                        if (optionMode == OptionMode.ADD) {
                            versionDbHelper.addVersion(version)
                            downloadedVersions.add(version)
                        } else {
                            updateInfo(context, version, versionDbViewModel)
                        }
                    }
                }
            )
        }

        SettingsDialog(
            showDialog = showSettings.value,
            userProfileInfo = userProfileInfo,
            onDismissRequest = { showSettings.value = false }
        )
    }
}

private fun loadExternalApps(
    context: Context
): List<Pair<SavedVersionData, String>> {
    val packageManager = context.packageManager
    val externalApps = mutableListOf<Pair<SavedVersionData, String>>()
    val versionsViewModels = Build.SUPPORTED_ABIS.map { VersionsViewModel(it) }
    val appPackages = listOf(
        "com.mojang.minecraftpe" to VersionType.UNKNOWN,
        "net.atlasclient.atlaspe" to VersionType.RELEASE,
        "net.atlasclient.atlaspe.premium" to VersionType.PREMIUM
    )

    for ((packageName, versionType) in appPackages) {
        try {
            val packageInfo = getPackageInfo(context, packageName)
            if (getPackageInstallSource(context, packageName) == context.packageName) continue
            val versionCode = packageInfo.let { PackageInfoCompat.getLongVersionCode(it) }
            val packageAppName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
            saveIconToFile(packageInfo.applicationInfo?.loadIcon(packageManager)!!, File("${context.filesDir}/versions/$packageAppName/default_icon.png"))
            val appData = SavedVersionData(
                name = packageAppName,
                installationId = packageInfo.applicationInfo?.loadLabel(packageManager).toString(),
                versionCode = versionCode.toString(),
                versionName = packageInfo.versionName ?: "Unknown",
                versionType = versionType.takeIf { it != VersionType.UNKNOWN }
                    ?: getVersionTypeByCode(versionCode, versionsViewModels),
                architecture = getPackageArch(packageInfo),
                status = Status.INSTALLED,
                patches = emptyList(),
                customIcon = false,
                dateModified = System.currentTimeMillis()
            )
            externalApps.add(Pair(appData, packageName))
        } catch (_: Exception) { }
    }
    return externalApps
}

private val client = OkHttpClient()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedVersions(
    items: SnapshotStateList<SavedVersionData>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onEditVersion: (String) -> Unit,
    onRemoveVersion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val versionDbViewModel: VersionDatabaseViewModel = viewModel()
    val context = LocalContext.current
    val externalApps = remember { mutableStateListOf<Pair<SavedVersionData, String>>() }
    val isUpdating = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        externalApps.addAll(loadExternalApps(context))
        try { getPackageInfo(context, "com.mojang.minecraftpe") } catch (_: Exception) {
            items.forEach {
                if (it.status == Status.INSTALLED) {
                    updateInfo(context, it.copy(status = Status.NOT_INSTALLED))
                    versionDbViewModel.loadSavedVersions(context)
                }
            }
        }
    }

    val packageStateReceiver = remember {
        PackageStatusReceiver { packageName, status ->
            externalApps.clear()
            externalApps.addAll(loadExternalApps(context))
            if (packageName == "com.mojang.minecraftpe" && status == "removed" && !isUpdating.value) {
                items.forEach {
                    if (it.status == Status.INSTALLED) {
                        updateInfo(context, it.copy(status = Status.NOT_INSTALLED))
                    }
                }
                versionDbViewModel.loadSavedVersions(context)
            }
        }
    }

    DisposableEffect (Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }
        context.registerReceiver(packageStateReceiver, filter)
        onDispose {
            context.unregisterReceiver(packageStateReceiver)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            if (externalApps.isNotEmpty()) {
                item {
                    Category("Not managed by Launchy")
                }
            }
            items(externalApps) { app ->
                VersionItem(
                    app.first,
                    onRemoveVersion = {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = "package:${app.second}".toUri()
                        context.startActivity(intent)
                    },
                    isLaunchly = false,
                    packageName = app.second
                )
            }
            if (items.isNotEmpty()) {
                item {
                    Category("Managed by Launchy")
                }
            }
            items(items.reversed(), key = { it.installationId }) {
                VersionItem(it, onEditVersion, onRemoveVersion, isUpdating = isUpdating) { status ->
                    if (status == Status.INSTALLED) {
                        items.forEach { version ->
                            if (version.installationId != it.installationId && version.status == Status.INSTALLED) {
                                updateInfo(context, version.copy(status = Status.NOT_INSTALLED))
                            }
                        }
                    }
                    updateInfo(
                        context,
                        it.copy(status = status),
                        versionDbViewModel
                    )
                    versionDbViewModel.loadSavedVersions(context)
                }
            }
        }
    }
}

private fun updateInfo(
    context: Context,
    savedVersionData: SavedVersionData,
    versionDbViewModel: VersionDatabaseViewModel = VersionDatabaseViewModel(),
    downloadedVersions: SnapshotStateList<SavedVersionData> = versionDbViewModel.downloadedVersions
) {
    VersionDatabaseHelper(context).editVersion(savedVersionData)
    val index = versionDbViewModel.getIndexById(savedVersionData.installationId)
    if (index != -1) {
        downloadedVersions[index] = savedVersionData
    }
}

@Composable
fun VersionItem(
    savedVersionData: SavedVersionData,
    onEditVersion: (String) -> Unit = {},
    onRemoveVersion: (String) -> Unit = {},
    isLaunchly: Boolean = true,
    packageName: String = "com.mojang.minecraftpe",
    isUpdating: MutableState<Boolean> = mutableStateOf(false),
    updateStatus: (Status) -> Unit = {}
    ) {
    val context = LocalContext.current
    val downloader = remember(savedVersionData.installationId) { Downloader(client) }
    val scope = rememberCoroutineScope()
    val isRemoving = remember { mutableStateOf(false) }
    val isPaused = remember { mutableStateOf(false) }
    val downloadProgress = remember { mutableFloatStateOf(0f) }
    val downloadSpeed = remember { mutableFloatStateOf(0f) }
    val downloadEta = remember { mutableFloatStateOf(0f) }
    val installProgress = remember { mutableIntStateOf(-1) }
    val isLoading = remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isRemoving.value) 0f else 1f,
        label = "alpha",
        finishedListener = {
            if (isRemoving.value) {
                isRemoving.value = false
                onRemoveVersion(savedVersionData.installationId)
            }
        }
    )

    VersionItemLayout(
        savedVersionData = savedVersionData,
        isLaunchly = isLaunchly,
        animatedAlpha = animatedAlpha,
        onEditVersion = onEditVersion,
        onRemoveVersion = onRemoveVersion,
        isRemoving = isRemoving.value,
        isPaused = isPaused.value,
        downloadProgress = downloadProgress.floatValue,
        downloadSpeed = downloadSpeed.floatValue,
        downloadEta = downloadEta.floatValue,
        installProgress = installProgress.intValue,
        isLoading = isLoading.value,
        packageName = packageName,
        onVersionAction = { action ->
            scope.launch {
                withContext(Dispatchers.Main) {
                    handleVersionAction(
                        action,
                        savedVersionData,
                        context,
                        downloader,
                        downloadProgress,
                        downloadSpeed,
                        downloadEta,
                        isPaused,
                        installProgress,
                        isRemoving,
                        isLoading,
                        packageName,
                        isUpdating,
                        scope,
                        updateStatus
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun VersionItemLayout(
    savedVersionData: SavedVersionData,
    isLaunchly: Boolean,
    animatedAlpha: Float,
    onEditVersion: (String) -> Unit,
    onRemoveVersion: (String) -> Unit,
    isRemoving: Boolean,
    isPaused: Boolean,
    downloadProgress: Float,
    downloadSpeed: Float,
    downloadEta: Float,
    isLoading: Boolean,
    installProgress: Int,
    packageName: String,
    onVersionAction: (VersionAction) -> Unit
) {
    val layoutMaxWidth = remember { mutableStateOf(0.dp) }
    BoxWithConstraints { layoutMaxWidth.value = maxWidth }
    val context = LocalContext.current
    val maxWidth = if (isLaunchly) 300 else 150
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val scope = rememberCoroutineScope()
    val height = 120.dp
    val isInstalled = savedVersionData.status == Status.INSTALLED
    val isDownloading = savedVersionData.status == Status.DOWNLOADING
    val progress = if (isDownloading) downloadProgress / 100f else if (installProgress > -1) installProgress / 100f else 1f
    val density : Density = LocalDensity.current
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height * if (isRemoving) animatedAlpha else 1f)
            .alpha(if (isRemoving) animatedAlpha else 1f)
            .swipeable(
                state = swipeableState,
                anchors = mapOf(0f to 0, -maxWidth.toFloat() to 1),
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .padding(8.dp)
    ) {
        println(swipeableState.offset.value)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width( with(density) { maxWidth.toDp() - 10.dp} )
                .align(Alignment.CenterEnd)
                .offset {
                    val offsetX = (swipeableState.offset.value + maxWidth) * 1.5
                    IntOffset(offsetX.roundToInt(), 0)
                }
                .clip(MaterialTheme.shapes.medium),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val actionModifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            if (isLaunchly) {
                ActionButton(actionModifier, Icons.Default.Edit, Color.Gray,{
                    scope.launch {
                        swipeableState.animateTo(0)
                        onEditVersion(savedVersionData.installationId)
                    }
                })

            }
            ActionButton(actionModifier, if (isInstalled) Icons.Default.Delete else Icons.Default.Close, Color.Red, {
                scope.launch {
                    swipeableState.animateTo(0)
                    if (isLaunchly) {
                        onVersionAction(VersionAction.Remove)
                    } else {
                        onRemoveVersion(savedVersionData.installationId)
                    }
                }
            })
        }

        ProgressBox(
            Modifier
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            MaterialTheme.colorScheme.surfaceContainerHigh,
            animatedProgress.value
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconFile = File("${context.filesDir}/versions/${savedVersionData.installationId}/${
                    if (savedVersionData.customIcon) "custom_icon" else "default_icon"
                }.png")

                Image(
                    bitmap = if (iconFile.exists()) BitmapFactory.decodeFile(iconFile.absolutePath)
                        .asImageBitmap() else BitmapFactory.decodeStream(context.assets.open("default_icon.png"))
                        .asImageBitmap(),
                    contentDescription = "Version Icon",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Gray)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Text(savedVersionData.name, style = MaterialTheme.typography.titleLarge)
                    Info("Version: ${savedVersionData.versionName}, Code: ${savedVersionData.versionCode}")
                    Info("Type: ${savedVersionData.versionType}, Architecture: ${savedVersionData.architecture}")
                    if (savedVersionData.patches.isNotEmpty()) Info("Patches: ${savedVersionData.patches.joinToString { it.patch }}")
                    if (isDownloading) {
                        Info(
                            "Downloading... " +
                            if (isPaused) {
                                "(Paused)"
                            } else {
                                "${f(downloadProgress)}% at ${f(downloadSpeed)} MB/s, ETA: ${f(downloadEta)}s"
                            }
                        )
                    }
                    if (installProgress > -1) Info("Installing... $installProgress%")
                }

                GetActionButton(
                    savedVersionData,
                    Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .aspectRatio(1f),
                    isLoading,
                    scope,
                    swipeableState,
                    context,
                    packageName,
                    onVersionAction,
                    isPaused
                )
            }
        }
    }
}


@Composable
@OptIn(ExperimentalWearMaterialApi::class)
private fun GetActionButton(
    savedVersionData: SavedVersionData,
    buttonModifier: Modifier,
    isLoading: Boolean,
    scope: CoroutineScope,
    swipeableState: SwipeableState<Int>,
    context: Context,
    packageName: String,
    onVersionAction: (VersionAction) -> Unit,
    isPaused: Boolean
) {
    when (savedVersionData.status) {
        Status.INSTALLED -> {
            ActionButton(
                icon = Icons.Default.PlayArrow,
                backgroundColor = MaterialTheme.colorScheme.primary,
                modifier = buttonModifier,
                loading = isLoading,
                onClick = {
                    scope.launch { swipeableState.animateTo(0) }
                    context.startActivity(
                        context.packageManager.getLaunchIntentForPackage(
                            packageName
                        )
                    )
                }
            )
        }

        Status.NOT_INSTALLED -> {
            ActionButton(
                icon = Icons.Default.InstallMobile,
                backgroundColor = MaterialTheme.colorScheme.primary,
                modifier = buttonModifier,
                loading = isLoading,
                onClick = {
                    scope.launch { swipeableState.animateTo(0) }
                    onVersionAction(VersionAction.Install)
                }
            )
        }

        Status.NOT_DOWNLOADED -> {
            ActionButton(
                icon = Icons.Default.Download,
                backgroundColor = MaterialTheme.colorScheme.primary,
                modifier = buttonModifier,
                loading = isLoading,
                onClick = {
                    scope.launch {
                        swipeableState.animateTo(0)
                        onVersionAction(VersionAction.StartDownload)
                    }
                }
            )
        }

        Status.DOWNLOADING -> {
            val expanded = remember { mutableStateOf(false) }
            ActionButton(
                icon = Icons.Default.MoreHoriz,
                backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                modifier = buttonModifier,
                loading = isLoading,
                onClick = {
                    scope.launch {
                        swipeableState.animateTo(0)
                        expanded.value = true
                    }
                }
            ) {
                DropDown(
                    menu = listOf(
                        if (isPaused)
                            "Resume" to { onVersionAction(VersionAction.ResumeDownload) }
                        else
                            "Pause" to { onVersionAction(VersionAction.PauseDownload) },
                        "Cancel" to { onVersionAction(VersionAction.CancelDownload) }
                    ),
                    expanded = expanded
                )
            }
        }
    }
}

suspend fun handleVersionAction(
    action: VersionAction,
    savedVersionData: SavedVersionData,
    context: Context,
    downloader: Downloader,
    downloadProgress: MutableFloatState,
    downloadSpeed: MutableFloatState,
    downloadEta: MutableFloatState,
    isPaused: MutableState<Boolean>,
    installProgress: MutableIntState,
    isRemoving: MutableState<Boolean>,
    isLoading: MutableState<Boolean>,
    packageName: String,
    isUpdating: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    updateStatus: (Status) -> Unit
) {
    when (action) {
        is VersionAction.StartDownload -> {
            updateStatus(Status.DOWNLOADING)
            var totalSize = 0L
            val urlToFileNameMap = getApks(savedVersionData.versionCode.toInt(), context).associate { apk ->
                totalSize += apk.size
                apk.name to apk.url
            }
            downloader.startDownload(
                urlToFileNameMap,
                File("${context.filesDir}/versions/${savedVersionData.installationId}")
            ) { status ->
                when (status) {
                    is Downloader.DownloadStatus.OnProgress -> {
                        downloadProgress.floatValue = (status.downloadedSize * 100f) / totalSize
                        downloadSpeed.floatValue = status.speed
                        downloadEta.floatValue = ((totalSize - status.downloadedSize) / 1024 / 1024) / status.speed
                    }

                    is Downloader.DownloadStatus.OnFinish -> {
                        println("Download completed. Files: ${status.files}")
                        updateStatus(Status.NOT_INSTALLED)
                    }

                    is Downloader.DownloadStatus.OnFailure -> {
                        println("Failed to download ${status.fileName}: ${status.failureReason}")
                        updateStatus(Status.NOT_DOWNLOADED)
                    }

                    is Downloader.DownloadStatus.OnCancel -> {
                        println("Download cancelled for file: ${status.fileName}")
                        updateStatus(Status.NOT_DOWNLOADED)
                    }

                    is Downloader.DownloadStatus.OnDownloadSuccess -> {
                        println("Successfully downloaded: ${status.fileName}")
                    }
                }
            }
        }
        is VersionAction.CancelDownload -> {
            downloader.cancelAll()
            isPaused.value = false
        }
        is VersionAction.PauseDownload -> {
            downloader.pauseAll()
            isPaused.value = true
        }
        is VersionAction.ResumeDownload -> {
            downloader.resumeAll()
            isPaused.value = false
        }
        is VersionAction.Install -> {
            println("Installing ${savedVersionData.name}")
            isLoading.value = true
            val apks = File("${context.filesDir}/versions/${savedVersionData.installationId}/").listFiles { file -> file.isFile && file.name.endsWith(".apk") }
            if (apks != null && apks.isNotEmpty()) {
                val onResult: (SessionResult) -> Unit = {
                    when (it) {
                        is SessionResult.OnProgress -> {
                            installProgress.intValue = it.progress
                        }
                        is SessionResult.OnSuccess -> {
                            val iconFile = File("${context.filesDir}/versions/${savedVersionData.installationId}/${
                                if (savedVersionData.customIcon) "custom_icon" else "default_icon"
                            }.png")
                            installProgress.intValue = -1
                            if (!savedVersionData.customIcon) {
                                saveIconToFile(
                                    context.packageManager.getApplicationIcon(packageName),
                                    iconFile
                                )
                            }
                            isLoading.value = false
                            updateStatus(Status.INSTALLED)

                            coroutineScope.launch {
                                delay(500)
                                isUpdating.value = false
                            }
                        }
                        is SessionResult.OnFailure -> {
                            installProgress.intValue = -1
                            isLoading.value = false
                            isUpdating.value = false
                            println("Failed to install: ${it.message}")
                        }
                        is SessionResult.OnCancel -> {
                            installProgress.intValue = -1
                            isLoading.value = false
                            isUpdating.value = false
                            println("Installation cancelled")
                        }
                    }
                }
                try {
                    val versionCode = getPackageInfo(context, packageName).let { PackageInfoCompat.getLongVersionCode(it) }
                    if (versionCode > savedVersionData.versionCode.toLong()) {
                        uninstallApp(context, packageName) { status ->
                            if (status == SessionResult.OnSuccess) {
                                installPackage(context, apks, onResult)
                            } else {
                                isLoading.value = false
                            }
                        }
                    } else {
                        isUpdating.value = true
                        installPackage(context, apks, onResult)
                    }
                } catch (e: Exception) { installPackage(context, apks, onResult) }
            }
        }
        is VersionAction.Remove -> {
            if (savedVersionData.status == Status.INSTALLED) {
                println("Uninstalling $packageName")
                isLoading.value = true
                uninstallApp(context, packageName) { status ->
                    when (status) {
                        is SessionResult.OnSuccess -> {
                            println("Successfully uninstalled $packageName")
                            isLoading.value = false
                            updateStatus(Status.NOT_INSTALLED)
                        }
                        is SessionResult.OnFailure -> {
                            println("Failed to uninstall $packageName: ${status.message}")
                            isLoading.value = false
                        }
                        else -> { }
                    }
                }
            } else {
                println("Removing ${savedVersionData.name}")
                isRemoving.value = true
            }
        }
    }
}

sealed class VersionAction {
    data object StartDownload : VersionAction()
    data object CancelDownload : VersionAction()
    data object PauseDownload : VersionAction()
    data object ResumeDownload : VersionAction()
    data object Install : VersionAction()
    data object Remove : VersionAction()
}