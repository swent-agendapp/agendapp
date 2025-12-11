package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceBottomBar
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeBottomBarTest : RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var fakeViewModel: AddEventViewModel

  @Before
  fun setUp() {
    setSelectedOrganization()

    fakeViewModel = AddEventViewModel()
    composeTestRule.setContent {
      AddEventTimeAndRecurrenceBottomBar(addEventViewModel = fakeViewModel)
    }
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
