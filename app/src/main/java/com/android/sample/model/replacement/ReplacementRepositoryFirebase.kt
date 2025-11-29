package com.android.sample.model.replacement

import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.REPLACEMENTS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.ReplacementMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Assisted by AI

class ReplacementRepositoryFirebase(private val db: FirebaseFirestore) : ReplacementRepository {

  override suspend fun getAllReplacements(orgId: String): List<Replacement> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(REPLACEMENTS_COLLECTION_PATH)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun insertReplacement(orgId: String, item: Replacement) {
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(REPLACEMENTS_COLLECTION_PATH)
        .document(item.id)
        .set(ReplacementMapper.toMap(item))
        .await()
  }

  override suspend fun updateReplacement(orgId: String, itemId: String, item: Replacement) {
    val updatedItem = item.copy(id = itemId)
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(REPLACEMENTS_COLLECTION_PATH)
        .document(itemId)
        .set(ReplacementMapper.toMap(updatedItem))
        .await()
  }

  override suspend fun deleteReplacement(orgId: String, itemId: String) {
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(REPLACEMENTS_COLLECTION_PATH)
        .document(itemId)
        .delete()
        .await()
  }

  override suspend fun getReplacementById(orgId: String, itemId: String): Replacement? {
    val document =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(REPLACEMENTS_COLLECTION_PATH)
            .document(itemId)
            .get()
            .await()
    return ReplacementMapper.fromDocument(document)
  }

  override suspend fun getReplacementsByAbsentUser(
      orgId: String,
      userId: String
  ): List<Replacement> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("absentUserId", userId)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun getReplacementsBySubstituteUser(
      orgId: String,
      userId: String
  ): List<Replacement> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("substituteUserId", userId)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun getReplacementsByStatus(
      orgId: String,
      status: ReplacementStatus
  ): List<Replacement> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("status", status.name)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }
}
