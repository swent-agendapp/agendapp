package com.android.sample.ui

import android.app.Application
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.map.MapRepository
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.hourRecap.HourRecapViewModel
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  // Dispatcher contrôlé
  private val testDispatcher = StandardTestDispatcher()

  private lateinit var repositoryEvent: EventRepository
  private lateinit var repositoryMap: MapRepository
  private lateinit var app: Application
  private lateinit var viewModel: HourRecapViewModel

  private val orgId = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    SelectedOrganizationRepository.changeSelectedOrganization(orgId)

    repositoryEvent = EventRepositoryLocal()

    viewModel = HourRecapViewModel(eventRepository = repositoryEvent)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ---------------------------------------------------------------
  // TEST 1 — Past events
  // ---------------------------------------------------------------
  @Test
  fun calculateWorkedHoursShouldCalculateCorrectlyForPastEvents() = runTest {
    val pastStart = Instant.parse("2000-01-01T10:00:00Z")
    val pastEnd = Instant.parse("2000-01-01T12:00:00Z") // 2 hours

    val pastEvent =
        createEvent(
            organizationId = orgId,
            title = "Past Meeting",
            startDate = pastStart,
            endDate = pastEnd,
            participants = setOf("user1", "user2"),
            presence = mapOf("user1" to true, "user2" to false))[0]

    repositoryEvent.insertEvent(orgId, pastEvent)

    viewModel.calculateWorkedHours(pastStart.minusSeconds(1), pastEnd.plusSeconds(1))
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0
    val user2Hours = result.find { it.first == "user2" }?.second ?: 0.0

    assertEquals(2.0, user1Hours, 0.01)
    assertEquals(0.0, user2Hours, 0.01)
  }

  // ---------------------------------------------------------------
  // TEST 2 — Future events
  // ---------------------------------------------------------------
  @Test
  fun calculateWorkedHoursShouldCalculateCorrectlyForFutureEvents() = runTest {
    val futureStart = Instant.now().plusSeconds(3600)
    val futureEnd = futureStart.plusSeconds(7200)

    val futureEvent =
        createEvent(
            organizationId = orgId,
            title = "Future Meeting",
            startDate = futureStart,
            endDate = futureEnd,
            participants = setOf("user1", "user2"),
            presence = emptyMap())[0]

    repositoryEvent.insertEvent(orgId, futureEvent)

    viewModel.calculateWorkedHours(futureStart.minusSeconds(1), futureEnd.plusSeconds(1))
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0
    val user2Hours = result.find { it.first == "user2" }?.second ?: 0.0

    assertEquals(2.0, user1Hours, 0.01)
    assertEquals(2.0, user2Hours, 0.01)
  }

  // ---------------------------------------------------------------
  // TEST 3 — Aggregation from multiple events
  // ---------------------------------------------------------------
  @Test
  fun calculateWorkedHoursShouldAggregateHoursFromMultipleEvents() = runTest {
    val start = Instant.parse("2000-01-01T00:00:00Z")
    val end = Instant.parse("2000-01-02T00:00:00Z")

    val event1 =
        createEvent(
            organizationId = orgId,
            title = "Event 1",
            startDate = Instant.parse("2000-01-01T10:00:00Z"),
            endDate = Instant.parse("2000-01-01T11:00:00Z"), // 1 hour
            participants = setOf("user1"),
            presence = mapOf("user1" to true))[0]

    val event2 =
        createEvent(
            organizationId = orgId,
            title = "Event 2",
            startDate = Instant.parse("2000-01-01T14:00:00Z"),
            endDate = Instant.parse("2000-01-01T16:00:00Z"), // 2 hours
            participants = setOf("user1"),
            presence = mapOf("user1" to true))[0]

    repositoryEvent.insertEvent(orgId, event1)
    repositoryEvent.insertEvent(orgId, event2)

    viewModel.calculateWorkedHours(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0

    assertEquals(3.0, user1Hours, 0.01)
  }
}
