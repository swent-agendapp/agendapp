package com.android.sample.model.map

import com.android.sample.utils.GeoUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeoUtilsTest {

  private lateinit var squareArea: Area
  private lateinit var triangleArea: Area
  private lateinit var arrowArea: Area

  private lateinit var insideSquare: Marker
  private lateinit var outsideSquare: Marker
  private lateinit var onVertexSquare: Marker
  private lateinit var onEdgeSquare: Marker

  private lateinit var insideTriangle: Marker
  private lateinit var outsideTriangle: Marker

  private lateinit var insideArrow1: Marker
  private lateinit var insideArrow2: Marker
  private lateinit var outsideArrow: Marker

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

    /*
     * Arrow-shaped area :
     *
     * .a4---------------------------------.a3
     * |                                 ⟋
     * |            .insideArrow1    ⟋
     * |                         ⟋
     * |                     ⟋
     * |                 ⟋
     * |            .a2
     * |           /
     * |         /
     * |  .insideArrow2
     * |      /
     * |    /           .outsideArrow
     * |  /
     * .a1
     *
     */
    val a1 = Marker(latitude = 46.517, longitude = 6.565)
    val a2 = Marker(latitude = 46.547, longitude = 6.595)
    val a3 = Marker(latitude = 46.567, longitude = 6.715)
    val a4 = Marker(latitude = 46.567, longitude = 6.565)

    arrowArea = Area(label = "Arrow", markers = listOf(a1, a2, a3, a4))

    insideArrow1 = Marker(latitude = 46.557, longitude = 6.595)
    insideArrow2 = Marker(latitude = 46.535, longitude = 6.572)
    outsideArrow = Marker(latitude = 46.527, longitude = 6.595)
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

  @Test
  fun `points inside arrow should be detected correctly`() {
    assertTrue(
        GeoUtils.isPointInPolygon(
            insideArrow1.location.latitude, insideArrow1.location.longitude, arrowArea))

    assertTrue(
        GeoUtils.isPointInPolygon(
            insideArrow2.location.latitude, insideArrow2.location.longitude, arrowArea))
  }

  @Test
  fun `points outside arrow should be detected correctly`() {
    assertFalse(
        GeoUtils.isPointInPolygon(
            outsideArrow.location.latitude, outsideArrow.location.longitude, arrowArea))
  }
}
