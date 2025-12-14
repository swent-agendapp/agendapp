package com.android.sample.model.filter

import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Assisted by AI

/**
 * Fake implementation of [SelectedOrganizationViewModel] used for unit testing.
 *
 * This ViewModel allows tests to manually control the currently selected organization by exposing a
 * mutable [StateFlow]. It avoids relying on the real organization selection logic and enables
 * deterministic testing of ViewModels that react to organization changes (e.g. FilterViewModel).
 */
class FakeSelectedOrganizationViewModel : SelectedOrganizationViewModel() {
  private val _orgId = MutableStateFlow<String?>(null)
  override val selectedOrganizationId: StateFlow<String?> = _orgId

  fun setOrg(id: String?) {
    _orgId.value = id
  }
}
