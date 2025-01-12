package com.zeuroux.launchly.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zeuroux.launchly.utils.Category
import com.zeuroux.launchly.utils.SavedVersionData
import com.zeuroux.launchly.utils.saveIconToFile
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionOptions(
    showDialog: Boolean,
    savedVersionData: SavedVersionData,
    onConfirmation: (version: SavedVersionData?) -> Unit
) {
    val context = LocalContext.current
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(savedVersionData.name) }
    var customIcon by remember { mutableStateOf(savedVersionData.customIcon) }
    var icon by remember { mutableStateOf<Bitmap?>(null) }
    val default = context.assets.open("default_icon.png").use {
        BitmapFactory.decodeStream(it)
    }.asImageBitmap()
    val showImageCropper = remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(savedVersionData) {
        name = savedVersionData.name
        customIcon = savedVersionData.customIcon
        icon = if (customIcon) BitmapFactory.decodeFile("${context.filesDir}/versions/${savedVersionData.installationId}/custom_icon.png") else null
        imageUri = null
    }
    if (imageUri != null) {
        ImageCropper(
            showDialog = showImageCropper.value,
            uri = imageUri!!,
            onConfirmation = {
                if (it != null) {
                    icon = it
                }
                showImageCropper.value = false
            }
        )
    }
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
                showImageCropper.value = true
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    )
    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            onConfirmation(null)
            animateIn = false
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
                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                    Text(
                        "Customize ${savedVersionData.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Column {
                        Category("General")
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            val iconModifier = Modifier
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.Gray)
                            Image(
                                bitmap = if (customIcon) icon?.asImageBitmap() ?: default else default,
                                contentDescription = "Version Icon",
                                modifier = iconModifier
                            )


                            TextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Display Name") },
                                modifier = Modifier
                                    .padding(8.dp, 0.dp, 0.dp)
                                    .fillMaxWidth()
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = customIcon,
                                onCheckedChange = {
                                    customIcon = it
                                },
                                modifier = Modifier.padding(8.dp)
                            )
                            if (customIcon) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        pickMediaLauncher.launch(
                                            PickVisualMediaRequest(
                                                PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                ) {
                                    Text("Edit Icon")
                                }
                            } else {
                                Text("Use Custom Icon", modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth())
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            val iconFile = File("${context.filesDir}/versions/${savedVersionData.installationId}/${
                                if (savedVersionData.customIcon) "custom_icon" else "default_icon"
                            }.png")
                            val bitmapDrawable: Drawable? = if (customIcon) {
                                BitmapDrawable(context.resources, icon)
                            } else {
                                try {
                                    context.packageManager.getApplicationIcon("com.mojang.minecraftpe")
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (bitmapDrawable != null) {
                                saveIconToFile(
                                    bitmapDrawable,
                                    iconFile
                                )
                            }

                            onConfirmation(
                                savedVersionData.copy(
                                    name = name,
                                    customIcon = customIcon,
                                    dateModified = System.currentTimeMillis()
                                )
                            )
                            animateIn = false
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Save")
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