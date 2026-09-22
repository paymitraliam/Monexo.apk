package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Status bar is black with white system icons (matching user's screenshot)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.BLACK
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = false
      isAppearanceLightNavigationBars = false
    }

    setContent {
      MyApplicationTheme {
        MonexoAppRoot()
      }
    }
  }
}

fun isNetworkAvailable(context: Context): Boolean {
  val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val network = connectivityManager.activeNetwork ?: return false
  val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
  return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// Solid blue splash background color matching user screenshot
val SplashBlueBackground = Color(0xFF4A8BF5)

@Composable
fun MonexoAppRoot() {
  var isSplashVisible by remember { mutableStateOf(true) }
  var isWebViewReady by remember { mutableStateOf(false) }

  // Permissions launcher for Notifications, SMS, and Storage
  val permissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    // Result handled gracefully
  }

  LaunchedEffect(Unit) {
    // Collect runtime permissions list based on Android API level
    val permissionsToRequest = mutableListOf<String>()

    // Notification permission (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    // SMS permissions
    permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
    permissionsToRequest.add(Manifest.permission.READ_SMS)

    // Storage permission (Android 12 and below)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
      permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    if (permissionsToRequest.isNotEmpty()) {
      permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Minimum display duration for splash
    delay(2000)
    if (!isWebViewReady) {
      delay(1500)
    }
    isSplashVisible = false
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Black header area for system status bar (where time, network, battery are displayed)
    Spacer(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsTopHeight(WindowInsets.statusBars)
        .background(Color.Black)
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(Color.White)
    ) {
      // WebView loaded in background with pure white backdrop
      MonexoWebViewScreen(
        initialUrl = "https://monexo.wiki",
        onFirstPageLoaded = {
          isWebViewReady = true
        }
      )

      if (isSplashVisible) {
        MonexoSplashScreen()
      }
    }
  }
}

/**
 * Splash screen reproducing user's exact screenshot design:
 * - Black status bar header at the top
 * - Compact logo at top center (smaller size ~76dp badge matching screenshot)
 * - Clean bright blue canvas
 * - Bottom "Already newest version" with normal (non-bold) text matching screenshot
 */
@Composable
fun MonexoSplashScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(SplashBlueBackground)
      .testTag("splash_screen")
  ) {
    // Top-Center Monexo App Badge (compact size matching user's screenshot)
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 48.dp, start = 32.dp, end = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top
    ) {
      Surface(
        modifier = Modifier
          .size(76.dp)
          .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.monexo_logo),
            contentDescription = "Monexo Logo",
            modifier = Modifier
              .size(62.dp)
              .clip(RoundedCornerShape(14.dp))
          )
        }
      }
    }

    // Bottom "Already newest version" floating dark pill badge with NORMAL text (non-bold)
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 54.dp)
        .shadow(elevation = 5.dp, shape = RoundedCornerShape(14.dp))
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xE61E2124))
        .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Surface(
          modifier = Modifier.size(20.dp),
          shape = RoundedCornerShape(5.dp),
          color = Color.White
        ) {
          Image(
            painter = painterResource(id = R.drawable.monexo_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
          text = "Already newest version",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal // Normal regular text as in screenshot
        )
      }
    }
  }
}

/**
 * 100% Full Screen WebView:
 * - Direct seamless display without black transition screen or loading bar
 * - Direct Telegram deep linking (opens Telegram app directly instead of web preview page)
 * - Direct WhatsApp & external app intent routing
 * - Full Android hardware back button gesture navigation
 * - Network error & offline recovery view
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonexoWebViewScreen(
  initialUrl: String = "https://monexo.wiki",
  onFirstPageLoaded: () -> Unit = {}
) {
  val context = LocalContext.current
  var webView by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }
  var hasError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var reloadTrigger by remember { mutableIntStateOf(0) }

  // Check network connectivity on launch or retry
  LaunchedEffect(reloadTrigger) {
    if (!isNetworkAvailable(context)) {
      hasError = true
      errorMessage = "No internet connection detected. Please check your network and retry."
    }
  }

  // Handle Android system back button seamlessly
  BackHandler(enabled = canGoBack) {
    webView?.let {
      if (it.canGoBack()) {
        it.goBack()
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .testTag("monexo_webview"),
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )

          setBackgroundColor(android.graphics.Color.WHITE)

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = settings.userAgentString + " MonexoApp/1.0"
          }

          webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
              if (newProgress >= 40) {
                onFirstPageLoaded()
              }
            }
          }

          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              super.onPageStarted(view, url, favicon)
              hasError = false
              canGoBack = view?.canGoBack() == true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              canGoBack = view?.canGoBack() == true
              onFirstPageLoaded()
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              super.onReceivedError(view, request, error)
              if (request?.isForMainFrame == true) {
                hasError = true
                val description = error?.description?.toString() ?: "Failed to connect to page"
                errorMessage = description
              }
            }

            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?
            ): Boolean {
              val uri = request?.url ?: return false
              val urlString = uri.toString()
              val scheme = uri.scheme?.lowercase() ?: ""
              val host = uri.host?.lowercase() ?: ""

              // 1. Direct Telegram Redirection (open Telegram app directly, not web preview page)
              if (scheme == "tg" || host == "t.me" || host.endsWith(".t.me") || host == "telegram.me" || host.endsWith(".telegram.me") || host == "telegram.dog") {
                return openTelegramDirectly(context, uri)
              }

              // 2. Direct WhatsApp Redirection
              if (scheme == "whatsapp" || host == "wa.me" || host == "api.whatsapp.com" || host.endsWith(".whatsapp.com")) {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, uri)
                  context.startActivity(intent)
                  return true
                } catch (_: Exception) {}
              }

              // 3. Telephony, SMS, Mailto schemes
              if (scheme == "tel" || scheme == "mailto" || scheme == "sms") {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, uri)
                  context.startActivity(intent)
                } catch (_: Exception) {}
                return true
              }

              // 4. Intent scheme (e.g. intent://...)
              if (scheme == "intent") {
                try {
                  val parsedIntent = Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME)
                  context.startActivity(parsedIntent)
                  return true
                } catch (_: Exception) {
                  return true
                }
              }

              // 5. Keep all normal web browsing (monexo.wiki etc.) inside the WebView
              if (scheme == "http" || scheme == "https") {
                return false
              }

              // 6. Any other 3rd party URI
              return try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
                true
              } catch (_: Exception) {
                true
              }
            }
          }

          webView = this
          loadUrl(initialUrl)
        }
      },
      update = { wv ->
        webView = wv
      }
    )

    // Error / Offline Screen Overlay
    if (hasError) {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .testTag("error_view"),
        color = MaterialTheme.colorScheme.background
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Connection Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Unable to load Monexo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(28.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                hasError = false
                reloadTrigger++
                webView?.reload()
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("error_retry_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
              )
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Retry")
            }

            Button(
              onClick = {
                hasError = false
                webView?.loadUrl(initialUrl)
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("error_home_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
              )
            ) {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Home")
            }
          }
        }
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webView?.destroy()
    }
  }
}

/**
 * Direct Telegram redirection helper:
 * Converts https://t.me/username to tg://resolve?domain=username
 * and launches Telegram app directly instead of opening Telegram's web preview page.
 */
fun openTelegramDirectly(context: Context, uri: Uri): Boolean {
  val scheme = uri.scheme?.lowercase() ?: ""
  val host = uri.host?.lowercase() ?: ""

  // If already tg:// scheme
  if (scheme == "tg") {
    return try {
      val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (_: Exception) {
      false
    }
  }

  // If web telegram url like https://t.me/MonexoCustomerSupport or https://t.me/+invite
  val path = uri.path?.trimStart('/') ?: ""

  val targetTgUri = when {
    path.startsWith("+") -> {
      // Invite link: tg://join?invite=...
      Uri.parse("tg://join?invite=" + path.substring(1))
    }
    path.startsWith("joinchat/") -> {
      Uri.parse("tg://join?invite=" + path.substring("joinchat/".length))
    }
    path.isNotEmpty() -> {
      // Channel or username link: tg://resolve?domain=...
      val cleanUser = path.split("/").firstOrNull() ?: path
      Uri.parse("tg://resolve?domain=$cleanUser")
    }
    else -> uri
  }

  return try {
    // Try launching with tg:// scheme (opens official Telegram app or Telegram X)
    val tgIntent = Intent(Intent.ACTION_VIEW, targetTgUri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(tgIntent)
    true
  } catch (_: Exception) {
    // Fallback: if Telegram app not installed, open external browser (Chrome/etc.)
    try {
      val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(browserIntent)
      true
    } catch (_: Exception) {
      true
    }
  }
}
