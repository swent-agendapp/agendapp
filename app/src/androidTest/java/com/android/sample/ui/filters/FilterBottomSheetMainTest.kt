package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.filters.FilterBottomSheet
import com.android.sample.ui.calendar.filters.FilterScreenTestTags
import com.android.sample.ui.calendar.filters.LocationFilterTestTags
import com.android.sample.ui.calendar.filters.ParticipantFilterTestTags
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class FilterBottomSheetMainTest {

  @get:Rule val compose = createComposeRule()

  /** Ensures the bottom sheet main page loads correctly */
  @Test
  fun filterBottomSheet_mainPage_displaysCategoryItems() {
    compose.setContent { FilterBottomSheet(onDismiss = {}, onApply = {}) }

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

  /** Clicking "Event Type" navigates to EventTypeFilterScreen */
  @Test
  fun clickingEventType_opensEventTypeScreen() {
    compose.setContent { FilterBottomSheet(onDismiss = {}, onApply = {}) }

    // Click Event Type
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).performClick()

    // Should now show EventType screen
    compose.onNodeWithTag(FilterScreenTestTags.EVENT_TYPE_SCREEN).assertExists()
  }

  /** Clicking Location navigates to LocationFilterScreen */
  @Test
  fun clickingLocation_opensLocationScreen() {
    compose.setContent { FilterBottomSheet(onDismiss = {}, onApply = {}) }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).performClick()

    compose.onNodeWithTag(LocationFilterTestTags.SCREEN).assertExists()
  }

  /** Clicking Participants navigates to ParticipantFilterScreen */
  @Test
  fun clickingParticipants_opensParticipantsScreen() {
    compose.setContent { FilterBottomSheet(onDismiss = {}, onApply = {}) }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).performClick()

    compose.onNodeWithTag(ParticipantFilterTestTags.SCREEN).assertExists()
  }
}
