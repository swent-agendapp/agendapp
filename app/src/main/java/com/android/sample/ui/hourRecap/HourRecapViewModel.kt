package com.android.sample.ui.hourRecap

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.sample.data.global.providers.EventRepositoryProvider
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.Event
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI State for the Hour Recap screen. */
data class HourRecapEventEntry(
    val id: String,
    val title: String,
    val startDate: Instant,
    val endDate: Instant,
    val isPast: Boolean,
    val wasPresent: Boolean?,
    val wasReplaced: Boolean,
    val tookReplacement: Boolean,
    val categoryColor: Color,
    val isExtra: Boolean = false,
)

data class HourRecapUserRecap(
    val userId: String,
    val displayName: String,
    val completedHours: Double,
    val plannedHours: Double,
    val events: List<HourRecapEventEntry>,
    val extraEventsCount: Int = 0,
) {
  val totalHours: Double
    get() = completedHours + plannedHours

  val hasPresenceIssue: Boolean
    get() = events.any { it.isPast && it.wasPresent != true }
}

data class HourRecapUiState(
    val userRecaps: List<HourRecapUserRecap> = emptyList(),
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
        val recaps = buildUserRecaps(orgId, start, end)
        _uiState.update { it.copy(userRecaps = recaps, isLoading = false) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, errorMsg = "Failed to calculate worked hours: ${e.message}")
        }
      }
    }
  }

  /** Allows test code to override worked hours data. */
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal fun setTestWorkedHours(hours: List<HourRecapUserRecap>) {
    _uiState.update { it.copy(userRecaps = hours) }
  }

  private suspend fun buildUserRecaps(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<HourRecapUserRecap> {
    val pastHours = eventRepository.calculateWorkedHoursPastEvents(orgId, start, end).toMap()
    val futureHours = eventRepository.calculateWorkedHoursFutureEvents(orgId, start, end).toMap()
    val events = eventRepository.getEventsBetweenDates(orgId, start, end)
    val eventParticipants = events.flatMap { event -> event.allEventUserIds() }
    val allEmployeeIds = (pastHours.keys + futureHours.keys + eventParticipants).distinct()
    val users = userRepository.getUsersByIds(allEmployeeIds)
    val userMap = users.associateBy { it.id }
    val now = Instant.now()

    return allEmployeeIds.mapNotNull { userId ->
      val user = userMap[userId] ?: return@mapNotNull null
      val completed = pastHours[userId] ?: 0.0
      val planned = futureHours[userId] ?: 0.0
      val userEvents =
          events
              .filter { event -> userId in event.allEventUserIds() }
              .map { event -> event.toHourRecapEntry(userId, now) }
              .sortedBy { it.startDate }
      val extraEventsCount = userEvents.count { it.isExtra }

      HourRecapUserRecap(
          userId = userId,
          displayName = user.display(),
          completedHours = completed,
          plannedHours = planned,
          events = userEvents,
          extraEventsCount = extraEventsCount)
    }
  }

  private fun Event.toHourRecapEntry(userId: String, now: Instant): HourRecapEventEntry {
    val isPast = startDate <= now
    val presenceStatus = if (isPast) presence[userId] else null
    val wasReplaced = userId in assignedUsers && userId !in participants
    val tookReplacement =
        userId in participants &&
            assignedUsers.any { assigned -> assigned != userId && assigned !in participants }

    return HourRecapEventEntry(
        id = id,
        title = title,
        startDate = startDate,
        endDate = endDate,
        isPast = isPast,
        wasPresent = presenceStatus,
        wasReplaced = wasReplaced,
        tookReplacement = tookReplacement,
        categoryColor = category.color,
        isExtra = isExtra,
    )
  }

  private fun Event.allEventUserIds(): Set<String> {
    return participants + assignedUsers + presence.keys
  }

  /**
   * Returns tag information for a given presence status.
   *
   * @param wasPresent The presence status (true = present, false = absent, null = unknown)
   * @return A Triple of (presenceType, colorType, useErrorColor) where:
   *     - presenceType: String identifier for the tag type ("present", "absent", "unknown")
   *     - colorType: String identifier for the color ("green", "error", "surfaceVariant")
   *     - useErrorColor: Boolean indicating if MaterialTheme.colorScheme.errorContainer should be
   *       used
   */
  fun getPresenceTagInfo(wasPresent: Boolean?): Triple<String, String, Boolean> {
    return when (wasPresent) {
      true -> Triple("present", "green", false)
      false -> Triple("absent", "error", true)
      null -> Triple("unknown", "surfaceVariant", false)
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { HourRecapViewModel() }
    }
  }
}
