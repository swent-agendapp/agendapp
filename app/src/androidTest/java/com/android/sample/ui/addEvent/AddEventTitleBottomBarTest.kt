package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventTitleAndDescriptionBottomBar
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTitleBottomBarTest : RequiresSelectedOrganizationTestBase {

  override val organizationId = "org_123"

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    setSelectedOrganization()

    composeTestRule.setContent { AddEventTitleAndDescriptionBottomBar() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun nextButtonDisabledWhenFieldsEmpty() {
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsNotEnabled()
  }
}
