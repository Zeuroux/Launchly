package com.zeuroux.launchly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageStatusReceiver(private val onAppStatusChange: (String, String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        if (packageName != null) {
            if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
                onAppStatusChange(packageName, "removed")
            } else if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
                onAppStatusChange(packageName, "added")
            }
        }
    }
}