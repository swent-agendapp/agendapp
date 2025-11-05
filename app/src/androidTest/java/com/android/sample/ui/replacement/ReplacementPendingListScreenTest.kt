package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.pendingReplacements
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplacementPendingListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var replacements: List<Replacement>

  @Before
  fun setUp() {
    replacements = getMockReplacements().pendingReplacements()
    composeTestRule.setContent { ReplacementPendingListScreen(replacements = replacements) }
  }

  @Test
  fun pendingListScreen_displaysScreenAndList() {
    composeTestRule
        .onNodeWithTag(ReplacementPendingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementPendingTestTags.LIST, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun pendingListScreen_displaysOneCardPerPendingReplacement() {
    composeTestRule
        .onNodeWithTag(ReplacementPendingTestTags.LIST, useUnmergedTree = true)
        .onChildren()
        .assertCountEquals(replacements.size)
  }
}
