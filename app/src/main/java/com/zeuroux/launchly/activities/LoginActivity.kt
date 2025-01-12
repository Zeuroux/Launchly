package com.zeuroux.launchly.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.Locale
import java.util.StringTokenizer
import java.util.regex.Matcher
import java.util.regex.Pattern




class LoginActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var cookieManager: CookieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        cookieManager = CookieManager.getInstance()
        setContentView(webView)
        setupWebView()

    }

    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        setResult(RESULT_CANCELED)
        finish()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        cookieManager.apply {
            removeAllCookies(null)
            acceptThirdPartyCookies(webView)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.settings.safeBrowsingEnabled = false


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val cookieMap = mutableMapOf<String, String>()
                val pattern: Pattern = Pattern.compile("([^=]+)=([^;]*);?\\s?")
                val matcher: Matcher = pattern.matcher(CookieManager.getInstance().getCookie(url))
                while (matcher.find()) {
                    cookieMap[matcher.group(1)!!] = matcher.group(2)!!
                }
                if (cookieMap.isNotEmpty() && cookieMap[AUTH_TOKEN] != null) {
                    webView.evaluateJavascript("(function() { return document.querySelector('[data-profile-identifier]').innerHTML; })();") {
                        val email = it.replace("\"".toRegex(), "")
                        retrieveAc2dmToken(email, cookieMap[AUTH_TOKEN])
                    }
                }
            }
        }

        webView.apply {
            settings.apply {
                allowContentAccess = true
                databaseEnabled = true
                domStorageEnabled = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            loadUrl(EMBEDDED_SETUP_URL)
        }
    }

    private fun retrieveAc2dmToken(email: String?, oAuthToken: String?) {
        if (email != null || oAuthToken != null) {
            var authToken = mutableMapOf<String, String>()
            val asyncTask = Thread {
                authToken = getAC2DMResponse(email, oAuthToken) as MutableMap<String, String>
            }
            asyncTask.start()
            asyncTask.join()
            if (authToken.isNotEmpty()) {
                val intent = Intent().apply {
                    putExtra("accountName", authToken["firstName"])
                    putExtra("accountEmail", authToken["Email"])
                    putExtra("accountToken", authToken["Token"])
                }
                setResult(RESULT_OK, intent)
                finish()
            } else {
                setResult(-2)
                finish()
            }
        }
        else {
            setResult(-3)
            finish()
        }
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

    companion object {
        private const val EMBEDDED_SETUP_URL = "https://accounts.google.com/EmbeddedSetup"
        private const val AUTH_TOKEN = "oauth_token"
        const val TOKEN_AUTH_URL = "https://android.clients.google.com/auth"
        const val BUILD_VERSION_SDK = 28
        const val PLAY_SERVICES_VERSION_CODE = 19629032
    }
}