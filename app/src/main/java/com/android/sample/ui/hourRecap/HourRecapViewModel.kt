package com.android.sample.ui.hourRecap

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository.selectedOrganizationId
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HourRecapUIState(
    val errorMsg: String? = null,
    val workedHours: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
)

class HourRecapViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  private val _uiState = MutableStateFlow(HourRecapUIState())
  val uiState: StateFlow<HourRecapUIState> = _uiState.asStateFlow()
  /** Sets an error message in the UI state. */
  fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }
  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Calculates the total worked hours for each employee within a given time range.
   *
   * @param start The start of the time range.
   * @param end The end of the time range.
   */
  fun calculateWorkedHours(start: Instant, end: Instant) {
    val orgId = selectedOrganizationId.value
    if (orgId == null) {
      setErrorMsg("No organization selected")
      return
    }

    viewModelScope.launch {
      try {
        val workedHours = eventRepository.calculateWorkedHours(orgId, start, end)
        _uiState.value = _uiState.value.copy(workedHours = workedHours)
      } catch (e: Exception) {
        setErrorMsg("Failed to calculate worked hours: ${e.message}")
      }
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal fun setTestWorkedHours(hours: List<Pair<String, Double>>) {
    _uiState.value = _uiState.value.copy(workedHours = hours)
  }
}
