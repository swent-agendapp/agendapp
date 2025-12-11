package com.android.sample.ui.calendar.filters

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Represents all filter selections used in the Calendar. */
data class EventFilters(
    val eventTypes: Set<String> = emptySet(),
    val locations: Set<String> = emptySet(),
    val participants: Set<String> = emptySet()
) {
  fun isEmpty(): Boolean = eventTypes.isEmpty() && locations.isEmpty() && participants.isEmpty()
}

/** ViewModel dedicated to storing and updating filter selections. */
class FilterViewModel : ViewModel() {

  private val _filters = MutableStateFlow(EventFilters())
  val filters: StateFlow<EventFilters> = _filters

  /** Update only event type filters */
  fun setEventTypes(types: List<String>) {
    _filters.update { it.copy(eventTypes = types.toSet()) }
  }

  /** Update only location filters */
  fun setLocations(locations: List<String>) {
    _filters.update { it.copy(locations = locations.toSet()) }
  }

  /** Update only participant filters */
  fun setParticipants(names: List<String>) {
    _filters.update { it.copy(participants = names.toSet()) }
  }

  /** Replace all filters at once */
  fun setFilters(filters: EventFilters) {
    _filters.value = filters
  }

  /** Remove all filters */
  fun clearFilters() {
    _filters.value = EventFilters()
  }
}
