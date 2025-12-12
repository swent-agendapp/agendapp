package com.android.sample.ui.hourRecap

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI State for the Hour Recap screen. */
data class HourRecapUiState(
    val workedHours: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

/**
 * ViewModel for the Hour Recap feature.
 *
 * This ViewModel is fully independent from CalendarViewModel and focuses only on Worked Hours
 * generation logic.
 */
class HourRecapViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    selectedOrganizationFlow: StateFlow<String?>? = null,
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  // Only used to get the current organization ID
  private val orgIdFlow: StateFlow<String?> =
      selectedOrganizationFlow ?: selectedOrganizationViewModel.selectedOrganizationId

  private val _uiState = MutableStateFlow(HourRecapUiState())
  val uiState: StateFlow<HourRecapUiState> = _uiState

  /** Sets an error message to be displayed. */
  fun setErrorMsg(message: String) {
    _uiState.value = _uiState.value.copy(errorMsg = message)
  }

  /** Clears the error message. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /** Generates worked hours recap for all employees between two dates. */
  fun calculateWorkedHours(start: Instant, end: Instant) {
    val orgId = orgIdFlow.value
    if (orgId == null) {
      _uiState.value = _uiState.value.copy(errorMsg = "No organization selected")
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      try {
        val result = eventRepository.calculateWorkedHours(orgId, start, end)
        val users = userRepository.getUsersByIds(result.map { it.first })
        val userMap = users.associateBy { it.id }
        val finalList: List<Pair<String, Double>> =
            result.mapNotNull { (userId, value) ->
              userMap[userId]?.let { user -> user.display() to value }
            }
        _uiState.update { it.copy(workedHours = finalList, isLoading = false) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, errorMsg = "Failed to calculate worked hours: ${e.message}")
        }
      }
    }
  }

  /** Allows test code to override worked hours data. */
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal fun setTestWorkedHours(hours: List<Pair<String, Double>>) {
    _uiState.update { it.copy(workedHours = hours) }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { HourRecapViewModel() }
    }
  }
}
