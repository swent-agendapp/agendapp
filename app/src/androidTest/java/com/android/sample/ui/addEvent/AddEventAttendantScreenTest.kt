package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantScreen
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventAttendantScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var addEventViewModel: AddEventViewModel

  @Before
  override fun setUp() {
    super.setUp()
    setSelectedOrganization()

    addEventViewModel = AddEventViewModel()

    composeTestRule.setContent { AddEventAttendantScreen(addEventViewModel = addEventViewModel) }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.EXTRA_EVENT_TOGGLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.EXTRA_EVENT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.LIST_USER).assertIsDisplayed()
  }
}
