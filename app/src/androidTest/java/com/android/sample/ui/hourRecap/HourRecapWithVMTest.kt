package com.android.sample.ui.hourRecap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HourRecapScreen composable using real CalendarViewModel, but injecting test
 * data into its UI state.
 */
class HourRecapWithVMTest : RequiresSelectedOrganizationTestBase {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  fun setUp() {
    setSelectedOrganization()
  }

  /**
   * Tests that the recap list displays the expected number of items based on the test data injected
   * into the CalendarViewModel's UI state.
   */
  @Test
  fun errorState_triggersEffectAndClearsError() {
    val vm = HourRecapViewModel()
    val msg = "Test error"

    // Inject error into UI state
    vm.setErrorMsg(msg)

    compose.setContent { HourRecapScreen(hourRecapViewModel = vm) }

    // Advance time to allow LaunchedEffect and snackbar to complete
    // Snackbar has a default timeout of ~4 seconds, so we need to wait longer
    compose.mainClock.advanceTimeBy(5000)
    compose.waitForIdle()

    // After LaunchedEffect, errorMsg must be cleared
    assertNull(vm.uiState.value.errorMsg)
  }
}
