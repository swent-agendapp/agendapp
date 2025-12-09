package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.filters.FilterListScreen
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class EventTypeFilterScreenTest {
  @get:Rule val compose = createComposeRule()

  /** Ensures the EventTypeFilterScreen displays correctly */
  @Test
  fun eventTypeScreen_displaysHeaderAndList() {
    compose.setContent {
      FilterListScreen(
          title = "Event Type",
          items = listOf("Course", "Workshop", "Conference"),
          selected = emptyList(),
          onBack = {},
          onApply = {},
          testTagPrefix = "EventTypeFilter")
    }

    compose.onNodeWithTag("EventTypeFilter_Screen").assertExists()
    compose.onNodeWithTag("EventTypeFilter_Header").assertExists()
    compose.onNodeWithTag("EventTypeFilter_Title").assertExists()
    compose.onNodeWithTag("EventTypeFilter_BackButton").assertExists()
    compose.onNodeWithTag("EventTypeFilter_List").assertExists()
  }

  /** Ensures a checkbox item renders and toggles correctly */
  @Test
  fun eventTypeScreen_checkbox_togglesSelection() {
    compose.setContent {
      FilterListScreen(
          title = "Event Type",
          items = listOf("Conference"),
          selected = emptyList(),
          onBack = {},
          onApply = {},
          testTagPrefix = "EventTypeFilter")
    }

    val itemTag = "EventTypeFilter_Item_Conference"
    compose.onNodeWithTag(itemTag).assertExists()

    val checkbox = compose.onNode(hasAnyAncestor(hasTestTag(itemTag)) and hasClickAction())

    checkbox.performClick()
    checkbox.assertIsOn()

    checkbox.performClick()
    checkbox.assertIsOff()
  }

  /** Clear button clears all selections */
  @Test
  fun eventTypeScreen_clearButton_clearsSelections() {
    var result: List<String>? = null

    compose.setContent {
      FilterListScreen(
          title = "Event Type",
          items = listOf("Course"),
          selected = listOf("Course"),
          onBack = {},
          onApply = { result = it },
          testTagPrefix = "EventTypeFilter")
    }

    val courseItemTag = "EventTypeFilter_Item_Course"
    val checkbox = compose.onNode(hasAnyAncestor(hasTestTag(courseItemTag)) and hasClickAction())

    checkbox.assertIsOn()

    compose.onNodeWithTag("EventTypeFilter_Clear").performClick()

    checkbox.assertIsOff()
  }

  /** Apply returns current selections */
  @Test
  fun eventTypeScreen_applyButton_returnsSelections() {
    var appliedSelections: List<String>? = null

    compose.setContent {
      FilterListScreen(
          title = "Event Type",
          items = listOf("Workshop"),
          selected = emptyList(),
          onBack = {},
          onApply = { appliedSelections = it },
          testTagPrefix = "EventTypeFilter")
    }

    val workshopTag = "EventTypeFilter_Item_Workshop"
    val checkbox = compose.onNode(hasAnyAncestor(hasTestTag(workshopTag)) and hasClickAction())

    checkbox.performClick()

    compose.onNodeWithTag("EventTypeFilter_Apply").performClick()

    assert(appliedSelections == listOf("Workshop")) {
      "Expected [Workshop], but got $appliedSelections"
    }
  }
}
