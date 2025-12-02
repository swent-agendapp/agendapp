package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.toProcessReplacements
import com.android.sample.model.replacement.waitingForAnswerAndDeclinedReplacements
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// assisted by AI
@RunWith(AndroidJUnit4::class)
class ReplacementPendingListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var replacementsToProcess: List<Replacement>
  private lateinit var waitingAndDeclined: List<Replacement>

  @Before
  fun setUp() {
    val allReplacements = getMockReplacements()
    replacementsToProcess = allReplacements.toProcessReplacements()
    waitingAndDeclined = allReplacements.waitingForAnswerAndDeclinedReplacements()

    composeTestRule.setContent {
      ReplacementPendingListScreen(
          replacementsToProcess = replacementsToProcess,
          replacementsWaitingForAnswer = waitingAndDeclined)
    }
  }

  @Test
  fun pendingListScreen_displaysScreenAndList() {
    composeTestRule
        .onNodeWithTag(ReplacementPendingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementPendingTestTags.LIST, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun pendingListScreen_displaysOneCardPerReplacementToProcess() {
    replacementsToProcess.forEach { replacement ->
      composeTestRule
          .onNodeWithTag(ReplacementPendingTestTags.itemTag(replacement.id), useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun waitingSection_displaysPendingAndDeclinedCounts() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val pendingCount =
        waitingAndDeclined.count {
          it.status == com.android.sample.model.replacement.ReplacementStatus.WaitingForAnswer
        }
    val declinedCount =
        waitingAndDeclined.count {
          it.status == com.android.sample.model.replacement.ReplacementStatus.Declined
        }

    val noResponseText = context.getString(R.string.replacement_no_response_label, pendingCount)
    val declinedText = context.getString(R.string.replacement_declined_label, declinedCount)

    composeTestRule.onNodeWithText(noResponseText).assertIsDisplayed()
    composeTestRule.onNodeWithText(declinedText).assertIsDisplayed()
  }

  @Test
  fun clickingChips_opensDialogsWithCorrectPeople() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    val pendingPeople =
        waitingAndDeclined
            .filter {
              it.status == com.android.sample.model.replacement.ReplacementStatus.WaitingForAnswer
            }
            .map { it.substituteUserId }
            .distinct()

    val declinedPeople =
        waitingAndDeclined
            .filter { it.status == com.android.sample.model.replacement.ReplacementStatus.Declined }
            .map { it.substituteUserId }
            .distinct()

    val noResponseText =
        context.getString(R.string.replacement_no_response_label, pendingPeople.size)
    val declinedText = context.getString(R.string.replacement_declined_label, declinedPeople.size)
    val closeText = context.getString(R.string.replacement_people_dialog_close)

    composeTestRule.onNodeWithText(noResponseText).performClick()

    pendingPeople.forEach { person -> composeTestRule.onNodeWithText(person).assertExists() }

    composeTestRule.onNodeWithText(closeText).performClick()

    composeTestRule.onNodeWithText(declinedText).performClick()

    declinedPeople.forEach { person -> composeTestRule.onNodeWithText(person).assertExists() }
  }
}
