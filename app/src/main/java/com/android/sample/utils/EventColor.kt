package com.android.sample.utils

import androidx.compose.ui.graphics.Color

/**
 * Inline value class representing a color for events. Wraps a Long value and provides conversion to
 * Jetpack Compose Color.
 */
@JvmInline
value class EventColor(val value: Long) {
  /** Converts the EventColor to a Jetpack Compose Color object. */
  fun toComposeColor(): Color = Color(value)

  companion object {
    // Predefined color constants for events
    val White = EventColor(0xFFFFFFFF)
    val Black = EventColor(0xFF000000)
    val Orange = EventColor(0xFFFFB74D)
    val Blue = EventColor(0xFF64B5F6)
    val Green = EventColor(0xFF81C784)
    val Red = EventColor(0xFFE57373)
    val Purple = EventColor(0xFFBA68C8)
  }
}
