package com.android.sample.model.category

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.EventPalette

/**
 * Represents a category for an event.
 *
 * A category has:
 * - a label (String)
 * - a color (Color)
 *
 * The user can create custom categories with any label and color. There is also a special default
 * category used when no category is selected.
 *
 * The label of the default category can be localized in the UI layer. For this reason, we store a
 * simple String here and add a flag isDefault.
 */
data class EventCategory(
    /** Label of the category. For example "Work" or "Personal". */
    val label: String,

    /** Color used to visually represent this category. */
    val color: Color,

    /**
     * True if this category is the special default category.
     *
     * When this flag is true, the UI should normally ignore the label and display a localized
     * string instead (using stringResource).
     */
    val isDefault: Boolean = false,
) {

  companion object {
    /**
     * Returns the default category.
     *
     * This category is used when the user does not select any category. The label stored here is
     * only a fallback for logs or debugging. The UI is expected to show a localized label instead.
     */
    fun defaultCategory(): EventCategory {
      return EventCategory(
          label = "Uncategorized", // Fallback label, UI should localize this.
          color = EventPalette.NoCategory,
          isDefault = true,
      )
    }
  }
}
