package com.android.sample.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.theme.ButtonElevationMedium
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium

/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 */
@Composable
@Preview
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String = "Test",
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    innerPadding: Dp = PaddingSmall,
    unabledText: String = "Button disabled",
    showUnabledText: Boolean = false,
    unabledTextTestTag: String = "",
) {
  Button(
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
              containerColor = GeneralPalette.Primary,
              contentColor = Color.White,
              disabledContainerColor = GeneralPalette.Primary.copy(alpha = WeightLight),
              disabledContentColor = Color.White.copy(alpha = WeightMedium))) {
        Text(text = text, modifier = Modifier.padding(innerPadding))
      }

  // Show unable text when button is disabled and flag is set
  if (!enabled && showUnabledText) {
    Spacer(modifier = Modifier.height(SpacingSmall))
    Text(
        text = unabledText,
        style = MaterialTheme.typography.bodyMedium,
        color = GeneralPalette.Error,
        textAlign = TextAlign.Center,
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = PaddingMedium).testTag(unabledTextTestTag))
  }
}
