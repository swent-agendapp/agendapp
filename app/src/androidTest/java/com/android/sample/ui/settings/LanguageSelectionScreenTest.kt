package com.android.sample.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.localization.LanguageOption
import org.junit.Rule
import org.junit.Test

class LanguageSelectionScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun languageSelectionScreen_selectsOptionAndSaves() {
    val options =
        listOf(
            LanguageOption(languageTag = "", displayName = "System", isSystemDefault = true),
            LanguageOption(languageTag = "en", displayName = "English"),
        )
    var selectedTag = ""
    var saveInvoked = false

    composeTestRule.setContent {
      LanguageSelectionScreen(
          languageOptions = options,
          selectedLanguageTag = selectedTag,
          onLanguageSelected = { option -> selectedTag = option.languageTag },
          onSave = { saveInvoked = true },
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(LanguageSelectionSectionTestTags.option("en"))
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(LanguageSelectionScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(selectedTag == "en")
    assert(saveInvoked)
  }
}
