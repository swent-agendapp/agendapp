package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantBottomBar
import com.android.sample.utils.RequiresSelectedOrganizationTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventAttendantBottomBarTest : RequiresSelectedOrganizationTest {

  override val organizationId: String = "org_123"

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    setSelectedOrganization()

    composeTestRule.setContent { AddEventAttendantBottomBar() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
  }
}
