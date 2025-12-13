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

interface ReplacementPendingContract {
  val uiState: StateFlow<ReplacementPendingUiState>

  fun refresh()
}

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
) : ViewModel(), ReplacementPendingContract {

  private val _uiState = MutableStateFlow(ReplacementPendingUiState())
  override val uiState: StateFlow<ReplacementPendingUiState> = _uiState.asStateFlow()

  private fun getOrgId(): String {
    val orgId = selectedOrganizationViewModel.selectedOrganizationId.value
    require(orgId != null) { "Organization must be selected to fetch pending replacements" }
    return orgId
  }

  override fun refresh() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val orgId = getOrgId()
        val all = repository.getAllReplacements(orgId)

        val rawToProcess = all.toProcessReplacements()
        val waiting = all.waitingForAnswerAndDeclinedReplacements()

        val alreadyProcessedKeys = waiting.map { it.event.id to it.absentUserId }.toSet()

        val filteredToProcess =
            rawToProcess.filter { (it.event.id to it.absentUserId) !in alreadyProcessedKeys }

        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                toProcess = filteredToProcess,
                waitingForAnswer = waiting,
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
