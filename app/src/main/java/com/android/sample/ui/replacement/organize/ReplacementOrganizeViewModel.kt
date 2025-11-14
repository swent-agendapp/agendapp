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

/**
 * UI state representing all fields involved in the replacement-organization flow.
 *
 * @property memberSearchQuery Current query string used for filtering organization members.
 * @property selectedMember The absent member selected for replacement.
 * @property memberList Full member list of the current organization.
 * @property selectedEvents Events explicitly selected by the user (optional if using date range).
 * @property startInstant Beginning of the date range for auto-selecting events.
 * @property endInstant End of the date range for auto-selecting events.
 * @property step Current step of the replacement-organize multi-step UI flow.
 * @property errorMsg Optional error message to surface to the UI.
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

/** Enumeration of the steps in the replacement-organization multi-step flow. */
enum class ReplacementOrganizeStep {
  SelectSubstitute,
  SelectEvents,
  SelectDateRange,
  SelectProcessMoment
}

/**
 * ViewModel managing the state and operations required to organize replacements for absent members.
 *
 * Responsibilities:
 * - Load organization members
 * - Allow selecting a member, selecting events, or choosing event ranges
 * - Validate date ranges
 * - Create and insert Replacement objects
 * - Enforce authorization for replacement creation
 *
 * The UI interacts only with the exposed methods (e.g., `setSelectedMember()`,
 * `toggleSelectedEvent()`, etc.) and observes state updates via [uiState].
 */
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

  /**
   * Loads all members of the user's organization.
   *
   * Currently uses mock organizations for development/testing.
   *
   * After loading, updates the member list in the UI state.
   */
  fun loadOrganizationMembers() {
    viewModelScope.launch {
      val currentOrganization = getMockOrganizations().last()
      // Later replace with actual user and organization
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

  /**
   * Creates replacements for the selected absent member.
   *
   * Event selection logic:
   * - If the user manually selected events → use them.
   * - If no manual selection → fetch events in the selected date range.
   *
   * Validation:
   * - Absent member must be selected
   * - Date range must be valid when auto-selecting events
   *
   * For each event, a [Replacement] object is constructed and passed to
   * [addReplacementToRepository].
   */
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

  /**
   * Persists a replacement into the repository after validating authorization.
   * - Ensures the caller has admin privileges.
   * - On failure, produces an error message instead of throwing.
   * - Catches unexpected exceptions and surfaces them to the UI.
   *
   * @param replacement The replacement entry to insert.
   */
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
        Log.e("ReplacementOrganizeVM", "Error adding replacement: ${e.message}")
        _uiState.value = _uiState.value.copy(errorMsg = "Unexpected error while creating the replacement")
      }
    }
  }

  /**
   * @return `true` if the end instant is strictly after the start instant.
   *
   * Used when auto-selecting events via date range.
   */
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
  /**
   * Toggles the selection state of an event:
   * - Adds it if not selected
   * - Removes it if already selected
   */
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

  /**
   * Resets all UI fields to their default values.
   *
   * Useful when:
   * - The user restarts the replacement organization flow
   * - The UI is closed or cancelled
   */
  fun resetUiState() {
    _uiState.value = ReplacementOrganizeUIState()
  }
}
