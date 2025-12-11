package com.android.sample.data.hybrid.repositories

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.RepoMethod
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.data.hybrid.utils.RemoteSyncError
import com.android.sample.data.local.objects.EventEntity
import com.android.sample.data.local.objects.MyObjectBox
import com.android.sample.data.local.repositories.EventRepositoryLocal
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import io.objectbox.Box
import io.objectbox.BoxStore
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EventRepositoryHybridTest { // Later: Make this inherit RequireSelectedOrganization
  // Hybrid repository under test
  private lateinit var hybridRepository: EventRepositoryHybrid

  // Local in-memory ObjectBox components
  private lateinit var boxStore: BoxStore
  private lateinit var eventBox: Box<EventEntity>

  // Local and Remote repositories
  private lateinit var localRepository: EventRepository
  private lateinit var remoteRepository: FakeEventRepository

  // Captured errors for assertions from remote sync errors callback
  private var capturedError: RemoteSyncError? = null
  private var capturedThrowable: Throwable? = null

  // Event reused in many tests
  private lateinit var event1: Event
  private lateinit var event2: Event

  // Organization ID for testing
  private val orgId: String = "test-org"

  @Before
  fun setUp() {
    // Initialize in-memory ObjectBox for testing
    boxStore =
        MyObjectBox.builder()
            // Unique name for each test run with timestamp of the run
            .inMemory("test-store-${System.nanoTime()}")
            .build()
    eventBox = boxStore.boxFor(EventEntity::class.java)

    // Initialize local repository with in-memory ObjectBox
    localRepository = EventRepositoryLocal(eventBox = eventBox)

    // Initialize remote with fake repository to simulate remote failures
    remoteRepository = FakeEventRepository()

    // Initialize hybrid repository with local in memory repository and Firebase remote repository
    hybridRepository = EventRepositoryHybrid(local = localRepository, remote = remoteRepository)

    // Set up remote sync error callback to capture errors for assertions
    hybridRepository.onSyncError = { error, throwable ->
      capturedError = error
      capturedThrowable = throwable
    }

    // Common event for tests
    event1 =
        Event(
            id = hybridRepository.getNewUid(),
            organizationId = orgId,
            title = "Sample Event",
            description = "Description",
            startDate = Instant.now(),
            endDate = Instant.now().plusSeconds(3600), // 1 hour later
            cloudStorageStatuses = emptySet(),
            personalNotes = "",
            participants = emptySet(),
            version = System.currentTimeMillis(),
            recurrenceStatus = RecurrenceStatus.OneTime,
            location = null
        )

    event2 =
        Event(
            id = hybridRepository.getNewUid(),
            organizationId = orgId,
            title = "Another Event",
            description = "Another Description",
            startDate = Instant.now().plusSeconds(7200), // 2 hours later
            endDate = Instant.now().plusSeconds(10800), // 3 hours later
            cloudStorageStatuses = emptySet(),
            personalNotes = "",
            participants = emptySet(),
            version = System.currentTimeMillis(),
            recurrenceStatus = RecurrenceStatus.OneTime,
            location = null
        )
  }

  @After
  fun tearDown() {
    // Close BoxStore after each test
    boxStore.close()
  }

  // Helper to simulate network failure by making all remote methods fail
  private fun simulateNetworkFailure(remoteRepository: FakeEventRepository) {
    remoteRepository.failMethods = RepoMethod.entries.toTypedArray().toMutableSet()
  }

  // Helper to simulate network recovery by clearing all failures
  private fun simulateNetworkRecovery(remoteRepository: FakeEventRepository) {
    remoteRepository.failMethods.clear()
  }

  private fun resetCapturedErrors() {
    capturedError = null
    capturedThrowable = null
  }

  @Test
  fun insertEvent_remoteSuccess_syncsLocal() = runBlocking {
    hybridRepository.insertEvent(orgId = orgId, item = event1)

    assertNull(capturedError)

    val localEvent = localRepository.getEventById(orgId = orgId, itemId = event1.id)
    val remoteEvent = remoteRepository.getEventById(orgId = orgId, itemId = event1.id)

    assertNotNull(remoteEvent)
    assertNotNull(localEvent)

    assertEquals(
        setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL),
        localEvent?.cloudStorageStatuses)
  }

  @Test
  fun insertEvent_remoteFails_thenRecover() = runBlocking {
    simulateNetworkFailure(remoteRepository)

    hybridRepository.insertEvent(orgId, event1)

    assertEquals(RemoteSyncError.REMOTE_INSERT_FAILED, capturedError)

    val localEvent = localRepository.getEventById(orgId, event1.id)

    assertNull(localEvent)

    // Simulate network recovery
    resetCapturedErrors()
    simulateNetworkRecovery(remoteRepository)

    // Try inserting again after recovery
    val updatedEvent = event1.copy(title = "Updated after recovery")
    hybridRepository.insertEvent(orgId = orgId, item = updatedEvent)

    assertNull(capturedError)

    // Verify both local and remote have the event after recovery
    val remoteEvent = remoteRepository.getEventById(orgId, event1.id)
    val updatedLocalEvent = localRepository.getEventById(orgId, event1.id)

    assertNotNull(remoteEvent)
    assertNotNull(updatedLocalEvent)

    // Check title was updated
    assertEquals("Updated after recovery", remoteEvent?.title)
    assertEquals("Updated after recovery", updatedLocalEvent?.title)
  }

  @Test
  fun getAllEvents_remoteSuccess_localIsSynced() = runBlocking {

    // Insert two events via hybrid repository
    hybridRepository.insertEvent(orgId, event1)
    hybridRepository.insertEvent(orgId, event2)

    // Get all events via hybrid repository
    val events = hybridRepository.getAllEvents(orgId)

    assertEquals(2, events.size)
    assertNotNull(localRepository.getEventById(orgId, event1.id))
    assertNotNull(localRepository.getEventById(orgId, event2.id))
  }

  @Test
  fun getAllEvents_remoteFails_returnsLocal() = runBlocking {
    // Insert one event
    hybridRepository.insertEvent(orgId, event1)

    simulateNetworkFailure(remoteRepository)

    // try getting all events via hybrid repository
    val events = hybridRepository.getAllEvents(orgId)

    assertEquals(RemoteSyncError.REMOTE_GET_FAILED, capturedError)

    // Verify that local event is returned
    assertEquals(1, events.size)
    assertEquals(event1.id, events.first().id)

    resetCapturedErrors()

    // Try inserting another event while remote is still failing
    hybridRepository.insertEvent(orgId, event2)

    assertEquals(RemoteSyncError.REMOTE_INSERT_FAILED, capturedError)

    resetCapturedErrors()

    // Try getting all events again
    val eventsAfterInsert = hybridRepository.getAllEvents(orgId)
    assertEquals(capturedError, RemoteSyncError.REMOTE_GET_FAILED)
    assertEquals(1, eventsAfterInsert.size)
    assertEquals(event1.id, eventsAfterInsert.first().id)
  }

  @Test
  fun updateEvent_remoteSuccess_syncsLocal() = runBlocking {
    // Insert an event first
    hybridRepository.insertEvent(orgId, event1)

    // Update the event
    val updatedEvent = event1.copy(title = "Updated Title")
    hybridRepository.updateEvent(orgId, event1.id, updatedEvent)

    // No sync error should be reported
    assertNull(capturedError)

    // Verify both remote and local have the updated event
    val remoteEvent = remoteRepository.getEventById(orgId, event1.id)
    val localEvent = localRepository.getEventById(orgId, event1.id)

    assertNotNull(remoteEvent)
    assertNotNull(localEvent)
    assertEquals("Updated Title", remoteEvent?.title)
    assertEquals("Updated Title", localEvent?.title)
  }

  @Test
  fun updateEvent_remoteFails_fallsBackToLocal() = runBlocking {
    // Insert an event first
    hybridRepository.insertEvent(orgId, event1)

    // Simulate network failure
    simulateNetworkFailure(remoteRepository)

    // Attempt to update the event
    val updatedEvent = event1.copy(title = "Updated While Offline")
    hybridRepository.updateEvent(orgId, event1.id, updatedEvent)

    // Remote sync error should be captured
    assertEquals(RemoteSyncError.REMOTE_UPDATE_FAILED, capturedError)

    // Local event should not be updated (still has original title)
    val localEvent = localRepository.getEventById(orgId, event1.id)
    assertEquals(event1.title, localEvent?.title)

    // Remote event also should not be updated
    simulateNetworkRecovery(remoteRepository) // Recover network to check remote state
    val remoteEvent = remoteRepository.getEventById(orgId, event1.id)
    assertEquals(event1.title, remoteEvent?.title)
  }

  @Test
  fun deleteEvent_remoteFails_localStillDeletes() = runBlocking {
    // Insert an event first
    hybridRepository.insertEvent(orgId, event1)

    // Simulate network failure
    simulateNetworkFailure(remoteRepository)

    // Attempt to delete the event
    hybridRepository.deleteEvent(orgId, event1.id)

    // Remote sync error should be captured
    assertEquals(RemoteSyncError.REMOTE_DELETE_FAILED, capturedError)

    // Local event should not be deleted
    val localEvent = localRepository.getEventById(orgId, event1.id)
    assertNotNull(localEvent)

    // Remote event should also not be deleted
    simulateNetworkRecovery(remoteRepository) // Recover network to check remote state
    val remoteEvent = remoteRepository.getEventById(orgId, event1.id)
    assertNotNull(remoteEvent)
  }
}
