package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.calendar.utils.DateTimeUtils.DATE_FORMAT_PATTERN
import com.android.sample.ui.theme.SampleAppTheme
import java.time.format.DateTimeFormatter
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class ReplacementUpcomingListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun upcomingMocks(): List<Replacement> {
    return getMockReplacements().take(3).map { it.copy(status = ReplacementStatus.Accepted) }
  }

  @Test
  fun screen_displaysScreenAndList() {
    composeTestRule.setContent {
      SampleAppTheme { ReplacementUpcomingListScreen(replacements = upcomingMocks()) }
    }

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.LIST, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun screen_displaysOneCardPerReplacement() {
    val replacements = upcomingMocks()

    composeTestRule.setContent {
      SampleAppTheme { ReplacementUpcomingListScreen(replacements = replacements) }
    }

    replacements.forEach { replacement ->
      composeTestRule
          .onNodeWithTag(
              ReplacementUpcomingTestTags.itemTag(replacement.id), useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun screen_displaysEventTitleAndSubstitute() {
    val replacements = upcomingMocks()
    val first = replacements.first()

    composeTestRule.setContent {
      SampleAppTheme { ReplacementUpcomingListScreen(replacements = replacements) }
    }

    composeTestRule.onNodeWithText(first.event.title).assertIsDisplayed()

    composeTestRule
        .onAllNodesWithText(first.substituteUserId, substring = true)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun screen_showsEmptyMessage_whenNoReplacements() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val emptyText = context.getString(R.string.replacement_upcoming_empty_message)

    composeTestRule.setContent {
      SampleAppTheme { ReplacementUpcomingListScreen(replacements = emptyList()) }
    }

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.LIST, useUnmergedTree = true)
        .assertDoesNotExist()

    composeTestRule.onNodeWithText(emptyText).assertIsDisplayed()
  }

  @Test
  fun backButton_callsOnBack() {
    var backCalled = false

    composeTestRule.setContent {
      SampleAppTheme {
        ReplacementUpcomingListScreen(
            replacements = upcomingMocks(),
            onBack = { backCalled = true },
        )
      }
    }

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.BACK_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()

    assertTrue(backCalled)
  }

  @Test
  fun card_displaysFormattedDateAndTime() {
    val replacements = upcomingMocks()
    val first = replacements.first()

    val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val expectedDate = first.event.startLocalDate.format(dateFormatter)
    val expectedTime =
        "${first.event.startLocalTime.format(timeFormatter)} - " +
            first.event.endLocalTime.format(timeFormatter)
    val expectedLine = "$expectedDate • $expectedTime"

    composeTestRule.setContent {
      SampleAppTheme { ReplacementUpcomingListScreen(replacements = replacements) }
    }

    composeTestRule.onNodeWithText(expectedLine).assertIsDisplayed()
  }

  @Test
  fun screen_usesDefaultReplacementsParameter() {
    composeTestRule.setContent { SampleAppTheme { ReplacementUpcomingListScreen() } }

    composeTestRule
        .onNodeWithTag(ReplacementUpcomingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
