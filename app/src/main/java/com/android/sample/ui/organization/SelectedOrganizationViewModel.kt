package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.flow.StateFlow

/** ViewModel to manage the selected organization state across the app. */
open class SelectedOrganizationViewModel : ViewModel() {

  /** StateFlow holding the currently selected organization ID, or null if none is selected. */
  open val selectedOrganizationId: StateFlow<String?> =
      SelectedOrganizationRepository.selectedOrganizationId

  /* Change the selected organization with the given organization ID. */
  fun selectOrganization(orgId: String) {
    SelectedOrganizationRepository.changeSelectedOrganization(orgId)
  }

  /** Clear the currently selected organization ID. */
  fun clearSelection() {
    SelectedOrganizationRepository.clearSelection()
  }

  /**
   * Returns the currently selected organization ID as a non-null String.
   *
   * @throws IllegalStateException if no organization is currently selected.
   */
  fun getSelectedOrganizationId(): String {
    return selectedOrganizationId.value
        ?: throw IllegalStateException("No organization is currently selected.")
  }
}
