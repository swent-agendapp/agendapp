package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.filters.EventTypeFilterScreen
import com.android.sample.ui.calendar.filters.FilterScreenTestTags
import org.junit.Rule
import org.junit.Test

class EventTypeFilterScreenTest {

  @get:Rule val compose = createComposeRule()

  /** Ensures the EventTypeFilterScreen displays correctly */
  @Test
  fun eventTypeScreen_displaysHeaderAndList() {
    compose.setContent { EventTypeFilterScreen(selected = emptyList(), onBack = {}, onApply = {}) }

    // Screen root exists
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_SCREEN).assertExists()

    // Header exists
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_HEADER).assertExists()

    // Title exists
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_TITLE).assertExists()

    // Back button exists
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_BACK_BUTTON).assertExists()

    // List container exists
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_LIST).assertExists()
  }

  /** Ensures a checkbox item renders and toggles correctly */
  @Test
  fun eventTypeScreen_checkbox_togglesSelection() {
    compose.setContent { EventTypeFilterScreen(selected = emptyList(), onBack = {}, onApply = {}) }

    // Pick one of the types (e.g., "Conference")
    val itemTag = FilterScreenTestTags.EVENT_TYPE_ITEM_PREFIX + "Conference"

    // Ensure item exists
    compose.onNodeWithTag(itemTag).assertExists()

    // Find checkbox inside item
    val checkbox = compose.onNode(hasAnyAncestor(hasTestTag(itemTag)) and hasClickAction())

    // Toggle ON
    checkbox.performClick()
    checkbox.assertIsOn()

    // Toggle OFF
    checkbox.performClick()
    checkbox.assertIsOff()
  }

  /** Clear button clears all selections */
  @Test
  fun eventTypeScreen_clearButton_clearsSelections() {
    // A captured applied result
    var result: List<String>? = null // to hold onApply result

    compose.setContent {
      EventTypeFilterScreen(selected = listOf("Course"), onBack = {}, onApply = { result = it })
    }

    // Course checkbox should initially be on
    val courseItemTag = FilterScreenTestTags.EVENT_TYPE_ITEM_PREFIX + "Course"
    val courseCheckbox =
        compose.onNode(hasAnyAncestor(hasTestTag(courseItemTag)) and hasClickAction())
    courseCheckbox.assertIsOn()

    // Click CLEAR
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_CLEAR_BUTTON).performClick()

    // Now checkbox should be OFF
    courseCheckbox.assertIsOff()
  }

  /** Apply returns current selections */
  @Test
  fun eventTypeScreen_applyButton_returnsSelections() {
    var appliedSelections: List<String>? = null

    compose.setContent {
      EventTypeFilterScreen(
          selected = emptyList(), onBack = {}, onApply = { appliedSelections = it })
    }

    // Toggle "Workshop"
    val workshopTag = FilterScreenTestTags.EVENT_TYPE_ITEM_PREFIX + "Workshop"
    val workshopCheckbox =
        compose.onNode(hasAnyAncestor(hasTestTag(workshopTag)) and hasClickAction())

    workshopCheckbox.performClick() // turn on

    // Click APPLY
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_APPLY_BUTTON).performClick()

    // Assert appliedSelections contains "Workshop"
    assert(appliedSelections == listOf("Workshop")) {
      "Expected [Workshop], but got $appliedSelections"
    }
  }
}
