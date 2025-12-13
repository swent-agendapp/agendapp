package com.android.sample.model.category

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.EventPalette
import java.util.UUID

/**
 * Represents a category that can be assigned to an event.
 *
 * @property id Unique identifier for the category. Defaults to a randomly generated UUID string.
 * @property organizationId Identifier for the organization the category belongs to.
 * @property label Human‑readable name of the category.
 * @property color Visual color associated with this category.
 * @property isDefault Indicates whether this is the special default category used when no category
 *   is selected.
 *
 * The user can create custom categories with any label and color. A default category also exists
 * and is used when the user does not explicitly choose one. When `isDefault` is true, the UI layer
 * is expected to display a localized name (using stringResource) instead of the stored label.
 */
data class EventCategory(
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val label: String,
    val color: Color,
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
          organizationId = "default_organization",
          label = "Uncategorized", // Fallback label, UI should localize this.
          color = EventPalette.NoCategory,
          isDefault = true,
      )
    }
  }
}
