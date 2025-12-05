package com.android.sample.ui.replacement.organize

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.organization.data.getMockOrganizations
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryProvider
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state representing all fields involved in the replacement-organization flow.
 *
 * This state is observed by composables and updated exclusively through
 * [ReplacementOrganizeViewModel].
 *
 * @property memberSearchQuery Current user input for filtering organization members.
 * @property selectedMember The absent member for whom replacements will be organized.
 * @property memberList All available members from the current organization.
 * @property selectedEvents Events manually selected by the user for replacement.
 * @property startInstant Start of the event lookup range (when auto-selecting events).
 * @property endInstant End of the event lookup range.
 * @property step Current UI step in the replacement workflow.
 * @property errorMsg Optional error message displayed in the UI.
 */
data class ReplacementOrganizeUIState(
    val memberSearchQuery: String = "",
    val selectedMember: User? = null,
    val memberList: List<User> = emptyList(),
    val selectedEvents: List<Event> = emptyList(),
    val startInstant: Instant = Instant.now(),
    val endInstant: Instant = Instant.now(),
    val step: ReplacementOrganizeStep = ReplacementOrganizeStep.SelectSubstitute,
    val errorMsg: String? = null
)

/**
 * Steps in the replacement-organization multi-step flow.
 *
 * This ordering defines the user journey:
 * 1. Select an absent substitute
 * 2. Select events or choose a date range
 * 3. Confirm the processing moment
 */
enum class ReplacementOrganizeStep {
  SelectSubstitute,
  SelectEvents,
  SelectDateRange
}

/**
 * ViewModel for managing the process of organizing replacements for absent members.
 *
 * Responsibilities:
 * - Load and expose organization members
 * - Track user input: absent member, selected events, date ranges
 * - Validate user selections
 * - Auto-select events based on date ranges
 * - Build and persist [Replacement] entries
 *
 * UI interacts with this ViewModel through exposed methods like:
 * - [setSelectedMember]
 * - [toggleSelectedEvent]
 * - [setStartInstant]
 * - [addReplacement]
 *
 * All ViewModel state is exposed through a [StateFlow] of [ReplacementOrganizeUIState].
 */
class ReplacementOrganizeViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val replacementRepository: ReplacementRepository =
        ReplacementRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) : ViewModel() {

  /** Internal mutable UI state. */
  private val _uiState = MutableStateFlow(ReplacementOrganizeUIState())

  /** Public immutable UI state observed by the UI. */
  val uiState: StateFlow<ReplacementOrganizeUIState> = _uiState.asStateFlow()

  /**
   * Loads all members from the selected organization.
   *
   * Currently uses mock data until real backend integration is implemented.
   */
  fun loadOrganizationMembers() {
    viewModelScope.launch {
      val currentOrganization = getMockOrganizations().last()
      _uiState.update { it.copy(memberList = currentOrganization.members) }
    }
  }

  /**
   * Creates replacement entries based on the current UI state.
   *
   * Event selection logic:
   * - If the user manually selected events → use those.
   * - Otherwise, fetch events between [startInstant] and [endInstant].
   *
   * Validation:
   * - A selected absent member is required
   * - If auto-selecting events, date range must be valid
   *
   * For each event found, a [Replacement] object is constructed and stored.
   */
  fun addReplacement(
      status: ReplacementStatus = ReplacementStatus.ToProcess,
      onReplacementsCreated: (List<Replacement>) -> Unit = {}
  ) {
    viewModelScope.launch {
      val state = uiState.value
      val absentMember = state.selectedMember

      if (absentMember == null) {
        _uiState.update { it.copy(errorMsg = "No absent member selected.") }
        return@launch
      }

      val events: List<Event> =
          if (state.selectedEvents.isNotEmpty()) {
            state.selectedEvents
          } else {
            if (!dateRangeValid()) {
              _uiState.update { it.copy(errorMsg = "Invalid date range. End must be after start.") }
              return@launch
            }

            val orgId = selectedOrganizationViewModel.selectedOrganizationId.value
            require(orgId != null) { "Organization must be selected to fetch events." }

            eventRepository.getEventsBetweenDates(
                orgId = orgId, startDate = state.startInstant, endDate = state.endInstant)
          }

      if (events.isEmpty()) {
        _uiState.update { it.copy(errorMsg = "No events available to create replacements.") }
        return@launch
      }

      val replacements =
          events.map { event ->
            Replacement(
                absentUserId = absentMember.id,
                substituteUserId = "",
                event = event,
                status = status,
            )
          }

      replacements.forEach { replacement -> addReplacementToRepository(replacement) }

      onReplacementsCreated(replacements)
    }
  }

  /**
   * Persists a single [Replacement] entry into the repository.
   *
   * Errors are caught and surfaced to the UI via [errorMsg].
   */
  private suspend fun addReplacementToRepository(replacement: Replacement) {
    try {
      val orgId = selectedOrganizationViewModel.selectedOrganizationId.value
      require(orgId != null) { "Organization must be selected to create a replacement." }

      replacementRepository.insertReplacement(orgId = orgId, item = replacement)
    } catch (e: Exception) {
      Log.e("ReplacementOrganizeVM", "Error adding replacement: ${e.message}")
      _uiState.update { it.copy(errorMsg = "Unexpected error while creating the replacement.") }
    }
  }

  /** @return `true` if the date range is valid and not in the past. */
  fun dateRangeValid(): Boolean {
    val state = uiState.value

    val startDate = state.startInstant.atZone(ZoneId.systemDefault()).toLocalDate()
    val endDate = state.endInstant.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()

    return !startDate.isBefore(today) && !endDate.isBefore(today) && !endDate.isBefore(startDate)
  }
  /** Navigates to a specific step in the multi-step flow. */
  fun goToStep(step: ReplacementOrganizeStep) {
    _uiState.update { it.copy(step = step) }
  }

  /** Updates the query used for filtering members. */
  fun setMemberSearchQuery(query: String) {
    _uiState.update { it.copy(memberSearchQuery = query) }
  }

  /** Sets or clears the absent member for whom replacements are being organized. */
  fun setSelectedMember(member: User?) {
    _uiState.update { it.copy(selectedMember = member) }
  }

  /** Adds an event to the selected event list if it is not already present. */
  fun addSelectedEvent(event: Event) {
    val events = _uiState.value.selectedEvents
    if (events.any { it.id == event.id }) return
    _uiState.update { it.copy(selectedEvents = events + event) }
  }

  /** Removes an event from the selected event list. */
  fun removeSelectedEvent(event: Event) {
    _uiState.update {
      it.copy(selectedEvents = it.selectedEvents.filterNot { e -> e.id == event.id })
    }
  }

  /** Toggles an event in or out of the selection list. */
  fun toggleSelectedEvent(event: Event) {
    if (uiState.value.selectedEvents.contains(event)) {
      removeSelectedEvent(event)
    } else {
      addSelectedEvent(event)
    }
  }

  /** Sets the beginning of the date range used to auto-select events. */
  fun setStartInstant(instant: Instant) {
    _uiState.update { it.copy(startInstant = instant) }
  }

  /** Sets the end of the date range used to auto-select events. */
  fun setEndInstant(instant: Instant) {
    _uiState.update { it.copy(endInstant = instant) }
  }

  /** Resets all UI state fields to their default values. */
  fun resetUiState() {
    _uiState.value = ReplacementOrganizeUIState()
  }
}
