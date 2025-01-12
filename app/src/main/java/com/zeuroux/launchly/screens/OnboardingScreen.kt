package com.zeuroux.launchly.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeuroux.launchly.R
import com.zeuroux.launchly.activities.LoginActivity


data class OnBoardModel(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Onboarding(onFinish: () -> Unit = {}, removes: List<String> = emptyList()) {
    val pages = mutableListOf(
        OnBoardModel(
            id = "Start",
            title = stringResource(R.string.onboarding_title_1),
            description = stringResource(R.string.onboarding_description_1),
            icon = Icons.Default.SyncAlt
        ),
        OnBoardModel(
            id = "SignIn",
            title = stringResource(R.string.onboarding_title_2),
            description = stringResource(R.string.onboarding_description_2),
            icon = Icons.Default.AccountCircle
        ),
        OnBoardModel(
            id = "Permissions",
            title = stringResource(R.string.onboarding_title_3),
            description = stringResource(R.string.onboarding_description_3),
            icon = Icons.Default.Key
        ),
        OnBoardModel(
            id = "Finish",
            title = stringResource(R.string.onboarding_title_4),
            description = stringResource(R.string.onboarding_description_4),
            icon = Icons.Default.Check
        )
    )
    removes.forEach { id ->
        pages.removeIf { it.id == id }
    }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("accountData", Context.MODE_PRIVATE)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    var userScrollEnabled by remember {
        mutableStateOf(true)
    }
    val preventForwardAt = remember {
        mutableIntStateOf(-1)
    }
    val message = stringResource(R.string.onboarding_warning)
    LaunchedEffect(pagerState.currentPage) {
        if (preventForwardAt.intValue < pagerState.currentPage && preventForwardAt.intValue != -1) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            pagerState.scrollToPage(preventForwardAt.intValue)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            do {
                                val event: PointerEvent = awaitPointerEvent(
                                    pass = PointerEventPass.Initial
                                )
                                event.changes.forEach {
                                    val diffX = it.position.x - it.previousPosition.x
                                    userScrollEnabled =
                                        if (pagerState.currentPage == preventForwardAt.intValue) {
                                            !(diffX < 0)
                                        } else {
                                            true
                                        }
                                }

                            } while (event.changes.any { it.pressed })
                        }
                    },
                userScrollEnabled = userScrollEnabled
            ) { page ->
                OnBoardItem(pages[page]){
                    val key = pages[page].id
                    when (key) {
                        "Start" -> {
                            Column {
                                LinkButton(
                                    title = stringResource(R.string.onboarding_source_code),
                                    icon = Icons.Default.Code,
                                    description = stringResource(R.string.onboarding_source_code_description),
                                    link = "https://github.com/Zeuroux/Launchly/"
                                )
                                LinkButton(
                                    title = stringResource(R.string.onboarding_license),
                                    icon = ImageVector.vectorResource(R.drawable.ic_license),
                                    description = stringResource(R.string.onboarding_license_description),
                                    link = "https://github.com/Zeuroux/Launchly/blob/master/LICENSE"
                                )
                                LinkButton(
                                    title = stringResource(R.string.onboarding_privacy_policy),
                                    icon = Icons.Default.Policy,
                                    description = stringResource(R.string.onboarding_privacy_policy_description),
                                    link = "https://github.com/Zeuroux/Launchly/blob/master/POLICY.md"
                                )
                                LinkButton(
                                    title = stringResource(R.string.onboarding_disclaimer),
                                    icon = Icons.Default.Info,
                                    description = stringResource(R.string.onboarding_disclaimer_description),
                                    link = "https://github.com/Zeuroux/Launchly/blob/master/DISCLAIMER.md"
                                )
                            }
                        }
                        "SignIn" -> {
                            if (sharedPreferences.getString("accountName", null) == null) {
                                preventForwardAt.intValue = pages.indexOfFirst { it.id == "SignIn" }
                            }
                            GoogleSignInButton(context, preventForwardAt)
                        }
                        "Permissions" -> {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                preventForwardAt.intValue = pages.indexOfFirst { it.id == "Permissions" }
                            }
                            PermissionsButton(context, preventForwardAt)
                        }
                        "Finish" -> {
                            Button(
                                onClick = onFinish,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Finish")
                            }
                        }
                    }
                }
            }
            IndexIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                currentPosition = pagerState.currentPage,
                count = pages.size
            )
        }
    }
}

@Composable
fun LinkButton(
    title: String,
    description: String,
    icon: ImageVector,
    link: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
        )
        VerticalDivider(
            modifier = Modifier.height(30.dp).padding(horizontal = 8.dp),
        )
        Column {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center,
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@Composable
fun GoogleSignInButton(context: Context, preventForwardAt: MutableIntState) {
    val textStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center,
    )
    val sharedPreferences = context.getSharedPreferences("accountData", Context.MODE_PRIVATE)

    var accountName by remember {
        mutableStateOf(sharedPreferences.getString("accountName", null))
    }
    var accountEmail by remember {
        mutableStateOf(sharedPreferences.getString("accountEmail", null))
    }

    val resultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val newAccountName = data?.getStringExtra("accountName")
            val newAccountEmail = data?.getStringExtra("accountEmail")
            val accountToken = data?.getStringExtra("accountToken")

            accountName = newAccountName
            accountEmail = newAccountEmail

            with(sharedPreferences.edit()) {
                putString("accountName", newAccountName)
                putString("accountEmail", newAccountEmail)
                putString("accountToken", accountToken)
                apply()
            }
            preventForwardAt.intValue = -1
        }
    }

    Row(
        modifier = Modifier
            .clickable {
                resultLauncher.launch(Intent(context, LoginActivity::class.java))
            }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
        )
        if (accountName != null) {
            Column(Modifier.padding(end = 8.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_signed_in, accountName!!),
                    style = textStyle
                )
                Text(
                    text = accountEmail!!,
                    style = textStyle
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = stringResource(R.string.onboarding_sign_in),
                style = textStyle
            )
        }
    }
}

@Composable
fun PermissionsButton(context: Context, preventForwardAt: MutableIntState) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()){
            preventForwardAt.intValue = -1
        }
    }
    val canInstall = context.packageManager.canRequestPackageInstalls()
    if (!canInstall) {
        Row(
            modifier = Modifier
                .clickable {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    intent.data = Uri.parse("package:${context.packageName}")
                    permissionLauncher.launch(intent)
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = stringResource(R.string.onboarding_allow_permissions),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@Composable
fun OnBoardItem(page: OnBoardModel, extras: @Composable () -> Unit = {}) {
    ReactiveLinear {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier
                .height(350.dp)
                .width(350.dp)
                .padding(bottom = 20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title, style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            )
            Text(
                text = page.description,
                modifier = Modifier.padding(horizontal = 50.dp, vertical = 10.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            )
            extras()
        }
    }
}

@Composable
fun ReactiveLinear(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
    else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { content() }
    }


}



@Composable
fun IndexIndicator(
    modifier: Modifier = Modifier,
    currentPosition: Int,
    count: Int
) {
    Row(
        modifier = modifier
    ) {
        repeat(count) { index ->
            val isSelected = currentPosition == index

            val width by animateDpAsState(targetValue = if (isSelected) 18.dp else 8.dp,
                label = "Size"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.inversePrimary else Color.Transparent,
                label = "Color"
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .width(width)
                    .height(8.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}