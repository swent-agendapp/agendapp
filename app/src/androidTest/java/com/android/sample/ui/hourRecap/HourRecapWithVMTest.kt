package com.android.sample.ui.hourRecap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import junit.framework.TestCase.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HourRecapScreen composable using real CalendarViewModel, but injecting test
 * data into its UI state.
 */
class HourRecapWithVMTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun errorState_triggersEffectAndClearsError() {
    val vm = HourRecapViewModel()
    val msg = "Test error"

    // Inject error into UI state
    vm.setErrorMsg(msg)

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    // Let LaunchedEffect run
    compose.waitForIdle()

    // After LaunchedEffect, errorMsg must be cleared
    assertNull(vm.uiState.value.errorMsg)
  }
}
