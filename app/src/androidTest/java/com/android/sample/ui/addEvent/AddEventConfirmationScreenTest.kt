package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationScreen
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import java.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventConfirmationScreenTest :
    FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()

    composeTestRule.setContent { AddEventConfirmationScreen() }
  }

  @Test
  fun displayAllComponents() {
    // Main parts of the summary card are visible
    composeTestRule.onNodeWithTag(EventSummaryCardTags.SIDE_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.DATE_LINE1).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.DATE_LINE2).assertIsDisplayed()

    // The first date line contains today's day of month (date is “today”)
    val today = LocalDate.now()
    composeTestRule
        .onNodeWithTag(EventSummaryCardTags.DATE_LINE1)
        .assertTextContains(today.dayOfMonth.toString(), substring = true)
  }
}
