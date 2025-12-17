package com.android.sample.ui.filter

import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.filter.FakeEventCategoryRepository
import com.android.sample.model.filter.FakeMapRepository
import com.android.sample.model.filter.FakeUserRepository
import com.android.sample.model.map.Area
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
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

  private lateinit var vm: FilterViewModel

  private val selectedOrganizationID = "org123"

  @Before
  fun setup() {
    // Required because ViewModel uses viewModelScope (Dispatchers.Main)
    Dispatchers.setMain(dispatcher)

    categoryRepo = FakeEventCategoryRepository()
    userRepo = FakeUserRepository()
    mapRepo = FakeMapRepository()

    // Seed map data
    mapRepo.seedAreas(
        orgId = selectedOrganizationID,
        areas =
            listOf(
                Area(
                    id = "a1",
                    label = "Salle 1",
                    marker = mapRepo.fakeMarker("Salle 1"),
                    radius = 10.0),
                Area(
                    id = "a2",
                    label = "Salle 2",
                    marker = mapRepo.fakeMarker("Salle 2"),
                    radius = 15.0)))

    // Use real selected organization (global app state)
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)

    vm = FilterViewModel(categoryRepo = categoryRepo, userRepo = userRepo, mapRepo = mapRepo)
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
    // Optional but good practice if available
    // SelectedOrganizationRepository.clear()
  }

  // ------------------------------------------------------------
  //  UI State tests
  // ------------------------------------------------------------

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
  fun `loads metadata when organization is selected`() = runTest {
    // Organization already set in @Before
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

  @Test
  fun `does not crash when repository throws exception`() = runTest {
    // GIVEN: a repository that throws
    val throwingCategoryRepo = ThrowingEventCategoryRepository()

    val vmWithError =
        FilterViewModel(categoryRepo = throwingCategoryRepo, userRepo = userRepo, mapRepo = mapRepo)

    // WHEN: organization is already selected -> metadata loading is triggered
    advanceUntilIdle()

    // THEN: ViewModel is still alive and state is safe
    val state = vmWithError.uiState.value

    assertTrue(state.eventTypes.isEmpty())
    assertTrue(state.locations.isEmpty())
    assertTrue(state.participants.isEmpty())
  }
}

private class ThrowingEventCategoryRepository : EventCategoryRepository {
  override fun getNewUid(): String {
    // Not needed for this test
    return "unused"
  }

  override suspend fun getAllCategories(orgId: String): List<EventCategory> {
    throw RuntimeException("Test exception")
  }

  override suspend fun deleteCategory(orgId: String, itemId: String) {
    // Not needed for this test
  }

  override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? {
    // Not needed for this test
    return null
  }
}
