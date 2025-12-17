package com.android.sample.ui.calendar.editEvent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.data.global.providers.EventRepositoryProvider
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Assisted by AI
data class EditCalendarEventUIState(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val startInstant: Instant = Instant.now(),
    val endInstant: Instant = Instant.now().plus(Duration.ofHours(1)),
    val recurrenceMode: RecurrenceStatus = RecurrenceStatus.OneTime,
    val participants: Set<User> = emptySet(),
    val category: EventCategory = EventCategory.defaultCategory(),
    val notifications: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val users: List<User> = emptyList(),
    val step: EditEventStep = EditEventStep.MAIN,
    val isExtraEvent: Boolean = false,
)

/**
 * Enum representing the steps in the Edit Event flow.
 * - MAIN: The main event editing screen.
 * - ATTENDEES: The participants editing screen.
 */
enum class EditEventStep {
  MAIN,
  ATTENDEES
}

/**
 * ViewModel that manages the UI state for editing an existing calendar event.
 *
 * Responsibilities:
 * - Loading an existing event by ID.
 * - Managing edits to event fields (title, description, dates, recurrence, participants).
 * - Validating input fields.
 * - Saving changes back to the repository.
 * - Navigating between edit steps.
 */
class EditEventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {
  private val _uiState = MutableStateFlow(EditCalendarEventUIState())
  val uiState: StateFlow<EditCalendarEventUIState> = _uiState.asStateFlow()

  // Wrap for brevity
  private fun requireOrgId(): String = selectedOrganizationViewModel.getSelectedOrganizationId()

  init {
    loadUsers()
  }

  fun saveEditEventChanges() {
    viewModelScope.launch {
      val state = _uiState.value
      try {
        val orgId = requireOrgId()

        val updated =
            Event(
                id = state.eventId,
                organizationId = orgId,
                title = state.title,
                description = state.description,
                startDate = state.startInstant,
                endDate = state.endInstant,
                cloudStorageStatuses = emptySet(),
                locallyStoredBy = emptyList(),
                personalNotes = null,
                participants = state.participants.map { it.id }.toSet(),
                version = System.currentTimeMillis(),
                recurrenceStatus = state.recurrenceMode,
                hasBeenDeleted = false,
                category = state.category,
                location = null,
                isExtra = state.isExtraEvent)
        eventRepository.updateEvent(orgId = orgId, itemId = updated.id, item = updated)
      } catch (e: Exception) {
        Log.e("EditEventViewModel", "Error saving event changes: ${e.message}")
      }
    }
  }

  fun loadUsers() {
    viewModelScope.launch {
      val orgId = requireOrgId()

      val userIds = userRepository.getMembersIds(orgId)
      val users = userRepository.getUsersByIds(userIds)
      _uiState.update { it.copy(users = users) }
    }
  }

  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val orgId = requireOrgId()

        val event = eventRepository.getEventById(orgId = orgId, itemId = eventId)
        if (event != null) {
          _uiState.value =
              _uiState.value.copy(
                  eventId = event.id,
                  title = event.title,
                  description = event.description,
                  startInstant = event.startDate,
                  endInstant = event.endDate,
                  recurrenceMode = event.recurrenceStatus,
                  participants =
                      _uiState.value.users.filter { it.id in event.participants }.toSet(),
                  category = event.category,
                  isExtraEvent = event.isExtra,
                  isLoading = false)
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Event not found.")
        }
      } catch (e: Exception) {
        Log.e("EditEventViewModel", "Error loading event: ${e.message}")
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load event: ${e.message}")
      }
    }
  }

  // --- field updates ---
  fun setTitle(value: String) {
    _uiState.value = _uiState.value.copy(title = value)
  }

  fun setCategory(category: EventCategory) {
    _uiState.value = _uiState.value.copy(category = category)
  }

  fun setDescription(value: String) {
    _uiState.value = _uiState.value.copy(description = value)
  }

  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }

  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }

  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }

  fun setIsExtra(isExtra: Boolean) {
    _uiState.value = _uiState.value.copy(isExtraEvent = isExtra)
  }

  fun addParticipant(name: User) {
    _uiState.value = _uiState.value.copy(participants = _uiState.value.participants + name)
  }

  fun removeParticipant(name: User) {
    _uiState.value = _uiState.value.copy(participants = _uiState.value.participants - name)
  }

  // --- validation ---
  private fun titleIsBlank() = _uiState.value.title.isBlank()

  private fun descriptionIsBlank() = _uiState.value.description.isBlank()

  private fun startAfterEnd() = _uiState.value.startInstant.isAfter(_uiState.value.endInstant)

  fun allFieldsValid() = !(titleIsBlank() || descriptionIsBlank() || startAfterEnd())

  // --- step navigation: ONLY via uiState.step ---
  fun goToAttendeesStep() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.ATTENDEES)
  }

  fun goBackToMainStep() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.MAIN)
  }

  fun resetUiState() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.MAIN)
  }

  fun setEditStep(step: EditEventStep) {
    _uiState.value = _uiState.value.copy(step = step)
  }
}
