package com.zeuroux.launchly.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(showDialog: Boolean, title: String, description:String, onConfirmation: () -> Unit, onDismiss: () -> Unit, confirmText : String) {
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            animateIn = false
            onDismiss()
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
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                animateIn = false
                                onDismiss()
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                onConfirmation()
                                animateIn = false
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                        ) {
                            Text(confirmText)
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