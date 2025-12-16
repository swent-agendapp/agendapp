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
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium

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
    icon: ImageVector = Icons.Default.Add,
    enabled: Boolean = true
) {
  FloatingActionButton(
      modifier =
          modifier.then(
              if (!enabled) {
                Modifier.semantics { disabled() }
              } else {
                Modifier
              }),
      onClick = onClick.takeIf { enabled } ?: {},
      containerColor =
          if (enabled) GeneralPalette.Primary else GeneralPalette.Primary.copy(alpha = WeightLight),
      contentColor = if (enabled) Color.White else Color.White.copy(alpha = WeightMedium),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = PaddingSmall),
        verticalAlignment = Alignment.CenterVertically) {
          Icon(icon, contentDescription = stringResource(R.string.create_area_button))
          if (text != null) Text(text, modifier = Modifier.padding(start = PaddingSmall))
        }
  }
}
