package com.android.sample.model.filter

import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSelectedOrganizationViewModel : SelectedOrganizationViewModel() {
  private val _orgId = MutableStateFlow<String?>(null)
  override val selectedOrganizationId: StateFlow<String?> = _orgId

  fun setOrg(id: String?) {
    _orgId.value = id
  }
}
