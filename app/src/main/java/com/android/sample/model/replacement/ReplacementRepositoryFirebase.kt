package com.android.sample.model.replacement

import com.android.sample.model.constants.FirestoreConstants.REPLACEMENTS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.ReplacementMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Assisted by AI

class ReplacementRepositoryFirebase(private val db: FirebaseFirestore) : ReplacementRepository {

  override suspend fun getAllReplacements(): List<Replacement> {
    val snapshot = db.collection(REPLACEMENTS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun insertReplacement(item: Replacement) {
    db.collection(REPLACEMENTS_COLLECTION_PATH)
        .document(item.id)
        .set(ReplacementMapper.toMap(item))
        .await()
  }

  override suspend fun updateReplacement(itemId: String, item: Replacement) {
    val updatedItem = item.copy(id = itemId)
    db.collection(REPLACEMENTS_COLLECTION_PATH)
        .document(itemId)
        .set(ReplacementMapper.toMap(updatedItem))
        .await()
  }

  override suspend fun deleteReplacement(itemId: String) {
    db.collection(REPLACEMENTS_COLLECTION_PATH).document(itemId).delete().await()
  }

  override suspend fun getReplacementById(itemId: String): Replacement? {
    val document = db.collection(REPLACEMENTS_COLLECTION_PATH).document(itemId).get().await()
    return ReplacementMapper.fromDocument(document)
  }

  override suspend fun getReplacementsByAbsentUser(userId: String): List<Replacement> {
    val snapshot =
        db.collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("absentUserId", userId)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun getReplacementsBySubstituteUser(userId: String): List<Replacement> {
    val snapshot =
        db.collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("substituteUserId", userId)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }

  override suspend fun getReplacementsByStatus(status: ReplacementStatus): List<Replacement> {
    val snapshot =
        db.collection(REPLACEMENTS_COLLECTION_PATH)
            .whereEqualTo("status", status.name)
            .get()
            .await()
    return snapshot.mapNotNull { ReplacementMapper.fromDocument(it) }
  }
}
