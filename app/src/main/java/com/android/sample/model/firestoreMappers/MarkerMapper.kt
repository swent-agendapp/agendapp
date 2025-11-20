package com.android.sample.model.firestoreMappers

import com.android.sample.model.map.Marker
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Marker] objects and vice versa. */
object MarkerMapper : FirestoreMapper<Marker> {

  override fun fromDocument(document: DocumentSnapshot): Marker? {
    val id = document.getString("id") ?: document.id

    val locationObj = document["location"] ?: return null
    val location = LocationMapper.fromAny(locationObj) ?: return null

    return Marker(id = id, location = location)
  }

  override fun fromMap(data: Map<String, Any?>): Marker? {
    val id = data["id"] as? String ?: return null

    val locationData = data["location"] ?: return null
    val location = LocationMapper.fromAny(locationData) ?: return null

    return Marker(id = id, location = location)
  }

  override fun toMap(model: Marker): Map<String, Any?> {
    val locationMap = LocationMapper.toMap(model.location)

    return mapOf("id" to model.id, "location" to locationMap)
  }
}
