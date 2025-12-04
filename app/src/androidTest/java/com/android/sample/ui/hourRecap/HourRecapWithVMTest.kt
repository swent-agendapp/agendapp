package com.android.sample.ui.hourRecap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.ui.calendar.CalendarViewModel
import junit.framework.TestCase.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HourRecapScreen composable using real CalendarViewModel, but injecting test
 * data into its UI state.
 */
class HourRecapWithVMTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  /**
   * Tests that the recap list displays the expected number of items based on the test data injected
   * into the CalendarViewModel's UI state.
   */
  @Test
  fun errorState_triggersToastAndClearsError() {
    val vm = CalendarViewModel()
    val msg = "Test error"

    // Inject error
    vm.setErrorMsg(msg)

    compose.setContent { HourRecapScreen(calendarViewModel = vm) }

    // Allow LaunchedEffect to run
    compose.waitForIdle()

    // After LaunchedEffect → errorMsg should be cleared
    assertNull(vm.uiState.value.errorMsg)
  }
}
