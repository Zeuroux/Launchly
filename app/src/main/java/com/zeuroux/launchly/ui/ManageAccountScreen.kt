package com.zeuroux.launchly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zeuroux.launchly.R
import com.zeuroux.launchly.globals.ConfirmationDialogData
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.globals.Links.DISCLAIMER
import com.zeuroux.launchly.globals.Links.LICENSE
import com.zeuroux.launchly.globals.Links.TERMS

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageAccountScreen() {
    val showScreen = GlobalData.showManageAccountScreen
    BaseScreen(showScreen.value, { showScreen.value = false }, topBar = {
        ScreenTopBar("Manage Account") { showScreen.value = false }
    }) {
        Surface(Modifier.padding(it)) {
            FlowRow(modifier = Modifier.padding(8.dp)) {
                listOf("Terms of Service" to TERMS, "Disclaimer" to DISCLAIMER, "License" to LICENSE).forEach { (text, link) ->
                    LinkButton(text, link, ButtonDefaults.textButtonColors(MaterialTheme.colorScheme.surfaceContainer), Modifier.padding(end = 8.dp))
                }
            }
            LinearAuto(
                Modifier.fillMaxSize(),
                Arrangement.SpaceEvenly,
                Arrangement.SpaceEvenly,
                Alignment.CenterHorizontally,
                Alignment.CenterVertically
            ) {
                AccountDisplay()
                LogOutDisplay()
            }
        }
    }
}


@Composable
fun AccountDisplay(){
    val authData = GlobalData.authData.value
    Box {
        Column(
            Modifier.align(Alignment.Center),
            Arrangement.spacedBy(10.dp),
            Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(authData?.userProfile?.artwork?.url ?: R.drawable.ic_google)
                    .crossfade(true)
                    .build(),
                contentDescription = "Account Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredSize(100.dp)
                    .clip(CircleShape)
            )
            Text(
                text = authData?.userProfile?.name?: "Sign In to download versions",
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = authData?.userProfile?.email ?: "Sign In to download versions",
                fontWeight = FontWeight.Light,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LogOutDisplay() {
    val context = LocalContext.current
    Box {
        Column(
            Modifier.align(Alignment.Center),
            Arrangement.spacedBy(10.dp),
            Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log out of Launchly",
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(
                onClick = {
                    GlobalData.confirmationDialogData.value = ConfirmationDialogData(
                        title = "Log out?",
                        message = "Are you sure you want to log out?",
                        confirmText = "Log out",
                        cancelText = "Cancel",
                        onConfirm = {
                            GlobalData.removeAuthData(context)
                            GlobalData.showManageAccountScreen.value = false
                        }
                    )
                    GlobalData.showConfirmationDialog.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Logout,
                    contentDescription = "Log out"
                )
                Spacer(Modifier.width(8.dp))
                Text("Log out")
            }
        }
    }
}