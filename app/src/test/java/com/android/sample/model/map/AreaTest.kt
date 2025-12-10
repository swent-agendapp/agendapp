package com.android.sample.model.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AreaTest {

  private lateinit var area: Area
  private lateinit var insidePoint: Marker
  private lateinit var outsidePoint: Marker
  private lateinit var m1: Marker

  @Before
  fun setup() {
    // Define markers forming a square area in a counter-clockwise order
    m1 = Marker(latitude = 0.0, longitude = 0.0, label = "Circle-Center")

    area = Area(label = "Test Area", marker = m1, radius = 1000.0)

    // Points to test
    insidePoint = Marker(latitude = 0.008983, longitude = 0.0, label = "Inside Point")
    outsidePoint = Marker(latitude = 48.870, longitude = 2.350, label = "Outside Point")
  }

  @Test
  fun `area contains should return true for point inside and false for point outside`() {
    assertTrue(area.contains(insidePoint))
    assertFalse(area.contains(outsidePoint))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `area creation should fail with negative radius`() {
    Area(label = "Empty Area", marker = Marker(latitude = 0.0, longitude = 10.0), radius = -1.0)
  }
}
