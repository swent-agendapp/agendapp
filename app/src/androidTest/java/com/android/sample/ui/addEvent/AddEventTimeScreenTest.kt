package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.AddEventTimeAndRecurrenceScreen
import com.android.sample.ui.calendar.AddEventViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var fakeViewModel: AddEventViewModel

  @Before
  fun setUp() {
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

  @Test
  fun displayNextButton() {
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayBackButton() {
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
  }
}
