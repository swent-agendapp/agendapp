package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.filters.FilterBottomSheet
import com.android.sample.ui.calendar.filters.FilterScreenTestTags
import com.android.sample.ui.calendar.filters.FilterViewModel
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class FilterBottomSheetMainTest {

  @get:Rule val compose = createComposeRule()

  /** Ensures the bottom sheet main page loads correctly */
  @Test
  fun filterBottomSheet_mainPage_displaysCategoryItems() {
    compose.setContent {
      val filterVM = FilterViewModel()
      FilterBottomSheet(onDismiss = {}, onApply = {}, filterVM)
    }

    // Bottom sheet root exists
    compose.onNodeWithTag(CalendarScreenTestTags.FILTER_BOTTOM_SHEET).assertExists()
    compose.onNodeWithTag(CalendarScreenTestTags.FILTER_BOTTOM_SHEET).assertIsDisplayed()

    // Main content container exists
    compose.onNodeWithTag(FilterScreenTestTags.FILTER_SHEET_CONTENT).assertExists()
    compose.onNodeWithTag(FilterScreenTestTags.FILTER_SHEET_CONTENT).assertIsDisplayed()

    // Category: Event Type
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).assertExists()
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).assertIsDisplayed()

    // Category: Location
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).assertExists()
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).assertIsDisplayed()

    // Category: Participants
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).assertExists()
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).assertIsDisplayed()
  }

  /** Clicking "Event Type" navigates to EventType filter screen */
  @Test
  fun clickingEventType_opensEventTypeScreen() {
    compose.setContent {
      val filterVM = FilterViewModel()
      FilterBottomSheet(onDismiss = {}, onApply = {}, filterVM)
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).performClick()

    // Updated test tag
    compose.onNodeWithTag("EventTypeFilter_Screen").assertExists()
  }

  /** Clicking Location navigates to Location filter screen */
  @Test
  fun clickingLocation_opensLocationScreen() {
    compose.setContent {
      val filterVM = FilterViewModel()
      FilterBottomSheet(onDismiss = {}, onApply = {}, filterVM)
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).performClick()

    // Updated test tag
    compose.onNodeWithTag("LocationFilter_Screen").assertExists()
  }

  /** Clicking Participants navigates to Participant filter screen */
  @Test
  fun clickingParticipants_opensParticipantsScreen() {
    compose.setContent {
      val filterVM = FilterViewModel()
      FilterBottomSheet(onDismiss = {}, onApply = {}, filterVM)
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).performClick()

    // Updated test tag
    compose.onNodeWithTag("ParticipantFilter_Screen").assertExists()
  }
}
