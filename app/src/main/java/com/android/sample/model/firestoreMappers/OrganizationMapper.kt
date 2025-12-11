package com.android.sample.model.firestoreMappers

import com.android.sample.model.organization.data.Organization
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Organization] objects and vice versa. */
object OrganizationMapper : FirestoreMapper<Organization> {

  override fun fromDocument(document: DocumentSnapshot): Organization? {
    val id = document.id
    val name = document.getString("name") ?: return null
    val geoCheckEnabled = document.getBoolean("geoCheckEnabled") ?: false

    val eventsData = document["events"] as? List<*> ?: emptyList<Any>()

    val events = eventsData.mapNotNull { EventMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        events = events,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun fromMap(data: Map<String, Any?>): Organization? {
    val id = data["id"] as? String ?: return null
    val name = data["name"] as? String ?: return null
    val geoCheckEnabled = data["geoCheckEnabled"] as? Boolean ?: false

    val eventsData = data["events"] as? List<*> ?: emptyList<Any>()

    val events = eventsData.mapNotNull { EventMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        events = events,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun toMap(model: Organization): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "name" to model.name,
        "events" to model.events.map { EventMapper.toMap(it) },
        "areas" to model.areas.map { AreaMapper.toMap(it) },
        "geoCheckEnabled" to model.geoCheckEnabled)
  }
}
