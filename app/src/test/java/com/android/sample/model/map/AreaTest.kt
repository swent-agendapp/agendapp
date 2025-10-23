package com.android.sample.model.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AreaTest {

  private lateinit var area: Area
  private lateinit var insidePoint: Marker
  private lateinit var outsidePoint: Marker

  @Before
  fun setup() {
    // Define markers forming a square area
    val m1 = Marker(latitude = 48.8566, longitude = 2.3522)
    val m2 = Marker(latitude = 48.8666, longitude = 2.3522)
    val m3 = Marker(latitude = 48.8666, longitude = 2.3622)
    val m4 = Marker(latitude = 48.8566, longitude = 2.3622)

    area = Area(label = "Test Area", markers = listOf(m1, m2, m3, m4))

    // Points to test
    insidePoint = Marker(latitude = 48.861, longitude = 2.357)
    outsidePoint = Marker(latitude = 48.870, longitude = 2.350)
  }

  @Test
  fun `area contains should return true for point inside and false for point outside`() {
    assertTrue(area.contains(insidePoint))
    assertFalse(area.contains(outsidePoint))
  }
}
