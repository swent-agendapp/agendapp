package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.authentication.User
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.filters.FilterBottomSheet
import com.android.sample.ui.calendar.filters.FilterScreenTestTags
import com.android.sample.ui.theme.EventPalette
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class FilterBottomSheetMainTest {

  @get:Rule val compose = createComposeRule()

  private val fakeUsers =
      listOf(
          User(
              id = "u1",
              displayName = "Alice",
              email = "alice@test.com",
              phoneNumber = null,
              organizations = emptyList()),
          User(
              id = "u2",
              displayName = "Bob",
              email = "bob@test.com",
              phoneNumber = null,
              organizations = emptyList()),
      )

  private val fakeCategories =
      listOf(
          EventCategory(
              id = "c1",
              organizationId = "org1",
              label = "Course",
              isDefault = false,
              color = EventPalette.Purple),
          EventCategory(
              id = "c2",
              organizationId = "org1",
              label = "Meeting",
              isDefault = false,
              color = EventPalette.Purple),
      )

  /** Ensures the bottom sheet main page loads correctly */
  @Test
  fun filterBottomSheet_mainPage_displaysCategoryItems() {
    compose.setContent {
      FilterBottomSheet(
          users = fakeUsers, categories = fakeCategories, onDismiss = {}, onApply = {})
    }

    compose
        .onNodeWithTag(CalendarScreenTestTags.FILTER_BOTTOM_SHEET)
        .assertExists()
        .assertIsDisplayed()
    compose
        .onNodeWithTag(FilterScreenTestTags.FILTER_SHEET_CONTENT)
        .assertExists()
        .assertIsDisplayed()

    compose
        .onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE)
        .assertExists()
        .assertIsDisplayed()
    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).assertExists().assertIsDisplayed()
    compose
        .onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS)
        .assertExists()
        .assertIsDisplayed()
  }

  /** Clicking "Event Type" navigates to EventType filter screen */
  @Test
  fun clickingEventType_opensEventTypeScreen() {
    compose.setContent {
      FilterBottomSheet(
          users = fakeUsers, categories = fakeCategories, onDismiss = {}, onApply = {})
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_EVENT_TYPE).performClick()
    compose.onNodeWithTag("EventTypeFilter_Screen").assertExists()
  }

  /** Clicking Location navigates to Location filter screen */
  @Test
  fun clickingLocation_opensLocationScreen() {
    compose.setContent {
      FilterBottomSheet(
          users = fakeUsers, categories = fakeCategories, onDismiss = {}, onApply = {})
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_LOCATION).performClick()
    compose.onNodeWithTag("LocationFilter_Screen").assertExists()
  }

  /** Clicking Participants navigates to Participant filter screen */
  @Test
  fun clickingParticipants_opensParticipantsScreen() {
    compose.setContent {
      FilterBottomSheet(
          users = fakeUsers, categories = fakeCategories, onDismiss = {}, onApply = {})
    }

    compose.onNodeWithTag(FilterScreenTestTags.CATEGORY_PARTICIPANTS).performClick()
    compose.onNodeWithTag("ParticipantFilter_Screen").assertExists()
  }
}
