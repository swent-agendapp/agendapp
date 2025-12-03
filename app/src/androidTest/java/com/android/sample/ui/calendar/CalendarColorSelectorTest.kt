package com.android.sample.ui.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.addEvent.AddEventTestTags.COLOR_SELECTOR
import com.android.sample.ui.calendar.components.ColorSelector
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Assisted by AI

class ColorSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val colors = listOf(Color.Red, Color.Green, Color.Blue)

  @Test
  fun initialState_shouldShowMainField_andHideMenuOptions() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = COLOR_SELECTOR,
          colors = colors,
      )
    }

    // Main field is visible
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).assertExists().assertIsDisplayed()

    // No option is visible before opening the menu
    composeTestRule.onNodeWithTag("${COLOR_SELECTOR}_option_0").assertDoesNotExist()
  }

  @Test
  fun clickingField_shouldOpenMenu_andShowAllOptions() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = COLOR_SELECTOR,
          colors = colors,
      )
    }

    // Open the dropdown menu by clicking on the main field
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).performClick()

    // All options should now be visible
    colors.indices.forEach { index ->
      composeTestRule
          .onNodeWithTag("${COLOR_SELECTOR}_option_$index")
          .assertExists()
          .assertIsDisplayed()
    }
  }

  @Test
  fun selectingOption_shouldCallOnColorSelected_andCloseMenu() {
    var selectedColor = colors[0]

    composeTestRule.setContent {
      ColorSelector(
          selectedColor = selectedColor,
          onColorSelected = { newColor -> selectedColor = newColor },
          testTag = COLOR_SELECTOR,
          colors = colors,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).performClick()

    // Select the second option (index 1)
    composeTestRule.onNodeWithTag("${COLOR_SELECTOR}_option_1").performClick()

    // Menu should be closed (option no longer visible)
    composeTestRule.onNodeWithTag("${COLOR_SELECTOR}_option_1").assertDoesNotExist()

    // Callback should have been called with the correct color
    assertEquals(colors[1], selectedColor)
  }

  @Test
  fun clickingFieldTwice_shouldStayStable_andFieldShouldRemainVisible() {
    composeTestRule.setContent {
      ColorSelector(
          selectedColor = colors[0],
          onColorSelected = {},
          testTag = COLOR_SELECTOR,
          colors = colors,
      )
    }

    // Click 1 — open
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).performClick()

    // Click 2 — should not crash and field must remain visible
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).performClick()

    composeTestRule.onNodeWithTag(COLOR_SELECTOR).assertExists().assertIsDisplayed()
  }

  @Test
  fun emptyColorList_shouldNotCrash_andShowNoOptions() {
    val emptyColors = emptyList<Color>()

    composeTestRule.setContent {
      ColorSelector(
          selectedColor = Color.Red,
          onColorSelected = {},
          testTag = COLOR_SELECTOR,
          colors = emptyColors,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(COLOR_SELECTOR).performClick()

    // There is no option_0 when the list is empty
    composeTestRule.onNodeWithTag("${COLOR_SELECTOR}_option_0").assertDoesNotExist()
  }
}
