package com.android.sample.ui.replacement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.Red80
import com.android.sample.ui.theme.SpacingMedium

// Assisted by AI

/** Contains the test tags used across the replacement screen UI. */
object ReplacementOverviewTestTags {
  const val SCREEN = "replacement_screen"
  const val CARD_LIST = "replacement_card_list"

  // Individual cards
  const val CARD_ORGANIZE = "card_organize_replacement"
  const val CARD_PROCESS = "card_replacement_to_process"
  const val CARD_WAITING = "card_replacement_waiting_answers"
  const val CARD_CONFIRMED = "card_replacement_confirmed"
}

/**
 * Represents a single replacement item in the UI.
 *
 * @property title The text displayed on the card.
 * @property icon The icon representing the card.
 * @property tag The test tag used for UI testing.
 * @property onClick The action triggered when the card is clicked.
 */
data class ReplacementItem(
    val title: String,
    val icon: ImageVector,
    val tag: String,
    val onClick: () -> Unit = {}
)

/**
 * Displays the main replacement screen with multiple replacement-related options.
 *
 * Each option is represented as a clickable card that navigates to a specific replacement flow
 * (organize, process, waiting confirmation, or confirmed).
 *
 * @param onOrganizeClick Callback when the "Organize Replacement" card is clicked.
 * @param onProcessClick Callback when the "Process Replacement" card is clicked.
 * @param onWaitingConfirmationClick Callback when the "Waiting Confirmation" card is clicked.
 * @param onConfirmedClick Callback when the "Confirmed Replacements" card is clicked.
 */
@Composable
fun ReplacementOverviewScreen(
    onOrganizeClick: () -> Unit = {},
    onProcessClick: () -> Unit = {},
    onWaitingConfirmationClick: () -> Unit = {},
    onConfirmedClick: () -> Unit = {}
) {
  val items =
      listOf(
          ReplacementItem(
              stringResource(R.string.organize_replacement),
              Icons.Default.GroupAdd,
              ReplacementOverviewTestTags.CARD_ORGANIZE,
              onClick = onOrganizeClick),
          ReplacementItem(
              stringResource(R.string.process_replacement),
              Icons.Default.Work,
              ReplacementOverviewTestTags.CARD_PROCESS,
              onClick = onProcessClick),
          ReplacementItem(
              stringResource(R.string.waiting_confirmation_replacement),
              Icons.Default.QuestionAnswer,
              ReplacementOverviewTestTags.CARD_WAITING,
              onClick = onWaitingConfirmationClick),
          ReplacementItem(
              stringResource(R.string.confirmed_replacements),
              Icons.Default.CheckCircle,
              ReplacementOverviewTestTags.CARD_CONFIRMED,
              onClick = onConfirmedClick))

  Scaffold(
      topBar = {
        MainPageTopBar(
          title = stringResource(R.string.replacement),
        )
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(PaddingMedium)
                    .testTag(ReplacementOverviewTestTags.SCREEN)) {
              Column(modifier = Modifier.testTag(ReplacementOverviewTestTags.CARD_LIST)) {
                items.forEach { item ->
                  ReplacementCard(item, onClick = item.onClick)
                  Spacer(modifier = Modifier.height(SpacingMedium))
                }
              }
            }
      })
}

/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 *
 * @param item The [ReplacementItem] containing display data.
 * @param onClick The callback executed when the card is clicked.
 */
@Composable
fun ReplacementCard(item: ReplacementItem, onClick: () -> Unit = {}) {
  Card(
      shape = RoundedCornerShape(CornerRadiusLarge),
      modifier = Modifier.fillMaxWidth().testTag(item.tag),
      onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = item.icon, contentDescription = null, tint = Red80)
                Spacer(modifier = Modifier.width(SpacingMedium))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
              }
            }
      }
}

/** Preview of the [ReplacementOverviewScreen] for design inspection in Android Studio. */
@Preview(showBackground = true)
@Composable
fun ReplacementScreenPreview() {
  ReplacementOverviewScreen()
}
