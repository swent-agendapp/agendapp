package com.android.sample.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.theme.TopBarPalette

/**
 * Composable function to create a top bar.
 *
 * @param modifier The modifier to be applied to the top bar.
 * @param title The text to display in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryPageTopBar(
    modifier: Modifier = Modifier,
    title: String = "Untitled",
    canGoBack: Boolean = true,
    onClick: () -> Unit = {},
    backButtonTestTags: String = "",
    actions: @Composable RowScope.() -> Unit = {}
) {
  TopAppBar(
      title = { Text(text = title, modifier = modifier) },
      navigationIcon = {
        if (canGoBack) {
          IconButton(onClick = onClick, modifier = Modifier.testTag(backButtonTestTags)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back))
          }
        }
      },
      actions = actions,
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = TopBarPalette.Background,
              titleContentColor = TopBarPalette.Font,
          ))
}
