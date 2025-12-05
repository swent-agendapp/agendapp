package com.android.sample.ui.replacement

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessReplacementScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val replacement = getMockReplacements().first()

  @Test
  fun screen_displaysBasicElements() {
    composeTestRule.setContent {
      SampleAppTheme { ProcessReplacementScreen(replacement = replacement) }
    }

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.ROOT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SEARCH_BAR, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.MEMBER_LIST, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun noSelection_buttonDisabled_andSummaryShowsNone() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val noneText = context.getString(R.string.replacement_selected_members_none)

    composeTestRule.setContent {
      SampleAppTheme { ProcessReplacementScreen(replacement = replacement) }
    }

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY)
        .assertIsDisplayed()
        .assert(hasTextExactly(noneText))
  }

  @Test
  fun selectingMembers_andEnablesButton_andCallsCallback() {
    var sentMembers: List<String>? = null

    composeTestRule.setContent {
      SampleAppTheme {
        ProcessReplacementScreen(
            replacement = replacement,
            onSendRequests = { sentMembers = it },
        )
      }
    }

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Emilien"), useUnmergedTree = true)
        .performClick()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsEnabled()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY).assertIsDisplayed()
    composeTestRule.onNodeWithText("Emilien").assertIsDisplayed()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).performClick()

    assertTrue(sentMembers != null)
    assertEquals(listOf("Emilien"), sentMembers)
  }

  @Test
  fun searchFilter_filtersList() {
    composeTestRule.setContent {
      SampleAppTheme { ProcessReplacementScreen(replacement = replacement) }
    }

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("Noa")

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Noa"), useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Emilien"), useUnmergedTree = true)
        .assertDoesNotExist()
  }
}
