package com.android.sample.ui.map

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapViewModelTest {

  @Test
  fun `fetch user position without permission`() {
    val vm = MapViewModel(ApplicationProvider.getApplicationContext())
    vm.fetchUserLocation()
    assertTrue(vm.state.value.errorLocation != null)
    assertFalse(vm.state.value.hasPermission)
  }
}
