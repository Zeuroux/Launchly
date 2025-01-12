package com.zeuroux.launchly.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageView
import com.zeuroux.launchly.utils.ActionButton
import com.zeuroux.launchly.utils.CropImageViewCompose
import com.zeuroux.launchly.utils.scaleBitmap
import com.zeuroux.launchly.utils.squareBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropper(
    showDialog: Boolean,
    uri: Uri,
    onConfirmation: (version: Bitmap?) -> Unit
) {
    val context = LocalContext.current
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }
    var croppedImageView by remember { mutableStateOf<CropImageView?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        croppedImageView = null
        bitmap = squareBitmap(
            scaleBitmap(
                BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri)
                ), 1000
            )
        )
    }

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

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Crop Icon",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        ActionButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                                .aspectRatio(1f),
                            icon = Icons.Default.Rotate90DegreesCw,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                croppedImageView?.rotateImage(90)
                            }
                        )
                    }
                    CropImageViewCompose(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    ) {
                        setImageBitmap(bitmap!!)
                        setAspectRatio(1, 1)
                        setFixedAspectRatio(true)
                        croppedImageView = this
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                onConfirmation(null)
                                animateIn = false
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                onConfirmation(croppedImageView?.getCroppedImage())
                                animateIn = false
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                        ) {
                            Text("Save")
                        }
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