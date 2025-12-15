package com.android.sample.ui.filter

import com.android.sample.model.filter.FakeEventCategoryRepository
import com.android.sample.model.filter.FakeMapRepository
import com.android.sample.model.filter.FakeSelectedOrganizationViewModel
import com.android.sample.model.filter.FakeUserRepository
import com.android.sample.ui.calendar.filters.FilterViewModel
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilterViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  private lateinit var categoryRepo: FakeEventCategoryRepository
  private lateinit var userRepo: FakeUserRepository
  private lateinit var mapRepo: FakeMapRepository
  private lateinit var orgVM: FakeSelectedOrganizationViewModel

  private lateinit var vm: FilterViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)

    categoryRepo = FakeEventCategoryRepository()
    userRepo = FakeUserRepository()
    mapRepo = FakeMapRepository()
    orgVM = FakeSelectedOrganizationViewModel()

    vm =
        FilterViewModel(
            categoryRepo = categoryRepo, userRepo = userRepo, mapRepo = mapRepo, orgVM = orgVM)
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state is empty`() {
    val state = vm.uiState.value

    // Filters
    assertTrue(state.filters.isEmpty())

    // Available options
    assertTrue(state.eventTypes.isEmpty())
    assertTrue(state.locations.isEmpty())
    assertTrue(state.participants.isEmpty())
  }

  @Test
  fun `loads metadata when organization changes`() = runTest {
    orgVM.setOrg("org123")
    advanceUntilIdle()

    val state = vm.uiState.value

    assertEquals(listOf("Course", "Meeting", "Workshop"), state.eventTypes)
    assertEquals(listOf("Salle 1", "Salle 2"), state.locations)
    assertEquals(listOf("Alice", "Bob"), state.participants.map { it.label })
  }

  @Test
  fun `set filters updates state`() {
    vm.setEventTypes(listOf("Course"))
    vm.setLocations(listOf("Salle 1"))
    vm.setParticipants(listOf("Alice"))

    val filters = vm.uiState.value.filters

    assertEquals(setOf("Course"), filters.eventTypes)
    assertEquals(setOf("Salle 1"), filters.locations)
    assertEquals(setOf("Alice"), filters.participants)
  }

  @Test
  fun `clearFilters resets filters`() {
    vm.setEventTypes(listOf("Course"))
    vm.setLocations(listOf("Salle 1"))
    vm.setParticipants(listOf("Alice"))

    vm.clearFilters()

    assertTrue(vm.uiState.value.filters.isEmpty())
  }
}
