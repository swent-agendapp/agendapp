package com.android.sample.data.local.utils

import org.junit.Assert.*
import org.junit.Test

class SerializationHelpersTest {

  @Test
  fun `encodeList should join elements with comma`() {
    val list = listOf("a", "b", "c")
    val encoded = encodeList(list)
    assertEquals("a,b,c", encoded)
  }

  @Test
  fun `decodeList should split comma-separated values`() {
    val raw = "a,b,c"
    val decoded = decodeList(raw)
    assertEquals(listOf("a", "b", "c"), decoded)
  }

  @Test
  fun `decodeList should return emptyList for blank input`() {
    assertTrue(decodeList("").isEmpty())
  }

  @Test
  fun `List round-trip encode and decode`() {
    val list = listOf("x", "y", "z")
    val roundTrip = decodeList(encodeList(list))
    assertEquals(list, roundTrip)
  }

  @Test
  fun `encodeSet should join elements with comma`() {
    val set = setOf("a", "b")
    val encoded = encodeSet(set)
    val parts = encoded.split(",").sorted() // order not guaranteed
    assertEquals(listOf("a", "b"), parts)
  }

  @Test
  fun `decodeSet should split comma-separated values into a set`() {
    val raw = "x,y"
    val decoded = decodeSet(raw)
    assertEquals(setOf("x", "y"), decoded)
  }

  @Test
  fun `decodeSet should return emptySet for blank input`() {
    assertTrue(decodeSet("").isEmpty())
  }

  @Test
  fun `Set round-trip encode and decode`() {
    val set = setOf("x", "y", "z")
    val roundTrip = decodeSet(encodeSet(set))
    assertEquals(set, roundTrip)
  }

  @Test
  fun `encodeBooleanMap should encode map into keyColonValue`() {
    val map = mapOf("a" to true, "b" to false)
    val encoded = encodeBooleanMap(map)

    val parts = encoded.split(",").sorted()
    assertEquals(listOf("a:true", "b:false"), parts)
  }

  @Test
  fun `decodeBooleanMap should decode keyColonValue pairs`() {
    val raw = "a:true,b:false"
    val decoded = decodeBooleanMap(raw)

    assertEquals(2, decoded.size)
    assertEquals(true, decoded["a"])
    assertEquals(false, decoded["b"])
  }

  @Test
  fun `decodeBooleanMap should return emptyMap for blank input`() {
    assertTrue(decodeBooleanMap("").isEmpty())
  }

  @Test
  fun `decodeBooleanMap should skip malformed entries`() {
    val raw = "a:true,WRONG,b:false"
    val decoded = decodeBooleanMap(raw)

    assertEquals(mapOf("a" to true, "b" to false), decoded)
    assertFalse(decoded.containsKey("WRONG"))
  }

  @Test
  fun `Boolean Map round-trip encode and decode`() {
    val map = mapOf("one" to true, "two" to false, "three" to true)
    val roundTrip = decodeBooleanMap(encodeBooleanMap(map))
    assertEquals(map, roundTrip)
  }

  @Test
  fun `Generic Map round-trip with Int values`() {
    val map = mapOf("x" to 1, "y" to 2, "z" to 3)
    val encoded = encodeMap(map) { it.toString() }
    val decoded = decodeMap(encoded) { it.toIntOrNull() }
    assertEquals(map, decoded)
  }
}
