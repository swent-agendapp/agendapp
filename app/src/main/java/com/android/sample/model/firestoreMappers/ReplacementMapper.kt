package com.android.sample.model.firestoreMappers

import android.util.Log
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.google.firebase.firestore.DocumentSnapshot

// Assisted by AI

/** Maps Firestore documents to [Replacement] objects and vice versa. */
object ReplacementMapper : FirestoreMapper<Replacement> {

  override fun fromDocument(document: DocumentSnapshot): Replacement? {
    val id = document.id
    val absentUserId = document.getString("absentUserId") ?: return null
    val substituteUserId = document.getString("substituteUserId") ?: return null
    val statusString = document.getString("status") ?: return null

    val status =
        runCatching { ReplacementStatus.valueOf(statusString) }
            .getOrElse {
              Log.e("ReplacementMapper", "Unknown status \"$statusString\". By default : Pending.")
              ReplacementStatus.Pending
            }

    // The event is stored as a nested map inside the replacement document
    val rawEvent = document.get("event") as? Map<*, *>
    val eventMap = rawEvent?.mapKeys { it.key.toString() }
    val event = eventMap?.let { EventMapper.fromMap(it) } ?: return null

    return Replacement(
        id = id,
        absentUserId = absentUserId,
        substituteUserId = substituteUserId,
        event = event,
        status = status)
  }

  override fun fromMap(data: Map<String, Any?>): Replacement? {
    val id = data["id"] as? String ?: return null
    val absentUserId = data["absentUserId"] as? String ?: return null
    val substituteUserId = data["substituteUserId"] as? String ?: return null
    val statusString = data["status"] as? String ?: return null

    val status =
        runCatching { ReplacementStatus.valueOf(statusString) }
            .getOrElse {
              Log.e("ReplacementMapper", "Unknown status \"$statusString\". By default : Pending.")
              ReplacementStatus.Pending
            }

    val rawEvent = data["event"] as? Map<*, *>
    val eventMap = rawEvent?.mapKeys { it.key.toString() }
    val event = eventMap?.let { EventMapper.fromMap(it) } ?: return null

    return Replacement(
        id = id,
        absentUserId = absentUserId,
        substituteUserId = substituteUserId,
        event = event,
        status = status)
  }

  override fun toMap(model: Replacement): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "absentUserId" to model.absentUserId,
        "substituteUserId" to model.substituteUserId,
        "status" to model.status.name,
        "event" to EventMapper.toMap(model.event))
  }
}
