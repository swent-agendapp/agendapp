package com.android.sample.data.local.repositories

import com.android.sample.data.global.repositories.BaseEventRepository
import com.android.sample.data.local.BoxProvider
import com.android.sample.data.local.mappers.EventMapper.toEntity
import com.android.sample.data.local.mappers.EventMapper.toEvent
import com.android.sample.data.local.objects.EventEntity
import com.android.sample.data.local.objects.EventEntity_
import com.android.sample.data.local.utils.encodeSet
import com.android.sample.model.calendar.Event
import io.objectbox.Box
import java.time.Instant
import java.util.UUID

class EventRepositoryLocal(private val eventBox: Box<EventEntity> = BoxProvider.eventBox()) :
    BaseEventRepository() {

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    return eventBox
        .query(
            EventEntity_.organizationId.equal(orgId).and(EventEntity_.hasBeenDeleted.equal(false)))
        .build()
        .find()
        .map { it.toEvent() }
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.insertEvent(orgId, item)

    val existing =
        eventBox
            .query(EventEntity_.id.equal(item.id).and(EventEntity_.organizationId.equal(orgId)))
            .build()
            .findFirst()

    require(existing == null) { "Item with id ${item.id} already exists." }

    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented

    val updated =
        item.copy(
            locallyStoredBy = item.locallyStoredBy + currentUserId,
            version = System.currentTimeMillis())

    eventBox.put(updated.toEntity())
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.updateEvent(orgId, itemId, item)

    val existing = eventBox.query(EventEntity_.id.equal(itemId)).build().findFirst()

    require(existing != null) { "Item with id $itemId and organizationId $orgId does not exist." }
    require(!existing.hasBeenDeleted) { "Cannot update a deleted event." }
    require(existing.organizationId == orgId) {
      "Item's organizationId ${existing.organizationId} does not match the provided orgId $orgId."
    }

    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented

    val updated =
        item
            .copy(
                locallyStoredBy = item.locallyStoredBy + currentUserId,
                version = System.currentTimeMillis(),
                cloudStorageStatuses = emptySet())
            .toEntity()

    // Preserve ObjectBox internal ID
    updated.objectId = existing.objectId

    eventBox.put(updated)
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {

    val existing = eventBox.query(EventEntity_.id.equal(itemId)).build().findFirst()

    require(existing != null) { "Item with id $itemId does not exist." }

    require(!existing.hasBeenDeleted) { "Item with id $itemId already deleted." }
    require(existing.organizationId == orgId) {
      "Item's organizationId ${existing.organizationId} does not match the provided orgId $orgId."
    }

    // Soft delete: mark as deleted and update version
    existing.version = System.currentTimeMillis()
    existing.hasBeenDeleted = true
    existing.cloudStorageStatuses = encodeSet(emptySet())

    eventBox.put(existing)
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {

    val existing = eventBox.query(EventEntity_.id.equal(itemId)).build().findFirst() ?: return null

    if (existing.hasBeenDeleted) return null
    if (existing.organizationId != orgId) return null

    return existing.toEvent()
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }

    val results =
        eventBox
            .query(
                EventEntity_.organizationId
                    .equal(orgId)
                    .and(EventEntity_.hasBeenDeleted.equal(false))
                    .and(EventEntity_.startDate.lessOrEqual(endDate.toEpochMilli()))
                    .and(EventEntity_.endDate.greaterOrEqual(startDate.toEpochMilli())))
            .build()
            .find()

    return results.map { it.toEvent() }
  }

  override suspend fun ensureOrganizationExists(orgId: String) {

    val exists =
        eventBox.query(EventEntity_.organizationId.equal(orgId)).build().find().isNotEmpty()

    require(exists) {
      "Organization with id $orgId not found (no events associated with this organization)"
    }
  }
}
