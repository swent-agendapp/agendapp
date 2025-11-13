package com.android.sample.model.replacement

import com.android.sample.model.calendar.Event
import java.util.UUID

// Assisted by AI

class ReplacementRepositoryLocal(preloadSampleData: Boolean = false) : ReplacementRepository {

  private val replacements: MutableList<Replacement> = mutableListOf()

  init {
    if (preloadSampleData) {
      populateSampleReplacements()
    }
  }

  override suspend fun getAllReplacements(): List<Replacement> {
    return replacements.toList()
  }

  override suspend fun insertReplacement(item: Replacement) {
    require(replacements.indexOfFirst { it.id == item.id } == -1) {
      "Replacement with id ${item.id} already exists."
    }
    replacements.add(item)
  }

  override suspend fun updateReplacement(itemId: String, item: Replacement) {
    val index = replacements.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }
    replacements[index] = item
  }

  override suspend fun deleteReplacement(itemId: String) {
    val index = replacements.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }
    replacements.removeAt(index)
  }

  override suspend fun getReplacementById(itemId: String): Replacement? {
    return replacements.find { it.id == itemId }
  }

  override suspend fun getReplacementsByAbsentUser(userId: String): List<Replacement> {
    return replacements.filter { it.absentUserId == userId }
  }

  override suspend fun getReplacementsBySubstituteUser(userId: String): List<Replacement> {
    return replacements.filter { it.substituteUserId == userId }
  }

  override suspend fun getReplacementsByStatus(status: ReplacementStatus): List<Replacement> {
    return replacements.filter { it.status == status }
  }

  // Optional helper for preloading test data
  private fun populateSampleReplacements() {
    val sampleEvent =
        Event(
            id = "E001",
            title = "Team Standup Meeting",
            description = "Daily morning sync-up",
            startDate = java.time.Instant.now(),
            endDate = java.time.Instant.now().plusSeconds(3600),
            participants = setOf("Alice", "Bob"),
            recurrenceStatus = com.android.sample.model.calendar.RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            color = com.android.sample.utils.EventColor.Blue,
            version = System.currentTimeMillis(),
            locallyStoredBy = listOf("LOCAL_USER"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)

    val replacement1 =
        Replacement(
            id = UUID.randomUUID().toString(),
            absentUserId = "Alice",
            substituteUserId = "Charlie",
            event = sampleEvent,
            status = ReplacementStatus.ToProcess)

    val replacement2 =
        Replacement(
            id = UUID.randomUUID().toString(),
            absentUserId = "Bob",
            substituteUserId = "Eve",
            event = sampleEvent.copy(id = "E002", title = "Project Briefing"),
            status = ReplacementStatus.Accepted)

    replacements.addAll(listOf(replacement1, replacement2))
  }
}
