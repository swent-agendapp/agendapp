package com.android.sample.ui.replacement

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementEmployeeListScreen
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementEmployeeListTestTags
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class ReplacementEmployeeListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun replacementEmployeeListScreen_displaysAllUiElements() {
    composeTestRule.setContent { SampleAppTheme { ReplacementEmployeeListScreen() } }

    // ✅ Root container
    composeTestRule.onNodeWithTag(ReplacementEmployeeListTestTags.ROOT).assertIsDisplayed()

    // ✅ Ask-to-be-replaced button
    composeTestRule
        .onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()

    // ✅ Check that both sample request cards are displayed
    composeTestRule.onNodeWithTag(ReplacementEmployeeListTestTags.card("req1")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementEmployeeListTestTags.card("req2")).assertIsDisplayed()

    // ✅ Check the accept/refuse buttons exist on each card
    composeTestRule
        .onNodeWithTag(ReplacementEmployeeListTestTags.accept("req1"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementEmployeeListTestTags.refuse("req1"))
        .assertIsDisplayed()
  }
}
