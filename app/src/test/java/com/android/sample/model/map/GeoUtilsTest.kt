package com.android.sample.model.map

import com.android.sample.utils.GeoUtils
import kotlin.test.assertEquals
import org.junit.Test

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

class GeoUtilsTest {
  @Test
  fun `distance between 2 point is correctly calculated with 1km margin`() {
    val parisNewYork = GeoUtils.haversineDistance(38.898, -77.037, 48.858, 2.294)
    assertEquals(parisNewYork, 6161600.00, 1000.00)
  }
}
