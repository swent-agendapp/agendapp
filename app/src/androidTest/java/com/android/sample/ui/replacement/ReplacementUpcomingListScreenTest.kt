package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.theme.SampleAppTheme
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
}
