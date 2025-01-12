package com.zeuroux.launchly.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(showDialog: Boolean, userProfileInfo: List<String?>, onDismissRequest: () -> Unit) {
    var animateIn by remember { mutableStateOf(false) }
    var showAnimatedDialog by remember { mutableStateOf(false) }
    var showAppSettings by remember { mutableStateOf(false) }
    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (!showAnimatedDialog) return
    BasicAlertDialog(
        onDismissRequest = {
            animateIn = false
            onDismissRequest()
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
                Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp)) {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.shapes.extraLarge
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(19.dp)
                    ) {
                        if (userProfileInfo.isNotEmpty() && userProfileInfo.size ==3) {
                            Row(modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    model = userProfileInfo[2] ?: "",
                                    contentDescription = "User Profile"
                                )
                                Column {
                                    Text(text = userProfileInfo[0] ?: "", style = MaterialTheme.typography.bodyLarge, overflow = TextOverflow.Ellipsis, maxLines = 1)
                                    Text(text = userProfileInfo[1] ?: "", style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis, maxLines = 1)
                                }
                            }
                            OutlinedButton (
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage your account")
                            }
                        }
                    }
                    Column(modifier= Modifier.padding(0.dp, 8.dp)) {
                        Setting_Option("Manage Installations", Icons.Default.AllInbox) {

                        }
                        Setting_Option("Show Logs", Icons.Default.Terminal) {

                        }
                        Setting_Option("Settings", Icons.Default.Settings) {
                            showAppSettings = true
                        }
                        Setting_Option("About", Icons.Default.Info) {

                        }
                    }
                    Text(
                        text = buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://github.com/Zeuroux/Launchly/blob/master/POLICY.md"
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            append(" • ")
                            withLink(
                                LinkAnnotation.Url(
                                    "https://github.com/Zeuroux/Launchly/blob/master/TERMS.md"
                                )
                            ) {
                                append("Terms of Service")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    showAnimatedDialog = false
                }
            }
        }
    }

    AppSettingsDialog(showAppSettings, onDismissRequest = {
        showAppSettings = false
    })

}
@Composable
fun Setting_Option(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                onClick()
            }.padding(12.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            title,
            Modifier.size(24.dp)
        )
        Text(title)
    }
    HorizontalDivider(Modifier.padding(16.dp, 8.dp))
}