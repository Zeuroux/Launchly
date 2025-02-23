package com.zeuroux.launchly.globals

import android.os.Build

object Links {
    const val TERMS = "https://github.com/Zeuroux/Launchly/blob/master/TERMS.md"
    const val README = "https://github.com/Zeuroux/Launchly/blob/master/README.md"
    const val POLICY = "https://github.com/Zeuroux/Launchly/blob/master/POLICY.md"
    const val DISCLAIMER = "https://github.com/Zeuroux/Launchly/blob/master/DISCLAIMER.md"
    const val LICENSE = "https://github.com/Zeuroux/Launchly/blob/master/LICENSE"
    const val VERSIONS_LIST_BASE = "https://raw.githubusercontent.com/minecraft-linux/mcpelauncher-versiondb/refs/heads/master/versions.arch.json.min"
}

val architectures = listOf(
    "arm64-v8a",
    "armeabi-v7a",
    "x86",
    "x86_64"
).filter { Build.SUPPORTED_ABIS.contains(it) }

val releaseTypes = listOf(
    "Release",
    "Beta"
)

const val SUPPORTED_MIN = 871000500