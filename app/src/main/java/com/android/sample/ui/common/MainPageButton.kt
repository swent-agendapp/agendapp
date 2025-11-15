package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.Salmon
import com.android.sample.ui.theme.SpacingMedium

/**
 * Represents a single replacement item in the UI.
 *
 * @property title The text displayed on the card.
 * @property icon The icon representing the card.
 * @property tag The test tag used for UI testing.
 * @property onClick The action triggered when the card is clicked.
 */
data class ButtonItem(
    val title: String,
    val icon: ImageVector,
    val tag: String,
    val onClick: () -> Unit = {}
)

/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 *
 * @param item The [ButtonItem] containing display data.
 * @param onClick The callback executed when the card is clicked.
 */
@Composable
fun MainPageButton(item: ButtonItem, onClick: () -> Unit = {}) {
  Card(
      shape = RoundedCornerShape(CornerRadiusLarge),
      modifier = Modifier.fillMaxWidth().testTag(item.tag),
      onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = item.icon, contentDescription = null, tint = Salmon)
                Spacer(modifier = Modifier.width(SpacingMedium))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
              }
            }
      }
}
