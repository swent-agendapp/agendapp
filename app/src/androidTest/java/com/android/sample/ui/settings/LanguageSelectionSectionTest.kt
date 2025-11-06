package com.android.sample.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.localization.LanguageOption
import org.junit.Rule
import org.junit.Test

class LanguageSelectionSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun languageSection_displaysLanguagesAndSelectsOption() {
    val options =
        listOf(
            LanguageOption(languageTag = "", displayName = "System", isSystemDefault = true),
            LanguageOption(languageTag = "en", displayName = "English"),
            LanguageOption(languageTag = "fr", displayName = "French"),
        )
    var selectedTag = ""

    composeTestRule.setContent {
      LanguageSelectionSection(
          options = options,
          selectedLanguageTag = selectedTag,
          onLanguageSelected = { selectedTag = it.languageTag })
    }

    composeTestRule.onNodeWithTag(LanguageSelectionSectionTestTags.ROOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LanguageSelectionSectionTestTags.option("fr"))
        .assertIsDisplayed()
        .performClick()

    assert(selectedTag == "fr")
  }
}
