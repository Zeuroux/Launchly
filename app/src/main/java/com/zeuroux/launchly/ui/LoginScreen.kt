package com.zeuroux.launchly.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.utils.retrieveAc2dmToken
import java.util.regex.Pattern

private const val EMBEDDED_SETUP_URL = "https://accounts.google.com/EmbeddedSetup"
private const val AUTH_TOKEN = "oauth_token"
val loadProgress = mutableFloatStateOf(0f)

@Composable
fun LoginScreen() {
    val showScreen = GlobalData.showLoginScreen
    val context = LocalContext.current
    val animatedProgress by animateFloatAsState(targetValue = loadProgress.floatValue, label = "progress")
    BaseBottomSheet(showScreen.value, { showScreen.value = false }, true) {
        Box {
            AndroidView({
                WebView(it).apply {
                    this.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    this.setupLogin { email: String, token: String ->
                        showScreen.value = false
                        if (email.isNotEmpty() && token.isNotEmpty()) {
                            val accountData = context.getSharedPreferences("account_data", Context.MODE_PRIVATE)
                            accountData.edit().apply {
                                putString("email", email)
                                putString("token", token)
                                commit()
                            }
                            GlobalData.updateAuthData(context)
                        } else {
                            Toast.makeText(context, "Failed to login.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            AnimatedVisibility(animatedProgress != 1f, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator({ animatedProgress }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupLogin(callback: (String, String) -> Unit) {
    val webView = this
    CookieManager.getInstance().apply {
        removeAllCookies(null)
        acceptThirdPartyCookies(webView)
        setAcceptThirdPartyCookies(webView, true)
    }

    webView.apply {
        settings.apply {
            allowContentAccess = true
            databaseEnabled = true
            domStorageEnabled = true
            javaScriptEnabled = true
            settings.safeBrowsingEnabled = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                loadProgress.floatValue = newProgress.toFloat() / 100
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val cookieMap = mutableMapOf<String, String>()
                val pattern = Pattern.compile("([^=]+)=([^;]*);?\\s?")
                val matcher = pattern.matcher(CookieManager.getInstance().getCookie(url))
                while (matcher.find()) {
                    cookieMap[matcher.group(1)!!] = matcher.group(2)!!
                }
                if (cookieMap.isNotEmpty() && cookieMap[AUTH_TOKEN] != null) {
                    webView.evaluateJavascript("(function() { return document.querySelector('[data-profile-identifier]').innerHTML; })();") {
                        val email = it.replace("\"".toRegex(), "")
                        retrieveAc2dmToken(email, cookieMap[AUTH_TOKEN], callback)
                    }
                }
            }
        }
        loadUrl(EMBEDDED_SETUP_URL)
    }
}