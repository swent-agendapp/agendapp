package com.android.sample.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.calendar.CalendarScreenTestTags
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
    title = {
      Text(
        text = title,
        modifier = modifier)
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = TopBarBackgroundColor,
        titleContentColor = TopBarTextColor,
      ),
  )
}
