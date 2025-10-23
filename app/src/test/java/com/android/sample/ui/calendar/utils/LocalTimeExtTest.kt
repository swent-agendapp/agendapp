package com.android.sample.ui.calendar.utils

import java.time.LocalTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalTimeExtTest {

  @Test
  fun `toLocalString formats time with localized short style`() {
    val previous = Locale.getDefault()
    try {
      Locale.setDefault(Locale.US)
      val formatted = LocalTime.of(14, 5).toLocalString()
      // Some JDKs render the localized short style using a narrow no-break space or a regular
      // space before the meridiem indicator (e.g., "PM"). Normalize the whitespace so the
      // expectation matches regardless of the formatter output variant.
      val normalized = formatted.replace('\u202f', ' ').replace('\u00a0', ' ')
      assertEquals("2:05 PM", normalized)
    } finally {
      Locale.setDefault(previous)
    }
  }
}
