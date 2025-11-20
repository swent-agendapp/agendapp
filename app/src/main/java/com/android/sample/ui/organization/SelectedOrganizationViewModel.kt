package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import com.android.sample.model.organization.Organization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** ViewModel to manage the selected organization state across the app. */
class SelectedOrganizationViewModel : ViewModel() {

  private val _selectedOrganizationId = MutableStateFlow<String?>(null)
  val selectedOrganizationId: StateFlow<String?> = _selectedOrganizationId

  // Select an organization by its instance
  fun changeSelectedOrganization(org: Organization) {
    _selectedOrganizationId.value = org.id
  }

  // Clear the selected organization
  fun clearSelection() {
    _selectedOrganizationId.value = null
  }
}
