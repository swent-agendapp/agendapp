package com.android.sample.model.firestoreMappers

import com.android.sample.model.map.Area
import com.android.sample.model.map.Marker
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Area] objects and vice versa. */
object AreaMapper : FirestoreMapper<Area> {

  override fun fromDocument(document: DocumentSnapshot): Area? {
    val id = document.getString("id") ?: document.id
    val label = document.getString("label") ?: return null

    val radius = document.getDouble("radius") ?: return null

    val markerData = document["marker"] ?: return null
    val marker = MarkerMapper.fromAny(markerData) ?: return null

    return try {
      Area(id = id, label = label, marker = marker, radius = radius)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  override fun fromMap(data: Map<String, Any?>): Area? {
    val id = data["id"] as? String ?: return null
    val label = data["label"] as? String ?: return null

    val markerData = data["marker"] ?: return null
    val marker = MarkerMapper.fromAny(markerData) ?: return null

    val radius = (data["radius"] as? Number)?.toDouble() ?: return null

    return try {
      Area(id = id, label = label, marker = marker, radius = radius)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  override fun toMap(model: Area): Map<String, Any?> {
    val marker = MarkerMapper.toMap(model.marker)

    return mapOf(
        "id" to model.id, "label" to model.label, "marker" to marker, "radius" to model.radius)
  }
}
