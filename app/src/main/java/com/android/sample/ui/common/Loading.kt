package com.android.sample.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.BorderWidthExtraThick
import com.android.sample.ui.theme.PaddingMedium

/**
 * Displays a centered loading indicator with an optional label.
 *
 * This composable is a lightweight building block used to indicate a loading state inside a
 * specific area of the screen. It shows a [CircularProgressIndicator] and, optionally, a
 * descriptive text below it.
 *
 * Typical use cases:
 * - Inline loading inside a screen section
 * - Content replacement while data is being fetched
 *
 * @param modifier Optional [Modifier] applied to the root container.
 * @param label Optional text displayed below the loading indicator to describe the current loading
 *   operation (e.g. "Loading...", "Saving event").
 */
@Composable
fun Loading(modifier: Modifier = Modifier, label: String? = null) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator(strokeWidth = BorderWidthExtraThick)
      if (!label.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(PaddingMedium))
        Text(text = label)
      }
    }
  }
}

/**
 * Displays a full-screen loading overlay that blocks user interaction.
 *
 * This composable covers the entire screen with a semi-transparent background and displays a
 * centered [Loading] indicator on top. It is intended to be layered above existing content to
 * indicate a blocking loading state.
 *
 * Typical use cases:
 * - Saving or submitting a form
 * - Performing a critical operation where user interaction must be disabled
 * - Loading initial screen data
 *
 * The overlay visually dims the background content and prevents further interaction until the
 * loading state is resolved.
 *
 * @param label Optional text displayed below the loading indicator to describe the ongoing
 *   operation.
 */
@Composable
fun LoadingOverlay(label: String? = null) {
  Box(
      modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = AlphaLow)),
      contentAlignment = Alignment.Center) {
        Loading(label = label)
      }
}
