package com.android.sample.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

/** Test tags for BottomBar and its items. */
object BottomBarTestTags {
  const val BOTTOM_BAR = "bottom_bar"
  const val ITEM_CALENDAR = "bottom_bar_item_calendar"
  const val ITEM_REPLACEMENT = "bottom_bar_item_replacement"
  const val ITEM_SETTINGS = "bottom_bar_item_settings"
}

/**
 * Represents an item in the bottom navigation bar.
 *
 * @param icon The icon to display for the item.
 * @param onClick The action to perform when the item is clicked.
 * @param contentDescription The content description for accessibility.
 * @param isSelected Whether the item is currently selected.
 */
data class BottomBarItem(
    val icon: ImageVector? = null,
    val label: String = "",
    val route: String = "",
    val onClick: () -> Unit = {},
    val contentDescription: String? = null,
    val isSelected: Boolean = false,
    val testTag: String = "",
)

/**
 * Composable function to create a bottom navigation bar.
 *
 * @param items The list of [BottomBarItem] to display in the bottom bar.
 * @param modifier The modifier to be applied to the bottom bar.
 */
@Composable
fun BottomBar(
    items: List<BottomBarItem>,
    modifier: Modifier = Modifier,
) {
  NavigationBar(modifier = modifier.testTag(BottomBarTestTags.BOTTOM_BAR)) {
    items.forEach { item ->
      NavigationBarItem(
          selected = item.isSelected,
          onClick = item.onClick,
          icon = {
            Icon(
                imageVector = item.icon ?: Icons.Default.Warning,
                contentDescription = item.contentDescription)
          },
          label = { Text(text = item.label) },
          modifier = modifier.testTag(item.testTag))
    }
  }
}
