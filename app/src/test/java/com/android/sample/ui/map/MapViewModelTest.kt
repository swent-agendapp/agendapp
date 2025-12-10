package com.android.sample.ui.map

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.sample.model.map.MapRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapViewModelTest {
  private var repository = MapRepositoryLocal()

  @Test
  fun `fetch user position without permission`() {
    val vm = MapViewModel(ApplicationProvider.getApplicationContext(), repository)
    vm.fetchUserLocation()
    assertTrue(vm.state.value.errorMessage != null)
    assertFalse(vm.state.value.hasPermission)
  }
}

//@RunWith(AndroidJUnit4::class)
//class MapViewModelTestWithPermission {
//  private var repository = MapRepositoryLocal()
//
//  @get:Rule
//  val permissionRule: GrantPermissionRule =
//      GrantPermissionRule.grant(
//          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
//
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @Before
//  fun setup() {
//    // Set a selected organization before tests
//    SelectedOrganizationRepository.changeSelectedOrganization("testOrg")
//  }
//}
