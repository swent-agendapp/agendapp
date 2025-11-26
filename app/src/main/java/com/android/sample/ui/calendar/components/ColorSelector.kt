package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.android.sample.ui.theme.BorderWidthThick
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SizeMedium
import com.android.sample.ui.theme.heightMedium

/**
 * A color selector displayed as a form field.
 * - The parent controls the selected color via [selectedColor] and [onColorSelected].
 * - When closed, it looks like an OutlinedTextField-like box showing a single color circle.
 * - When opened, it shows a dropdown menu with all available colors as circles in a vertical list.
 *
 * This composable is intentionally built around [Color] only. Later, it can easily be adapted to
 * work with "tags" (label + color) instead of plain colors:
 * - Replace the [colors] list with a list of tag objects (e.g. EventTag(label, color)).
 * - Update the field content and the menu items to display both the circle and the label.
 */
@Composable
fun ColorSelector(
    modifier: Modifier = Modifier,
    selectedColor: Color = EventPalette.Blue,
    onColorSelected: (Color) -> Unit = {},
    testTag: String = "",
    colors: List<Color> = EventPalette.defaultColors
) {
  // Local state only controls whether the dropdown menu is open or closed.
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    // This Box is styled to look like an OutlinedTextField-like form field.
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(CornerRadiusLarge))
                .border(
                    width = BorderWidthThin,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(CornerRadiusLarge))
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(CornerRadiusLarge))
                .clickable { expanded = true }
                .padding(horizontal = PaddingMedium, vertical = PaddingSmall)
                .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
          // Center area shows the currently selected color.
          // Later, when using tags, this can show both the color circle and the tag label.
          Box(modifier = modifier.height(heightMedium), contentAlignment = Alignment.Center) {
            ColorCircle(
                color = selectedColor,
                isSelected =
                    false, // The field already shows the selection, no extra emphasis needed here.
            )
          }

          // Small arrow on the right to indicate it is a dropdown field.
          Icon(
              imageVector = Icons.Filled.ArrowDropDown,
              contentDescription = null,
          )
        }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
      colors.forEachIndexed { index, color ->
        DropdownMenuItem(
            // The whole row is clickable and represents one color option.
            text = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    // For the selected color, we draw a thicker border to highlight it.
                    ColorCircle(
                        color = color,
                        isSelected = color == selectedColor,
                    )

                    // When switching to tags later, a tag label Text can be placed next to the
                    // circle:
                    // Text(text = tag.label) for example.
                  }
            },
            onClick = {
              // Delegate the new color to the parent.
              onColorSelected(color)
              expanded = false
            },
            modifier =
                if (testTag.isNotEmpty()) {
                  // Pattern to make it easy to have one test tag per option.
                  Modifier.testTag("${testTag}_option_$index")
                } else {
                  Modifier
                },
        )
      }
    }
  }
}

/**
 * Small helper composable that renders a color as a circle.
 *
 * When [isSelected] is true, the border is thicker to visually highlight the selected option.
 *
 * In the future, this helper can stay the same even if the selector becomes a "tag selector": only
 * the callers need to change, not this drawing logic.
 */
@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
  Box(
      modifier =
          modifier
              .size(SizeMedium)
              .clip(CircleShape)
              .background(color)
              .border(
                  width = if (isSelected) BorderWidthThick else BorderWidthThin,
                  color = MaterialTheme.colorScheme.onSurface,
                  shape = CircleShape),
  )
}
