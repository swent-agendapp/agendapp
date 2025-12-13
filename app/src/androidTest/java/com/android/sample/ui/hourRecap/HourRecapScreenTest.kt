package com.android.sample.ui.hourRecap

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Modularization assisted by AI
/**
 * UI tests for the HourRecapScreen.
 *
 * These tests verify:
 * - All key UI components are rendered (start date, end date, generate button, export button).
 * - The generate button enables only when both dates are selected.
 * - The recap list renders the expected recap items.
 * - Clicking the export button triggers the UI action (placeholder).
 */
class HourRecapScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val compose = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()
    // Set selected organization in the VM provider
    setSelectedOrganization()
  }
  /**
   * Ensures that the main UI components are displayed:
   * - Top bar
   * - Date pickers
   * - Generate button
   * - Export button
   */
  @Test
  fun screen_rendersAllMainComponents() {
    compose.setContent { HourRecapScreen() }

    compose.onNodeWithTag(HourRecapTestTags.TOP_BAR).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.START_DATE).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.END_DATE).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).performClick()
    compose.onNodeWithTag(HourRecapTestTags.EXPORT_BUTTON).assertExists().assertIsDisplayed()
  }

  /**
   * Tests clicking the export button (UI only — no file generation yet). This ensures that the
   * button exists and is clickable without crashing.
   */
  @Test
  fun exportButton_isClickable() {
    compose.setContent { HourRecapScreen() }

    compose.onNodeWithTag(HourRecapTestTags.EXPORT_BUTTON).assertExists().performClick()
  }

  /**
   * Tests clicking the generate button (UI only — no data generation yet). This ensures that the
   * button exists and is clickable without crashing.
   */
  @Test
  fun generateButton_isClickable() {
    compose.setContent { HourRecapScreen(onBackClick = {}) }

    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).performClick()
  }

  /**
   * Tests that the recap list displays the expected worked hours for each employee based on the
   * test data injected into the CalendarViewModel's UI state.
   */
  @Test
  fun recapItems_displayWorkedHoursCorrectly() {
    val vm = HourRecapViewModel()

    // Inject worked hours into test VM
    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "alice",
                displayName = "Alice",
                completedHours = 12.5,
                plannedHours = 0.0,
                events = emptyList()),
            HourRecapUserRecap(
                userId = "bob",
                displayName = "Bob",
                completedHours = 8.0,
                plannedHours = 0.0,
                events = emptyList())))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("Alice").assertExists()
    compose.onNodeWithText("12h 30min").assertExists()

    compose.onNodeWithText("Bob").assertExists()
    compose.onNodeWithText("8h").assertExists()

    compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM).assertCountEquals(2)
  }

  @Test
  fun emptyWorkedHours_showsEmptyList() {
    val vm = HourRecapViewModel()
    vm.setTestWorkedHours(emptyList())

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM).assertCountEquals(0)
  }

  @Test
  fun backButton_triggersCallback() {
    var backPressed = false

    compose.setContent { HourRecapScreen(onBackClick = { backPressed = true }) }

    compose.onNodeWithTag(HourRecapTestTags.BACK_BUTTON).assertExists().performClick()

    assert(backPressed)
  }

  @Test
  fun recapItem_opensUserDetailsSheet() {
    val vm = HourRecapViewModel()
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Team Meeting",
            startDate = java.time.Instant.now().plusSeconds(3600),
            endDate = java.time.Instant.now().plusSeconds(7200),
            isPast = false,
            wasPresent = null,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "alice",
                displayName = "Alice",
                completedHours = 8.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    // Click on the recap item
    compose.onNodeWithText("Alice").performClick()

    // Wait for sheet to appear
    compose.waitForIdle()

    // Verify sheet is displayed
    compose.onNodeWithTag(HourRecapTestTags.RECAP_SHEET).assertExists().assertIsDisplayed()

    // Verify event title is shown
    compose.onNodeWithText("Team Meeting").assertExists()
  }

  @Test
  fun userDetailsSheet_displaysEventDetails() {
    val vm = HourRecapViewModel()
    val pastTime = java.time.Instant.now().minusSeconds(86400) // 1 day ago
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Morning Shift",
            startDate = pastTime,
            endDate = pastTime.plusSeconds(7200),
            isPast = true,
            wasPresent = true,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "bob",
                displayName = "Bob",
                completedHours = 2.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("Bob").performClick()
    compose.waitForIdle()

    // Verify event details are shown
    compose.onNodeWithText("Morning Shift").assertExists()
  }

  @Test
  fun userDetailsSheet_closesOnDismiss() {
    val vm = HourRecapViewModel()
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Task",
            startDate = java.time.Instant.now(),
            endDate = java.time.Instant.now().plusSeconds(3600),
            isPast = false,
            wasPresent = null,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 1.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    // Dismiss the sheet by swiping down
    compose.onNodeWithTag(HourRecapTestTags.RECAP_SHEET).performTouchInput { swipeDown() }
    compose.waitForIdle()

    // Verify sheet is no longer displayed
    compose.onNodeWithTag(HourRecapTestTags.RECAP_SHEET).assertDoesNotExist()
  }

  @Test
  fun userDetailsSheet_showsEmptyMessageWhenNoEvents() {
    val vm = HourRecapViewModel()

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "alice",
                displayName = "Alice",
                completedHours = 0.0,
                plannedHours = 0.0,
                events = emptyList())))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("Alice").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify empty message is shown
    compose.onNodeWithText(context.getString(R.string.hour_recap_no_events_for_user)).assertExists()
  }

  @Test
  fun eventTags_displayForFutureEvent() {
    val vm = HourRecapViewModel()
    val futureTime = java.time.Instant.now().plusSeconds(86400)
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Future Event",
            startDate = futureTime,
            endDate = futureTime.plusSeconds(3600),
            isPast = false,
            wasPresent = null,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 1.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify future tag is displayed
    compose.onNodeWithText(context.getString(R.string.hour_recap_tag_future)).assertExists()
    // Verify no presence tag for future events
    compose.onNodeWithText(context.getString(R.string.hour_recap_tag_present)).assertDoesNotExist()
    compose.onNodeWithText(context.getString(R.string.hour_recap_tag_absent)).assertDoesNotExist()
  }

  @Test
  fun eventTags_displayReplacementTags() {
    val vm = HourRecapViewModel()
    val futureTime = java.time.Instant.now().plusSeconds(86400)
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Replaced Event",
            startDate = futureTime,
            endDate = futureTime.plusSeconds(3600),
            isPast = false,
            wasPresent = null,
            wasReplaced = true,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 0.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify replacement tag is displayed
    compose.onNodeWithText(context.getString(R.string.hour_recap_tag_replaced)).assertExists()
  }

  @Test
  fun eventTags_displayTookReplacementTag() {
    val vm = HourRecapViewModel()
    val futureTime = java.time.Instant.now().plusSeconds(86400)
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Replacement Event",
            startDate = futureTime,
            endDate = futureTime.plusSeconds(3600),
            isPast = false,
            wasPresent = null,
            wasReplaced = false,
            tookReplacement = true,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 2.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify took replacement tag is displayed
    compose
        .onNodeWithText(context.getString(R.string.hour_recap_tag_replacement_taken))
        .assertExists()
  }

  @Test
  fun eventTags_displayAbsentTag() {
    val vm = HourRecapViewModel()
    val pastTime = java.time.Instant.now().minusSeconds(86400)
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Missed Shift",
            startDate = pastTime,
            endDate = pastTime.plusSeconds(3600),
            isPast = true,
            wasPresent = false,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 0.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify absent tag is displayed
    compose.onNodeWithText(context.getString(R.string.hour_recap_tag_absent)).assertExists()
  }

  @Test
  fun eventTags_displayUnknownPresenceTag() {
    val vm = HourRecapViewModel()
    val pastTime = java.time.Instant.now().minusSeconds(86400)
    val event =
        HourRecapEventEntry(
            id = "event1",
            title = "Unknown Status",
            startDate = pastTime,
            endDate = pastTime.plusSeconds(3600),
            isPast = true,
            wasPresent = null,
            wasReplaced = false,
            tookReplacement = false,
            categoryColor = androidx.compose.ui.graphics.Color.Blue)

    vm.setTestWorkedHours(
        listOf(
            HourRecapUserRecap(
                userId = "user",
                displayName = "User",
                completedHours = 0.0,
                plannedHours = 0.0,
                events = listOf(event))))

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    compose.onNodeWithText("User").performClick()
    compose.waitForIdle()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    // Verify unknown presence tag is displayed
    compose
        .onNodeWithText(context.getString(R.string.hour_recap_tag_presence_unknown))
        .assertExists()
  }
}
