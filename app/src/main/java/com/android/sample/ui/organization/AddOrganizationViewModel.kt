package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AddOrganizationUIState(val name: String? = null)

class AddOrganizationViewModel() : ViewModel() {
  // State holding the UI state for adding an organization
  private val _uiState = MutableStateFlow(AddOrganizationUIState())
  val uiState: StateFlow<AddOrganizationUIState> = _uiState

  // Update the name field in the UI state
  fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
  }

  // Validate if the organization name is not null or blank
  fun isValidOrganizationName(): Boolean {
    val name = _uiState.value.name
    return !name.isNullOrBlank()
  }
}
