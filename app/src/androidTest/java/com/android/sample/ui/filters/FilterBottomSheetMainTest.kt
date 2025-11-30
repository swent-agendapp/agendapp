package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.filters.FilterBottomSheet
import com.android.sample.ui.calendar.filters.FilterScreenTestTags
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

    // Main content container exists
    compose.onNodeWithTag(FilterScreenTestTags.FILTER_SHEET_CONTENT).assertExists()

    // Category: Event Type
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).assertExists()

    // Category: Location
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).assertExists()

    // Category: Participants
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).assertExists()
  }
}
