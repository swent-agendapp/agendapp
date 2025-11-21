package com.android.sample.model.replacement

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.FirebaseEmulatedTest
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI

@RunWith(AndroidJUnit4::class)
class ReplacementRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repository: ReplacementRepository
  private lateinit var sampleEvent: Event
  private lateinit var replacement1: Replacement
  private lateinit var replacement2: Replacement

  @Before
  override fun setUp() {
    super.setUp()
    repository = createInitializedReplacementRepository() // implement helper to get Firebase repo

    sampleEvent =
        createEvent(
            title = "Meeting",
            description = "Team meeting",
            startDate = Instant.parse("2025-11-12T10:00:00Z"),
            endDate = Instant.parse("2025-11-12T11:00:00Z"))[0]

    replacement1 =
        Replacement(
            id = "replacement1",
            absentUserId = "user123",
            substituteUserId = "user456",
            event = sampleEvent,
            status = ReplacementStatus.ToProcess)

    replacement2 =
        Replacement(
            id = "replacement2",
            absentUserId = "user789",
            substituteUserId = "user321",
            event = sampleEvent,
            status = ReplacementStatus.Accepted)
  }

  @Test
  fun insertReplacement_andGetById_shouldWork() = runBlocking {
    repository.insertReplacement(replacement1)
    val retrieved = repository.getReplacementById(replacement1.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals(replacement1.absentUserId, retrieved?.absentUserId)
    Assert.assertEquals(replacement1.event.id, retrieved?.event?.id)
  }

  @Test
  fun getAllReplacements_shouldReturnInsertedOnes() = runBlocking {
    repository.insertReplacement(replacement1)
    repository.insertReplacement(replacement2)
    val allReplacements = repository.getAllReplacements()
    Assert.assertEquals(2, allReplacements.size)
  }

  @Test
  fun updateReplacement_shouldReplaceExistingReplacement() = runBlocking {
    repository.insertReplacement(replacement1)
    val updated = replacement1.copy(status = ReplacementStatus.Accepted)
    repository.updateReplacement(replacement1.id, updated)
    val retrieved = repository.getReplacementById(replacement1.id)
    Assert.assertEquals(ReplacementStatus.Accepted, retrieved?.status)
  }

  @Test
  fun deleteReplacement_shouldRemoveReplacement() = runBlocking {
    repository.insertReplacement(replacement1)
    repository.deleteReplacement(replacement1.id)
    Assert.assertNull(repository.getReplacementById(replacement1.id))
  }

  @Test
  fun getReplacementsByUser_returnsCorrectly() = runBlocking {
    repository.insertReplacement(replacement1)
    repository.insertReplacement(replacement2)

    val user123Replacements = repository.getReplacementsByAbsentUser("user123")
    Assert.assertEquals(1, user123Replacements.size)
    Assert.assertEquals("replacement1", user123Replacements.first().id)
  }

  @Test
  fun getReplacementById_unknownId_returnsNull() = runBlocking {
    val retrieved = repository.getReplacementById("unknown-id")
    Assert.assertNull(retrieved)
  }

  @Test
  fun getReplacementsByAbsentUser_noMatch_returnsEmptyList() = runBlocking {
    repository.insertReplacement(replacement1)
    val result = repository.getReplacementsByAbsentUser("someone-else")
    Assert.assertTrue(result.isEmpty())
  }
}
