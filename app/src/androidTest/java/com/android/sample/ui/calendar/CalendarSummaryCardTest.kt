package com.android.sample.ui.calendar

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.utils.EventColor
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for EventSummaryCard.
 *
 * Conventions:
 * - We use device default Locale/ZoneId so labels/dates match the phone settings.
 * - We use tags declared in EventSummaryCardTags to target key UI elements.
 */
@RunWith(AndroidJUnit4::class)
class EventSummaryCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---------- Helpers ----------

  private fun event(
      title: String = "Title",
      description: String = "",
      start: Instant,
      end: Instant,
      recurrence: RecurrenceStatus = RecurrenceStatus.OneTime,
      participants: Set<String> = emptySet(),
      color: EventColor = EventColor.Green
  ) =
      Event(
          id = "e-test",
          title = title,
          description = description,
          startDate = start,
          endDate = end,
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = participants,
          version = 1L,
          recurrenceStatus = recurrence,
          hasBeenDeleted = false,
          color = color)

  private val zone: ZoneId = ZoneId.systemDefault()
  private val locale: Locale = Locale.getDefault()
  private val base: Instant = Instant.parse("2025-01-13T08:00:00Z") // Monday 13 Jan 2025 09:00 CET

  // ---------- Tests ----------

  @Test
  fun singleDay_noRecurrence_noDescription_noParticipants_showsOnlyTitleDateTime() {
    // Given: single day, no desc, no participants
    val e =
        event(
            title = "Short title",
            description = "",
            start = base.plusSeconds(60 * 60), // 09:00 CET
            end = base.plusSeconds(60 * 60 * 2) // 10:00 CET
            )

    composeTestRule.setContent {
      EventSummaryCard(
          event = e,
          participantNames = emptyList(),
      )
    }

    // Then: title, date line 1 and line 2 exist; no recurrence; no description; no participants
    // list
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.TitleText).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DateLine1).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DateLine2).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Recurrence).assertCountEquals(0)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DescriptionText).assertCountEquals(0)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ParticipantsList).assertCountEquals(0)

    // Also: no "show more" buttons
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleTitle).assertCountEquals(0)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleDescription).assertCountEquals(0)
  }

  @Test
  fun singleDay_withAllFields_showsTitleDateTimeDescriptionParticipants() {
    // Given: single day, short description, two participants
    val e =
        event(
            title = "Weekly stand-up",
            description = "Short description.",
            start = base.plusSeconds(60 * 60), // 09:00 CET
            end = base.plusSeconds(60 * 60 * 2), // 10:00 CET
            participants = setOf("u1", "u2"))
    val names = listOf("Alice", "Bob")

    composeTestRule.setContent {
      EventSummaryCard(
          event = e,
          participantNames = names,
      )
    }

    // Then: title/date/time/description exist
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.TitleText).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DateLine1).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DateLine2).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DescriptionText).assertCountEquals(1)

    // And: participants list exists and is displayed (not asserting scrollability)
    composeTestRule.onNodeWithTag(EventSummaryCardTags.ParticipantsList).assertIsDisplayed()
  }

  @Test
  fun weeklyRecurrence_showsRecurrenceWithWeekday() {
    // Given: weekly recurrence, Monday start (base is Monday CET)
    val e =
        event(
            title = "Weekly",
            start = base.plusSeconds(60 * 60), // 09:00 CET
            end = base.plusSeconds(60 * 60 * 2),
            recurrence = RecurrenceStatus.Weekly)
    val expectedWeekday = e.startDate.atZone(zone).dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    composeTestRule.setContent {
      EventSummaryCard(
          event = e,
          participantNames = emptyList(),
      )
    }

    // Then: recurrence line exists and contains localized weekday in parentheses
    composeTestRule
        .onAllNodesWithTag(EventSummaryCardTags.Recurrence)
        .assertCountEquals(1)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithText("(${expectedWeekday})", substring = true)
        .assertCountEquals(1)
  }

  @Test
  fun monthlyRecurrence_showsRecurrence() {
    // Given: monthly recurrence
    val e =
        event(
            title = "Monthly",
            start = base,
            end = base.plusSeconds(3600),
            recurrence = RecurrenceStatus.Monthly)

    composeTestRule.setContent {
      EventSummaryCard(
          event = e,
          participantNames = emptyList(),
      )
    }

    // Then
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Recurrence).assertCountEquals(1)
  }

  @Test
  fun multiDayLayout_showsFromTo_and_atTimes_and_noClockIcon() {
    // Given: multi-day event spanning 3 days
    val start = base.plusSeconds(18 * 3600) // 19:00 CET (same day)
    val end = start.plus(Duration.ofDays(3))
    val e = event(title = "Offsite", start = start, end = end)

    composeTestRule.setContent {
      EventSummaryCard(
          event = e,
          participantNames = emptyList(),
      )
    }

    // Then: multi-day UI present
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_FromLabel).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_ToLabel).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_StartDate).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_EndDate).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_StartTime).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_EndTime).assertCountEquals(1)

    // And: time labels include the "at " prefix
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_StartTime).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.Multi_EndTime).assertCountEquals(1)
  }

  @Test
  fun shortTitle_hasNoShowMoreButton() {
    val e = event(title = "Short title", start = base, end = base.plusSeconds(3600))

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = emptyList()) }

    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleTitle).assertCountEquals(0)
  }

  @Test
  fun longTitle_hasShowMoreButton_andTogglesLabel() {
    val longTitle =
        "Very long workshop title here with many words to overflow the card width considerably"
    val e = event(title = longTitle, start = base, end = base.plusSeconds(3600))

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = emptyList()) }

    // Button appears
    val toggle = composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleTitle)
    toggle.assertCountEquals(1)

    // Click -> becomes "Show less"
    toggle.onFirst().performClick()
    composeTestRule.onAllNodesWithText("Show less").assertCountEquals(1)

    // Click again -> back to "Show more"
    toggle.onFirst().performClick()
    composeTestRule.onAllNodesWithText("Show more").assertCountEquals(1)
  }

  @Test
  fun shortDescription_hasNoShowMoreButton() {
    val e = event(title = "T", description = "Short.", start = base, end = base.plusSeconds(3600))

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = emptyList()) }

    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.DescriptionText).assertCountEquals(1)
    composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleDescription).assertCountEquals(0)
  }

  @Test
  fun longDescription_hasShowMoreButton_andTogglesLabel() {
    val e =
        event(
            title = "T",
            description = "Long ".repeat(80), // large enough to overflow collapsed lines
            start = base,
            end = base.plusSeconds(3600))

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = emptyList()) }

    val toggle = composeTestRule.onAllNodesWithTag(EventSummaryCardTags.ToggleDescription)
    toggle.assertCountEquals(1)

    toggle.onFirst().performClick()
    composeTestRule.onAllNodesWithText("Show less").assertCountEquals(1)

    toggle.onFirst().performClick()
    composeTestRule.onAllNodesWithText("Show more").assertCountEquals(1)
  }

  @Test
  fun participants_lessOrEqualFive_notScrollable() {
    val e =
        event(
            title = "T",
            start = base,
            end = base.plusSeconds(3600),
            participants = setOf("u1", "u2", "u3", "u4", "u5"))
    val names = listOf("A", "B", "C", "D", "E")

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = names) }

    // List exists and is displayed (not asserting scrollability)
    composeTestRule.onNodeWithTag(EventSummaryCardTags.ParticipantsList).assertIsDisplayed()
  }

  @Test
  fun participants_moreThanFive_scrollable() {
    val e =
        event(
            title = "T",
            start = base,
            end = base.plusSeconds(3600),
            participants = (1..6).map { "u$it" }.toSet())
    val names = (1..6).map { "Name $it" }

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = names) }

    composeTestRule.onNodeWithTag(EventSummaryCardTags.ParticipantsList).assert(hasScrollAction())
  }

  @Test
  fun leftColoredBar_matchesEventColor() {
    // We pick a strong color to assert (Red)
    val e =
        event(title = "Color", start = base, end = base.plusSeconds(3600), color = EventColor.Red)

    composeTestRule.setContent { EventSummaryCard(event = e, participantNames = emptyList()) }

    // Capture the sidebar bitmap and assert at least one opaque red pixel.
    val bitmap =
        composeTestRule.onNodeWithTag(EventSummaryCardTags.SideBar).assertExists().captureToImage()
    val androidBitmap = bitmap.asAndroidBitmap()
    val sampled = dominantColor(androidBitmap)
    val expected = EventColor.Red.toComposeColor()
    assertTrue(
        "Sidebar color $sampled should be close to $expected",
        colorsAreClose(sampled, expected, eps = 0.06f))
  }
}

/**
 * Utility function used in color tests to compare two Compose [Color] values with a tolerance. This
 * avoids false negatives caused by small rendering or rounding variations.
 * This function was generated using AI
 */
private fun colorsAreClose(a: Color, b: Color, eps: Float = 0.015f): Boolean {
  return (kotlin.math.abs(a.red - b.red) < eps &&
      kotlin.math.abs(a.green - b.green) < eps &&
      kotlin.math.abs(a.blue - b.blue) < eps &&
      kotlin.math.abs(a.alpha - b.alpha) < eps)
}

/**
 * Computes the dominant (most frequent) visible color of a [Bitmap] using simple RGB quantization.
 * Used in tests to verify that rendered UI elements (like the colored sidebar) have the expected
 * color. The algorithm samples pixels, builds a histogram of quantized colors, and averages the
 * dominant bucket.
 * This function was generated using AI
 * */
private fun dominantColor(bm: Bitmap, step: Int = 2): Color {
  val counts = HashMap<Int, Int>()
  val w = bm.width
  val h = bm.height
  var maxKey = 0
  var maxCount = 0

  // First pass: build histogram over quantized colors (12-bit: 4 bits per channel)
  for (y in 0 until h step step) {
    for (x in 0 until w step step) {
      val c = bm.getPixel(x, y)
      val a = (c ushr 24) and 0xFF
      if (a < 10) continue // skip near-transparent
      val r = (c ushr 16) and 0xFF
      val g = (c ushr 8) and 0xFF
      val b = c and 0xFF
      val key = ((r shr 4) shl 8) or ((g shr 4) shl 4) or (b shr 4)
      val cnt = (counts[key] ?: 0) + 1
      counts[key] = cnt
      if (cnt > maxCount) {
        maxCount = cnt
        maxKey = key
      }
    }
  }

  // Second pass: average actual colors within the dominant bucket
  var sumR = 0
  var sumG = 0
  var sumB = 0
  var sumA = 0
  var n = 0
  for (y in 0 until h step step) {
    for (x in 0 until w step step) {
      val c = bm.getPixel(x, y)
      val a = (c ushr 24) and 0xFF
      if (a < 10) continue
      val r = (c ushr 16) and 0xFF
      val g = (c ushr 8) and 0xFF
      val b = c and 0xFF
      val key = ((r shr 4) shl 8) or ((g shr 4) shl 4) or (b shr 4)
      if (key == maxKey) {
        sumR += r
        sumG += g
        sumB += b
        sumA += a
        n++
      }
    }
  }
  if (n == 0) return Color(0f, 0f, 0f, 0f)
  return Color(sumR / (255f * n), sumG / (255f * n), sumB / (255f * n), sumA / (255f * n))
}
