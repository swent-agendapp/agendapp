package com.android.sample.ui.map

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.maps.MapsInitializer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
  @Before
  fun setUp() {
    MapsInitializer.initialize(
        ApplicationProvider.getApplicationContext(),
    )
  }

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topTitleIsCorrectlySet() {
    composeTestRule.setContent { MapScreen() }

    composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_GO_BACK_BUTTON).assertIsDisplayed()
  }
}
