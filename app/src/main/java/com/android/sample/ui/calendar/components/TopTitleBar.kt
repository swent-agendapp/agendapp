package com.android.sample.ui.calendar.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.R
import com.android.sample.ui.theme.CornerRadiusExtraLarge

/**
 * A configurable top app bar with optional navigation and logout actions.
 *
 * This composable wraps Material3's [TopAppBar] and provides:
 * - A title
 * - An optional back / navigation icon (`canNavigateBack = true`)
 * - An optional logout icon (`canLogOut = true`)
 * - Support for custom click actions via callbacks
 *
 * It is designed to be reusable across screens that need a title bar with minimal UI controls, such
 * as Add Event, Replacement, or any detail screen.
 *
 * @param modifier Optional [Modifier] applied to the top app bar (e.g., padding or test tags).
 * @param title Text displayed at the center of the app bar.
 * @param canNavigateBack Whether the back button should be shown. If `true`, the back arrow
 *   appears.
 * @param onBack Callback invoked when the back button is pressed. Defaults to a no-op.
 * @param canLogOut Whether the logout button should be shown. If `true`, the logout icon appears.
 * @param onLogOut Callback invoked when the logout button is pressed. Defaults to a no-op.
 *
 * Example:
 * ```
 * TopTitleBar(
 *     title = "Add Event",
 *     canNavigateBack = true,
 *     onBack = { navController.popBackStack() },
 *     canLogOut = true,
 *     onLogOut = { viewModel.logout() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    canNavigateBack: Boolean = false,
    onBack: () -> Unit = {},
    canLogOut: Boolean = false,
    onLogOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
      },
      navigationIcon = {
        if (canNavigateBack) {
          IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("")) { // later : NavigationTestTags.GO_BACK_BUTTON
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.top_bar_back_content_description))
              }
        }
      },
      actions = {
        if (canLogOut) {
          IconButton(
              onClick = onLogOut,
              modifier = Modifier.testTag("")) { // later : NavigationTestTags.LOGOUT_BUTTON
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Logout,
                    contentDescription =
                        stringResource(R.string.top_bar_logout_content_description))
              }
        }
      },
      modifier =
          modifier.clip(
              RoundedCornerShape(
                  bottomStart = CornerRadiusExtraLarge, bottomEnd = CornerRadiusExtraLarge)),
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
}
