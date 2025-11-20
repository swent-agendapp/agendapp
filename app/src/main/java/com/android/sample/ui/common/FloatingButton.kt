package com.android.sample.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingSmall

/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 */
@Composable
@Preview
fun FloatingButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    onClick: () -> Unit = {},
    icon: ImageVector = Icons.Default.Add
) {
  FloatingActionButton(
      modifier = modifier,
      onClick = onClick,
      containerColor = GeneralPalette.Primary,
      contentColor = Color.White) {
        Row(
            modifier = Modifier.padding(horizontal = PaddingSmall),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(icon, contentDescription = stringResource(R.string.create_area_button))
              if (text != null) Text(text, modifier = Modifier.padding(start = PaddingSmall))
            }
      }
}
