import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationScreen
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import java.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventConfirmationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventConfirmationScreen() }
  }

  @Test
  fun displayAllComponents() {
    // Main parts of the summary card are visible
    composeTestRule.onNodeWithTag(EventSummaryCardTags.SIDE_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.DATE_LINE1).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventSummaryCardTags.DATE_LINE2).assertIsDisplayed()

    // The first date line contains today's day of month (date is “today”)
    val today = LocalDate.now()
    composeTestRule
        .onNodeWithTag(EventSummaryCardTags.DATE_LINE1)
        .assertTextContains(today.dayOfMonth.toString(), substring = true)

    // Go back and Create button are visible
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun finishButtonIsEnabled() {
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertIsEnabled()
  }
}
