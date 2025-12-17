package com.android.sample.ui.calendar.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.category.EventCategoryRepositoryProvider
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.MapRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Assisted by AI

/** Represents all filter selections used in the Calendar. */
data class EventFilters(
    val eventTypes: Set<String> = emptySet(),
    val locations: Set<String> = emptySet(),
    val participants: Set<String> = emptySet()
) {
  fun isEmpty(): Boolean = eventTypes.isEmpty() && locations.isEmpty() && participants.isEmpty()
}

/** UI state exposed by [FilterViewModel]. */
data class FilterUiState(
    val filters: EventFilters = EventFilters(),
    val eventTypes: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val participants: List<ParticipantUi> = emptyList()
)

/** UI representation of a participant for filtering purposes. */
data class ParticipantUi(val id: String, val label: String)

/**
 * ViewModel responsible for managing calendar event filters.
 *
 * This ViewModel:
 * - Stores the currently selected filter values (event types, locations, participants)
 * - Loads available filter options dynamically based on the selected organization
 *
 * ### Data sources
 * - Event categories are loaded from [EventCategoryRepository]
 * - Locations are derived from organization areas via [MapRepository]
 * - Participants are resolved from organization members via [UserRepository]
 *
 * The ViewModel automatically refreshes available filter options whenever the selected organization
 * changes.
 *
 * @param categoryRepo Repository providing event category metadata
 * @param userRepo Repository providing organization user information
 * @param mapRepo Repository providing organization location/area data
 * @param orgVM ViewModel exposing the currently selected organization
 */
class FilterViewModel(
    private val categoryRepo: EventCategoryRepository = EventCategoryRepositoryProvider.repository,
    private val userRepo: UserRepository = UserRepositoryProvider.repository,
    private val mapRepo: MapRepository = MapRepositoryProvider.repository,
    private val orgVM: SelectedOrganizationViewModel = SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  // ---------------------------
  // UI State
  // ---------------------------
  private val _uiState = MutableStateFlow(FilterUiState())
  val uiState: StateFlow<FilterUiState> = _uiState

  /** Observe organization changes to reload filter metadata */
  init {
    observeOrganizationChanges()
  }

  /** Reload filter options whenever selected organization changes */
  private fun observeOrganizationChanges() {
    viewModelScope.launch {
      orgVM.selectedOrganizationId.collect { orgId ->
        if (orgId != null) loadMetadata(orgId) else clearMetadata()
      }
    }
  }

  /** Clear all loaded metadata (called when no organization is selected) */
  private fun clearMetadata() {
    _uiState.update {
      it.copy(eventTypes = emptyList(), locations = emptyList(), participants = emptyList())
    }
  }

  /** Load available filter options for the given organization */
  private suspend fun loadMetadata(orgId: String) {
    try {
      val eventTypes = categoryRepo.getAllCategories(orgId).map { it.label }

      val locations = mapRepo.getAllAreas(orgId).map { it.label }

      val users = userRepo.getUsersByIds(userRepo.getMembersIds(orgId))

      val participants =
          users.map { ParticipantUi(id = it.id, label = it.displayName ?: it.email ?: "Unknown") }

      _uiState.update {
        it.copy(eventTypes = eventTypes, locations = locations, participants = participants)
      }
    } catch (_: Exception) {
      // Optional: add error state later
    }
  }

  // ---------------------------
  // Filter selection setters
  // ---------------------------
  /** Set selected event types */
  fun setEventTypes(types: List<String>) {
    _uiState.update { it.copy(filters = it.filters.copy(eventTypes = types.toSet())) }
  }

  /** Set selected locations */
  fun setLocations(locations: List<String>) {
    _uiState.update { it.copy(filters = it.filters.copy(locations = locations.toSet())) }
  }

  /** Set selected participants */
  fun setParticipants(ids: List<String>) {
    _uiState.update { it.copy(filters = it.filters.copy(participants = ids.toSet())) }
  }

  /** Clear all selected filters */
  fun clearFilters() {
    _uiState.update { it.copy(filters = EventFilters()) }
  }
}
