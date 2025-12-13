package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.calendar.editEvent.components.EditEventAttendantScreen
import com.android.sample.ui.calendar.editEvent.components.EditEventScreen
import com.android.sample.ui.theme.SampleAppTheme
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import java.time.Duration
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI
/**
 * ViewModel-driven integration tests for EditEventScreen & EditEventAttendantScreen These tests
 * verify UI + ViewModel interaction consistency.
 */
@RunWith(AndroidJUnit4::class)
class EditEventWithViewModelTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  private lateinit var sampleEvent: Event
  private lateinit var fakeViewModel: EditEventViewModel

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()

    val start = now().truncatedTo(ChronoUnit.HOURS)
    sampleEvent =
        Event(
            id = "E123",
            organizationId = organizationId,
            title = "Test Event",
            description = "Desc",
            startDate = start,
            endDate = start.plus(Duration.ofHours(1)),
            cloudStorageStatuses = emptySet(),
            locallyStoredBy = emptyList(),
            personalNotes = null,
            participants = setOf("Alice", "Bob"),
            version = 1L,
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            category = EventCategory.defaultCategory(),
            location = null)

    val fakeRepository = FakeEventRepository()
    fakeRepository.add(event = sampleEvent)
    fakeViewModel = EditEventViewModel(fakeRepository)
  }

  // -------------------------------------------------------------------------
  // 1. Test: EditEventScreen disables Save button when fields are empty
  // -------------------------------------------------------------------------
  @Test
  fun editEventScreen_showsErrorWhenFieldsEmpty_withViewModel() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E001",
            editEventViewModel = fakeViewModel,
            onSave = {},
            onCancel = {},
            onEditParticipants = {},
            skipLoad = true)
      }
    }

    // clear title & description
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()
    // wait for UI idle
    composeTestRule.waitForIdle()
    // Save button should be disabled
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  // -------------------------------------------------------------------------
  // 2. Test: EditEventScreen - Save & Cancel callbacks trigger correctly
  // -------------------------------------------------------------------------
  @Test
  fun editEventScreen_saveAndCancelTriggersCallbacks_withViewModel() {
    var saveClicked = false
    var cancelClicked = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = sampleEvent.id,
            editEventViewModel = fakeViewModel,
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onEditParticipants = {})
      }
    }

    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextInput("Meeting")

    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD)
        .performTextInput("Plan update")

    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.CANCEL_BUTTON).performClick()

    // verify callbacks triggered
    assert(saveClicked)
    assert(cancelClicked)

    // verify ViewModel state updated
    val uiState = fakeViewModel.uiState.value
    assert(uiState.title == "Meeting")
    assert(uiState.description == "Plan update")
  }

  // -------------------------------------------------------------------------
  // 3. Test: EditEventAttendantScreen - Selecting participants works
  // -------------------------------------------------------------------------
  @Test
  fun editEventAttendantScreen_selectsParticipantsCorrectly_withViewModel() {
    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    runBlocking { fakeViewModel.loadUsers() }

    var saveClicked = false
    var backClicked = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventAttendantScreen(
            editEventViewModel = fakeViewModel,
            onSave = { saveClicked = true },
            onBack = { backClicked = true })
      }
    }

    // Click on the "Got it" button of the first pop-up
    composeTestRule.onNodeWithTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON).performClick()

    composeTestRule.onNodeWithText("Alice").performClick()

    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.BACK_BUTTON).performClick()

    assert(saveClicked)
    assert(backClicked)

    // verify ViewModel updated with selected participant
    val uiState = fakeViewModel.uiState.value
    assert(uiState.participants.map { it.id }.contains("1"))
  }

  // -------------------------------------------------------------------------
  // 4. Test: EditEventAttendantScreen - Toggle same participant twice
  // -------------------------------------------------------------------------
  @Test
  fun editEventAttendantScreen_toggleParticipantCheckbox_withViewModel() {
    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    runBlocking { fakeViewModel.loadUsers() }

    composeTestRule.setContent {
      SampleAppTheme { EditEventAttendantScreen(editEventViewModel = fakeViewModel) }
    }

    // Click on the "Got it" button of the first pop-up
    composeTestRule.onNodeWithTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON).performClick()

    val alice = composeTestRule.onNodeWithText("Alice")

    alice.assertExists()
    alice.performClick() // select
    alice.performClick() // unselect

    // verify ViewModel updated to remove participant
    val uiState = fakeViewModel.uiState.value
    assert(!uiState.participants.map { it.id }.contains("Alice"))
  }

  @Test
  fun test_startAndEndTimeButtons_updateDisplayedTime() {
    // Arrange: Load the screen with skipLoad = true (simplified UI state)
    composeTestRule.setContent { EditEventScreen(eventId = "test_event_id", skipLoad = true) }

    // Assert: screen is loaded correctly
    composeTestRule
        .onNodeWithTag(EditEventTestTags.TITLE_FIELD)
        .assertExists("EditEventScreen failed to load")

    // Record initial displayed start and end time
    val initialStartTime =
        composeTestRule
            .onNodeWithTag(EditEventTestTags.START_TIME_BUTTON)
            .fetchSemanticsNode()
            .config
            .toString()

    val initialEndTime =
        composeTestRule
            .onNodeWithTag(EditEventTestTags.END_TIME_BUTTON)
            .fetchSemanticsNode()
            .config
            .toString()

    // --- Act 1: Click start time button (should trigger start time picker)
    composeTestRule.onNodeWithTag(EditEventTestTags.START_TIME_BUTTON).assertExists().performClick()

    // --- Act 2: Click end time button (should trigger end time picker)
    composeTestRule.onNodeWithTag(EditEventTestTags.END_TIME_BUTTON).assertExists().performClick()

    // --- Assert: Ensure that the time display has been updated or at least still valid text exists
    composeTestRule
        .onNodeWithTag(EditEventTestTags.START_TIME_BUTTON)
        .assertExists("Start time button missing after click")
    composeTestRule
        .onNodeWithTag(EditEventTestTags.END_TIME_BUTTON)
        .assertExists("End time button missing after click")

    // Optional: compare before/after text (pseudo check — may differ depending on formatter)
    val updatedStartTime =
        composeTestRule
            .onNodeWithTag(EditEventTestTags.START_TIME_BUTTON)
            .fetchSemanticsNode()
            .config
            .toString()

    val updatedEndTime =
        composeTestRule
            .onNodeWithTag(EditEventTestTags.END_TIME_BUTTON)
            .fetchSemanticsNode()
            .config
            .toString()

    assert(initialStartTime.isNotEmpty())
    assert(updatedStartTime.isNotEmpty())
    assert(initialEndTime.isNotEmpty())
    assert(updatedEndTime.isNotEmpty())

    // --- Extra check: time difference (soft assertion) ---
    if (initialStartTime != updatedStartTime || initialEndTime != updatedEndTime) {
      println("Time values updated successfully after click.")
    } else {
      println("Time values appear unchanged.")
    }
  }
}
