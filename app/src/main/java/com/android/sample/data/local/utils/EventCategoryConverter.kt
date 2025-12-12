package com.android.sample.data.local.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette
import io.objectbox.converter.PropertyConverter
import java.util.UUID
import org.json.JSONObject

class EventCategoryConverter : PropertyConverter<EventCategory, String> {
  // Keys for serializing and deserializing EventCategory properties
  private companion object {
    const val KEY_ID = "id"
    const val KEY_ORG_ID = "organizationId"
    const val KEY_LABEL = "label"
    const val KEY_COLOR = "color"
    const val KEY_DEFAULT = "isDefault"
  }

  override fun convertToDatabaseValue(entityProperty: EventCategory?): String {
    if (entityProperty == null) return ""
    val json =
        JSONObject().apply {
          put(KEY_ID, entityProperty.id)
          put(KEY_ORG_ID, entityProperty.organizationId)
          put(KEY_LABEL, entityProperty.label)
          put(KEY_COLOR, entityProperty.color.toArgb())
          put(KEY_DEFAULT, entityProperty.isDefault)
        }
    return json.toString()
  }

  override fun convertToEntityProperty(databaseValue: String?): EventCategory {
    if (databaseValue.isNullOrBlank()) return EventCategory.defaultCategory()
    val json = JSONObject(databaseValue)
    val id = json.optString(KEY_ID, UUID.randomUUID().toString())
    val organizationId = json.optString(KEY_ORG_ID, UUID.randomUUID().toString())
    val label = json.optString(KEY_LABEL, "")
    val colorLong = json.optLong(KEY_COLOR, EventPalette.NoCategory.value.toLong())
    val color = Color(colorLong.toULong())
    val isDefault = json.optBoolean(KEY_DEFAULT, false)

    return EventCategory(
        id = id,
        organizationId = organizationId,
        label = label,
        color = color,
        isDefault = isDefault,
    )
  }
}
