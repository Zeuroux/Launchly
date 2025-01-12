package com.zeuroux.launchly.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(showDialog: Boolean, title: String, label: String, initialValue: String, filters: Regex, keyboardOptions: KeyboardOptions = KeyboardOptions.Default , onDismissRequest: (String) -> Unit) {
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("") }
    text.value = initialValue
    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            animateIn = false
            onDismissRequest(initialValue)
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
                Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 0.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(24.dp, 0.dp))
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = text.value,
                        onValueChange = {
                            if (it.matches(filters) || it.isEmpty()) {
                                text.value = it
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.padding(24.dp, 0.dp),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onDismissRequest(text.value)
                            }
                        )
                    )
                    TextButton(
                        onClick = {
                            if(text.value.matches(filters))
                                onDismissRequest(text.value)
                            animateIn = false
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Done")
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