package com.android.sample.model.firestoreMappers

import com.android.sample.model.organization.data.Organization
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Organization] objects and vice versa. */
object OrganizationMapper : FirestoreMapper<Organization> {

  override fun fromDocument(document: DocumentSnapshot): Organization? {
    val id = document.id
    val name = document.getString("name") ?: return null
    val geoCheckEnabled = document.getBoolean("geoCheckEnabled") ?: false

    val admins = (document["admins"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val members = (document["members"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

    val areasData = document["areas"] as? List<*> ?: emptyList<Any>()
    val eventsData = document["events"] as? List<*> ?: emptyList<Any>()

    val areas = areasData.mapNotNull { AreaMapper.fromAny(it) }
    val events = eventsData.mapNotNull { EventMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        admins = admins,
        members = members,
        events = events,
        areas = areas,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun fromMap(data: Map<String, Any?>): Organization? {
    val id = data["id"] as? String ?: return null
    val name = data["name"] as? String ?: return null
    val geoCheckEnabled = data["geoCheckEnabled"] as? Boolean ?: false

    val admins = (data["admins"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val members = (data["members"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

    val areasData = data["areas"] as? List<*> ?: emptyList<Any>()
    val eventsData = data["events"] as? List<*> ?: emptyList<Any>()

    val areas = areasData.mapNotNull { AreaMapper.fromAny(it) }
    val events = eventsData.mapNotNull { EventMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        admins = admins,
        members = members,
        events = events,
        areas = areas,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun toMap(model: Organization): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "name" to model.name,
        "admins" to model.admins,
        "members" to model.members,
        "events" to model.events.map { EventMapper.toMap(it) },
        "areas" to model.areas.map { AreaMapper.toMap(it) },
        "geoCheckEnabled" to model.geoCheckEnabled)
  }
}
