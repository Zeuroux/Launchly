package com.zeuroux.launchly.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU

fun getPackageInfo(context: Context, packageName: String, flags: Int = 0): PackageInfo {
    return if (SDK_INT >= TIRAMISU) {
        context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        context.packageManager.getPackageInfo(packageName, flags)
    }
}