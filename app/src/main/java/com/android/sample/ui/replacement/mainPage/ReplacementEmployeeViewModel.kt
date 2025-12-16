package com.android.sample.ui.replacement.mainPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.data.global.providers.EventRepositoryProvider
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.Event
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
import kotlinx.coroutines.launch

interface ReplacementEmployeeActions {
  fun loadReplacementForProcessing(
      replacementId: String,
      onResult: (replacement: Replacement?, users: List<User>) -> Unit,
  )

  fun sendRequestsForPendingReplacement(
      replacementId: String,
      selectedSubstitutes: List<User>,
      onFinished: () -> Unit,
  )
}

// Assisted by AI
enum class ReplacementEmployeeLastAction {
  ACCEPTED,
  REFUSED,
}
/**
 * Steps for the employee-side replacement flow.
 *
 * LIST -> see all replacement requests where the user is substitute CREATE_OPTIONS -> choose
 * between "select event" or "date range" SELECT_EVENT -> (future) select one event in a calendar
 * SELECT_DATE_RANGE -> pick a date range; multiple replacements will be created
 */
enum class ReplacementEmployeeStep {
  LIST,
  CREATE_OPTIONS,
  SELECT_EVENT,
  SELECT_DATE_RANGE
}

/** UI state for the employee replacement flow. */
data class ReplacementEmployeeUiState(
    val step: ReplacementEmployeeStep = ReplacementEmployeeStep.LIST,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val incomingRequests: List<Replacement> = emptyList(),
    val allUser: List<User> = emptyList(),
    val isAdmin: Boolean = false,

    // Creation via "select event"
    val selectedEventId: String? = null,

    // Creation via "date range"
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    // For debugging / future UI feedback
    val lastCreatedReplacements: List<Replacement> = emptyList(),
    // Requests currently being processed (accept/refuse in progress)
    val processingRequestIds: Set<String> = emptySet(),
    // Last user action on a request (for UI feedback)
    val lastAction: ReplacementEmployeeLastAction? = null,
)

/**
 * ViewModel for the **employee-side replacement flow**.
 *
 * Responsibilities:
 * - Load replacement requests where the current user acts as **substitute**.
 * - Allow the user to **accept** or **refuse** a replacement.
 * - Drive the "ask to be replaced" flow:
 *     - via **selecting a single event**
 *     - or via **selecting a date range** → create multiple replacement requests.
 *
 * NOTE: This VM currently uses a placeholder `currentUserId`. It should be wired to the
 * authentication repository later (e.g., AuthRepository.getCurrentUser()).
 */
class ReplacementEmployeeViewModel(
    private val replacementRepository: ReplacementRepository =
        ReplacementRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel(), ReplacementEmployeeActions {

  private val errorMessageNoAuthenticated = "No authenticated user found."
  private val _userState = MutableStateFlow(authRepository.getCurrentUser())
  val userState: StateFlow<User?> = _userState
  private val _uiState = MutableStateFlow(ReplacementEmployeeUiState())
  val uiState: StateFlow<ReplacementEmployeeUiState> = _uiState.asStateFlow()

  private val selectedOrganizationId: StateFlow<String?> =
      selectedOrganizationViewModel.selectedOrganizationId

  init {
    refreshIncomingRequests()
    refreshAllUsers()
    initCurrentUser()
  }

  // ---------------------------------------------------------------------------
  //  Helpers
  // ---------------------------------------------------------------------------

  // Wrap for brevity
  private fun requireOrgId(): String = selectedOrganizationViewModel.getSelectedOrganizationId()

  // ---------------------------------------------------------------------------
  //  Loading / refreshing
  // ---------------------------------------------------------------------------

  // Only for preview/testing purposes
  fun forceStepForPreview(step: ReplacementEmployeeStep) {
    _uiState.value = _uiState.value.copy(step = step)
  }

  fun createReplacementForEvent(event: Event) {

    viewModelScope.launch {
      val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)
      val r =
          Replacement(
              absentUserId = user.id,
              substituteUserId = "",
              event = event,
              status = ReplacementStatus.ToProcess)

      val orgId = requireOrgId()
      replacementRepository.insertReplacement(orgId = orgId, item = r)
      refreshIncomingRequests()
    }
  }

  fun createReplacementsForDateRange(start: Instant, end: Instant) {

    val orgId = requireOrgId()

    viewModelScope.launch {
      val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)

      val events =
          eventRepository.getEventsBetweenDates(orgId = orgId, startDate = start, endDate = end)
      events.forEach { e ->
        val r =
            Replacement(
                absentUserId = user.id,
                substituteUserId = "",
                event = e,
                status = ReplacementStatus.ToProcess)

        replacementRepository.insertReplacement(orgId = orgId, item = r)
      }
      refreshIncomingRequests()
    }
  }

  /** Loads all replacement requests where the user is the **substitute**. */
  fun refreshIncomingRequests() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)
        val orgId = requireOrgId()
        val list =
            replacementRepository.getReplacementsBySubstituteUser(
                orgId = orgId, userId = user.id) // Substitute side
        _uiState.value =
            _uiState.value.copy(incomingRequests = list, isLoading = false, errorMessage = null)
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error loading incoming replacements", e)
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load replacements: ${e.message}")
      }
    }
  }

  fun refreshAllUsers() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val orgId = requireOrgId()
        val listId = userRepository.getMembersIds(organizationId = orgId)
        val list = userRepository.getUsersByIds(userIds = listId)
        _uiState.value = _uiState.value.copy(allUser = list, isLoading = false, errorMessage = null)
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error loading incoming users", e)
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load users: ${e.message}")
      }
    }
  }

  fun initCurrentUser() {
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()
        val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)
        val adminsId = userRepository.getAdminsIds(organizationId = orgId)
        val isAdmin = adminsId.contains(user.id)
        _uiState.value =
            _uiState.value.copy(isAdmin = isAdmin, isLoading = false, errorMessage = null)
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error loading user's right", e)
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load users: ${e.message}")
      }
    }
  }

  // ---------------------------------------------------------------------------
  //  List actions: accept / refuse
  // ---------------------------------------------------------------------------

  /**
   * Mark a replacement as Accepted, update the event with the new teacher, and close other
   * requests.
   */
  fun acceptRequest(id: String) {
    viewModelScope.launch {
      _uiState.value =
          _uiState.value.copy(
              processingRequestIds = _uiState.value.processingRequestIds + id,
          )
      try {
        val orgId = requireOrgId()
        val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)

        val replacement =
            replacementRepository.getReplacementById(
                orgId = orgId,
                itemId = id,
            ) ?: return@launch

        val event = replacement.event

        val updatedParticipants = event.participants.minus(replacement.absentUserId).plus(user.id)

        val updatedEvent =
            event.copy(
                participants = updatedParticipants,
                version = System.currentTimeMillis(),
            )

        eventRepository.updateEvent(
            orgId = orgId,
            itemId = event.id,
            item = updatedEvent,
        )

        val accepted =
            replacement.copy(
                substituteUserId = user.id,
                status = ReplacementStatus.Accepted,
            )

        replacementRepository.updateReplacement(
            orgId = orgId,
            itemId = accepted.id,
            item = accepted,
        )

        val allReplacements = replacementRepository.getAllReplacements(orgId)
        val othersForSameEvent =
            allReplacements.filter {
              it.event.id == event.id &&
                  it.id != id &&
                  it.status == ReplacementStatus.WaitingForAnswer
            }

        othersForSameEvent.forEach { other ->
          val declined = other.copy(status = ReplacementStatus.Declined)
          replacementRepository.updateReplacement(
              orgId = orgId,
              itemId = other.id,
              item = declined,
          )
        }

        refreshIncomingRequests()

        _uiState.value =
            _uiState.value.copy(
                lastAction = ReplacementEmployeeLastAction.ACCEPTED,
            )
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error accepting replacement", e)
        _uiState.value =
            _uiState.value.copy(
                errorMessage = "Could not accept replacement: ${e.message}",
            )
      } finally {
        _uiState.value =
            _uiState.value.copy(
                processingRequestIds = _uiState.value.processingRequestIds - id,
            )
      }
    }
  }

  /** Mark a replacement as **Declined**. */
  fun refuseRequest(id: String) {
    viewModelScope.launch {
      _uiState.value =
          _uiState.value.copy(
              processingRequestIds = _uiState.value.processingRequestIds + id,
          )
      try {
        updateRequestStatus(id, ReplacementStatus.Declined)
        refreshIncomingRequests()

        _uiState.value =
            _uiState.value.copy(
                lastAction = ReplacementEmployeeLastAction.REFUSED,
            )
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error refusing replacement", e)
        _uiState.value =
            _uiState.value.copy(
                errorMessage = "Could not update replacement: ${e.message}",
            )
      } finally {
        _uiState.value =
            _uiState.value.copy(
                processingRequestIds = _uiState.value.processingRequestIds - id,
            )
      }
    }
  }

  fun clearLastAction() {
    _uiState.value = _uiState.value.copy(lastAction = null)
  }

  private fun updateRequestStatus(id: String, newStatus: ReplacementStatus) {
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()

        val existing =
            replacementRepository.getReplacementById(orgId = orgId, itemId = id) ?: return@launch
        val updated = existing.copy(status = newStatus)
        replacementRepository.updateReplacement(orgId = orgId, itemId = id, item = updated)
        refreshIncomingRequests()
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error updating replacement status", e)
        _uiState.value =
            _uiState.value.copy(errorMessage = "Could not update replacement: ${e.message}")
      }
    }
  }

  // ---------------------------------------------------------------------------
  //  Step navigation
  // ---------------------------------------------------------------------------

  /** From list → go to "create replacement" options. */
  fun goToCreateOptions() {
    _uiState.value =
        _uiState.value.copy(
            step = ReplacementEmployeeStep.CREATE_OPTIONS,
            errorMessage = null // clear previous errors
            )
  }

  /** From create options → go back to list. */
  fun backToList() {
    _uiState.value =
        _uiState.value.copy(
            step = ReplacementEmployeeStep.LIST,
            selectedEventId = null,
            startDate = null,
            endDate = null,
            errorMessage = null)
    refreshIncomingRequests()
  }

  /** From create options → select a specific event. */
  fun goToSelectEvent() {
    _uiState.value =
        _uiState.value.copy(
            step = ReplacementEmployeeStep.SELECT_EVENT,
            // reset previous choice
            selectedEventId = null,
            errorMessage = null)
  }

  /** From create options → select a date range. */
  fun goToSelectDateRange() {
    _uiState.value =
        _uiState.value.copy(
            step = ReplacementEmployeeStep.SELECT_DATE_RANGE,
            startDate = null,
            endDate = null,
            errorMessage = null)
  }

  /** From second-step screens → back to create options. */
  fun backToCreateOptions() {
    _uiState.value =
        _uiState.value.copy(step = ReplacementEmployeeStep.CREATE_OPTIONS, errorMessage = null)
  }

  // ---------------------------------------------------------------------------
  //  "Select Event" path
  // ---------------------------------------------------------------------------

  /** Called when the user selects a specific event (from calendar UI, to be implemented). */
  fun setSelectedEvent(eventId: String) {
    _uiState.value = _uiState.value.copy(selectedEventId = eventId)
  }

  /** Creates a replacement for the currently selected event. */
  fun confirmSelectedEventAndCreateReplacement() {
    val orgId = selectedOrganizationId.value ?: return
    val eventId = _uiState.value.selectedEventId ?: return
    viewModelScope.launch {
      try {
        val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)

        val event: Event =
            eventRepository.getEventById(orgId = orgId, itemId = eventId)
                ?: run {
                  _uiState.value = _uiState.value.copy(errorMessage = "Event $eventId not found.")
                  return@launch
                }

        val replacement =
            Replacement(
                // Employee is asking to be replaced → they are the absent user
                absentUserId = user.id,
                // Substitute not decided yet → empty string by convention
                substituteUserId = "",
                event = event,
                status = ReplacementStatus.ToProcess)

        val orgId = requireOrgId()

        replacementRepository.insertReplacement(orgId = orgId, item = replacement)

        _uiState.value =
            _uiState.value.copy(
                step = ReplacementEmployeeStep.LIST,
                selectedEventId = null,
                lastCreatedReplacements = listOf(replacement),
                errorMessage = null)

        refreshIncomingRequests()
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error creating replacement for event", e)
        _uiState.value =
            _uiState.value.copy(errorMessage = "Could not create replacement: ${e.message}")
      }
    }
  }

  // ---------------------------------------------------------------------------
  //  "Select Date Range" path  (Q2: multiple replacements)
  // ---------------------------------------------------------------------------

  fun setStartDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(startDate = date)
  }

  fun setEndDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(endDate = date)
  }

  /**
   * Confirm the selected date range:
   * - find all events within the range (Later: use EventRepository properly)
   * - create one [Replacement] per event where current user is the absent member
   */
  fun confirmDateRangeAndCreateReplacements() {
    val orgId = selectedOrganizationId.value ?: return

    val start = _uiState.value.startDate
    val end = _uiState.value.endDate
    if (start == null || end == null) return

    viewModelScope.launch {
      try {
        val user = userState.value ?: throw IllegalStateException(errorMessageNoAuthenticated)

        val startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val eventsInRange =
            eventRepository.getEventsBetweenDates(
                orgId = orgId, startDate = startInstant, endDate = endInstant)

        val eligibleEvents = eventsInRange.filter { event -> event.participants.contains(user.id) }

        val created =
            eligibleEvents.map { event ->
              Replacement(
                  absentUserId = user.id,
                  substituteUserId = "",
                  event = event,
                  status = ReplacementStatus.ToProcess)
            }

        val orgId = requireOrgId()

        created.forEach { replacementRepository.insertReplacement(orgId = orgId, item = it) }

        _uiState.value =
            _uiState.value.copy(
                step = ReplacementEmployeeStep.LIST,
                startDate = null,
                endDate = null,
                lastCreatedReplacements = created,
                errorMessage = null)

        refreshIncomingRequests()
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error creating replacements for date range", e)
        _uiState.value =
            _uiState.value.copy(errorMessage = "Could not create replacements: ${e.message}")
      }
    }
  }

  override fun loadReplacementForProcessing(
      replacementId: String,
      onResult: (replacement: Replacement?, users: List<User>) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()
        val replacement =
            replacementRepository.getReplacementById(
                orgId = orgId,
                itemId = replacementId,
            )
        val users =
            userRepository.getUsersByIds(
                userRepository.getMembersIds(
                    organizationId = orgId,
                ))
        onResult(replacement, users)
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error loading replacement $replacementId", e)
        onResult(null, _uiState.value.allUser)
      }
    }
  }

  override fun sendRequestsForPendingReplacement(
      replacementId: String,
      selectedSubstitutes: List<User>,
      onFinished: () -> Unit,
  ) {
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()

        val original =
            replacementRepository.getReplacementById(
                orgId = orgId,
                itemId = replacementId,
            )

        if (original == null) {
          Log.e("ReplacementEmployeeVM", "Original replacement not found for id=$replacementId")
          return@launch
        }

        selectedSubstitutes.forEach { substitute ->
          val request =
              Replacement(
                  absentUserId = original.absentUserId,
                  substituteUserId = substitute.id,
                  event = original.event,
                  status = ReplacementStatus.WaitingForAnswer,
              )

          replacementRepository.insertReplacement(
              orgId = orgId,
              item = request,
          )
        }

        replacementRepository.deleteReplacement(
            orgId = orgId,
            itemId = replacementId,
        )
      } catch (e: Exception) {
        Log.e("ReplacementEmployeeVM", "Error sending requests", e)
        _uiState.value =
            _uiState.value.copy(errorMessage = "Could not send replacement requests: ${e.message}")
      }
    }
    onFinished()
  }
}
