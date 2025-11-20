package com.android.sample.model.firestoreMappers

import android.util.Log
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Replacement] objects and vice versa. */
object ReplacementMapper : FirestoreMapper<Replacement> {

    private const val ID_FIELD = "id"
    private const val FIELD_ABSENT_USER_ID = "absentUserId"
    private const val FIELD_SUBSTITUTE_USER_ID = "substituteUserId"
    private const val FIELD_STATUS = "status"
    private const val FIELD_EVENT = "event"
    private const val FIELD_VERSION = "version"

    override fun fromDocument(document: DocumentSnapshot): Replacement? {
        val id = document.getString(ID_FIELD) ?: return null
        val absentUserId = document.getString(FIELD_ABSENT_USER_ID) ?: return null
        val substituteUserId = document.getString(FIELD_SUBSTITUTE_USER_ID) ?: return null
        val statusString = document.getString(FIELD_STATUS) ?: return null

        val status = runCatching {
            ReplacementStatus.valueOf(statusString)
        }.getOrElse {
            Log.e("ReplacementMapper", "Unknown status \"$statusString\", defaulting to ToProcess")
            ReplacementStatus.ToProcess
        }

        // event is stored as nested map
        val rawEvent = document[FIELD_EVENT] as? Map<*, *> ?: return null
        val eventMap = rawEvent.mapKeys { it.key.toString() }
        val event = EventMapper.fromMap(eventMap) ?: return null
        val version = document.getLong(FIELD_VERSION) ?: return null

        return Replacement(
            id = id,
            absentUserId = absentUserId,
            substituteUserId = substituteUserId,
            event = event,
            status = status,
            version = version
        )
    }

    override fun fromMap(data: Map<String, Any?>): Replacement? {
        val id = data[ID_FIELD] as? String ?: return null
        val absentUserId = data[FIELD_ABSENT_USER_ID] as? String ?: return null
        val substituteUserId = data[FIELD_SUBSTITUTE_USER_ID] as? String ?: return null
        val statusString = data[FIELD_STATUS] as? String ?: return null

        val status = runCatching {
            ReplacementStatus.valueOf(statusString)
        }.getOrElse {
            Log.e("ReplacementMapper", "Unknown status \"$statusString\", defaulting to ToProcess")
            ReplacementStatus.ToProcess
        }

        val rawEvent = data[FIELD_EVENT] as? Map<*, *> ?: return null
        val eventMap = rawEvent.mapKeys { it.key.toString() }
        val event = EventMapper.fromMap(eventMap) ?: return null
        val version = (data[FIELD_VERSION] as? Number)?.toLong() ?: return null

        return Replacement(
            id = id,
            absentUserId = absentUserId,
            substituteUserId = substituteUserId,
            event = event,
            status = status,
            version = version
        )
    }

    override fun toMap(model: Replacement): Map<String, Any?> {
        return mapOf(
            ID_FIELD to model.id,
            FIELD_ABSENT_USER_ID to model.absentUserId,
            FIELD_SUBSTITUTE_USER_ID to model.substituteUserId,
            FIELD_STATUS to model.status.name,
            FIELD_EVENT to EventMapper.toMap(model.event),
            FIELD_VERSION to model.version
        )
    }
}