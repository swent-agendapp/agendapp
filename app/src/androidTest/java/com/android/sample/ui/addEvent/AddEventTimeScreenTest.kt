package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceScreen
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var addEventViewModel: AddEventViewModel

  @Before
  override fun setUp() {
    super.setUp()
    setSelectedOrganization()
    addEventViewModel = AddEventViewModel()

    composeTestRule.setContent {
      AddEventTimeAndRecurrenceScreen(addEventViewModel = addEventViewModel)
    }
  }

  @Test
  fun displayStartDateField() {
    composeTestRule.onNodeWithTag(AddEventTestTags.START_DATE_FIELD).assertIsDisplayed()
  }

  @Test
  fun displayEndDateField() {
    composeTestRule.onNodeWithTag(AddEventTestTags.END_DATE_FIELD).assertIsDisplayed()
  }

  @Test
  fun displayStartTimeButton() {
    composeTestRule.onNodeWithTag(AddEventTestTags.START_TIME_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayEndTimeButton() {
    composeTestRule.onNodeWithTag(AddEventTestTags.END_TIME_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayRecurrenceOptions() {
    composeTestRule
        .onNodeWithTag(AddEventTestTags.recurrenceTag(RecurrenceStatus.Weekly))
        .assertExists()
  }

  @Test
  fun displayEndRecurrenceFieldIfNotOneTimeRecurrence() {
    composeTestRule.runOnUiThread { addEventViewModel.setRecurrenceMode(RecurrenceStatus.Weekly) }
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsDisplayed()
  }
}
