package com.zeuroux.launchly.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zeuroux.launchly.R
import com.zeuroux.launchly.extensions.browse
import com.zeuroux.launchly.extensions.shimmerLoadingAnimation
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.globals.Links.POLICY
import com.zeuroux.launchly.globals.Links.README
import com.zeuroux.launchly.globals.Links.TERMS

data class Option(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun MoreDialog() {
    val context = LocalContext.current
    val showMoreDialog = GlobalData.showMoreDialog
    BaseDialog(showMoreDialog, { showMoreDialog.value = false }) {
        Surface(Modifier.clip(MaterialTheme.shapes.extraLarge)) {
            Column(
                Modifier
                    .width(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(rememberScrollState()),
                Arrangement.spacedBy(4.dp),
                Alignment.CenterHorizontally
            ) {
                DialogTopBar(stringResource(R.string.app_name)) { showMoreDialog.value = false }
                AccountHeader()
                Column {
                    listOf(
                        Option("Settings", Icons.Default.Settings) { GlobalData.showSettingsScreen.value = !GlobalData.showSettingsScreen.value },
                        Option("About", Icons.Default.Info) { context.browse(README) }
                    ).forEach { option ->
                        SettingOption(option)
                    }
                }
                Footer()
            }
        }
    }
}

@Composable
private fun AccountHeader() {
    val loading = GlobalData.isAuthDataLoading.value
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val authData = GlobalData.authData.value
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.Start)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(authData?.userProfile?.artwork?.url ?: R.drawable.ic_google)
                    .crossfade(true)
                    .build(),
                contentDescription = "Account Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredSize(36.dp)
                    .clip(CircleShape)
                    .shimmerLoadingAnimation(loading)
            )
            Column {
                val name = authData?.userProfile?.name ?: "Sign In to download versions"
                val email = authData?.userProfile?.email ?: "Sign In to download versions"
                repeat(2) {
                    Text(
                        text = if (it == 0) name else email,
                        modifier = Modifier
                            .padding(1.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .shimmerLoadingAnimation(loading),
                        fontWeight = if (it == 0) FontWeight.Normal else FontWeight.Light,
                        fontSize = if (it == 0) 15.sp else 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        OutlinedButton(
            onClick = {
                if (!loading) {
                    if (authData != null) {
                        GlobalData.showManageAccountScreen.value = true
                    } else {
                        GlobalData.showLoginScreen.value = true
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (loading) {
                        it
                            .padding(vertical = 4.dp)
                            .height(ButtonDefaults.MinHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerLoadingAnimation()
                    } else it
                }
        ) { Text(if (authData != null) "Manage Account" else "Sign In") }
    }
}

@Composable
private fun Footer() {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally), Alignment.CenterVertically) {
        LinkButton("Privacy Policy", POLICY)
        Text("•")
        LinkButton("Terms of Service", TERMS)
    }
}