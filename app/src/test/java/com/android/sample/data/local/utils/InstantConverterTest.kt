package com.android.sample.data.local.utils

import java.time.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InstantConverterTest {

  private lateinit var converter: InstantConverter

  // Allowable delta for comparing epoch milli values in round-trip test
  // This is due because of precision loss when converting to/from Long
  private val DELTA_MILLIS = 1.0

  @Before
  fun setUp() {
    converter = InstantConverter()
  }

  @Test
  fun testConvertToDatabaseValue_nonNull() {
    val instant = Instant.parse("2025-12-02T12:34:56.789Z")
    val dbValue = converter.convertToDatabaseValue(instant)
    assertEquals(instant.toEpochMilli(), dbValue)
  }

  @Test
  fun testConvertToDatabaseValue_null() {
    val dbValue = converter.convertToDatabaseValue(null)
    assertEquals(0L, dbValue)
  }

  @Test
  fun testConvertToEntityProperty_nonNull() {
    val epochMilli = 1764964496789L
    val instant = converter.convertToEntityProperty(epochMilli)
    assertEquals(Instant.ofEpochMilli(epochMilli), instant)
  }

  @Test
  fun testConvertToEntityProperty_null() {
    val instant = converter.convertToEntityProperty(null)
    assertEquals(Instant.ofEpochMilli(0L), instant)
  }

  @Test
  fun testRoundTrip() {
    val original = Instant.now()
    val dbValue = converter.convertToDatabaseValue(original)
    val restored = converter.convertToEntityProperty(dbValue)

    val expected = original.toEpochMilli().toDouble()
    val actual = restored.toEpochMilli().toDouble()

    assertEquals(expected, actual, DELTA_MILLIS)
  }
}
