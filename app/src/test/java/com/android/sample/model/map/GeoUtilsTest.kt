package com.android.sample.model.map

import com.android.sample.utils.GeoUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeoUtilsTest {

  private lateinit var squareArea: Area
  private lateinit var triangleArea: Area

  private lateinit var insideSquare: Marker
  private lateinit var outsideSquare: Marker
  private lateinit var onVertexSquare: Marker
  private lateinit var onEdgeSquare: Marker

  private lateinit var insideTriangle: Marker
  private lateinit var outsideTriangle: Marker

  @Before
  fun setup() {
    // Square area
    val s1 = Marker(latitude = 0.0, longitude = 0.0)
    val s2 = Marker(latitude = 0.0, longitude = 1.0)
    val s3 = Marker(latitude = 1.0, longitude = 1.0)
    val s4 = Marker(latitude = 1.0, longitude = 0.0)

    squareArea = Area(label = "Square", markers = listOf(s1, s2, s3, s4))

    insideSquare = Marker(latitude = 0.5, longitude = 0.5)
    outsideSquare = Marker(latitude = 1.5, longitude = 0.5)
    onVertexSquare = Marker(latitude = 0.0, longitude = 0.0)
    onEdgeSquare = Marker(latitude = 0.0, longitude = 0.5)

    // Triangle area
    val t1 = Marker(latitude = 0.0, longitude = 0.0)
    val t2 = Marker(latitude = 2.0, longitude = 0.0)
    val t3 = Marker(latitude = 1.0, longitude = 2.0)

    triangleArea = Area(label = "Triangle", markers = listOf(t1, t2, t3))

    insideTriangle = Marker(latitude = 1.0, longitude = 0.5)
    outsideTriangle = Marker(latitude = 2.0, longitude = 2.0)
  }

  @Test
  fun `points inside square should be detected correctly`() {
    assertTrue(
        GeoUtils.isPointInPolygon(
            insideSquare.location.latitude, insideSquare.location.longitude, squareArea))
  }

  @Test
  fun `points outside square should be detected correctly`() {
    assertFalse(
        GeoUtils.isPointInPolygon(
            outsideSquare.location.latitude, outsideSquare.location.longitude, squareArea))
  }

  @Test
  fun `point on vertex should be detected as inside square`() {
    assertTrue(
        GeoUtils.isPointInPolygon(
            onVertexSquare.location.latitude, onVertexSquare.location.longitude, squareArea))
  }

  @Test
  fun `point on edge should be detected as inside square`() {
    assertTrue(
        GeoUtils.isPointInPolygon(
            onEdgeSquare.location.latitude, onEdgeSquare.location.longitude, squareArea))
  }

  @Test
  fun `points inside triangle should be detected correctly`() {
    assertTrue(
        GeoUtils.isPointInPolygon(
            insideTriangle.location.latitude, insideTriangle.location.longitude, triangleArea))
  }

  @Test
  fun `points outside triangle should be detected correctly`() {
    assertFalse(
        GeoUtils.isPointInPolygon(
            outsideTriangle.location.latitude, outsideTriangle.location.longitude, triangleArea))
  }
}
