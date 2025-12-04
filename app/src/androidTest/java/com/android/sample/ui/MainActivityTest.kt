package com.android.sample.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.android.sample.MainActivity
import com.android.sample.MainActivityTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class MainActivityTest {

  private val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val ruleChain: RuleChain = RuleChain.outerRule(permissionRule).around(composeTestRule)

  @Test
  fun mainActivity_displaysRootContainer() {
    composeTestRule.onNodeWithTag(MainActivityTestTags.MAIN_SCREEN_CONTAINER).assertIsDisplayed()
  }
}
