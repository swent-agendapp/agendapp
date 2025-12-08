package com.android.sample.model.firestoreMappers

import androidx.compose.ui.graphics.Color
import com.android.sample.model.calendar.Event
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Event] objects and vice versa. */
object EventCategoryMapper : FirestoreMapper<EventCategory> {

  override fun fromDocument(document: DocumentSnapshot): EventCategory? {
    val id = document.id
    val organizationId = document.getString("organizationId") ?: return null
    val label = document.getString("label") ?: "Uncategorized"
    val color = document.getLong("color") ?: EventPalette.NoCategory.value.toLong()
    val isDefault = document.getBoolean("isDefault") ?: true

    return EventCategory(
        id = id,
        organizationId = organizationId,
        label = label,
        color = Color(color.toULong()),
        isDefault = isDefault)
  }

  override fun fromMap(data: Map<String, Any?>): EventCategory? {
    val id = data["id"] as? String ?: return null
    val organizationId = data["organizationId"] as? String ?: return null
    val label = data["label"] as? String ?: "Uncategorized"
    val color = data["color"] as? Long ?: EventPalette.NoCategory.value.toLong()
    val isDefault = data["isDefault"] as? Boolean ?: true

    return EventCategory(
        id = id,
        organizationId = organizationId,
        label = label,
        color = Color(color.toULong()),
        isDefault = isDefault)
  }

  override fun toMap(model: EventCategory): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "organizationId" to model.organizationId,
        "label" to model.label,
        "color" to model.color.value.toLong(), // Color -> Long
        "isDefault" to model.isDefault)
  }
}
