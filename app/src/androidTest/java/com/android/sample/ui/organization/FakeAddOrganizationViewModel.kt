package com.android.sample.ui.organization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake ViewModel for AddOrganizationScreen tests. Allows controlling the name field and validation
 * state.
 */
class FakeAddOrganizationViewModel(initialName: String? = null) : AddOrganizationViewModel() {

  private val _uiState = MutableStateFlow(AddOrganizationUIState(name = initialName))
  override val uiState: StateFlow<AddOrganizationUIState> = _uiState

  /** Simulate updating the organization name */
  override fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
  }

  /** Simulate validation: name is valid if not null or blank */
  override fun isValidOrganizationName(): Boolean {
    val name = _uiState.value.name
    return !name.isNullOrBlank()
  }
}
