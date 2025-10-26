package com.android.sample.model.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AreaTest {

  private lateinit var area: Area
  private lateinit var insidePoint: Marker
  private lateinit var outsidePoint: Marker
  private lateinit var m1: Marker
  private lateinit var m2: Marker
  private lateinit var m3: Marker
  private lateinit var m4: Marker

  @Before
  fun setup() {
    // Define markers forming a square area in a counter-clockwise order
    m1 = Marker(latitude = 48.8566, longitude = 2.3522, label = "Bottom-left")
    m2 = Marker(latitude = 48.8566, longitude = 2.3622, label = "Bottom-right")
    m3 = Marker(latitude = 48.8666, longitude = 2.3622, label = "Top-right")
    m4 = Marker(latitude = 48.8666, longitude = 2.3522, label = "Top-left")

    area = Area(label = "Test Area", markers = listOf(m1, m2, m3, m4))

    // Points to test
    insidePoint = Marker(latitude = 48.861, longitude = 2.357, label = "Inside Point")
    outsidePoint = Marker(latitude = 48.870, longitude = 2.350, label = "Outside Point")
  }

  @Test
  fun `area contains should return true for point inside and false for point outside`() {
    assertTrue(area.contains(insidePoint))
    assertFalse(area.contains(outsidePoint))
  }

  @Test
  fun `area should correctly sort markers even when given in random order`() {
    val sortedMarkers = listOf(m1, m2, m3, m4)

    // Randomized order of the same markers
    val shuffledMarkers = sortedMarkers.shuffled()
    val randomOrderArea = Area(label = "Random Order Area", markers = shuffledMarkers)

    // Retrieve the sorted markers from the area
    val sorted = randomOrderArea.getSortedMarkers()

    // Check that the sorted list contains the same markers
    assertEquals(sortedMarkers.toSet(), sorted.toSet())

    // Check that the order is the same up to a circular rotation
    val expectedIds = sortedMarkers.map { it.id }
    val actualIds = sorted.map { it.id }
    assertTrue((expectedIds + expectedIds).windowed(expectedIds.size).any { it == actualIds })

    // Check that in area point detection still works
    assertTrue(area.contains(insidePoint))
    assertFalse(area.contains(outsidePoint))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `area creation should fail with less than 3 distinct markers`() {
    val duplicate = Marker(latitude = 48.8566, longitude = 2.3522)
    Area(label = "Invalid", markers = listOf(m1, m2, duplicate))
  }

  @Test
  fun `area should ignore duplicate markers and still be valid`() {
    // Define 4 markers, where m4 is a duplicate of m1
    val m1 = Marker(latitude = 48.8566, longitude = 2.3522)
    val m2 = Marker(latitude = 48.8666, longitude = 2.3522)
    val m3 = Marker(latitude = 48.8666, longitude = 2.3622)
    val m4 = m1.copy()

    // Should still be valid since there are 3 distinct coordinates
    val area = Area(label = "Duplicate Marker Area", markers = listOf(m1, m2, m3, m4))

    // Ensure only 3 distinct markers are kept internally
    val sorted = area.getSortedMarkers()
    assertEquals(3, sorted.size)
    assertTrue(sorted.containsAll(listOf(m1, m2, m3)))
  }

  @Test
  fun `area should correctly handle clockwise marker order`() {
    val clockwiseArea = Area(label = "Clockwise Area", markers = listOf(m1, m4, m3, m2))
    assertTrue(clockwiseArea.contains(insidePoint))
    assertFalse(clockwiseArea.contains(outsidePoint))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `area creation should fail with empty marker list`() {
    Area(label = "Empty Area", markers = emptyList())
  }
}
