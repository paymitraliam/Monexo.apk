package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testWebViewScreenDisplays() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MonexoWebViewScreen(initialUrl = "https://monexo.wiki")
      }
    }

    composeTestRule.onNodeWithTag("monexo_webview").assertExists()
  }
}
