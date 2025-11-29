package com.android.sample.model.organization.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** ViewModel to manage the selected organization state across the app. */
object SelectedOrganizationRepository {

  private val _selectedOrganizationId = MutableStateFlow<String?>(null)
  val selectedOrganizationId: StateFlow<String?> = _selectedOrganizationId

  // Select an organization by its instance
  fun changeSelectedOrganization(orgId: String) {
    _selectedOrganizationId.value = orgId
  }

  // Clear the selected organization
  fun clearSelection() {
    _selectedOrganizationId.value = null
  }
}
