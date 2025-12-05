package com.android.sample.data.local.utils

import io.objectbox.converter.PropertyConverter
import java.time.Instant

/**
 * Converter class to convert between Instant and Long for ObjectBox database storage.
 *
 * Instant is converted to its epoch milli representation (Long) for database storage, and vice
 * versa.
 */
class InstantConverter : PropertyConverter<Instant, Long> {
  // Convert Instant to Long (epoch milli) for database storage
  override fun convertToDatabaseValue(entityProperty: Instant?): Long {
    return entityProperty?.toEpochMilli() ?: 0L
  }

  // Convert Long (epoch milli) back to Instant for entity property
  override fun convertToEntityProperty(databaseValue: Long?): Instant {
    return Instant.ofEpochMilli(databaseValue ?: 0L)
  }
}
