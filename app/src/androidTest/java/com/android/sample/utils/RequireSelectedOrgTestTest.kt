package com.android.sample.utils

import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RequiresSelectedOrganizationTestTest : RequiresSelectedOrganizationTest {

  override val organizationId: String = "test_org"

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
