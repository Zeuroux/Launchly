package com.zeuroux.launchly.ui

import android.view.ViewGroup.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageView
import com.zeuroux.launchly.extensions.browse

val animationEnter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(
    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow), .8f
)
val animationExit = scaleOut(
    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow), .8f
) + fadeOut(spring(stiffness = Spring.StiffnessMedium))

@Composable
fun BaseDialog(showDialog: MutableState<Boolean>, onDismissRequest: () -> Unit = {}, content: @Composable () -> Unit) {
    var animateDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showDialog.value) { if (showDialog.value) animateDialog = true }
    if (!animateDialog) return
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var animateIn by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { animateIn = true }
        Box(Modifier.pointerInput(Unit) { detectTapGestures { onDismissRequest() } }.fillMaxSize().background(
            Color.Black.copy(alpha = .56f))  )
        AnimatedComposable(animateIn && showDialog.value, { animateDialog = false }){
            content()
        }
        BackHandler {
            onDismissRequest()
        }
    }
}

@Composable
fun BaseScreen(
    showScreen: Boolean,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition. End,
    containerColor: Color = MaterialTheme. colorScheme. background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults. contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val animateDialog = remember { mutableStateOf(false) }
    LaunchedEffect(showScreen) { if (showScreen) animateDialog.value = true }
    if (!animateDialog.value) return
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }
    AnimatedComposable(animateIn && showScreen, { animateDialog.value = false }) {
        Scaffold(modifier, topBar, bottomBar, snackbarHost, floatingActionButton, floatingActionButtonPosition, containerColor, contentColor, contentWindowInsets, content)
    }
    BackHandler {
        onDismissRequest()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(showBottomSheet: Boolean, onDismissRequest: () -> Unit = { }, skipExpanded: Boolean = false, content: @Composable () -> Unit) {
    if (!showBottomSheet) return
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = rememberModalBottomSheetState(skipExpanded),
    ) {
        content()
    }
}

@Composable
fun LinearAuto(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val isPortrait = maxWidth < maxHeight
        if (isPortrait) {
            Column(
                modifier = modifier,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment
            ) {
                content()
            }
        } else {
            Row(
                modifier = modifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                content()
            }
        }
    }
}

@Composable
fun LinkButton(text: String, link: String, colors: ButtonColors = ButtonDefaults.textButtonColors(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TextButton({ context.browse(link) }, modifier, colors = colors) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SettingOption(option: Option) {
    Row(Modifier.fillMaxWidth().height(50.dp).clickable { option.onClick() }.padding(horizontal = 24.dp), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
        Icon(option.icon, option.title, Modifier.size(24.dp))
        Text(option.title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(title: String, actions: @Composable() (RowScope.() -> Unit) = {}, ignoreSystemBars: Boolean = false, onDismissRequest: () -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onDismissRequest) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
        },
        actions = actions,
        windowInsets = TopAppBarDefaults.windowInsets.let{
            if (ignoreSystemBars) it.exclude(WindowInsets.systemBars) else it
        }
    )
}

@Composable
private fun AnimatedComposable(visible: Boolean, onDismissRequest: () -> Unit = {}, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = animationEnter,
        exit = animationExit
    ) {
        content()
        DisposableEffect(Unit) { onDispose { onDismissRequest() } }
    }
}

@Composable
fun CropImageViewCompose(
    modifier: Modifier = Modifier,
    configureCropImageView: CropImageView.() -> Unit
) {
    Box (
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                CropImageView(context).apply {
                    configureCropImageView()
                }
            },
            update = {
                it.layoutParams.width = LayoutParams.MATCH_PARENT
                it.layoutParams.height = LayoutParams.MATCH_PARENT
                configureCropImageView(it)
            }
        )
    }
}

@Composable
fun DialogTopBar(title: String, extras: @Composable BoxScope.() -> Unit = {}, onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        IconButton({ onClose() }) {
            Icon(Icons.Default.Close,"Close", Modifier.size(24.dp))
        }
        Text(
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        extras(this)
    }
}

@Composable
fun ProgressBox(progress: Float, modifier: Modifier, progressColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh, content: @Composable () -> Unit) {
    Box(modifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(progressColor)
        )
        content()
    }
}