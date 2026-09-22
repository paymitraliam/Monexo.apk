package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

    // Configure status bar color to pure black with white status icons (matching user's screenshot)
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
  var showSplash by remember { mutableStateOf(true) }

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

    // Show splash screen for 2.2 seconds
    delay(2200)
    showSplash = false
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Dedicated black header area behind system status bar (time, network, battery)
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
    ) {
      if (showSplash) {
        MonexoSplashScreen()
      } else {
        MonexoWebViewScreen(initialUrl = "https://monexo.wiki")
      }
    }
  }
}

/**
 * Splash screen reproducing user's exact screenshot design:
 * Clean bright blue canvas, centered rounded white badge with Monexo logo,
 * and bottom "Already newest version" indicator badge.
 */
@Composable
fun MonexoSplashScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(SplashBlueBackground)
      .testTag("splash_screen")
  ) {
    // Centered Monexo App Badge
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        modifier = Modifier
          .size(112.dp)
          .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
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
              .size(92.dp)
              .clip(RoundedCornerShape(18.dp))
          )
        }
      }
    }

    // Bottom "Already newest version" floating badge
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 54.dp)
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xE6212121))
        .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Surface(
          modifier = Modifier.size(24.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color.White
        ) {
          Image(
            painter = painterResource(id = R.drawable.monexo_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Already newest version",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

/**
 * 100% Full Screen WebView:
 * - NO top app bar / header at all (maximum screen real estate for the website)
 * - Pure edge-to-edge web content with slim top loading indicator
 * - Full Android hardware back button gesture navigation
 * - Network error & offline recovery view
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonexoWebViewScreen(
  initialUrl: String = "https://monexo.wiki"
) {
  val context = LocalContext.current
  var webView by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }
  var progress by remember { mutableFloatStateOf(0f) }
  var isLoading by remember { mutableStateOf(true) }
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
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Pure Fullscreen WebView
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
              progress = newProgress / 100f
              isLoading = newProgress < 100
            }
          }

          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              super.onPageStarted(view, url, favicon)
              isLoading = true
              hasError = false
              canGoBack = view?.canGoBack() == true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              isLoading = false
              canGoBack = view?.canGoBack() == true
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              super.onReceivedError(view, request, error)
              if (request?.isForMainFrame == true) {
                hasError = true
                isLoading = false
                val description = error?.description?.toString() ?: "Failed to connect to page"
                errorMessage = description
              }
            }

            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?
            ): Boolean {
              val uri = request?.url ?: return false
              val scheme = uri.scheme?.lowercase() ?: ""

              // Handle intent schemes or tel/mailto/sms
              if (scheme == "tel" || scheme == "mailto" || scheme == "sms") {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, uri)
                  context.startActivity(intent)
                } catch (_: Exception) {}
                return true
              }

              // Keep all web URLs inside the full screen WebView
              if (scheme == "http" || scheme == "https") {
                return false
              }

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

    // Subtle loading indicator at the very top edge
    AnimatedVisibility(
      visible = isLoading,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(2.5.dp)
          .testTag("page_loading_progress"),
        color = SplashBlueBackground,
        trackColor = Color.Transparent
      )
    }

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
            .padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = "Error icon",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "Unable to connect",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = errorMessage.ifBlank { "Could not load Monexo. Please check your connection." },
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          Spacer(modifier = Modifier.height(28.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
                hasError = false
                isLoading = true
                reloadTrigger++
                webView?.reload()
              },
              colors = ButtonDefaults.buttonColors(containerColor = SplashBlueBackground),
              modifier = Modifier.testTag("retry_button")
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
                isLoading = true
                webView?.loadUrl(initialUrl)
              },
              colors = ButtonDefaults.filledTonalButtonColors(),
              modifier = Modifier.testTag("home_button")
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
      webView?.stopLoading()
      webView?.destroy()
    }
  }
}
