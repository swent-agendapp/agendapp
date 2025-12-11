package com.android.sample.ui.hourRecap

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.RepoMethod
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*

/** Minimal ViewModel test, without complex fake VM providers. */
@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repo: FakeEventRepository

  private val orgId = "org123" // Pretend the user selected an organization
  private val selectedOrgFlow = MutableStateFlow(orgId)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repo = FakeEventRepository()
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
    val participantId = "user1"
    val event =
        Event(
            id = "event1",
            organizationId = orgId,
            title = "Test Event",
            description = "Just a test",
            startDate = Instant.EPOCH.minusSeconds(3600), // passé
            endDate = Instant.EPOCH, // passé
            cloudStorageStatuses = emptySet(),
            personalNotes = null,
            participants = setOf(participantId),
            presence = mapOf(participantId to true),
            version = System.currentTimeMillis(),
            recurrenceStatus = RecurrenceStatus.OneTime,
            location = null)
    repo.add(event)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)

    advanceUntilIdle()

    val expectedDuration = Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
    val expectedWorkedHours = listOf(participantId to expectedDuration)

    assertEquals(expectedWorkedHours, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours sets error on exception`() = runTest {
    val vm = makeVm()
    repo.failMethods.add(RepoMethod.GET_EVENTS_BETWEEN_DATES)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.errorMsg?.contains("Failed") == true)
  }
}
