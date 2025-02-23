package com.zeuroux.launchly.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Locale
import java.util.StringTokenizer

const val TOKEN_AUTH_URL = "https://android.clients.google.com/auth"
const val BUILD_VERSION_SDK = 28
const val PLAY_SERVICES_VERSION_CODE = 19629032

fun retrieveAc2dmToken(email: String?, oAuthToken: String?, callback: (String, String) -> Unit) {
    if (email == null && oAuthToken == null) {
        callback("", "")
        return
    }

    Thread {
        val authToken = getAC2DMResponse(email, oAuthToken) as? Map<String, String> ?: emptyMap()
        val emailResult = authToken["Email"] ?: ""
        val tokenResult = authToken["Token"] ?: ""
        callback(emailResult, tokenResult)
    }.start()
}

private fun getAC2DMResponse(email: String?, oAuthToken: String?): Map<String, String> {
    if (email == null || oAuthToken == null) return mapOf()

    val params: MutableMap<String, Any> = hashMapOf()
    params["lang"] = Locale.getDefault().toString().replace("_", "-")
    params["google_play_services_version"] = PLAY_SERVICES_VERSION_CODE
    params["sdk_version"] = BUILD_VERSION_SDK
    params["device_country"] = Locale.getDefault().country.lowercase(Locale.US)
    params["Email"] = email
    params["service"] = "ac2dm"
    params["get_accountid"] = 1
    params["ACCESS_TOKEN"] = 1
    params["callerPkg"] = "com.google.android.gms"
    params["add_account"] = 1
    params["Token"] = oAuthToken
    params["callerSig"] = "38918a453d07199354f8b19af05ec6562ced5788"

    val body = params.map { "${it.key}=${it.value}" }.joinToString(separator = "&")

    val client = OkHttpClient()

    val request = Request.Builder()
        .url(TOKEN_AUTH_URL)
        .post(body.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull()))
        .addHeader("app", "com.google.android.gms")
        .addHeader("User-Agent", "")
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: ""
            val keyValueMap: MutableMap<String, String> = HashMap()
            val st = StringTokenizer(responseBody, "\n\r")
            while (st.hasMoreTokens()) {
                val keyValue = st.nextToken().split("=".toRegex(), limit = 2).toTypedArray()
                if (keyValue.size >= 2) {
                    keyValueMap[keyValue[0]] = keyValue[1]
                }
            }
            return keyValueMap
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return mapOf()
}