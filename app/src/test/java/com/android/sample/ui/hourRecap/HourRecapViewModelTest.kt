package com.android.sample.ui.hourRecap

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*

/** Minimal fake repository implementing only what HourRecapViewModel needs. */
class SimpleFakeEventRepository : EventRepository {
  var result: List<Pair<String, Double>> = emptyList()
  var shouldThrow = false

  override fun getNewUid(): String {
    error("Not needed in test")
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    error("Not needed in test")
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    error("Not needed in test")
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    error("Not needed in test")
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    if (shouldThrow) throw RuntimeException("error!")
    return result
  }
}

/** Minimal ViewModel test, without complex fake VM providers. */
@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repo: SimpleFakeEventRepository

  private val orgId = "org123" // Pretend the user selected an organization
  private val selectedOrgFlow = MutableStateFlow(orgId)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repo = SimpleFakeEventRepository()
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
  }

  private fun makeVm(): HourRecapViewModel {
    return HourRecapViewModel(eventRepository = repo, selectedOrganizationFlow = selectedOrgFlow)
  }

  @Test
  fun `initial state is correct`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.workedHours.isEmpty())
    assertNull(state.errorMsg)
    assertTrue(!state.isLoading)
  }

  @Test
  fun `setErrorMsg updates error`() {
    val vm = makeVm()

    vm.setErrorMsg("Oops")

    assertEquals("Oops", vm.uiState.value.errorMsg)
  }

  @Test
  fun `clearErrorMsg resets error`() {
    val vm = makeVm()

    vm.setErrorMsg("Err")
    vm.clearErrorMsg()

    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun `setTestWorkedHours updates worked hours`() {
    val vm = makeVm()
    val data = listOf("Alice" to 5.0)

    vm.setTestWorkedHours(data)

    assertEquals(data, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours loads data successfully`() = runTest {
    val vm = makeVm()
    repo.result = listOf("Bob" to 10.5)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)

    advanceUntilIdle()

    assertEquals(repo.result, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours sets error on exception`() = runTest {
    val vm = makeVm()
    repo.shouldThrow = true

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.errorMsg?.contains("Failed") == true)
  }
}
