package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** Represents the different calendar view modes available in the UI. */
enum class ViewMode {
  ONE_DAY,
  FIVE_DAYS,
  SEVEN_DAYS,
  MONTH,
}

/**
 * A compact floating action button that opens a dropdown menu to select the calendar view mode (1
 * day, 5 days, 7 days, or 1 month).
 *
 * This composable is controlled by the parent through [currentMode]. The icon of the button always
 * reflects the mode currently selected in the parent.
 */
@Composable
fun ViewModeSelector(
    modifier: Modifier = Modifier,
    currentMode: ViewMode = ViewMode.SEVEN_DAYS,
    onModeSelected: (ViewMode) -> Unit = {},
) {
  // Local state only controls whether the menu is open or closed.
  var expanded by remember { mutableStateOf(false) }

  // Icons
  val oneDayIcon = Icons.Filled.CalendarToday
  val multiDayIcon = Icons.Filled.CalendarViewWeek
  val monthIcon = Icons.Filled.CalendarMonth

  // List of options
  val items =
      listOf(
          Triple(ViewMode.ONE_DAY, "1 day", oneDayIcon),
          Triple(ViewMode.FIVE_DAYS, "5 days", multiDayIcon),
          Triple(ViewMode.SEVEN_DAYS, "7 days", multiDayIcon),
          Triple(ViewMode.MONTH, "Month", monthIcon),
      )

  // The icon of the FAB is directly taken from the currently selected mode.
  val iconToShow = items.first { it.first == currentMode }.third

  Box(modifier = modifier.semantics { contentDescription = "Change calendar view mode" }) {
    SmallFloatingActionButton(
        onClick = { expanded = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
      // Smaller icon to keep the overall control compact
      Icon(
          imageVector = iconToShow,
          contentDescription = null,
      )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
      items.forEach { (mode, label, icon) ->
        DropdownMenuItem(
            text = { Text(label) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
            onClick = {
              // Delegate the new mode to the parent. The parent updates currentMode.
              onModeSelected(mode)
              expanded = false
            },
        )
      }
    }
  }
}
