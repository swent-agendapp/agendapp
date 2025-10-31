package com.android.sample.ui.map

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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



