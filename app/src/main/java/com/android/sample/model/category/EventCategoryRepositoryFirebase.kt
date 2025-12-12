package com.android.sample.model.category

import com.android.sample.model.constants.FirestoreConstants.CATEGORIES_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.EventCategoryMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

open class EventCategoryRepositoryFirebase(private val db: FirebaseFirestore) :
    EventCategoryRepository {

  override fun getNewUid(): String {
    return db.collection(CATEGORIES_COLLECTION_PATH).document().id
  }

  override suspend fun getAllCategories(orgId: String): List<EventCategory> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(CATEGORIES_COLLECTION_PATH)
            .whereEqualTo("organizationId", orgId)
            .get()
            .await()
    return snapshot.mapNotNull { EventCategoryMapper.fromDocument(document = it) }
  }

  override suspend fun insertCategory(orgId: String, item: EventCategory) {
    // Calls the interface check to ensure the organizationId matches
    super.insertCategory(orgId, item)

    val data = EventCategoryMapper.toMap(item).toMutableMap()

    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(CATEGORIES_COLLECTION_PATH)
        .document(item.id)
        .set(data)
        .await()
  }

  override suspend fun updateCategory(orgId: String, itemId: String, item: EventCategory) {
    super.updateCategory(orgId, itemId, item)

    val data = EventCategoryMapper.toMap(item).toMutableMap()

    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(CATEGORIES_COLLECTION_PATH)
        .document(itemId)
        .set(data)
        .await()
  }

  override suspend fun deleteCategory(orgId: String, itemId: String) {
    val retrievedItem =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(CATEGORIES_COLLECTION_PATH)
            .document(itemId)
            .get()
            .await()

    require(retrievedItem.getString("organizationId") == orgId) {
      "Category's organizationId ${retrievedItem.getString("organizationId")} does not match the provided orgId $orgId."
    }

    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(CATEGORIES_COLLECTION_PATH)
        .document(itemId)
        .delete()
        .await()
  }

  override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? {
    val document =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(CATEGORIES_COLLECTION_PATH)
            .document(itemId)
            .get()
            .await()

    // Return null if the document does not exist or has been deleted
    if (!document.exists()) return null

    require(document.getString("organizationId") == orgId) {
      "Category's organizationId ${document.getString("organizationId")} does not match the provided orgId $orgId."
    }

    return EventCategoryMapper.fromDocument(document)
  }
}
