package com.android.sample.model.firestoreMappers

import com.android.sample.model.map.Location
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Location] objects and vice versa. */
object LocationMapper : FirestoreMapper<Location> {

  override fun fromDocument(document: DocumentSnapshot): Location? {
    val latitude = document.getDouble("latitude") ?: return null
    val longitude = document.getDouble("longitude") ?: return null

    return Location(latitude = latitude, longitude = longitude)
  }

  override fun fromMap(data: Map<String, Any?>): Location? {
    val latitude = (data["latitude"] as? Number)?.toDouble() ?: return null
    val longitude = (data["longitude"] as? Number)?.toDouble() ?: return null

    return Location(latitude = latitude, longitude = longitude)
  }

  override fun toMap(model: Location): Map<String, Any?> {
    return mapOf("latitude" to model.latitude, "longitude" to model.longitude)
  }
}
