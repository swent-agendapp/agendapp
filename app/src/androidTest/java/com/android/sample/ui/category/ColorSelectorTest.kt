package com.android.sample.ui.category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.common.ColorSelector
import com.android.sample.ui.common.ColorSelectorTestTags
import com.android.sample.ui.theme.EventPalette
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Assisted by AI

class ColorSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val colors: List<Color> =
      listOf(
          EventPalette.LightGreen,
          EventPalette.Blue,
          EventPalette.Pink,
      )

  private val baseTag = ColorSelectorTestTags.BASE
  private val gridTag = "${baseTag}_${ColorSelectorTestTags.GRID_SUFFIX}"

  @Test
  fun initialState_shouldShowMainField_andHideGridAndOptions() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = baseTag,
          colors = colors,
      )
    }

    // Main field is visible
    composeTestRule.onNodeWithTag(baseTag).assertExists().assertIsDisplayed()

    // Grid and options are not visible before opening the menu
    composeTestRule.onNodeWithTag(gridTag).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag("${baseTag}_${ColorSelectorTestTags.COLOR_PREFIX}_0")
        .assertDoesNotExist()
  }

  @Test
  fun clickingField_shouldOpenMenu_andShowGridAndAllOptions() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = baseTag,
          colors = colors,
      )
    }

    // Open the dropdown menu by clicking on the main field
    composeTestRule.onNodeWithTag(baseTag).performClick()

    // Grid should now be visible
    composeTestRule.onNodeWithTag(gridTag).assertExists().assertIsDisplayed()

    // All color options should now be visible
    colors.indices.forEach { index ->
      val optionTag = "${baseTag}_${ColorSelectorTestTags.COLOR_PREFIX}_$index"
      composeTestRule.onNodeWithTag(optionTag).assertExists().assertIsDisplayed()
    }
  }

  @Test
  fun selectingOption_shouldCallOnColorSelected_andCloseMenu() {
    var selectedColor: Color = colors[0]

    composeTestRule.setContent {
      ColorSelector(
          selectedColor = selectedColor,
          onColorSelected = { newColor -> selectedColor = newColor },
          testTag = baseTag,
          colors = colors,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(baseTag).performClick()

    // Select the second option (index 1)
    val optionTag = "${baseTag}_${ColorSelectorTestTags.COLOR_PREFIX}_1"
    composeTestRule.onNodeWithTag(optionTag).performClick()

    // Menu should be closed (option no longer visible)
    composeTestRule.onNodeWithTag(optionTag).assertDoesNotExist()

    // Callback should have been called with the correct color
    assertEquals(colors[1], selectedColor)
  }

  @Test
  fun clickingFieldTwice_shouldStayStable_andFieldShouldRemainVisible() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = baseTag,
          colors = colors,
      )
    }

    // Click 1 — open
    composeTestRule.onNodeWithTag(baseTag).performClick()

    // Click 2 — should not crash and field must remain visible
    composeTestRule.onNodeWithTag(baseTag).performClick()

    composeTestRule.onNodeWithTag(baseTag).assertExists().assertIsDisplayed()
  }

  @Test
  fun emptyColorList_shouldNotCrash_andShowNoOptions() {
    val emptyColors = emptyList<Color>()

    composeTestRule.setContent {
      ColorSelector(
          selectedColor = EventPalette.NoCategory,
          onColorSelected = {},
          testTag = baseTag,
          colors = emptyColors,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(baseTag).performClick()

    // There is no color_0 when the list is empty
    composeTestRule
        .onNodeWithTag("${baseTag}_${ColorSelectorTestTags.COLOR_PREFIX}_0")
        .assertDoesNotExist()
  }
}
