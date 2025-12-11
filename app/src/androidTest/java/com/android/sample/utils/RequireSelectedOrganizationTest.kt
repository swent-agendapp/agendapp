package com.android.sample.utils

import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RequiresSelectedOrganizationTest : RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  fun setUp() {
    // Provided by the interface
    setSelectedOrganization()
  }

  @Test
  fun selectedOrganization_isCorrectlySet() {
    val viewModel = SelectedOrganizationVMProvider.viewModel
    val selectedId = viewModel.getSelectedOrganizationId()

    // Verify that the selected organization ID matches the organizationId property
    assertEquals(organizationId, selectedId)
  }
}
