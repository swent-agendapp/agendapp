package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.MainActivity
import com.android.sample.MainActivityTestTags
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun mainActivity_displaysRootContainer() {
    composeTestRule.onNodeWithTag(MainActivityTestTags.MAIN_SCREEN_CONTAINER).assertIsDisplayed()
  }
}
