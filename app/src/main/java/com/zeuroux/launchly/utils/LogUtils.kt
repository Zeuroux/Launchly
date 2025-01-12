@file:Suppress("unused")
package com.zeuroux.launchly.utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class LogUtils {
    companion object {
        @JvmStatic
        fun stackToString(e: Exception): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            e.printStackTrace()
            return sw.toString()
        }

        @SuppressLint("SetTextI18n")
        @JvmStatic
        fun log(message: String) {
            Log.i("Launchly", message)
        }
    }
}