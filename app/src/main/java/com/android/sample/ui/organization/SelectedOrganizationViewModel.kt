package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.flow.StateFlow

class SelectedOrganizationViewModel : ViewModel() {

  val selectedOrganizationId: StateFlow<String?> =
      SelectedOrganizationRepository.selectedOrganizationId

  fun selectOrganization(orgId: String) {
    SelectedOrganizationRepository.changeSelectedOrganization(orgId)
  }

  fun clearSelection() {
    SelectedOrganizationRepository.clearSelection()
  }
}
