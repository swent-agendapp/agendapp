package com.android.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.swipeUp
import com.android.sample.ui.calendar.CalendarGridContent
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.utils.workWeekDays
import org.junit.Rule
import org.junit.Test

class CalendarGridCanvasTest {
  @get:Rule val compose = createComposeRule()

  @Test
  fun gridCanvasWithWorkWeekDays() {
    val days = workWeekDays()

    compose.setContent {
      Box(Modifier.size(300.dp)) {
        GridCanvas(modifier = Modifier.fillMaxSize(), columnCount = days.size, days = days)
      }
    }
    compose.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertExists().assertIsDisplayed()
  }

  @Test
  fun calendarGridContentShows() {
    compose.setContent { CalendarGridContent() }
    compose.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_isScrollable() {
    compose.setContent { CalendarGridContent() }

    val scrollables = compose.onAllNodes(hasScrollAction())
    if (scrollables.fetchSemanticsNodes().isNotEmpty()) {
      scrollables.onFirst().performTouchInput { swipeUp() }
    }
  }
}
