package com.android.sample.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.components.BottomNavigationButtons
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class BottomNavigationButtonsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val backTag = "back_btn"
  private val nextTag = "next_btn"

  @Test
  fun version1_buttonsAreDisplayed_andClickWorks() {
    var backClicked = false
    var nextClicked = false

    composeTestRule.setContent {
      BottomNavigationButtons(
          version = 1,
          canGoBack = true,
          canGoNext = true,
          backButtonText = "Back",
          nextButtonText = "Next",
          backButtonTestTag = backTag,
          nextButtonTestTag = nextTag,
          onBack = { backClicked = true },
          onNext = { nextClicked = true })
    }

    // Buttons visible
    composeTestRule.onNodeWithTag(backTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag(nextTag).assertIsDisplayed()

    // Click actions
    composeTestRule.onNodeWithTag(backTag).performClick()
    composeTestRule.onNodeWithTag(nextTag).performClick()

    assertTrue(backClicked)
    assertTrue(nextClicked)
  }

  @Test
  fun version1_backButtonHidden_whenCanGoBackFalse() {
    composeTestRule.setContent {
      BottomNavigationButtons(
          version = 1,
          canGoBack = false,
          canGoNext = true,
          backButtonTestTag = backTag,
          nextButtonTestTag = nextTag)
    }

    // Back should not exist
    composeTestRule.onNodeWithTag(backTag).assertDoesNotExist()

    // Next should exist
    composeTestRule.onNodeWithTag(nextTag).assertIsDisplayed()
  }

  @Test
  fun version1_nextButtonDisabled_whenCanGoNextFalse() {
    composeTestRule.setContent {
      BottomNavigationButtons(
          version = 1,
          canGoBack = true,
          canGoNext = false,
          backButtonTestTag = backTag,
          nextButtonTestTag = nextTag)
    }

    composeTestRule.onNodeWithTag(nextTag).assertIsNotEnabled()
  }

  @Test
  fun version2_buttonsDisplayCorrectly_andClickWorks() {
    var nextClicked = false

    composeTestRule.setContent {
      BottomNavigationButtons(
          version = 2,
          canGoBack = true,
          canGoNext = true,
          backButtonText = "Cancel",
          nextButtonText = "Confirm",
          backButtonTestTag = backTag,
          nextButtonTestTag = nextTag,
          onNext = { nextClicked = true })
    }

    composeTestRule.onNodeWithTag(backTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag(nextTag).assertIsDisplayed()

    // nextButton should be enabled and clickable
    composeTestRule.onNodeWithTag(nextTag).assertIsEnabled().performClick()
    assertTrue(nextClicked)
  }

  @Test
  fun version2_nextButtonDisabled_whenCanGoNextFalse() {
    composeTestRule.setContent {
      BottomNavigationButtons(
          version = 2,
          canGoBack = true,
          canGoNext = false,
          backButtonTestTag = backTag,
          nextButtonTestTag = nextTag)
    }

    composeTestRule.onNodeWithTag(nextTag).assertIsNotEnabled()
  }
}
