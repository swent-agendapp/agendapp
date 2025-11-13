package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DangerRed
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.Red80
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium
import com.android.sample.ui.theme.heightLarge
import com.android.sample.ui.theme.widthLarge


/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 *
 */
@Composable
@Preview
fun PrimaryButton(
  text: String = "Test",
  onClick: () -> Unit = {},
  testTags: String = "",
  enabled: Boolean = true
) {
  Button(
    onClick = onClick,
    modifier =
      Modifier.size(width = widthLarge, height = heightLarge)
        .testTag(testTags),
    shape = RoundedCornerShape(CornerRadiusLarge),
    enabled = enabled,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = DangerRed,
        contentColor = Color.White,
        disabledContainerColor = DangerRed.copy(alpha = WeightLight),
        disabledContentColor = Color.White.copy(alpha = WeightMedium))) {
    Text(
      text = text,
      modifier =
        Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
  }
}
