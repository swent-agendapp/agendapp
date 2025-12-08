package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeFlow
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeListTestTags
import com.android.sample.utils.RequiresSelectedOrganizationTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReplacementEmployeeFlowTest : RequiresSelectedOrganizationTest {

  @get:Rule val compose = createComposeRule()
  override val organizationId = "orgTest"

  @Before
  fun init() {
    setSelectedOrganization()
  }

  private fun setContent() {
    compose.setContent {
      ReplacementEmployeeFlow(
          onOrganizeClick = {},
          onWaitingConfirmationClick = {},
          onConfirmedClick = {},
      )
    }
  }

  @Test
  fun listScreen_showsAskButton() {
    setContent()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ROOT).assertExists().assertIsDisplayed()

    compose
        .onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun clickingAskButton_showsCreateOptions() {
    setContent()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.SELECT_EVENT_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ReplacementEmployeeListTestTags.DATE_RANGE_BUTTON).assertDoesNotExist()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()

    compose
        .onNodeWithTag(ReplacementEmployeeListTestTags.SELECT_EVENT_BUTTON)
        .assertExists()
        .assertIsDisplayed()
    compose
        .onNodeWithTag(ReplacementEmployeeListTestTags.DATE_RANGE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun selectEventOption_goesToSelectEventScreen() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val nextText = context.getString(R.string.next)

    setContent()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.SELECT_EVENT_BUTTON).performClick()

    compose.onNodeWithText(nextText).assertExists().assertIsDisplayed()
  }

  @Test
  fun dateRangeOption_goesToDateRangeScreen() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val startDateText = context.getString(R.string.startDatePickerLabel)

    setContent()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.DATE_RANGE_BUTTON).performClick()

    compose.onNodeWithText(startDateText).assertExists().assertIsDisplayed()
  }
}
