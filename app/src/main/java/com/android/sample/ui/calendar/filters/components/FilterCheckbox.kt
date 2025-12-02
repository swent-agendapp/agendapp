package com.android.sample.ui.calendar.filters.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object FilterCheckboxTestTags {
  const val PREFIX = "FilterCheckbox_"
}

/**
 * A reusable checkbox row used in filter screens (Event Type, Location, Participants).
 *
 * This component shows a label on the left and a checkbox on the right. The checkbox state is fully
 * controlled by the caller through [isChecked] and [onCheckedChange].
 *
 * ### Semantics & Testing
 * - The entire row receives a test tag formatted as: `"FilterCheckbox_<key>"`
 * - The checkbox receives a test tag formatted as: `"FilterCheckbox_<key>_Box"`
 *
 * This makes the component easily discoverable in UI tests.
 *
 * @param label The text displayed to the user next to the checkbox.
 * @param isChecked Whether the checkbox is currently checked (controlled state).
 * @param onCheckedChange Callback triggered when the checkbox is toggled.
 * @param key A unique identifier for this filter item (used for test tags).
 */
@Composable
fun FilterCheckbox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    key: String
) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag(FilterCheckboxTestTags.PREFIX + key),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(FilterCheckboxTestTags.PREFIX + key + "_Box"))
      }
}
