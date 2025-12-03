package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.mockData.getMockEventCategory
import com.android.sample.ui.theme.BorderWidthThick
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SizeMedium
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.heightMedium

// Assisted by AI

/**
 * A category selector displayed as a form field.
 * - The parent controls the selected category via [selectedCategory] and [onCategorySelected].
 * - When closed, it looks like an OutlinedTextField-like box showing the selected category label
 *   together with its color.
 * - When opened, it shows a dropdown menu listing all [categories] with their label and color.
 *
 * The selector works with [EventCategory] objects:
 * - The [EventCategory.color] is rendered as the colored circle.
 * - The [EventCategory.label] is shown next to the circle, except for the default category where a
 *   localized string (see `R.string.default_category_label`) is displayed instead.
 *
 * For now, [categories] are populated with mock data of our stakeholder.
 */
@Composable
fun CategorySelector(
    modifier: Modifier = Modifier,
    selectedCategory: EventCategory = EventCategory.defaultCategory(),
    onCategorySelected: (EventCategory) -> Unit = {},
    testTag: String = "",
    categories: List<EventCategory> = getMockEventCategory()
) {
  // Local state only controls whether the dropdown menu is open or closed.
  var expanded by remember { mutableStateOf(false) }

  val categoryLabel =
      if (selectedCategory.isDefault) {
        stringResource(R.string.default_category_label)
      } else selectedCategory.label

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
          Row(
              modifier = Modifier.fillMaxWidth().height(heightMedium).weight(WeightExtraHeavy),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically) {
                ColorCircle(
                    color = selectedCategory.color,
                    isSelected =
                        false, // The field already shows the selection, no extra emphasis needed
                    // here.
                )
                Spacer(modifier = Modifier.width(SpacingMedium))
                Text(text = categoryLabel)
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
      categories.forEachIndexed { index, category ->
        DropdownMenuItem(
            // The whole row is clickable and represents one color option.
            text = {
              val isSelectedCategory = category == selectedCategory
              val fontWeight =
                  if (isSelectedCategory) {
                    FontWeight.ExtraBold
                  } else {
                    FontWeight.Medium
                  }
              val categoryLabel =
                  if (category.isDefault) {
                    stringResource(R.string.default_category_label)
                  } else category.label
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically) {
                    // For the selected category, we draw a thicker border to highlight it.
                    ColorCircle(
                        color = category.color,
                        isSelected = isSelectedCategory,
                    )
                    Spacer(modifier = Modifier.width(SpacingSmall))
                    Text(text = categoryLabel, fontWeight = fontWeight)
                  }
            },
            onClick = {
              // Delegate the new color to the parent.
              onCategorySelected(category)
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
