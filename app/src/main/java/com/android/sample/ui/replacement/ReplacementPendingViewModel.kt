package com.android.sample.ui.replacement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryProvider
import com.android.sample.model.replacement.toProcessReplacements
import com.android.sample.model.replacement.waitingForAnswerAndDeclinedReplacements
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReplacementPendingUiState(
    val isLoading: Boolean = false,
    val toProcess: List<Replacement> = emptyList(),
    val waitingForAnswer: List<Replacement> = emptyList(),
    val errorMessage: String? = null,
)

class ReplacementPendingViewModel(
    private val repository: ReplacementRepository = ReplacementRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReplacementPendingUiState())
  val uiState: StateFlow<ReplacementPendingUiState> = _uiState.asStateFlow()

  private fun getOrgId(): String {
    val orgId = selectedOrganizationViewModel.selectedOrganizationId.value
    require(orgId != null) { "Organization must be selected to fetch pending replacements" }
    return orgId
  }

  fun refresh() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val orgId = getOrgId()
        val all = repository.getAllReplacements(orgId)

        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                toProcess = all.toProcessReplacements(),
                waitingForAnswer = all.waitingForAnswerAndDeclinedReplacements(),
                errorMessage = null,
            )
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to load pending replacements: ${e.message}")
      }
    }
  }
}
