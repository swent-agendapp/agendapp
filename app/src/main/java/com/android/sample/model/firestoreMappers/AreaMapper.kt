package com.android.sample.model.firestoreMappers

import com.android.sample.model.map.Area
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Area] objects and vice versa. */
object AreaMapper : FirestoreMapper<Area> {

  override fun fromDocument(document: DocumentSnapshot): Area? {
    val id = document.getString("id") ?: document.id
    val label = document.getString("label")
    val version = document.getLong("version") ?: 0L

    val markersData = document["markers"] as? List<*> ?: return null

    val markers = markersData.mapNotNull { MarkerMapper.fromAny(it) }

    return try {
      Area(id = id, label = label, markers = markers, version = version)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  override fun fromMap(data: Map<String, Any?>): Area? {
    val id = data["id"] as? String ?: return null
    val label = data["label"] as? String
    val version = (data["version"] as? Number)?.toLong() ?: 0L

    val markersData = data["markers"] as? List<*> ?: return null

    val markers = markersData.mapNotNull { MarkerMapper.fromAny(it) }

    return try {
      Area(id = id, label = label, markers = markers, version = version)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  override fun toMap(model: Area): Map<String, Any?> {
    val markersList = model.getSortedMarkers().map { MarkerMapper.toMap(it) }

    return mapOf(
        "id" to model.id,
        "label" to model.label,
        "markers" to markersList,
        "version" to model.version,
    )
  }
}
