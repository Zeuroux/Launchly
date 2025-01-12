package com.zeuroux.launchly.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.core.graphics.drawable.toBitmap
import ru.solrudev.ackpine.DisposableSubscriptionContainer
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.parameters.InstallParameters
import ru.solrudev.ackpine.installer.parameters.InstallerType
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.parameters.Confirmation
import ru.solrudev.ackpine.uninstaller.PackageUninstaller
import ru.solrudev.ackpine.uninstaller.UninstallFailure
import ru.solrudev.ackpine.uninstaller.parameters.UninstallParameters
import java.io.File
import java.io.IOException
import java.util.UUID


fun getPackageInfo(context: Context, packageName: String, flags: Int = 0): PackageInfo {
    return if (SDK_INT >= TIRAMISU) {
        context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        context.packageManager.getPackageInfo(packageName, flags)
    }
}

fun getPackageInstallSource(context: Context, packageName: String): String? {
    return if (SDK_INT >= R) {
        context.packageManager.getInstallSourceInfo(packageName).installingPackageName
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getInstallerPackageName(packageName)
    }
}

fun saveIconToFile(icon: Drawable, destination: File) {
    try {
        if (!destination.exists() || BitmapFactory.decodeFile(destination.absolutePath)?.let { !icon.toBitmap().sameAs(it) } != false) {
            destination.parentFile?.takeIf { !it.exists() }?.mkdirs()
            destination.outputStream().use { outputStream ->
                icon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        println("SaveIcon: Failed to save icon: ${e.message}")
    }
}

fun getPackageArch(packageInfo: PackageInfo): Architecture {
    return packageInfo.applicationInfo?.nativeLibraryDir?.let {
        when {
            it.contains("arm64") -> Architecture.ARM64
            it.contains("armeabi-v7a") -> Architecture.ARM
            else -> Architecture.UNKNOWN
        }
    } ?: Architecture.UNKNOWN
}

fun installPackage(context: Context, apks: Array<File>, onStatus: (SessionResult) -> Unit) {
    val packageInstaller = PackageInstaller.getInstance(context)
    val session = packageInstaller.createSession(
        InstallParameters.Builder(apks.map { Uri.fromFile(it) })
            .setConfirmation(Confirmation.IMMEDIATE)
            .setInstallerType(InstallerType.SESSION_BASED)
            .build()
    )
    val subscription = DisposableSubscriptionContainer()
    session.addProgressListener(subscription) { _, progress ->
        onStatus(SessionResult.OnProgress(progress.progress))
    }
    session.addStateListener(subscription, object : Session.TerminalStateListener<InstallFailure>(session) {
        override fun onSuccess(sessionId: UUID) {
            onStatus(SessionResult.OnSuccess)
        }

        override fun onFailure(sessionId: UUID, failure: InstallFailure) {
            onStatus(SessionResult.OnFailure(failure.message ?: "Unknown error"))
        }

        override fun onCancelled(sessionId: UUID) {
            onStatus(SessionResult.OnCancel)
        }
    })
}

fun uninstallApp(context: Context, packageName: String, onStatus: (SessionResult) -> Unit) {
    val packageInstaller = PackageUninstaller.getInstance(context)
    val session = packageInstaller.createSession(
        UninstallParameters.Builder(packageName)
            .setConfirmation(Confirmation.IMMEDIATE)
            .build()
    )
    session.addStateListener(DisposableSubscriptionContainer(), object : Session.TerminalStateListener<UninstallFailure>(session) {
        override fun onSuccess(sessionId: UUID) {
            onStatus(SessionResult.OnSuccess)
        }

        override fun onFailure(sessionId: UUID, failure: UninstallFailure) {
            onStatus(SessionResult.OnFailure("Unknown error"))
        }

        override fun onCancelled(sessionId: UUID) {
            onStatus(SessionResult.OnCancel)
        }
    })
}

sealed class SessionResult {
    data object OnSuccess : SessionResult()
    data class OnFailure(val message: String? = null) : SessionResult()
    data class OnProgress(val progress: Int) : SessionResult()
    data object OnCancel : SessionResult()
}