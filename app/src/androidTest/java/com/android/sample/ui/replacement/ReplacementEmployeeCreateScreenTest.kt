package com.android.sample.ui.replacement

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.replacement.employee.components.ReplacementCreateScreen
import com.android.sample.ui.replacement.employee.components.ReplacementEmployeeCreateTestTags
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class ReplacementEmployeeCreateScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun replacementCreateScreen_displaysButtonsAndTopBar() {
    composeTestRule.setContent { SampleAppTheme { ReplacementCreateScreen() } }

    // ✅ Select Event button
    composeTestRule
        .onNodeWithTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()

    // ✅ Choose Date Range button
    composeTestRule
        .onNodeWithTag(ReplacementEmployeeCreateTestTags.CHOOSE_DATE_RANGE_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()
  }
}
