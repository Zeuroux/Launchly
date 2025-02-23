package com.zeuroux.launchly.version

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zeuroux.launchly.extensions.toBoolean
import com.zeuroux.launchly.globals.Links.VERSIONS_LIST_BASE
import com.zeuroux.launchly.globals.SUPPORTED_MIN
import com.zeuroux.launchly.globals.architectures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class VersionData(
    val name: String,
    val type: String,
    val architecture: String
)
fun Any.toJsonString(): String {
    return Gson().toJson(this)
}

fun String.toVersionDataMap(): Map<String, VersionData> {
    return Gson().fromJson(this, object : TypeToken<Map<String, VersionData>>() {}.type) ?: emptyMap()
}

fun String.toVersionDataPair(): Pair<String, VersionData>? {
    return Gson().fromJson(this, object : TypeToken<Pair<String, VersionData>>() {}.type)
}


class VersionDB(private val client: OkHttpClient, private val dataDir: File) : ViewModel() {
    private val _versions = MutableStateFlow(dataDir.resolve("versions.json").readText().toVersionDataMap())
    val versions = _versions.asStateFlow()
    private val _latestRelease = MutableStateFlow(dataDir.resolve("latestRelease.json").readText().toVersionDataPair())
    val latestRelease = _latestRelease.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun fetchVersions(context : Context, onFinish: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            val tempVersions = mutableMapOf<String, VersionData>()

            try {
                architectures.forEach { arch ->
                    val request = Request.Builder()
                        .url(VERSIONS_LIST_BASE.replace("arch", arch))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string()?.let { jsonResponse ->
                                tempVersions.putAll(parseVersionData(jsonResponse, arch))
                            }
                        } else {
                            _error.value = "Failed to fetch data: ${response.code}"
                        }
                    }
                }
                _versions.value = tempVersions
                dataDir.resolve("versions.json").writeText(tempVersions.toJsonString())
                tempVersions.toSortedMap(compareBy { it.toLong() }).forEach {
                    if (it.value.type == "Release") {
                        _latestRelease.value = it.key to it.value
                        dataDir.resolve("latestRelease.json").writeText(_latestRelease.value!!.toJsonString())
                        return@forEach
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                onFinish()
            }
        }
    }

    private fun parseVersionData(json: String, arch: String): Map<String, VersionData> {
        return try {
            json.removeSurrounding("[[", "]]")
                .split("],[")
                .associate {
                    val parts = it.split(",").map { part -> part.trim('"') }
                    if (parts.size == 3) {
                        val (versionCode, versionName, isBeta) = parts
                        versionCode to VersionData(versionName, if (isBeta.toBoolean()) "Beta" else "Release", arch)
                    } else {
                        throw IllegalArgumentException("Invalid version data format")
                    }
                }.filter { it.key.toLong() >= SUPPORTED_MIN }

        } catch (e: Exception) {
            _error.value = "Parsing error: ${e.message}"
            emptyMap()
        }
    }
}
