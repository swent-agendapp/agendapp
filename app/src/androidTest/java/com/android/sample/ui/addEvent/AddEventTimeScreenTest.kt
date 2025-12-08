package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceScreen
import com.android.sample.utils.RequiresSelectedOrganizationTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeScreenTest : RequiresSelectedOrganizationTest {

  override val organizationId: String = "test-org-id"

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var fakeViewModel: AddEventViewModel

  @Before
  fun setUp() {
    setSelectedOrganization()

    fakeViewModel = AddEventViewModel()
    composeTestRule.setContent {
      AddEventTimeAndRecurrenceScreen(addEventViewModel = fakeViewModel)
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
  fun displayRecurrenceStatusDropdown() {
    composeTestRule.onNodeWithTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN).assertIsDisplayed()
  }

  @Test
  fun displayEndRecurrenceFieldIfNotOneTimeRecurrence() {
    composeTestRule.runOnUiThread { fakeViewModel.setRecurrenceMode(RecurrenceStatus.Weekly) }
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsDisplayed()
  }
}
