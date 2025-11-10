package com.android.sample.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

/**
 * Represents an item in the bottom navigation bar.
 *
 * @param icon The icon to display for the item.
 * @param onClick The action to perform when the item is clicked.
 * @param contentDescription The content description for accessibility.
 * @param isSelected Whether the item is currently selected.
 */
data class BottomBarItem(
    val icon: ImageVector = Icons.Default.Home,
    val label: String = "",
    val route: String = "",
    val onClick: () -> Unit = {},
    val contentDescription: String? = null,
    val isSelected: Boolean = false
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
  NavigationBar(modifier = modifier) {
    items.forEach { item ->
      NavigationBarItem(
          selected = item.isSelected,
          onClick = item.onClick,
          icon = { Icon(imageVector = item.icon, contentDescription = item.contentDescription) },
          label = { Text(text = item.label) })
    }
  }
}

/** Preview of the BottomBar composable with sample items. */
@Preview
@Composable
fun BottomBarPreview() {
  val items =
      listOf(
          BottomBarItem(
              icon = Icons.Default.Home,
              onClick = {},
              contentDescription = "Home",
              isSelected = true,
              label = "Home"),
          BottomBarItem(
              icon = Icons.Default.Settings,
              onClick = {},
              contentDescription = "Settings",
              isSelected = false,
              label = "Settings"))
  BottomBar(items = items, modifier = Modifier)
}
