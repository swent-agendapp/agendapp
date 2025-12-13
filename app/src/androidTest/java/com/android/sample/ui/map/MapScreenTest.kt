package com.android.sample.ui.map

import android.Manifest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.android.sample.model.map.Area
import com.android.sample.model.map.MapRepositoryProvider
import com.android.sample.model.map.Marker
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DELETE_MARKER_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET_FORM
import com.android.sample.ui.map.MapScreenTestTags.SLIDER
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {
  lateinit var mapViewModel: MapViewModel

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()
    setSelectedOrganization()
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
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_GO_BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun showCreateAreaBottomBar() {
    composeTestRule.setContent { MapScreen(mapViewModel = mapViewModel) }
    composeTestRule.waitForIdle()

    mapViewModel.createArea(LatLng(DefaultLocation.LATITUDE, DefaultLocation.LONGITUDE))

    composeTestRule.onNodeWithTag(DOWN_SHEET).assertIsDisplayed()
    composeTestRule.onNodeWithTag(DOWN_SHEET_FORM).assertIsDisplayed()
    mapViewModel.setNewAreaRadius(100.0)
    composeTestRule.onNodeWithTag(SLIDER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CREATE_AREA_BUTTON).assertIsDisplayed().performClick()
  }

  @Test
  fun showEditAreaBottomBar() {
    composeTestRule.setContent { MapScreen(mapViewModel = mapViewModel) }
    composeTestRule.waitForIdle()

    mapViewModel.selectArea(
        Area(
            label = "my new area",
            marker =
                Marker(latitude = DefaultLocation.LATITUDE, longitude = DefaultLocation.LONGITUDE),
            radius = 10.0))

    composeTestRule.onNodeWithTag(DOWN_SHEET).assertIsDisplayed()
    composeTestRule.onNodeWithTag(DOWN_SHEET_FORM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SLIDER).assertIsDisplayed().performSemanticsAction(
        SemanticsActions.SetProgress) { setProgress ->
          setProgress(30F) // 🔹 même unité que valueRange
    }
    composeTestRule.onNodeWithTag(DELETE_MARKER_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CREATE_AREA_BUTTON).assertIsDisplayed().performClick()
  }
}
