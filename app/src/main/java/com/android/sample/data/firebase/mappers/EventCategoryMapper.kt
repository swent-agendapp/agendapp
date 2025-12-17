package com.android.sample.data.firebase.mappers

import androidx.compose.ui.graphics.Color
import com.android.sample.model.calendar.Event
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Event] objects and vice versa. */
object EventCategoryMapper : FirestoreMapper<EventCategory> {

  const val ID_FIELD = "id"
  const val ORGANIZATION_ID_FIELD = "organizationId"
  const val INDEX_FIELD = "index"
  const val LABEL_FIELD = "label"
  const val COLOR_FIELD = "color"
  const val IS_DEFAULT_FIELD = "isDefault"

  override fun fromDocument(document: DocumentSnapshot): EventCategory? {
    val id = document.id
    val organizationId = document.getString(ORGANIZATION_ID_FIELD) ?: return null
    val index = document.getLong(INDEX_FIELD) ?: 0
    val label = document.getString(LABEL_FIELD) ?: "Uncategorized"
    val color = document.getLong(COLOR_FIELD) ?: EventPalette.NoCategory.value.toLong()
    val isDefault = document.getBoolean(IS_DEFAULT_FIELD) ?: true

    return EventCategory(
        id = id,
        organizationId = organizationId,
        index = index.toInt(),
        label = label,
        color = Color(color.toULong()),
        isDefault = isDefault)
  }

  override fun fromMap(data: Map<String, Any?>): EventCategory? {
    val id = data[ID_FIELD] as? String ?: return null
    val organizationId = data[ORGANIZATION_ID_FIELD] as? String ?: return null
    val index = data[INDEX_FIELD] as? Long ?: 0
    val label = data[LABEL_FIELD] as? String ?: "Uncategorized"
    val color = data[COLOR_FIELD] as? Long ?: EventPalette.NoCategory.value.toLong()
    val isDefault = data[IS_DEFAULT_FIELD] as? Boolean ?: true

    return EventCategory(
        id = id,
        organizationId = organizationId,
        index = index.toInt(),
        label = label,
        color = Color(color.toULong()),
        isDefault = isDefault)
  }

  override fun toMap(model: EventCategory): Map<String, Any?> {
    return mapOf(
        ID_FIELD to model.id,
        ORGANIZATION_ID_FIELD to model.organizationId,
        INDEX_FIELD to model.index,
        LABEL_FIELD to model.label,
        COLOR_FIELD to model.color.value.toLong(), // Color -> Long
        IS_DEFAULT_FIELD to model.isDefault)
  }
}
