package com.android.sample.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.sample.ui.theme.TopBarBackgroundColor
import com.android.sample.ui.theme.TopBarTextColor

/**
 * Composable function to create a top bar.
 *
 * @param modifier The modifier to be applied to the top bar.
 * @param title The text to display in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageTopBar(
    modifier: Modifier = Modifier,
    title: String = "Untitled",
) {
  TopAppBar(
      title = { Text(text = title, modifier = modifier) },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = TopBarBackgroundColor,
              titleContentColor = TopBarTextColor,
          ),
  )
}
