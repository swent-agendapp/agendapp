package com.android.sample.ui.replacement.mainPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryProvider
import com.android.sample.model.replacement.ReplacementStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Assisted by AI

/**
 * Steps for the employee-side replacement flow.
 *
 * LIST -> see all replacement requests where the user is substitute CREATE_OPTIONS -> choose
 * between "select event" or "date range" SELECT_EVENT -> (future) select one event in a calendar
 * SELECT_DATE_RANGE -> pick a date range; multiple replacements will be created
 */
enum class ReplacementEmployeeStep {
  LIST,
  SELECT_EVENT,
  SELECT_DATE_RANGE
}

/** UI state for the employee replacement flow. */
data class ReplacementEmployeeUiState(
    val step: ReplacementEmployeeStep = ReplacementEmployeeStep.LIST,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val incomingRequests: List<Replacement> = emptyList(),

    // Creation via "select event"
    val selectedEventId: String? = null,

    // Creation via "date range"
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    // For debugging / future UI feedback
    val lastCreatedReplacements: List<Replacement> = emptyList()
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
    myUserId: String = "EMP001"
) : ViewModel() {

  // Later: replace with real authenticated user id
  private val currentUserId: String = myUserId

  private val _uiState = MutableStateFlow(ReplacementEmployeeUiState())
  val uiState: StateFlow<ReplacementEmployeeUiState> = _uiState.asStateFlow()

  init {
    refreshIncomingRequests()
  }

  // ---------------------------------------------------------------------------
  //  Loading / refreshing
  // ---------------------------------------------------------------------------

  // Only for preview/testing purposes
  fun forceStepForPreview(step: ReplacementEmployeeStep) {
    _uiState.value = _uiState.value.copy(step = step)
  }

  fun createReplacementForEvent(event: Event) {

    viewModelScope.launch {
      val r =
          Replacement(
              absentUserId = currentUserId,
              substituteUserId = "",
              event = event,
              status = ReplacementStatus.ToProcess)
      replacementRepository.insertReplacement(r)
      refreshIncomingRequests()
    }
  }

  fun createReplacementsForDateRange(start: Instant, end: Instant) {

    viewModelScope.launch {
      val events = eventRepository.getEventsBetweenDates(start, end)
      events.forEach { e ->
        val r =
            Replacement(
                absentUserId = currentUserId,
                substituteUserId = "",
                event = e,
                status = ReplacementStatus.ToProcess)
        replacementRepository.insertReplacement(r)
      }
      refreshIncomingRequests()
    }
  }

  /** Loads all replacement requests where the user is the **substitute**. */
  fun refreshIncomingRequests() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val list =
            replacementRepository.getReplacementsBySubstituteUser(currentUserId).filter {
              it.status == ReplacementStatus.WaitingForAnswer
            }
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

  // ---------------------------------------------------------------------------
  //  List actions: accept / refuse
  // ---------------------------------------------------------------------------

  /** Mark a replacement as **Accepted**. */
  fun acceptRequest(id: String) {
    updateRequestStatus(id, ReplacementStatus.Accepted)
  }

  /** Mark a replacement as **Declined**. */
  fun refuseRequest(id: String) {
    updateRequestStatus(id, ReplacementStatus.Declined)
  }

  private fun updateRequestStatus(id: String, newStatus: ReplacementStatus) {
    viewModelScope.launch {
      try {
        val existing = replacementRepository.getReplacementById(id) ?: return@launch
        val updated = existing.copy(status = newStatus)
        replacementRepository.updateReplacement(id, updated)
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

  // ---------------------------------------------------------------------------
  //  "Select Event" path
  // ---------------------------------------------------------------------------

  /** Called when the user selects a specific event (from calendar UI, to be implemented). */
  fun setSelectedEvent(eventId: String) {
    _uiState.value = _uiState.value.copy(selectedEventId = eventId)
  }

  /** Creates a replacement for the currently selected event. */
  fun confirmSelectedEventAndCreateReplacement() {
    val eventId = _uiState.value.selectedEventId ?: return
    viewModelScope.launch {
      try {
        val event: Event =
            eventRepository.getEventById(eventId)
                ?: run {
                  _uiState.value = _uiState.value.copy(errorMessage = "Event $eventId not found.")
                  return@launch
                }

        val replacement =
            Replacement(
                // Employee is asking to be replaced → they are the absent user
                absentUserId = currentUserId,
                // Substitute not decided yet → empty string by convention
                substituteUserId = "",
                event = event,
                status = ReplacementStatus.ToProcess)

        replacementRepository.insertReplacement(replacement)

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
    val start = _uiState.value.startDate
    val end = _uiState.value.endDate
    if (start == null || end == null) return

    viewModelScope.launch {
      try {
        val startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val eventsInRange = eventRepository.getEventsBetweenDates(startInstant, endInstant)

        val created =
            eventsInRange.map { event ->
              Replacement(
                  absentUserId = currentUserId,
                  substituteUserId = "",
                  event = event,
                  status = ReplacementStatus.ToProcess)
            }

        created.forEach { replacementRepository.insertReplacement(it) }

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
}
