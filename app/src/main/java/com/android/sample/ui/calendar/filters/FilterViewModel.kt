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
  // Selected filters
  // ---------------------------
  private val _filters = MutableStateFlow(EventFilters())
  val filters: StateFlow<EventFilters> = _filters

  // ---------------------------
  // Available filter options
  // ---------------------------

  // Event types (categories)
  private val _eventTypes = MutableStateFlow<List<String>>(emptyList())
  val eventTypes: StateFlow<List<String>> = _eventTypes

  // Location labels
  private val _locations = MutableStateFlow<List<String>>(emptyList())
  val locations: StateFlow<List<String>> = _locations

  // Participants (organization members)
  private val _participants = MutableStateFlow<List<String>>(emptyList())
  val participants: StateFlow<List<String>> = _participants

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

  private fun clearMetadata() {
    _eventTypes.value = emptyList()
    _locations.value = emptyList()
    _participants.value = emptyList()
  }

  private suspend fun loadMetadata(orgId: String) {
    try {
      // -------- Event Types
      _eventTypes.value = categoryRepo.getAllCategories(orgId).map { it.label }

      // -------- Locations
      _locations.value = mapRepo.getAllAreas(orgId).map { it.label }

      // -------- Participants
      val memberIds = userRepo.getMembersIds(orgId)
      val users = userRepo.getUsersByIds(memberIds)

      _participants.value = users.map { it.displayName ?: it.email ?: "Unknown" }
    } catch (e: Exception) {
      // optional: expose error state later
    }
  }

  // ---------------------------
  // Filter selection setters
  // ---------------------------
  fun setEventTypes(types: List<String>) {
    _filters.update { it.copy(eventTypes = types.toSet()) }
  }

  fun setLocations(locations: List<String>) {
    _filters.update { it.copy(locations = locations.toSet()) }
  }

  fun setParticipants(names: List<String>) {
    _filters.update { it.copy(participants = names.toSet()) }
  }

  fun clearFilters() {
    _filters.value = EventFilters()
  }
}
