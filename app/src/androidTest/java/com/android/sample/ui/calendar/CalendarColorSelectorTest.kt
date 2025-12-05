package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.addEvent.AddEventTestTags.CATEGORY_SELECTOR
import com.android.sample.ui.calendar.components.CategorySelector
import com.android.sample.ui.theme.EventPalette
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Assisted by AI

class CategorySelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val categories =
      listOf(
          EventCategory(label = "Category 1", color = EventPalette.Green),
          EventCategory(label = "Category 2", color = EventPalette.Blue),
          EventCategory(label = "Category 3", color = EventPalette.Pink),
      )

  @Test
  fun initialState_shouldShowMainField_andHideMenuOptions() {
    composeTestRule.setContent {
      CategorySelector(
          selectedCategory = categories[0],
          onCategorySelected = {},
          testTag = CATEGORY_SELECTOR,
          categories = categories,
      )
    }

    // Main field is visible
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).assertExists().assertIsDisplayed()

    // No option is visible before opening the menu
    composeTestRule.onNodeWithTag("${CATEGORY_SELECTOR}_option_0").assertDoesNotExist()
  }

  @Test
  fun clickingField_shouldOpenMenu_andShowAllOptions() {
    composeTestRule.setContent {
      CategorySelector(
          selectedCategory = categories[0],
          onCategorySelected = {},
          testTag = CATEGORY_SELECTOR,
          categories = categories,
      )
    }

    // Open the dropdown menu by clicking on the main field
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).performClick()

    // All options should now be visible
    categories.indices.forEach { index ->
      composeTestRule
          .onNodeWithTag("${CATEGORY_SELECTOR}_option_$index")
          .assertExists()
          .assertIsDisplayed()
    }
  }

  @Test
  fun selectingOption_shouldCallOnColorSelected_andCloseMenu() {
    var selectedCategory = categories[0]

    composeTestRule.setContent {
      CategorySelector(
          selectedCategory = selectedCategory,
          onCategorySelected = { newCategory -> selectedCategory = newCategory },
          testTag = CATEGORY_SELECTOR,
          categories = categories,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).performClick()

    // Select the second option (index 1)
    composeTestRule.onNodeWithTag("${CATEGORY_SELECTOR}_option_1").performClick()

    // Menu should be closed (option no longer visible)
    composeTestRule.onNodeWithTag("${CATEGORY_SELECTOR}_option_1").assertDoesNotExist()

    // Callback should have been called with the correct category
    assertEquals(categories[1], selectedCategory)
  }

  @Test
  fun clickingFieldTwice_shouldStayStable_andFieldShouldRemainVisible() {
    composeTestRule.setContent {
      CategorySelector(
          selectedCategory = categories[0],
          onCategorySelected = {},
          testTag = CATEGORY_SELECTOR,
          categories = categories,
      )
    }

    // Click 1 — open
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).performClick()

    // Click 2 — should not crash and field must remain visible
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).performClick()

    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).assertExists().assertIsDisplayed()
  }

  @Test
  fun emptyColorList_shouldNotCrash_andShowNoOptions() {
    val emptyCategories = emptyList<EventCategory>()

    composeTestRule.setContent {
      CategorySelector(
          selectedCategory = EventCategory.defaultCategory(),
          onCategorySelected = {},
          testTag = CATEGORY_SELECTOR,
          categories = emptyCategories,
      )
    }

    // Open menu
    composeTestRule.onNodeWithTag(CATEGORY_SELECTOR).performClick()

    // There is no option_0 when the list is empty
    composeTestRule.onNodeWithTag("${CATEGORY_SELECTOR}_option_0").assertDoesNotExist()
  }
}
