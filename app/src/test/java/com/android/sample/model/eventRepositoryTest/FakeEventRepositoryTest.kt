package com.android.sample.model.eventRepositoryTest

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.RepoMethod
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeEventRepositoryTest {

  private lateinit var repo: FakeEventRepository
  private val orgId = "org1"

  @Before
  fun setup() {
    repo = FakeEventRepository()
  }

  // Helper to create a simple event
  private fun newEvent(): Event =
      createEvent(
              organizationId = orgId,
              title = "Test",
              startDate = Instant.now(),
              endDate = Instant.now().plusSeconds(3600))
          .first()

  // -------------------------------------------------------------
  // Basic behavior
  // -------------------------------------------------------------

  @Test
  fun insertEvent_addsEventToList() = runTest {
    val event = newEvent()

    repo.insertEvent(orgId, event)

    val all = repo.getAllEvents(orgId)
    assertEquals(1, all.size)
    assertEquals(event.id, all.first().id)
  }

  @Test
  fun deleteEvent_removesEvent() = runTest {
    val event = newEvent()
    repo.insertEvent(orgId, event)

    repo.deleteEvent(orgId, event.id)

    val all = repo.getAllEvents(orgId)
    assertTrue(all.isEmpty())
    assertTrue(event.id in repo.deletedIds)
  }

  @Test
  fun updateEvent_modifiesExistingEvent() = runTest {
    val event = newEvent()
    repo.insertEvent(orgId, event)

    val updated = event.copy(title = "Updated")
    repo.updateEvent(orgId, event.id, updated)

    val read = repo.getEventById(orgId, event.id)
    assertEquals("Updated", read?.title)
  }

  @Test
  fun getEventsBetweenDates_returnsCorrectEvents() = runTest {
    val now = Instant.now()
    val inside = newEvent()
    val outside =
        newEvent().copy(startDate = now.minusSeconds(10000), endDate = now.minusSeconds(9000))
    repo.insertEvent(orgId, inside)
    repo.insertEvent(orgId, outside)

    val result = repo.getEventsBetweenDates(orgId, now.minusSeconds(10), now.plusSeconds(50000))

    assertEquals(1, result.size)
    assertEquals(inside.id, result.first().id)
  }

  // -------------------------------------------------------------
  // Failure simulation
  // -------------------------------------------------------------

  @Test(expected = RuntimeException::class)
  fun getAllEvents_failsWhenListedInFailMethods() = runTest {
    repo.failMethods.add(RepoMethod.GET_ALL_EVENTS)

    repo.getAllEvents(orgId)
  }

  @Test(expected = RuntimeException::class)
  fun insertEvent_failsWhenListedInFailMethods() = runTest {
    repo.failMethods.add(RepoMethod.INSERT_EVENT)

    repo.insertEvent(orgId, newEvent())
  }

  @Test(expected = RuntimeException::class)
  fun deleteEvent_failsWhenListedInFailMethods() = runTest {
    val event = newEvent()
    repo.insertEvent(orgId, event)
    repo.failMethods.add(RepoMethod.DELETE_EVENT)

    repo.deleteEvent(orgId, event.id)
  }

  @Test(expected = RuntimeException::class)
  fun updateEvent_failsWhenListedInFailMethods() = runTest {
    val event = newEvent()
    repo.insertEvent(orgId, event)
    repo.failMethods.add(RepoMethod.UPDATE_EVENT)

    repo.updateEvent(orgId, event.id, event.copy(title = "Updated"))
  }
}
