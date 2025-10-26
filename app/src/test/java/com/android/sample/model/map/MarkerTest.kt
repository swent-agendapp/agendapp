package com.android.sample.model.map

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkerTest {

  @Test
  fun `marker secondary constructor should initialize location correctly`() {
    val latitude = 46.5191
    val longitude = 6.5668
    val label = "EPFL"

    val marker = Marker(latitude = latitude, longitude = longitude, label = label)

    assertEquals(latitude, marker.location.latitude, 0.0001)
    assertEquals(longitude, marker.location.longitude, 0.0001)
    assertEquals(label, marker.label)
  }

  @Test
  fun `marker default id should be generated`() {
    val marker1 = Marker(latitude = 0.0, longitude = 0.0)
    val marker2 = Marker(latitude = 0.0, longitude = 0.0)

    // Check that two markers have different generated IDs
    assert(marker1.id != marker2.id)
  }
}
