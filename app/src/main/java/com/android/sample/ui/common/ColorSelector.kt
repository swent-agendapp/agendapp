package com.android.sample.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.components.ColorCircle
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall

private const val GRID_MAX_COLUMNS = 4

object ColorSelectorTestTags {
  const val BASE = "color_selector"
  const val GRID_SUFFIX = "grid"
  const val COLOR_PREFIX = "color"
}

// Assisted by AI

/**
 * A color selector displayed as a form field.
 *
 * The parent controls the selected color via [selectedColor] and [onColorSelected].
 * - When closed, it looks like an OutlinedTextField-like box showing the currently selected color
 *   as a circle together with a dropdown arrow.
 * - When opened, it shows a dropdown menu with a grid of all available [colors].
 *
 * The list of colors is provided by [colors] and defaults to [EventPalette.defaultColors].
 *
 * This composable only manages the open/close state of the dropdown and delegates color changes to
 * [onColorSelected].
 */
@Composable
fun ColorSelector(
    modifier: Modifier = Modifier,
    selectedColor: Color = EventPalette.NoCategory,
    onColorSelected: (Color) -> Unit = {},
    testTag: String = ColorSelectorTestTags.BASE,
    colors: List<Color> = EventPalette.defaultColors,
) {
  // Local state only controls whether the dropdown menu is open or closed.
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    // Top-level field that always stays visible.
    ColorSelectorField(
        selectedColor = selectedColor,
        onClick = { expanded = true },
        testTag = testTag,
    )

    // Dropdown menu that shows all available colors.
    ColorDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        colors = colors,
        selectedColor = selectedColor,
        onColorSelected = { color ->
          // Delegate the new color to the parent and close the menu.
          onColorSelected(color)
          expanded = false
        },
        testTag = testTag,
    )
  }
}

@Composable
private fun ColorSelectorField(
    selectedColor: Color = EventCategory.defaultCategory().color,
    onClick: () -> Unit = {},
    testTag: String = "",
) {
  // This Row is styled to look like an OutlinedTextField-like form field.
  Row(
      modifier =
          Modifier.heightIn(min = TextFieldDefaults.MinHeight)
              .clip(RoundedCornerShape(CornerRadiusLarge))
              .border(
                  width = BorderWidthThin,
                  color = MaterialTheme.colorScheme.outline,
                  shape = RoundedCornerShape(CornerRadiusLarge),
              )
              .background(
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(CornerRadiusLarge),
              )
              .clickable { onClick() }
              .padding(horizontal = PaddingMedium, vertical = PaddingSmall)
              .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    ColorCircle(
        color = selectedColor,
        isSelected = false,
    )
    // Small arrow on the right to indicate it is a dropdown field.
    Icon(
        imageVector = Icons.Filled.ArrowDropDown,
        contentDescription = null,
    )
  }
}

@Composable
private fun ColorDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    testTag: String,
) {
  DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
  ) {
    ColorGrid(
        colors = colors,
        selectedColor = selectedColor,
        onColorSelected = onColorSelected,
        testTag = testTag,
    )
  }
}

@Composable
private fun ColorGrid(
    colors: List<Color> = EventPalette.defaultColors,
    selectedColor: Color = EventCategory.defaultCategory().color,
    onColorSelected: (Color) -> Unit = {},
    testTag: String = "",
) {
  Box(
      modifier =
          Modifier.testTag("${testTag}_${ColorSelectorTestTags.GRID_SUFFIX}")
              .padding(PaddingMedium),
      contentAlignment = Alignment.Center,
  ) {
    // Split into rows of up to GRID_MAX_COLUMNS items
    val rows: List<List<Color>> = colors.chunked(GRID_MAX_COLUMNS)

    Column(
        verticalArrangement = Arrangement.spacedBy(PaddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      rows.forEachIndexed { rowIndex, rowColors ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          rowColors.forEachIndexed { columnIndex, color ->
            val index = rowIndex * GRID_MAX_COLUMNS + columnIndex

            Box(
                modifier =
                    Modifier.clickable { onColorSelected(color) }
                        .testTag("${testTag}_${ColorSelectorTestTags.COLOR_PREFIX}_$index"),
                contentAlignment = Alignment.Center,
            ) {
              ColorCircle(
                  color = color,
                  isSelected = color == selectedColor,
              )
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ColorSelectorPreview() {
  MaterialTheme {
    ColorSelector(
        selectedColor = EventPalette.defaultColors.first(),
        onColorSelected = {},
        testTag = "colorSelectorPreview",
    )
  }
}
