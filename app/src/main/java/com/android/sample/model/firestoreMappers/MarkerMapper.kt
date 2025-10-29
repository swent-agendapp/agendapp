package com.android.sample.model.firestoreMappers

import com.android.sample.model.map.Marker
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Marker] objects and vice versa. */
object MarkerMapper : FirestoreMapper<Marker> {

  override fun fromDocument(document: DocumentSnapshot): Marker? {
    val id = document.getString("id") ?: document.id
    val label = document.getString("label")

    val locationObj = document.get("location") ?: return null
    val location = LocationMapper.fromAny(locationObj) ?: return null

    return Marker(id = id, location = location, label = label)
  }

  override fun fromMap(data: Map<String, Any?>): Marker? {
    val id = data["id"] as? String ?: return null
    val label = data["label"] as? String

    val locationData = data["location"] ?: return null
    val location = LocationMapper.fromAny(locationData) ?: return null

    return Marker(id = id, location = location, label = label)
  }

  override fun toMap(model: Marker): Map<String, Any?> {
    val locationMap = LocationMapper.toMap(model.location)

    return mapOf("id" to model.id, "label" to model.label, "location" to locationMap)
  }
}
