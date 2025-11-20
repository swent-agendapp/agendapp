package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import com.android.sample.model.organization.Organization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SelectedOrganizationViewModel : ViewModel() {

  private val _selectedOrganization = MutableStateFlow<Organization?>(null)
  val selectedOrganization: StateFlow<Organization?> = _selectedOrganization

  fun selectOrganization(org: Organization) {
    _selectedOrganization.value = org
  }

  fun clearSelection() {
    _selectedOrganization.value = null
  }
}
