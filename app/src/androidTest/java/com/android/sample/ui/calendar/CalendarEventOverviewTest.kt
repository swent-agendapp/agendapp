package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreen
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreenTestTags
import com.android.sample.ui.calendar.eventOverview.EventOverviewViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [EventOverviewScreen].
 *
 * These tests focus on verifying that the screen shows its main structure (top bar and content
 * container) and that the back button correctly triggers the provided callback.
 */
class CalendarEventOverviewTest : FirebaseEmulatedTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private val selectedOrganizationId = "orgTest"
  private lateinit var repo: EventRepository

  @Before
  fun setup() {
    repo = EventRepositoryProvider.repository

    // Ensure the right organization is selected
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
  }

  @Test
  fun eventOverview_showsTopBarAndRootScreen() { // Arrange: create an in-memory repository and

    val event = createEvent(organizationId = selectedOrganizationId)[0]

    runBlocking { repo.insertEvent(orgId = selectedOrganizationId, item = event) }

    val viewModel = EventOverviewViewModel(repo)

    // Act: compose the EventOverviewScreen with our populated ViewModel
    composeTestRule.setContent {
      EventOverviewScreen(
          eventId = event.id,
          eventOverviewViewModel = viewModel,
      )
    }

    // Check if the screen container is displayed (by checking the root)
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.SCREEN_ROOT).assertIsDisplayed()

    // Check that the top bar is displayed
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.TOP_BAR).assertIsDisplayed()
  }

  @Test
  fun eventOverview_showsTitleDescriptionParticipantsAndSidebar() {
    val event =
        createEvent(
            organizationId = selectedOrganizationId,
            title = "Overviewed Event",
            description = "This is an event used to test the summary card.",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            participants = setOf("Alice", "Bob"))[0]

    runBlocking { repo.insertEvent(orgId = selectedOrganizationId, item = event) }

    val viewModel = EventOverviewViewModel(repo)

    // Act: compose the EventOverviewScreen with our populated ViewModel
    composeTestRule.setContent {
      EventOverviewScreen(
          eventId = event.id,
          eventOverviewViewModel = viewModel,
      )
    }

    // Assert: main EventSummaryCard sections are displayed
    composeTestRule.onNodeWithTag(EventSummaryCardTags.TITLE_TEXT).assertExists()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.DESCRIPTION_TEXT).assertExists()
    // later : uncomment when this will be correctly implemented
    //    composeTestRule.onNodeWithTag(EventSummaryCardTags.PARTICIPANTS_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.SIDE_BAR).assertExists()
  }

  @Test
  fun eventOverview_backButton_callsOnBackClick() {
    var backClicked = false

    val event =
        createEvent(
            organizationId = selectedOrganizationId,
            title = "Overviewed Event",
            description = "This is an event used to test the summary card.",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            participants = setOf("Alice", "Bob"))[0]

    runBlocking { repo.insertEvent(orgId = selectedOrganizationId, item = event) }

    val viewModel = EventOverviewViewModel(repo)

    // Act: compose the EventOverviewScreen with our populated ViewModel
    composeTestRule.setContent {
      EventOverviewScreen(
          eventId = event.id,
          onBackClick = { backClicked = true },
          eventOverviewViewModel = viewModel,
      )
    }

    // We trigger the back button
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.BACK_BUTTON).performClick()

    // Assert that the callback has been executed
    Assert.assertTrue(
        "Back callback should be triggered when clicking the back button", backClicked)
  }
}
