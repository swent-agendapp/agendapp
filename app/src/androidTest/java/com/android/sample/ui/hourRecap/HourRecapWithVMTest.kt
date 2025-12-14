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

    // Wait for the LaunchedEffect to trigger and clear the error
    // The snackbar's showSnackbar() is a suspend function that completes when dismissed
    compose.waitUntil(timeoutMillis = 6000) { vm.uiState.value.errorMsg == null }

    // Verify error message was cleared
    assertNull(vm.uiState.value.errorMsg)
  }
}
