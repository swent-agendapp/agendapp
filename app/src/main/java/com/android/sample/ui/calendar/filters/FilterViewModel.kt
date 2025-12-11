package com.android.sample.ui.calendar.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.metadata.EventMetadataRepository
import com.android.sample.model.metadata.EventMetadataRepositoryProvider
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Represents all filter selections used in the Calendar. */
data class EventFilters(
    val eventTypes: Set<String> = emptySet(),
    val locations: Set<String> = emptySet(),
    val participants: Set<String> = emptySet()
) {
  fun isEmpty(): Boolean = eventTypes.isEmpty() && locations.isEmpty() && participants.isEmpty()
}

/** ViewModel dedicated to storing filter selections + loading metadata lists from repo. */
class FilterViewModel(
    private val metadataRepo: EventMetadataRepository = EventMetadataRepositoryProvider.repository,
    private val organizationRepo: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepo: AuthRepository = AuthRepositoryProvider.repository,
    private val orgVM: SelectedOrganizationViewModel = SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  // ---------------------------
  // Selected filters
  // ---------------------------
  private val _filters = MutableStateFlow(EventFilters())
  val filters: StateFlow<EventFilters> = _filters

  // ---------------------------
  // Available metadata options
  // ---------------------------
  private val _eventTypes = MutableStateFlow<List<String>>(emptyList())
  val eventTypes: StateFlow<List<String>> = _eventTypes

  private val _locations = MutableStateFlow<List<String>>(emptyList())
  val locations: StateFlow<List<String>> = _locations

  private val _participants = MutableStateFlow<List<String>>(emptyList())
  val participants: StateFlow<List<String>> = _participants

  init {
    observeOrganizationChanges()
  }

  /** Reload metadata whenever organization changes */
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

  /** Loads event types, locations, and participants */
  private suspend fun loadMetadata(orgId: String) {
    try {
      // ------------- Event Types & Locations (from metadata repo)
      _eventTypes.value = metadataRepo.getEventTypes(orgId)
      _locations.value = metadataRepo.getLocations(orgId)

      // ------------- Participants (from organization members)
      val currentUser =
          authRepo.getCurrentUser() ?: throw IllegalStateException("User not logged in")

      val members = organizationRepo.getMembersOfOrganization(orgId, currentUser)

      _participants.value = members.map { it.displayName ?: it.email ?: "Unknown" }
    } catch (e: Exception) {
      // Optionally handle errors
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

  fun setFilters(filters: EventFilters) {
    _filters.value = filters
  }

  fun clearFilters() {
    _filters.value = EventFilters()
  }
}
