package com.android.sample.utils

import com.android.sample.ui.organization.SelectedOrganizationVMProvider

/**
 * An interface to be implemented by tests that require a selected organization.
 *
 * This interface provides a mechanism to set a specific organization as selected before each test
 * is run.
 */
interface RequiresSelectedOrganizationTestBase {

  /** The ID of the organization to be selected for the test. */
  val organizationId: String

  /** Default organization ID for tests if none is specified. */
  companion object {
    const val DEFAULT_TEST_ORG_ID = "test_org_id"
  }

  /**
   * Prepares the selected organization before each test.
   *
   * Call this method in a @Before annotated function (e.g., in the setUp() method) of the test
   * class.
   */
  fun setSelectedOrganization() {
    val viewModel = SelectedOrganizationVMProvider.viewModel
    viewModel.selectOrganization(organizationId)
  }
}
