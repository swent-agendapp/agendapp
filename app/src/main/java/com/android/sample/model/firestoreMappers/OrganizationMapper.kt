package com.android.sample.model.firestoreMappers

import com.android.sample.model.organizations.Organization
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Organization] objects and vice versa. */
object OrganizationMapper : FirestoreMapper<Organization> {

  override fun fromDocument(document: DocumentSnapshot): Organization? {
    val id = document.id
    val name = document.getString("name") ?: return null
    val geoCheckEnabled = document.getBoolean("geoCheckEnabled") ?: false

    val adminsData = document["admins"] as? List<*> ?: emptyList<Any>()
    val membersData = document["members"] as? List<*> ?: emptyList<Any>()
    val areasData = document["areas"] as? List<*> ?: emptyList<Any>()

    val admins = adminsData.mapNotNull { UserMapper.fromAny(it) }
    val members = membersData.mapNotNull { UserMapper.fromAny(it) }
    val areas = areasData.mapNotNull { AreaMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        admins = admins,
        members = members,
        areas = areas,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun fromMap(data: Map<String, Any?>): Organization? {
    val id = data["id"] as? String ?: return null
    val name = data["name"] as? String ?: return null
    val geoCheckEnabled = data["geoCheckEnabled"] as? Boolean ?: false

    val adminsData = data["admins"] as? List<*> ?: emptyList<Any>()
    val membersData = data["members"] as? List<*> ?: emptyList<Any>()
    val areasData = data["areas"] as? List<*> ?: emptyList<Any>()

    val admins = adminsData.mapNotNull { UserMapper.fromAny(it) }
    val members = membersData.mapNotNull { UserMapper.fromAny(it) }
    val areas = areasData.mapNotNull { AreaMapper.fromAny(it) }

    return Organization(
        id = id,
        name = name,
        admins = admins,
        members = members,
        areas = areas,
        geoCheckEnabled = geoCheckEnabled)
  }

  override fun toMap(model: Organization): Map<String, Any?> {
    return mapOf(
        "name" to model.name,
        "admins" to model.admins.map { UserMapper.toMap(it) },
        "members" to model.members.map { UserMapper.toMap(it) },
        "areas" to model.areas.map { AreaMapper.toMap(it) },
        "geoCheckEnabled" to model.geoCheckEnabled)
  }
}
