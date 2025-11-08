package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReplacementScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { ReplacementOverviewScreen() }
  }

  @Test
  fun displayScreenAndCardList() {
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.CARD_LIST).assertIsDisplayed()
  }

  @Test
  fun displayAllCards() {
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.CARD_ORGANIZE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.CARD_PROCESS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.CARD_WAITING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOverviewTestTags.CARD_CONFIRMED).assertIsDisplayed()
  }
}
