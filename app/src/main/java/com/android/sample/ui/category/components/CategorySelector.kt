package com.android.sample.ui.category.components

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.mockData.getMockEventCategory
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
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
    // Top-level field that always stays visible.
    CategorySelectorField(
        selectedCategory = selectedCategory,
        categoryLabel = categoryLabel,
        onClick = { expanded = true },
        testTag = testTag,
    )

    // Dropdown menu that shows all available categories.
    CategoryDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { category ->
          // Delegate the new category to the parent and close the menu.
          onCategorySelected(category)
          expanded = false
        },
        testTag = testTag,
    )
  }
}

@Composable
private fun CategorySelectorField(
    selectedCategory: EventCategory,
    categoryLabel: String,
    onClick: () -> Unit,
    testTag: String,
) {
  // This Row is styled to look like an OutlinedTextField-like form field.
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
              .clickable { onClick() }
              .padding(horizontal = PaddingMedium, vertical = PaddingSmall)
              .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        CategorySelectorValue(
            selectedCategory = selectedCategory,
            categoryLabel = categoryLabel,
            modifier = Modifier.fillMaxWidth().height(heightMedium).weight(WeightExtraHeavy),
        )
        // Small arrow on the right to indicate it is a dropdown field.
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
        )
      }
}

@Composable
private fun CategorySelectorValue(
    selectedCategory: EventCategory,
    categoryLabel: String,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically) {
        ColorCircle(
            color = selectedCategory.color,
            isSelected =
                false, // The field already shows the selection, no extra emphasis needed here.
        )
        Spacer(modifier = Modifier.width(SpacingMedium))
        Text(text = categoryLabel)
      }
}

@Composable
private fun CategoryDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    categories: List<EventCategory>,
    selectedCategory: EventCategory,
    onCategorySelected: (EventCategory) -> Unit,
    testTag: String,
) {
  DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
  ) {
    categories.forEachIndexed { index, category ->
      val isSelectedCategory = category == selectedCategory
      CategoryDropdownMenuItem(
          category = category,
          isSelected = isSelectedCategory,
          index = index,
          testTag = testTag,
          onClick = { onCategorySelected(category) },
      )
    }
  }
}

@Composable
private fun CategoryDropdownMenuItem(
    category: EventCategory,
    isSelected: Boolean,
    index: Int,
    testTag: String,
    onClick: () -> Unit,
) {
  DropdownMenuItem(
      // The whole row is clickable and represents one color option.
      text = {
        CategoryDropdownItemContent(
            category = category,
            isSelected = isSelected,
        )
      },
      onClick = onClick,
      modifier =
          if (testTag.isNotEmpty()) {
            // Pattern to make it easy to have one test tag per option.
            Modifier.testTag("${testTag}_option_$index")
          } else {
            Modifier
          },
  )
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
private fun CategoryDropdownItemContent(
    category: EventCategory,
    isSelected: Boolean,
) {
  val fontWeight =
      if (isSelected) {
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
            isSelected = isSelected,
        )
        Spacer(modifier = Modifier.width(SpacingSmall))
        Text(text = categoryLabel, fontWeight = fontWeight)
      }
}
