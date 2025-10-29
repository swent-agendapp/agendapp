package com.android.sample.ui.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.android.sample.Agendapp
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.createEvent
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultSwipeThreshold
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  /** Converts a (LocalDate, LocalTime) to an Instant in the system zone for concise test setup. */
  private fun at(date: LocalDate, time: LocalTime) =
      // Build a LocalDateTime, attach the system zone, then convert to Instant
      date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()

  /**
   * Builds a deterministic set of events spanning previous, current, and next weeks.
   * Used to verify that CalendarScreen filters by date range and reacts to swipe gestures.
   */
  private fun buildTestEvents(): List<Event> {
      // Pick Monday of this week as the reference anchor
      val thisWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY)

      // Events that belong to the CURRENT visible week
      val current = listOf(
          // Current week
          createEvent(
              title = "First Event",
              startDate = at(thisWeekMonday.plusDays(1), LocalTime.of(9, 30)), // Tue 09:30–11:30
              endDate = at(thisWeekMonday.plusDays(1), LocalTime.of(9, 30)).plus(Duration.ofHours(2)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
          createEvent(
              title = "Nice Event",
              startDate = at(thisWeekMonday.plusDays(2), LocalTime.of(14, 0)), // Wed 14:00–18:00
              endDate = at(thisWeekMonday.plusDays(2), LocalTime.of(14, 0)).plus(Duration.ofHours(4)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
          createEvent(
              title = "Top Event",
              startDate = at(thisWeekMonday.plusDays(3), LocalTime.of(11, 0)), // Thu 11:00–13:00
              endDate = at(thisWeekMonday.plusDays(3), LocalTime.of(11, 0)).plus(Duration.ofHours(2)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
      )

      // Events that belong to the NEXT week (should appear after swipe-left)
      val next = listOf(
          // Next week
          createEvent(
              title = "Next Event",
              startDate = at(thisWeekMonday.plusWeeks(1), LocalTime.of(10, 0)), // Mon 10:00–13:00
              endDate = at(thisWeekMonday.plusWeeks(1), LocalTime.of(10, 0)).plus(Duration.ofHours(3)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
          createEvent(
              title = "Later Event",
              startDate = at(thisWeekMonday.plusWeeks(1).plusDays(3), LocalTime.of(16, 0)), // Thu
              endDate = at(thisWeekMonday.plusWeeks(1).plusDays(3), LocalTime.of(16, 0)).plus(Duration.ofHours(4)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
      )

      // Events that belong to the PREVIOUS week (should appear after swipe-right)
      val previous = listOf(
          // Previous week
          createEvent(
              title = "Previous Event",
              startDate = at(thisWeekMonday.minusWeeks(1).plusDays(1), LocalTime.of(17, 0)), // Tue
              endDate = at(thisWeekMonday.minusWeeks(1).plusDays(1), LocalTime.of(17, 0)).plus(Duration.ofHours(2)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
          createEvent(
              title = "Earlier Event",
              startDate = at(thisWeekMonday.minusWeeks(1).plusDays(4), LocalTime.of(8, 0)), // Fri 8–12
              endDate = at(thisWeekMonday.minusWeeks(1).plusDays(4), LocalTime.of(8, 0)).plus(Duration.ofHours(4)),
              cloudStorageStatuses = emptySet(),
              participants = emptySet(),
          ),
      )

      // Merge in this order so tests can check visibility by title across ranges
      return previous + current + next
  }

  /** Inserts [events] into the given in-memory local repository.
   *  Preload the repo with our test events before composing the screen. */
  private fun populateRepo(repo: EventRepositoryLocal, events: List<Event>) = runBlocking {
    // Synchronously insert events so data is ready when the UI composes
    events.forEach { repo.insertEvent(it) }
  }

  /** Minimal factory that builds a `CalendarViewModel` backed by [repo].
   *  Create the ViewModel using our test repository. */
  private class CalendarVMFactory(private val repo: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
      // Provide CalendarViewModel wired to the local repo for tests
      modelClass.isAssignableFrom(CalendarViewModel::class.java) -> CalendarViewModel(repo) as T
      else -> error("Unknown ViewModel class: $modelClass")
    }
  }

  /** Lightweight `ViewModelStoreOwner` exposing [defaultViewModelProviderFactory] to Compose.
   *  Plug our factory so `viewModel()` in CalendarScreen gets the right instance. */
  private class TestOwner(override val defaultViewModelProviderFactory: ViewModelProvider.Factory) :
      ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    // Hold ViewModels created during the test composition
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
  }

  /**
   * Composes `CalendarScreen()` with an in-memory local repository pre-populated with [events].
   * Keeps `CalendarScreen` and its `ViewModel` unchanged by providing a custom `ViewModelStoreOwner`
   * exposing a default factory that builds the real `CalendarViewModel` with our local repo.
   * Munt the real screen, but make it read from our test data. */
  private fun setContentWithLocalRepo(events: List<Event> = buildTestEvents()) {
    // Create an in-memory repository instance for tests
    val repo = EventRepositoryLocal()
    // Preload repository with our test events
    populateRepo(repo, events)
    // Provide a ViewModel factory that uses this repo
    val owner = TestOwner(CalendarVMFactory(repo))
    composeTestRule.setContent {
      // Compose CalendarScreen, viewModel() will resolve using our TestOwner factory
      CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        CalendarScreen()
      }
    }
  }

  /**
   * Performs a horizontal swipe gesture on the calendar event grid.
   *
   * @param deltaX The horizontal drag in pixels. Use a **negative** value to swipe **left**
   * (navigate to next range) and a **positive** value to swipe **right** (previous range).
   * In tests we typically pass `±2 * DefaultSwipeThreshold` to guarantee crossing the threshold. */
  private fun swipeEventGrid(deltaX: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          // Press, drag horizontally by deltaX, then release
          down(center)
          moveBy(Offset(deltaX, 0f))
          up()
        }
  }

  /** Fast swipe: single, large horizontal drag to cross the threshold quickly. */
  private fun swipeEventGridFast(deltaX: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          // Press, drag once by deltaX, then release
          down(center)
          moveBy(Offset(deltaX, 0f))
          up()
        }
  }

  /** Slow swipe: multiple small drags that sum to the target delta, to verify cumulative handling. */
  private fun swipeEventGridSlow(totalDeltaX: Float, steps: Int = 8) {
    // Break the total distance into small steps to simulate a slow gesture
    val step = totalDeltaX / steps
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          // Press, drag in small increments, then release
          down(center)
          repeat(steps) { moveBy(Offset(step, 0f)) }
          up()
        }
  }

  /** Vertical-only gesture: should not trigger horizontal navigation. */
  private fun swipeEventGridVertical(deltaY: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          // Press, drag vertically by deltaY, then release
          down(center)
          moveBy(Offset(0f, deltaY))
          up()
        }
  }

  /** Diagonal gesture with dominant vertical component: should not trigger navigation. */
  private fun swipeEventGridDiagonal(smallDeltaX: Float, largeDeltaY: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          // Press, drag diagonally (small X, large Y), then release
          down(center)
          moveBy(Offset(smallDeltaX, largeDeltaY))
          up()
        }
  }

  @Test
  fun testTagsAreCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TIME_AXIS_COLUMN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.DAY_ROW).assertIsDisplayed()
  }

  @Test
  fun topTitleIsCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCROLL_AREA).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertTextEquals("Calendar")
  }

  @Test
  fun agendappCorrectlySet() {
    composeTestRule.setContent { Agendapp() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertTextEquals("Calendar")
  }

  @Test
  fun calendarContainerComposesWithoutCrash() {
    composeTestRule.setContent { CalendarContainer() }
  }

  @Test
  fun calendarGridContentComposesWithoutCrash() {
    composeTestRule.setContent { CalendarGridContent(modifier = Modifier) }
  }

  @Test
  fun calendarContainerComposes() {
    composeTestRule.setContent { CalendarContainer() }
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeLeft_showsNextWeekWithEvents() {
    // Arrange & Act: compose CalendarScreen with a local in-memory repo populated with events
    setContentWithLocalRepo(buildTestEvents())

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // next week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeRight_showsPreviousWeekWithEvents() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // previous week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeRightThenLeft_showsCurrentWeekWithEvents() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeLeftThenRight_showsCurrentWeekWithEvents() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()
  }

  /** Returns the expected day-of-week short label (e.g., Mon, Tue) for a given date. */
  private fun dowLabel(date: LocalDate, locale: Locale = Locale.ENGLISH): String =
      date.format(DateTimeFormatter.ofPattern("EEE", locale))


  @Test
  fun calendarGridContent_whenSwipeJustBelowThreshold_doesNotChangeWeek() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // current week, should be displayed
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
    // next week, should not be displayed
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsNotDisplayed()

    // swipe left just below the threshold => should NOT trigger onSwipeLeft
    swipeEventGrid(-(DefaultSwipeThreshold - 1f))

    // Still current week
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeExactlyAtThreshold_doesNotChangeWeekUnlessInclusive() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // swipe left exactly at the threshold
    swipeEventGrid(-DefaultSwipeThreshold)

    // Require a swipe gesture strictly greater than threshold => remain on current week
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeFastAndSlow_bothTriggerNavigation() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // Fast swipe left should navigate to next week
    swipeEventGridFast(-2 * DefaultSwipeThreshold)
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsDisplayed()

    // Swipe back to current week using a slow cumulative swipe to the right
    swipeEventGridSlow(2 * DefaultSwipeThreshold)
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_whenVerticalOrDiagonalSwipe_doesNotNavigate() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    // Baseline: current week visible, next week not visible
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsNotDisplayed()

    // Vertical gesture only => must not change week
    swipeEventGridVertical(3 * DefaultSwipeThreshold)

    // Diagonal with small X (below threshold) and large Y => must not change week
    swipeEventGridDiagonal(-(DefaultSwipeThreshold - 1f), 3 * DefaultSwipeThreshold)

    // Still current week
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event").assertIsNotDisplayed()
  }

  @Test
  fun dayHeaderRow_showsCorrectDays_beforeAndAfterSwipe() {
    // Arrange & Act: CalendarScreen with local repo test data
    setContentWithLocalRepo()

    val monday = LocalDate.now().with(DayOfWeek.MONDAY)
    val expectedLabelsCurrent = (0 until 5).map { dowLabel(monday.plusDays(it.toLong())) }

    // Header labels should be visible for the current week (Mon ... Fri)
    expectedLabelsCurrent.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }

    // Swipe to next week
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    val nextMonday = monday.plusWeeks(1)
    val expectedLabelsNext = (0 until 5).map { dowLabel(nextMonday.plusDays(it.toLong())) }

    // Header labels should update to the next week
    expectedLabelsNext.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }

    // Swipe back to previous (current) week
    swipeEventGrid(2 * DefaultSwipeThreshold)

    expectedLabelsCurrent.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }
  }
}
