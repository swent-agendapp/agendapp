package com.android.sample.ui.calendar.addEvent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.createEvent
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

/**
 * UI state representing the current draft of an event being created.
 *
 * This state is consumed by composables and mutated only via [AddEventViewModel].
 *
 * @property title The title of the event.
 * @property description The event description.
 * @property startInstant The selected start date/time of the event.
 * @property endInstant The selected end date/time of the event.
 * @property recurrenceEndInstant End date/time of the recurrence rule (if applicable).
 * @property recurrenceMode Defines recurrence type (one-time, weekly, etc.).
 * @property participants Set of selected participant identifiers.
 * @property color Visual tag color of the event.
 * @property errorMsg Optional error message for UI display.
 * @property draftEvent Event instance used for preview/confirmation before saving.
 * @property step Current UI step in the event creation wizard.
 */
data class AddCalendarEventUIState(
    val title: String = "",
    val description: String = "",
    val startInstant: Instant = Instant.now(),
    val endInstant: Instant = Instant.now().plus(Duration.ofHours(1)),
    val recurrenceEndInstant: Instant? = null,
    val recurrenceMode: RecurrenceStatus = RecurrenceStatus.OneTime,
    val participants: Set<User> = emptySet(),
    val category: EventCategory = EventCategory.defaultCategory(),
    val errorMsg: String? = null,
    val draftEvent: Event = createEvent(organizationId = "").first(),
    val step: AddEventStep = AddEventStep.TITLE_AND_DESC,
    val users: List<User> = emptyList(),
)

/** Steps in the multi-screen Add Event flow. */
enum class AddEventStep {
  TITLE_AND_DESC,
  TIME_AND_RECURRENCE,
  ATTENDEES,
  CONFIRMATION
}

/**
 * ViewModel responsible for managing the multi-step Add Event flow.
 *
 * Responsibilities:
 * - Maintain draft event state
 * - Validate user input
 * - Build `Event` objects via [createEvent]
 * - Persist events using [EventRepository]
 *
 * The UI interacts with this ViewModel exclusively through exposed methods such as [setTitle],
 * [setStartInstant], and [addEvent].
 */
class AddEventViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  private val _uiState = MutableStateFlow(AddCalendarEventUIState())
  /** Public immutable UI state observed by composables. */
  val uiState: StateFlow<AddCalendarEventUIState> = _uiState.asStateFlow()

  // Wrap for brevity
  private fun requireOrgId(): String = selectedOrganizationViewModel.getSelectedOrganizationId()

  init {
    viewModelScope.launch {
      val selectedOrga = requireOrgId()
      val userIds = userRepository.getMembersIds(selectedOrga)
      val users = userRepository.getUsersByIds(userIds)
      _uiState.update { it.copy(users = users) }
    }
  }

  /**
   * Loads a draft event instance based on the current UI field values.
   *
   * This method is used when preparing a preview before final confirmation.
   */
  fun loadDraftEvent() {
    val state = _uiState.value

    val draftEvent =
        createEvent(
                repository = eventRepository,
                organizationId = "",
                title = state.title,
                description = state.description,
                startDate = state.startInstant,
                endDate = state.endInstant,
                cloudStorageStatuses = emptySet(),
                personalNotes = "",
                participants = state.participants.map { it.id }.toSet(),
                category = state.category,
                recurrence = state.recurrenceMode,
                endRecurrence = state.recurrenceEndInstant ?: state.endInstant)
            .first()

    _uiState.update { it.copy(draftEvent = draftEvent) }
  }

  /**
   * Creates and persists one or more events based on the current UI state.
   *
   * Some recurrence types produce multiple physical events, all returned by [createEvent]. Each
   * resulting event is persisted independently.
   *
   * A valid organization must be selected; otherwise an exception is thrown.
   */
  fun addEvent() {
    val orgId = requireOrgId()

    val state = _uiState.value

    val newEvents =
        createEvent(
            organizationId = orgId,
            repository = eventRepository,
            title = state.title,
            description = state.description,
            startDate = state.startInstant,
            endDate = state.endInstant,
            cloudStorageStatuses = emptySet(),
            personalNotes = "",
            category = state.category,
            participants = state.participants.map { it.id }.toSet(),
            recurrence = state.recurrenceMode,
            endRecurrence = state.recurrenceEndInstant ?: state.endInstant)

    newEvents.forEach { addEventToRepository(it) }
  }

  /**
   * Persists a single [Event] into the repository.
   *
   * Errors are caught and surfaced to the UI through [errorMsg].
   */
  fun addEventToRepository(event: Event) {
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()

        eventRepository.insertEvent(orgId = orgId, item = event)
      } catch (_: IllegalStateException) {
        _uiState.update { it.copy(errorMsg = "No organization selected") }
      } catch (_: Exception) {
        _uiState.update { it.copy(errorMsg = "Unexpected error while creating the event") }
      }
    }
  }

  /** @return `true` if the event title field is empty. */
  fun titleIsBlank() = _uiState.value.title.isBlank()

  /** @return `true` if the event description field is empty. */
  fun descriptionIsBlank() = _uiState.value.description.isBlank()

  /** @return `true` if the start time occurs after the end time. */
  fun startTimeIsAfterEndTime() = _uiState.value.startInstant.isAfter(_uiState.value.endInstant)

  /**
   * @return `true` if the recurrence mode is active and the start time occurs after the recurrence
   *   end time.
   */
  fun startTimeIsAfterEndRecurrenceTime(): Boolean {
    val state = _uiState.value
    val end = state.recurrenceEndInstant ?: return false
    return state.recurrenceMode != RecurrenceStatus.OneTime && state.startInstant.isAfter(end)
  }

  /**
   * @return `true` if all essential fields are valid.
   *
   * Validation rules:
   * - Title must not be blank
   * - Description must not be blank
   * - Start time must precede end time
   */
  fun allFieldsValid() =
      !(titleIsBlank() ||
          descriptionIsBlank() ||
          startTimeIsAfterEndTime() ||
          recurrenceEndIsMissing())

  /** Advances the add-event wizard to the next step, if any. */
  fun nextStep() {
    val steps = AddEventStep.entries
    val index = steps.indexOf(_uiState.value.step)
    if (index < steps.lastIndex) {
      _uiState.update { it.copy(step = steps[index + 1]) }
    }
  }

  /** Moves the add-event wizard to the previous step, if applicable. */
  fun previousStep() {
    val steps = AddEventStep.entries
    val index = steps.indexOf(_uiState.value.step)
    if (index > 0) {
      _uiState.update { it.copy(step = steps[index - 1]) }
    }
  }

  /** Sets a new recurrence mode for the event draft. */
  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.update { state ->
      val newEnd =
          when (mode) {
            RecurrenceStatus.OneTime -> null
            else ->
                if (state.recurrenceMode == RecurrenceStatus.OneTime) null
                else state.recurrenceEndInstant
          }

      state.copy(recurrenceMode = mode, recurrenceEndInstant = newEnd)
    }
  }

  /** Updates the event title. */
  fun setTitle(title: String) {
    _uiState.update { it.copy(title = title) }
  }

  /** Updates the event description. */
  fun setDescription(description: String) {
    _uiState.update { it.copy(description = description) }
  }

  /** Updates the event start time. */
  fun setStartInstant(instant: Instant) {
    _uiState.update { it.copy(startInstant = instant) }
  }

  /** Updates the event end time. */
  fun setEndInstant(instant: Instant) {
    _uiState.update { it.copy(endInstant = instant) }
  }

  /** Updates the recurrence end time. */
  fun setRecurrenceEndTime(instant: Instant) {
    _uiState.update { it.copy(recurrenceEndInstant = instant) }
  }

  /** Updates the visual color tag for the event. */
  fun setCategory(category: EventCategory) {
    _uiState.update { it.copy(category = category) }
  }

  /** Adds a participant to the event draft. */
  fun addParticipant(participant: User) {
    val updated = _uiState.value.participants + participant
    _uiState.update { it.copy(participants = updated) }
  }

  /** Removes a participant from the event draft. */
  fun removeParticipant(participant: User) {
    val updated = _uiState.value.participants - participant
    _uiState.update { it.copy(participants = updated) }
  }

  /**
   * Resets the entire event draft to its initial state.
   *
   * Useful when the user cancels or restarts the creation flow.
   */
  fun resetUiState() {
    _uiState.value = AddCalendarEventUIState()
  }

  fun recurrenceEndIsMissing() =
      _uiState.value.recurrenceMode != RecurrenceStatus.OneTime &&
          _uiState.value.recurrenceEndInstant == null
}
