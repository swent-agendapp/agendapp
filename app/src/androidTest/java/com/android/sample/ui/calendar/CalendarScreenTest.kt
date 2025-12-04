package com.android.sample.ui.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.MapRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.CalendarDefaults.DEFAULT_SWIPE_THRESHOLD
import com.android.sample.ui.calendar.utils.DateTimeUtils
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

/**
 * Base class exposing shared helpers and setup for CalendarScreen tests.
 *
 * Rationale: Smaller focused subclasses improve maintainability (grouped by concern) while reusing
 * a single set of utilities.
 */
abstract class BaseCalendarScreenTest {

  @get:Rule open val composeTestRule = createComposeRule()

  val selectedOrganizationId = "orgTest"

  /** Converts a (LocalDate, LocalTime) to an Instant in the system zone for concise test setup. */
  protected fun at(date: LocalDate, time: LocalTime) =
      date
          .atTime(time) // Build a LocalDateTime
          .atZone(ZoneId.systemDefault()) // Attach the system zone
          .toInstant() // Convert to Instant

  /** Returns the tag of the vertical scrollable area that hosts the hours grid. */
  protected fun scrollAreaTag() = CalendarScreenTestTags.SCROLL_AREA

  /**
   * Returns true if the node with [tag] intersects the root viewport (no assertions/exceptions).
   */
  protected fun isInViewport(tag: String): Boolean {
    val nodes = composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
    if (nodes.isEmpty()) return false

    val node = nodes.first()
    val root = composeTestRule.onRoot().fetchSemanticsNode()
    val nb = node.boundsInRoot
    val rb = root.boundsInRoot
    val horizontally = nb.right > rb.left && nb.left < rb.right
    val vertically = nb.bottom > rb.top && nb.top < rb.bottom
    return horizontally && vertically
  }

  /**
   * Scrolls vertically in a bounded way until the node with [tag] intersects the root viewport,
   * then performs a single display assertion. This avoids depending on production auto-scroll
   * timing.
   */
  protected fun scrollUntilVisible(
      tag: String,
      maxSwipesPerDirection: Int = 1
  ) { // For now, one swipe is enough to see the whole screen, we can increase it when zooming makes
    // grid larger
    // Fast path: already visible
    if (isInViewport(tag)) {
      composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
      return
    }

    // Sweep towards later hours first (swipe up). Check after each swipe.
    repeat(maxSwipesPerDirection) {
      composeTestRule.onNodeWithTag(scrollAreaTag()).performTouchInput { swipeUp() }
      if (isInViewport(tag)) {
        composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        return
      }
    }

    // Then sweep towards earlier hours (swipe down). Check after each swipe.
    repeat(maxSwipesPerDirection) {
      composeTestRule.onNodeWithTag(scrollAreaTag()).performTouchInput { swipeDown() }
      if (isInViewport(tag)) {
        composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        return
      }
    }

    // Final assertion if the node never entered the viewport.
    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
  }

  /** Makes event-block tag from a human title. */
  protected fun eventBlockTag(title: String) = "${CalendarScreenTestTags.EVENT_BLOCK}_$title"

  /** Helper: assert event block exists and is visible (auto-scrolls if needed). */
  protected fun assertEventVisible(title: String) = scrollUntilVisible(eventBlockTag(title))

  /** Helper: assert event block does not exist in the semantics tree. */
  protected fun assertEventAbsent(title: String) =
      composeTestRule.onNodeWithTag(eventBlockTag(title)).assertDoesNotExist()

  /** Performs a horizontal swipe gesture on the calendar event grid. */
  protected fun swipeEventGrid(deltaX: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          down(center)
          moveBy(Offset(deltaX, 0f))
          up()
        }
  }

  /** Fast swipe: single, large horizontal drag to cross the threshold quickly. */
  protected fun swipeEventGridFast(deltaX: Float) = swipeEventGrid(deltaX)

  /**
   * Slow swipe: multiple small drags that sum to the target delta, to verify cumulative handling.
   */
  protected fun swipeEventGridSlow(totalDeltaX: Float, steps: Int = 8) {
    val step = totalDeltaX / steps
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          down(center)
          repeat(steps) { moveBy(Offset(step, 0f)) }
          up()
        }
  }

  /** Vertical-only gesture: should not trigger horizontal navigation. */
  protected fun swipeEventGridVertical(deltaY: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          down(center)
          moveBy(Offset(0f, deltaY))
          up()
        }
  }

  /** Diagonal gesture with dominant vertical component: should not trigger navigation. */
  protected fun swipeEventGridDiagonal(smallDeltaX: Float, largeDeltaY: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          down(center)
          moveBy(Offset(smallDeltaX, largeDeltaY))
          up()
        }
  }

  /** Convenience helpers for common navigation gestures. */
  protected fun swipeLeft() = swipeEventGrid(-2 * DEFAULT_SWIPE_THRESHOLD)

  protected fun swipeRight() = swipeEventGrid(2 * DEFAULT_SWIPE_THRESHOLD)

  /** Builds a deterministic set of events spanning previous, current, and next weeks. */
  protected fun buildTestEvents(): List<Event> {
    // Pick Monday of this week as the reference anchor
    val thisWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY)

    // Events that belong to the CURRENT visible week
    val current =
        // Current week
        createEvent(
            organizationId = selectedOrganizationId,
            title = "First Event",
            startDate = at(thisWeekMonday.plusDays(1), LocalTime.of(9, 30)), // Tue 09:30–11:30
            endDate = at(thisWeekMonday.plusDays(1), LocalTime.of(9, 30)).plus(Duration.ofHours(2)),
            cloudStorageStatuses = emptySet(),
            participants = setOf("Alice", "Bob"),
        ) +
            createEvent(
                organizationId = selectedOrganizationId,
                title = "Nice Event",
                startDate = at(thisWeekMonday.plusDays(2), LocalTime.of(14, 0)), // Wed 14:00–18:00
                endDate =
                    at(thisWeekMonday.plusDays(2), LocalTime.of(14, 0)).plus(Duration.ofHours(4)),
                cloudStorageStatuses = emptySet(),
                participants = setOf("Charlie", "David"),
            ) +
            createEvent(
                organizationId = selectedOrganizationId,
                title = "Top Event",
                startDate = at(thisWeekMonday.plusDays(3), LocalTime.of(11, 0)), // Thu 11:00–13:00
                endDate =
                    at(thisWeekMonday.plusDays(3), LocalTime.of(11, 0)).plus(Duration.ofHours(2)),
                cloudStorageStatuses = emptySet(),
                participants = setOf("Eve"),
            )

    // Events that belong to the NEXT week (should appear after swipe-left)
    val next =
        // Next week
        createEvent(
            organizationId = selectedOrganizationId,
            title = "Next Event",
            startDate = at(thisWeekMonday.plusWeeks(1), LocalTime.of(10, 0)), // Mon 10:00–13:00
            endDate =
                at(thisWeekMonday.plusWeeks(1), LocalTime.of(10, 0)).plus(Duration.ofHours(3)),
            cloudStorageStatuses = emptySet(),
            participants = setOf("Alice", "Bob"),
        ) +
            createEvent(
                organizationId = selectedOrganizationId,
                title = "Later Event",
                startDate = at(thisWeekMonday.plusWeeks(1).plusDays(3), LocalTime.of(16, 0)), // Thu
                endDate =
                    at(thisWeekMonday.plusWeeks(1).plusDays(3), LocalTime.of(16, 0))
                        .plus(Duration.ofHours(4)),
                cloudStorageStatuses = emptySet(),
                participants = setOf("Charlie", "David"),
            )

    // Events that belong to the PREVIOUS week (should appear after swipe-right)
    val previous =
        // Previous week
        createEvent(
            organizationId = selectedOrganizationId,
            title = "Previous Event",
            startDate = at(thisWeekMonday.minusWeeks(1).plusDays(1), LocalTime.of(17, 0)),
            endDate =
                at(thisWeekMonday.minusWeeks(1).plusDays(1), LocalTime.of(17, 0))
                    .plus(Duration.ofHours(2)),
            cloudStorageStatuses = emptySet(),
            participants = setOf("Alice", "Bob"),
        ) +
            createEvent(
                organizationId = selectedOrganizationId,
                title = "Earlier Event",
                startDate = at(thisWeekMonday.minusWeeks(1).plusDays(4), LocalTime.of(8, 0)),
                endDate =
                    at(thisWeekMonday.minusWeeks(1).plusDays(4), LocalTime.of(8, 0))
                        .plus(Duration.ofHours(4)),
                cloudStorageStatuses = emptySet(),
                participants = setOf("Charlie", "David"),
            )

    // Merge in this order so tests can check visibility by title across ranges
    return previous + current + next
  }

  /**
   * Inserts [events] into the given in-memory local repository. Preload the repo with our test
   * events before composing the screen.
   */
  protected fun populateRepo(
      repo: EventRepositoryLocal,
      events: List<Event>,
      orgId: String = selectedOrganizationId
  ) = runBlocking {
    // Synchronously insert events so data is ready when the UI composes
    events.forEach { repo.insertEvent(orgId = orgId, item = it) }
  }

  /**
   * Minimal factory that builds a `CalendarViewModel` backed by [eventRepo] and [mapRepo]. Create
   * the ViewModel using our test repositories.
   */
  protected class CalendarVMFactory(
      private val eventRepo: EventRepository,
      private val mapRepo: MapRepository
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
          // Provide CalendarViewModel wired to the local repos for tests
          modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
              CalendarViewModel(
                  app = ApplicationProvider.getApplicationContext(),
                  eventRepository = eventRepo,
                  mapRepository = mapRepo)
                  as T
          else -> error("Unknown ViewModel class: $modelClass")
        }
  }

  /**
   * Lightweight `ViewModelStoreOwner` exposing [defaultViewModelProviderFactory] to Compose. Plug
   * our factory so `viewModel()` in CalendarScreen gets the right instance.
   */
  protected class TestOwner(
      override val defaultViewModelProviderFactory: ViewModelProvider.Factory
  ) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    // Hold ViewModels created during the test composition
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore
      get() = store
  }

  /**
   * Composes `CalendarScreen()` with in-memory local repositories pre-populated with [events].
   * Keeps `CalendarScreen` and its `ViewModel` unchanged by providing a custom
   * `ViewModelStoreOwner` exposing a default factory that builds the real `CalendarViewModel` with
   * our local repos. Mount the real screen, but make it read from our test data.
   */
  protected fun setContentWithLocalRepo(events: List<Event> = buildTestEvents()) {
    // Create in-memory repository instances for tests
    val eventRepo = EventRepositoryLocal()
    val mapRepo = MapRepositoryLocal()
    // Preload event repository with our test events
    populateRepo(eventRepo, events)
    // Provide a ViewModel factory that uses these repos
    val owner = TestOwner(CalendarVMFactory(eventRepo, mapRepo))
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)

    val viewModel =
        CalendarViewModel(
            app = ApplicationProvider.getApplicationContext(),
            eventRepository = eventRepo,
            mapRepository = mapRepo)
    composeTestRule.setContent {
      // Compose CalendarScreen, viewModel() will resolve using our TestOwner factory
      CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        CalendarScreen(calendarViewModel = viewModel)
      }
    }
  }

  /** Returns the expected day-of-week short label (e.g., Mon, Tue) for a given date. */
  protected fun dowLabel(date: LocalDate, locale: Locale = Locale.ENGLISH): String =
      date.format(DateTimeFormatter.ofPattern("EEE", locale))
}

/**
 * Sanity/basic composition tests (aka "smoke tests").
 *
 * These are fast, coarse checks that the calendar can compose without crashing and that the
 * essential UI structure is present (root container, top bar, day row, time axis, grid, etc.). They
 * are not concerned with business rules or interactions, the goal is simply to fail fast if the
 * screen cannot mount or if critical tags disappear. This keeps feedback quick and localizes
 * failures when deeper suites (visibility/swipe/header) would otherwise be noisy.
 */
class CalendarSanityTests : BaseCalendarScreenTest() {

  @Test
  fun testTagsAreCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.LOCATION_STATUS_CHIP).assertIsDisplayed()
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
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.LOCATION_STATUS_CHIP)
  }

  @Test
  fun calendarContainerComposesWithoutCrash() {
    val repoEvent = EventRepositoryLocal()
    val repoMap = MapRepositoryLocal()
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
    composeTestRule.setContent {
      CalendarContainer(
          calendarViewModel = viewModel(factory = CalendarVMFactory(repoEvent, repoMap)))
    }
  }

  @Test
  fun calendarGridContentComposesWithoutCrash() {
    composeTestRule.setContent { CalendarGridContent(modifier = Modifier) }
  }

  @Test
  fun calendarContainerComposes() {
    val repoEvent = EventRepositoryLocal()
    val repoMap = MapRepositoryLocal()
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
    composeTestRule.setContent {
      CalendarContainer(
          calendarViewModel = viewModel(factory = CalendarVMFactory(repoEvent, repoMap)))
    }
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
  }
}

/** Initial scroll behavior tests. */
class CalendarInitialScrollTests : BaseCalendarScreenTest() {

  @Test
  fun initialScroll_startsAtDefaultInitialHour() {
    // Purpose: Verify that on first composition, the grid auto-scrolls so that the
    // default initial hour is at the top of the viewport. We assert that
    // the default starting hour is visible while the hour before is not.

    // Mount only the grid content to exercise the scroll logic in isolation.
    composeTestRule.setContent { CalendarGridContent(modifier = Modifier) }

    // Build Instants for (DefaultInitialHour - 1) and DefaultInitialHour on an arbitrary base date,
    // then format them using the same utility as production, ensuring identical labels.
    val baseDate = LocalDate.now()

    val defaultStartingHourMinusOneInstant =
        DateTimeUtils.localDateTimeToInstant(
            baseDate, LocalTime.of(CalendarDefaults.DEFAULT_INITIAL_HOUR - 1, 0))
    val defaultStartingHourMinusOne =
        DateTimeUtils.formatInstantToTime(defaultStartingHourMinusOneInstant)

    val defaultStartingHourInstant =
        DateTimeUtils.localDateTimeToInstant(
            baseDate, LocalTime.of(CalendarDefaults.DEFAULT_INITIAL_HOUR, 0))
    val defaultStartingHour = DateTimeUtils.formatInstantToTime(defaultStartingHourInstant)
    // The default initial hour should be visible at the top after the initial scroll
    composeTestRule.onNodeWithText(defaultStartingHour, substring = false).assertIsDisplayed()

    // The hour before should exists but should not be visible in the viewport
    composeTestRule
        .onNodeWithText(defaultStartingHourMinusOne, substring = false)
        .assertIsNotDisplayed()
  }
}

/** Visibility-focused tests: which events exist/are visible vs absent by week. */
class CalendarVisibilityTests : BaseCalendarScreenTest() {

  @Test
  fun currentWeek_eventsVisible_othersAbsent() {
    setContentWithLocalRepo()

    // Current week visible
    assertEventVisible("First Event")
    assertEventVisible("Nice Event")
    assertEventVisible("Top Event")

    // Other weeks: nodes should not exist in the tree
    assertEventAbsent("Next Event")
    assertEventAbsent("Later Event")
    assertEventAbsent("Previous Event")
    assertEventAbsent("Earlier Event")
  }
}

/** Swipe/navigation-focused tests. */
class CalendarSwipeTests : BaseCalendarScreenTest() {

  @Test
  fun whenSwipeLeft_showsNextWeekWithEvents() {
    setContentWithLocalRepo(buildTestEvents())

    // Baseline
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")
    assertEventAbsent("Later Event")

    // Navigate to next range
    swipeLeft()

    // Next week now
    assertEventVisible("Next Event")
    assertEventVisible("Later Event")

    // Current week events are now filtered out
    assertEventAbsent("First Event")
    assertEventAbsent("Nice Event")
    assertEventAbsent("Top Event")
  }

  @Test
  fun whenSwipeRight_showsPreviousWeekWithEvents() {
    setContentWithLocalRepo()

    // Baseline
    assertEventVisible("First Event")
    assertEventAbsent("Previous Event")
    assertEventAbsent("Earlier Event")

    // Navigate to previous range
    swipeRight()

    // Previous week now
    assertEventVisible("Previous Event")
    assertEventVisible("Earlier Event")

    // Current week events filtered out
    assertEventAbsent("First Event")
    assertEventAbsent("Nice Event")
    assertEventAbsent("Top Event")
  }

  @Test
  fun whenSwipeRightThenLeft_backToCurrentWeek() {
    setContentWithLocalRepo()

    assertEventVisible("First Event")
    assertEventAbsent("Previous Event")
    assertEventAbsent("Next Event")

    swipeRight()
    swipeLeft()

    assertEventVisible("First Event")
    assertEventVisible("Nice Event")
    assertEventVisible("Top Event")

    assertEventAbsent("Previous Event")
    assertEventAbsent("Earlier Event")
    assertEventAbsent("Next Event")
    assertEventAbsent("Later Event")
  }

  @Test
  fun whenSwipeLeftThenRight_backToCurrentWeek() {
    setContentWithLocalRepo()

    assertEventVisible("First Event")
    assertEventAbsent("Previous Event")
    assertEventAbsent("Next Event")

    swipeLeft()
    swipeRight()

    assertEventVisible("First Event")
    assertEventVisible("Nice Event")
    assertEventVisible("Top Event")

    assertEventAbsent("Previous Event")
    assertEventAbsent("Earlier Event")
    assertEventAbsent("Next Event")
    assertEventAbsent("Later Event")
  }

  @Test
  fun whenSwipeJustBelowThreshold_doesNotChangeWeek() {
    setContentWithLocalRepo()

    assertEventVisible("First Event")
    assertEventAbsent("Next Event")

    swipeEventGrid(-(DEFAULT_SWIPE_THRESHOLD - 1f))

    // Still current week
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")
  }

  @Test
  fun whenSwipeExactlyAtThreshold_doesNotChangeWeekUnlessInclusive() {
    setContentWithLocalRepo()

    swipeEventGrid(-DEFAULT_SWIPE_THRESHOLD)

    // Require strictly greater than threshold -> remain on current
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")
  }

  @Test
  fun whenSwipeFastAndSlow_bothTriggerNavigation() {
    setContentWithLocalRepo()

    // Fast swipe left -> next week
    swipeEventGridFast(-2 * DEFAULT_SWIPE_THRESHOLD)
    assertEventVisible("Next Event")

    // Slow cumulative swipe right -> back to current week
    swipeEventGridSlow(2 * DEFAULT_SWIPE_THRESHOLD)
    assertEventVisible("First Event")
  }

  @Test
  fun whenVerticalOrDiagonalSwipe_doesNotNavigate() {
    setContentWithLocalRepo()

    // Baseline
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")

    // Vertical only
    swipeEventGridVertical(3 * DEFAULT_SWIPE_THRESHOLD)

    // Diagonal: small X below threshold, large Y
    swipeEventGridDiagonal(-(DEFAULT_SWIPE_THRESHOLD - 1f), 3 * DEFAULT_SWIPE_THRESHOLD)

    // Still current week
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")
  }
}

/** Header/labels-focused tests. */
class CalendarHeaderTests : BaseCalendarScreenTest() {

  @Test
  fun dayHeaderRow_showsCorrectDays_beforeAndAfterSwipe() {
    setContentWithLocalRepo()

    val monday = LocalDate.now().with(DayOfWeek.MONDAY)
    val expectedLabelsCurrent = (0 until 5).map { dowLabel(monday.plusDays(it.toLong())) }

    expectedLabelsCurrent.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }

    swipeLeft()

    val nextMonday = monday.plusWeeks(1)
    val expectedLabelsNext = (0 until 5).map { dowLabel(nextMonday.plusDays(it.toLong())) }

    expectedLabelsNext.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }

    swipeRight()

    expectedLabelsCurrent.forEach { label ->
      composeTestRule.onNodeWithText(label, substring = true).assertIsDisplayed()
    }
  }
}
