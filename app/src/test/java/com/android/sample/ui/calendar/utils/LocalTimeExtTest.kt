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

      // Some JDKs render the localized short style using a narrow no-break space before the
      // meridiem indicator (e.g., "PM"). Explicitly include that character so the expectation
      // matches the formatter output regardless of whitespace variant.
      assertEquals("2:05\u202fPM", formatted)
    } finally {
      Locale.setDefault(previous)
    }
  }
}
