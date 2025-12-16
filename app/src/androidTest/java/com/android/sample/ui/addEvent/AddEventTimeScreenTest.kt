package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.data.local.BoxProvider
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceScreen
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeScreenTest : RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var fakeViewModel: AddEventViewModel

  @Before
  fun setUp() {
    setSelectedOrganization()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    BoxProvider.init(context)

    UserRepositoryProvider.repository = UsersRepositoryLocal()
    EventRepositoryProvider.repository = EventRepositoryLocal()
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
  fun displayRecurrenceOptions() {
    composeTestRule
        .onNodeWithTag(AddEventTestTags.recurrenceTag(RecurrenceStatus.Weekly))
        .assertExists()
  }

  @Test
  fun displayEndRecurrenceFieldIfNotOneTimeRecurrence() {
    composeTestRule.runOnUiThread { fakeViewModel.setRecurrenceMode(RecurrenceStatus.Weekly) }
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsDisplayed()
  }
}
