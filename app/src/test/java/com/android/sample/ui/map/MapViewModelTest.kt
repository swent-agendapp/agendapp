package com.android.sample.ui.map

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.sample.model.map.MapRepositoryLocal
import org.junit.Assert.*
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

@RunWith(AndroidJUnit4::class)
class MapViewModelTestWithPermission {
  private var repository = MapRepositoryLocal()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `check area name is correct`() {
    val vm = MapViewModel(ApplicationProvider.getApplicationContext(), repository)
    vm.setNewAreaName("test")
    assertEquals("test", vm.state.value.nextAreaName)
  }

  @Test
  fun `check marker can be added and removed correctly`() {
    val vm = MapViewModel(ApplicationProvider.getApplicationContext(), repository)
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(0.0, 0.0))
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(1.0, 0.0))
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(0.0, 1.0))

    assertEquals(3, vm.state.value.listNewMarker.size)

    vm.deleteMarker(vm.state.value.listNewMarker[0].id)

    assertEquals(2, vm.state.value.listNewMarker.size)
  }

  @Test
  fun `create area with 2 marker make an error and clean it`() {

    val vm = MapViewModel(ApplicationProvider.getApplicationContext(), repository)
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(0.0, 0.0))
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(1.0, 0.0))

    assertEquals(2, vm.state.value.listNewMarker.size)
    vm.createNewArea()
    assertEquals(
        "An Area must have at least 3 distinct markers with unique coordinates",
        vm.state.value.errorMessage)
    vm.cleanMessageError()
    assertEquals(null, vm.state.value.errorMessage)
  }

  @Test
  fun `create area with 3 marker`() {
    val vm = MapViewModel(ApplicationProvider.getApplicationContext(), repository)
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(0.0, 0.0))
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(1.0, 0.0))
    vm.addNewMarker(com.google.android.gms.maps.model.LatLng(0.0, 1.0))
    vm.setNewAreaName("Test Area")

    assertEquals(3, vm.state.value.listNewMarker.size)

    vm.createNewArea()

    assertEquals(1, vm.state.value.listArea.size)
    assertEquals("Test Area", vm.state.value.listArea[0].label)
    assertEquals(3, vm.state.value.listArea[0].markers.size)
  }
}
