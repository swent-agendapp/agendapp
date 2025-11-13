package com.android.sample.ui.replacement.organize

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.User
import com.android.sample.model.authorization.AuthorizationService
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.organizations.OrganizationRepository
import com.android.sample.model.organizations.OrganizationRepositoryProvider
import com.android.sample.model.organizations.mockOrganizations.getMockOrganizations
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryProvider
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Assisted by AI

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

enum class ReplacementOrganizeStep {
  SelectSubstitute,
  SelectEvents,
  SelectDateRange,
  SelectProcessMoment
}

class ReplacementOrganizeViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val replacementRepository: ReplacementRepository =
        ReplacementRepositoryProvider.repository,
    private val authz: AuthorizationService = AuthorizationService()
) : ViewModel() {
  private val _uiState = MutableStateFlow(ReplacementOrganizeUIState())

  /** Public immutable state that the UI observes. */
  val uiState: StateFlow<ReplacementOrganizeUIState> = _uiState.asStateFlow()

  fun loadOrganizationMembers() {
    viewModelScope.launch {
      val currentOrganization = getMockOrganizations().last()
      // Later replace with actual user
      //    val currentUser =
      //        authRepository.getCurrentUser()
      //            ?: throw IllegalStateException(
      //                "There must be a logged in user to load organization members")
      //          val members =
      //              organizationRepository.getMembersOfOrganization(
      //                  organizationId = currentOrganization.id,
      //                  user = currentUser
      //              )

      _uiState.value = _uiState.value.copy(memberList = currentOrganization.members)
    }
  }

  fun addReplacement() {
    val state = uiState.value
    val absentMember = state.selectedMember

    if (absentMember == null) {
      _uiState.value = state.copy(errorMsg = "No absent member selected.")
      return
    }
    viewModelScope.launch {
      val selectedEvents = state.selectedEvents
      val events: List<Event> =
          selectedEvents.ifEmpty {
            if (!dateRangeValid()) {
              _uiState.value = state.copy(errorMsg = "Invalid date range. End must be after start.")
              return@launch
            }
            eventRepository.getEventsBetweenDates(
                startDate = state.startInstant, endDate = state.endInstant)
          }

      if (events.isEmpty()) {
        _uiState.value = state.copy(errorMsg = "No events available to create replacements.")
        return@launch
      }

      events.forEach { event ->
        val replacement =
            Replacement(
                absentUserId = absentMember.id,
                substituteUserId = "",
                event = event,
            )

        addReplacementToRepository(replacement)
      }
    }
  }

  fun addReplacementToRepository(replacement: Replacement) {
    viewModelScope.launch {
      try {
        val allowed = runCatching { authz.requireAdmin() }.isSuccess
        if (!allowed) {
          _uiState.value =
              _uiState.value.copy(errorMsg = "You are not allowed to organize replacements !")
          return@launch
        }

        replacementRepository.insertReplacement(replacement)
      } catch (e: Exception) {
        Log.e("ReplacementOrganizeVM", "Error adding event: ${e.message}")
        _uiState.value = _uiState.value.copy(errorMsg = "Unexpected error while creating the event")
      }
    }
  }

  fun dateRangeValid() = uiState.value.endInstant.isAfter(uiState.value.startInstant)

  /** Navigates directly to a specific step in the replacement organization flow. */
  fun goToStep(step: ReplacementOrganizeStep) {
    _uiState.value = _uiState.value.copy(step = step)
  }

  /** Sets the member search query used to filter the member list. */
  fun setMemberSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(memberSearchQuery = query)
  }

  /** Sets the selected member for whom the replacement is being organized. */
  fun setSelectedMember(member: User) {
    _uiState.value = _uiState.value.copy(selectedMember = member)
  }

  /** Adds an event to the list of selected events for replacement. */
  fun addSelectedEvent(event: Event) {
    if (_uiState.value.selectedEvents.any { it.id == event.id }) return
    _uiState.value = _uiState.value.copy(selectedEvents = _uiState.value.selectedEvents + event)
  }

  /** Removes an event from the list of selected events for replacement. */
  fun removeSelectedEvent(event: Event) {
    _uiState.value =
        _uiState.value.copy(
            selectedEvents = _uiState.value.selectedEvents.filterNot { it.id == event.id })
  }

  fun toggleSelectedEvent(event: Event) {
    if (uiState.value.selectedEvents.contains(event)) {
      removeSelectedEvent(event)
    } else {
      addSelectedEvent(event)
    }
  }

  /** Sets the start instant for the replacement date range. */
  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }

  /** Sets the end instant for the replacement date range. */
  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }

  fun resetUiState() {
    _uiState.value = ReplacementOrganizeUIState()
  }
}
