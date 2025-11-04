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
    composeTestRule.setContent { ReplacementScreen() }
  }

  @Test
  fun displayScreenAndCardList() {
    composeTestRule.onNodeWithTag(ReplacementTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementTestTags.CARD_LIST).assertIsDisplayed()
  }

  @Test
  fun displayAllCards() {
    composeTestRule.onNodeWithTag(ReplacementTestTags.CARD_ORGANIZE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementTestTags.CARD_PROCESS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementTestTags.CARD_WAITING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementTestTags.CARD_CONFIRMED).assertIsDisplayed()
  }
}
