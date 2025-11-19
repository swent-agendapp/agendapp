package com.android.sample.ui.theme

import androidx.compose.ui.graphics.toArgb
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorsTest {

  @Test
  fun `fromLong should create Color with correct ARGB components`() {
    // Arrange a Long ARGB with alpha = 255, red = 18, green = 52, blue = 86
    val argbLong = 0xFF123456

    val color = Palette.fromLong(argbLong)

    // Assert the components are correctly extracted
    assertThat(color.alpha).isEqualTo(1.0f) // alpha 255 → 1.0f
    assertThat(color.red).isEqualTo(18 / 255f)
    assertThat(color.green).isEqualTo(52 / 255f)
    assertThat(color.blue).isEqualTo(86 / 255f)
  }

  @Test
  fun `fromLong should handle semi transparent colors`() {
    // Arrange a Long ARGB with alpha = 128, red = 255, green = 0, blue = 0
    val argbLong = 0x80FF0000

    val color = Palette.fromLong(argbLong)

    // Assert the components are correctly extracted
    assertThat(color.alpha).isWithin(0.01f).of(128 / 255f)
    assertThat(color.red).isEqualTo(1.0f)
    assertThat(color.green).isEqualTo(0f)
    assertThat(color.blue).isEqualTo(0f)
  }

  @Test
  fun `fromLong works with colors in a palette`() {
    val expectedColor = Palette.CadmiumBlue_80
    val fromLongColor = Palette.fromLong(expectedColor.toArgb().toLong())

    assertThat(fromLongColor).isEqualTo(expectedColor)
  }
}
