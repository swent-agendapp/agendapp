package com.android.sample.ui.replacement.organize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.android.sample.ui.replacement.organize.components.SelectSubstitutedScreen

/**
 * Entry-point composable for the **Organize Replacement** feature.
 *
 * This composable manages the multi-step workflow involved in organizing a replacement for an
 * absent member. It internally manages the navigation logic with a view model. Individual screens
 * do not navigate directly; instead they request navigation by invoking callbacks.
 *
 * @param onCancel Callback triggered when the user cancels the flow
 */
@Composable
fun ReplacementOrganizeScreen(onCancel: () -> Unit = {}) {
  // This currentStep be handled by the view model in a complete implementation
  val currentStep by remember { mutableIntStateOf(1) }

  when (currentStep) {
    1 -> SelectSubstitutedScreen(onBack = onCancel)
  }
}
