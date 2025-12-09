package com.android.sample.ui.map

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.android.sample.model.map.MapRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.utils.FirebaseEmulatedTest
import com.google.android.gms.maps.MapsInitializer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapScreenTest : FirebaseEmulatedTest() {
  lateinit var mapViewModel: MapViewModel
  private val selectedOrganizationId = "orgTest"

  @Before
  override fun setUp() {
    super.setUp()
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
    val repo = MapRepositoryProvider.repository
    mapViewModel = MapViewModel(ApplicationProvider.getApplicationContext(), repo)
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
    composeTestRule.setContent { MapScreen(mapViewModel = mapViewModel) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.TOOLTIP_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_AREA_FLOATING_BUTTON).assertIsDisplayed()
  }

  @Test
  fun clickToolTipDisplayIt() {
    composeTestRule.setContent { MapScreen(mapViewModel = mapViewModel) }

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.TOOLTIP_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.TOOLTIP_TEXT).assertIsDisplayed()
  }

  @Test
  fun openTheCreateAreaDownSheet() {
    composeTestRule.setContent { MapScreen(mapViewModel = mapViewModel) }
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.CREATE_AREA_FLOATING_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.DOWN_SHEET).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.DOWN_SHEET_FORM)
        .assertIsDisplayed()
        .performTextInput("Office")
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.CREATE_AREA_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }
}
