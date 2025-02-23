package com.zeuroux.launchly.globals

import android.content.Context
import androidx.annotation.MainThread
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.AuthHelper.Token.AAS
import com.zeuroux.launchly.utils.getNativeDeviceProperties
import com.zeuroux.launchly.version.VersionDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class ConfirmationDialogData(
    val title: String = "Confirmation",
    val message: String = "Are you sure you want to continue?",
    val confirmText: String = "Confirm",
    val cancelText: String = "Cancel",
    val onConfirm: () -> Unit = {},
    val onCancel: () -> Unit = {}
)

object GlobalData {
    val showInstallationSetup = mutableStateOf(false)
    val authData: MutableState<AuthData?> = mutableStateOf(null)
    val isAuthDataLoading: MutableState<Boolean> = mutableStateOf(false)
    val showMoreDialog: MutableState<Boolean> = mutableStateOf(false)
    val showLoginScreen = mutableStateOf(false)
    val showManageAccountScreen = mutableStateOf(false)
    val showSettingsScreen = mutableStateOf(false)
    val showConfirmationDialog = mutableStateOf(false)
    val confirmationDialogData = mutableStateOf<ConfirmationDialogData?>(null)
    val showVersionChooser = mutableStateOf(false)

    private lateinit var okhttpClient: OkHttpClient
    private lateinit var versionDB: VersionDB

    @MainThread
    fun getVersionDB(context: Context): VersionDB {
        versionDB = if (::versionDB.isInitialized) versionDB else { VersionDB(getOkHttpClient(), context.dataDir) }
        return versionDB
    }

    @MainThread
    fun getOkHttpClient(): OkHttpClient {
        okhttpClient = if (::okhttpClient.isInitialized) okhttpClient else OkHttpClient()
        return okhttpClient
    }


    fun updateAuthData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            isAuthDataLoading.value = true
            val accountData = context.getSharedPreferences("account_data", Context.MODE_PRIVATE)
            val token = accountData.getString("token", null)
            val email = accountData.getString("email", null)

            authData.value = if (token != null && email != null) {
                AuthHelper.build(email, token, AAS, properties = getNativeDeviceProperties(context))
            } else null
            isAuthDataLoading.value = false
        }
    }

    fun removeAuthData(context: Context) {
        context.getSharedPreferences("account_data", Context.MODE_PRIVATE).edit().clear().apply()
        authData.value = null
    }
}