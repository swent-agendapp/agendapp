package com.android.sample.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.theme.ButtonElevationMedium
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
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
fun SecondaryButton(
    modifier: Modifier = Modifier,
    text: String = "Test",
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
  OutlinedButton(
      modifier =
          modifier.fillMaxWidth().padding(horizontal = PaddingMedium, vertical = PaddingSmall),
      elevation =
          ButtonDefaults.buttonElevation(
              defaultElevation = ButtonElevationMedium,
          ),
      shape = RoundedCornerShape(CornerRadiusLarge),
      onClick = onClick,
      enabled = enabled,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = Color.Black,
              disabledContainerColor = Color.White.copy(alpha = WeightLight),
              disabledContentColor = Color.Black.copy(alpha = WeightMedium))) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = PaddingSmall, vertical = PaddingSmall))
      }
}
