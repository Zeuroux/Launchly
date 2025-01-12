@file:Suppress("unused")
package com.zeuroux.launchly.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.opengl.GLES10
import android.os.Build
import android.text.TextUtils
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.aurora.gplayapi.helpers.UserProfileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.Locale
import java.util.Properties
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

data class ApkData(
    val url: String,
    val name: String,
    val size: Long
): Serializable

object GPlayAPI {
    private fun getAuthData(context: Context): AuthData {
        val sharedPreferences = context.getSharedPreferences("accountData", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("accountEmail", null)
        val token = sharedPreferences.getString("accountToken", null)

        if (email == null || token == null) {
            throw IllegalArgumentException("Email or token not found in SharedPreferences.")
        }

        return AuthHelper.build(
            email = email,
            token = token,
            tokenType = AuthHelper.Token.AAS,
            properties = getNativeDeviceProperties(context)
        )
    }

    suspend fun getApks(versionCode: Int, context: Context): List<ApkData> {
        return withContext(Dispatchers.IO) {
            val authData = getAuthData(context)
            val files = PurchaseHelper(authData).purchase(
                "com.mojang.minecraftpe",
                versionCode,
                0
            )
            files.map { ApkData(it.url, it.name, it.size) }
        }
    }

    suspend fun getUserProfileInfo(context: Context): List<String?> {
        return withContext(Dispatchers.IO) {
            val authData = getAuthData(context)
            val userProfile = UserProfileHelper(authData).getUserProfile()
            listOf(userProfile?.name, userProfile?.email, userProfile?.artwork?.url)
        }
    }

    private fun getNativeDeviceProperties(context: Context, isExport: Boolean = false): Properties {
        val properties = Properties().apply {
            setProperty("UserReadableName", "${Build.MANUFACTURER} ${Build.MODEL}")
            setProperty("Build.HARDWARE", Build.HARDWARE)
            setProperty("Build.RADIO", if (Build.getRadioVersion() != null) Build.getRadioVersion() else "unknown")
            setProperty("Build.FINGERPRINT", Build.FINGERPRINT)
            setProperty("Build.BRAND", Build.BRAND)
            setProperty("Build.DEVICE", Build.DEVICE)
            setProperty("Build.VERSION.SDK_INT", "${Build.VERSION.SDK_INT}")
            setProperty("Build.VERSION.RELEASE", Build.VERSION.RELEASE)
            setProperty("Build.MODEL", Build.MODEL)
            setProperty("Build.MANUFACTURER", Build.MANUFACTURER)
            setProperty("Build.PRODUCT", Build.PRODUCT)
            setProperty("Build.ID", Build.ID)
            setProperty("Build.BOOTLOADER", Build.BOOTLOADER)
            val config = context.resources.configuration
            setProperty("TouchScreen", "${config.touchscreen}")
            setProperty("Keyboard", "${config.keyboard}")
            setProperty("Navigation", "${config.navigation}")
            setProperty("ScreenLayout", "${config.screenLayout and 15}")
            setProperty("HasHardKeyboard", "${config.keyboard == Configuration.KEYBOARD_QWERTY}")
            setProperty("HasFiveWayNavigation", "${config.navigation == Configuration.NAVIGATIONHIDDEN_YES}")
            val metrics = context.resources.displayMetrics
            setProperty("Screen.Density", "${metrics.densityDpi}")
            setProperty("Screen.Width", "${metrics.widthPixels}")
            setProperty("Screen.Height", "${metrics.heightPixels}")
            setProperty("Platforms", Build.SUPPORTED_ABIS.joinToString(separator = ","))
            setProperty("Features", context.packageManager.systemAvailableFeatures.mapNotNull { it.name }.joinToString(separator = ","))
            setProperty("Locales", context.assets.locales.mapNotNull { it.replace("-", "_") }.joinToString(separator = ","))
            setProperty("SharedLibraries", (context.packageManager.systemSharedLibraryNames?.toList() ?: emptyList()).joinToString(separator = ","))
            val activityManager = context.getSystemService<ActivityManager>()
            setProperty("GL.Version", activityManager!!.deviceConfigurationInfo.reqGlEsVersion.toString())
            setProperty("GL.Extensions", eglExtensions.joinToString(separator = ","))
            setProperty("Client", "android-google")
            val gsfVersionProvider = getGsfAndVendingInfo(context, isExport)
            setProperty("GSF.version", gsfVersionProvider["gsfVersionCode"].toString())
            setProperty("Vending.version", gsfVersionProvider["vendingVersionCode"].toString())
            setProperty("Vending.versionString", gsfVersionProvider["vendingVersionString"].toString())
            setProperty("Roaming", "mobile-notroaming")
            setProperty("TimeZone", "UTC-10")
            setProperty("CellOperator", "310")
            setProperty("SimOperator", "38")
            if (Build.MANUFACTURER.lowercase(Locale.getDefault()).contains("huawei") || Build.HARDWARE.lowercase(
                    Locale.getDefault()).contains("kirin") || Build.HARDWARE.lowercase(Locale.getDefault()).contains("hi3") && !isExport) {
                setProperty("Build.HARDWARE", "lynx")
                setProperty("Build.BOOTLOADER", "lynx-1.0-9716681")
                setProperty("Build.BRAND", "google")
                setProperty("Build.DEVICE", "lynx")
                setProperty("Build.MODEL", "Pixel 7a")
                setProperty("Build.MANUFACTURER", "Google")
                setProperty("Build.PRODUCT", "lynx")
                setProperty("Build.ID", "TQ2A.230505.002")
            }
        }
        return properties
    }

    @Throws(Exception::class)
    private fun getGsfAndVendingInfo(context: Context, isExport: Boolean = false): Map<String, Any> {
        val googleServicesPackageId = "com.google.android.gms"
        val googleVendingPackageId = "com.android.vending"
        val defaultGsfVersionCode = 203019037L
        val defaultVendingVersionCode = 82151710L
        val defaultVendingVersionString = "21.5.17-21 [0] [PR] 326734551"

        var gsfVersionCode = defaultGsfVersionCode
        var vendingVersionCode = defaultVendingVersionCode
        var vendingVersionString = defaultVendingVersionString

        if (isExport) {
            try {
                gsfVersionCode = getPackageInfo(context, googleServicesPackageId).let { PackageInfoCompat.getLongVersionCode(it) }
                getPackageInfo(context, googleVendingPackageId).let {
                    vendingVersionCode = PackageInfoCompat.getLongVersionCode(it)
                    vendingVersionString = it.versionName ?: vendingVersionString
                }
            } catch (_: PackageManager.NameNotFoundException) {}
        }

        return mapOf(
            "gsfVersionCode" to gsfVersionCode,
            "vendingVersionCode" to vendingVersionCode,
            "vendingVersionString" to vendingVersionString
        )
    }

    private val eglExtensions: List<String>
        get() {
            val extensions = mutableSetOf<String>()
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            egl.eglInitialize(display, null)
            val configCount = IntArray(1)

            if (egl.eglGetConfigs(display, null, 0, configCount)) {
                val configs = arrayOfNulls<EGLConfig>(configCount[0])
                if (egl.eglGetConfigs(display, configs, configCount[0], configCount)) {
                    val pbufferAttribs = intArrayOf(
                        EGL10.EGL_WIDTH, EGL10.EGL_PBUFFER_BIT,
                        EGL10.EGL_HEIGHT, EGL10.EGL_PBUFFER_BIT,
                        EGL10.EGL_NONE
                    )
                    val contextAttributes = intArrayOf(12440, EGL10.EGL_PIXMAP_BIT, EGL10.EGL_NONE)

                    for (config in configs) {
                        if (isConfigValid(egl, display, config)) {
                            addExtensions(
                                egl,
                                display,
                                config,
                                pbufferAttribs,
                                null,
                                extensions
                            )
                            addExtensions(
                                egl,
                                display,
                                config,
                                pbufferAttribs,
                                contextAttributes,
                                extensions
                            )
                        }
                    }
                }
            }

            egl.eglTerminate(display)

            return extensions
                .filter { it.isNotEmpty() }
                .sorted()
        }

    private fun isConfigValid(egl: EGL10, display: EGLDisplay, config: EGLConfig?): Boolean {
        val configAttrib = IntArray(1)
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_CONFIG_CAVEAT, configAttrib)
        if (configAttrib[0] == EGL10.EGL_SLOW_CONFIG) return false

        egl.eglGetConfigAttrib(display, config, EGL10.EGL_SURFACE_TYPE, configAttrib)
        if (configAttrib[0] and 1 == 0) return false

        egl.eglGetConfigAttrib(display, config, EGL10.EGL_RENDERABLE_TYPE, configAttrib)
        return configAttrib[0] and 1 != 0
    }

    private fun addExtensions(
        egl: EGL10,
        display: EGLDisplay,
        config: EGLConfig?,
        pbufferAttribs: IntArray,
        contextAttribs: IntArray?,
        extensions: MutableSet<String>
    ) {
        val context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, contextAttribs)
        if (context == EGL10.EGL_NO_CONTEXT) return
        val surface = egl.eglCreatePbufferSurface(display, config, pbufferAttribs)
        if (surface == EGL10.EGL_NO_SURFACE) { egl.eglDestroyContext(display, context); return }
        egl.eglMakeCurrent(display, surface, surface, context)
        val extensionString = GLES10.glGetString(GLES10.GL_EXTENSIONS)
        if (!TextUtils.isEmpty(extensionString)) { extensions.addAll(extensionString.split(" ")) }
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        egl.eglDestroySurface(display, surface)
        egl.eglDestroyContext(display, context)
    }
}
