package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.TopBarPalette

/**
 * Composable function to create a top bar.
 *
 * @param modifier The modifier to be applied to the top bar.
 * @param title The text to display in the top bar.
 * @param actions Optional composable actions to display in the top bar (e.g., icons, chips).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageTopBar(
    modifier: Modifier = Modifier,
    title: String = "Untitled",
    actions: @Composable () -> Unit = {},
) {
  TopAppBar(
      title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingExtraSmall),
        ) {
          actions()
          Text(text = title, modifier = modifier)
        }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = TopBarPalette.Background,
              titleContentColor = TopBarPalette.Font,
          ),
  )
}
