package com.android.sample.ui.replacement

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.common.ButtonItem
import com.android.sample.ui.common.MainPageButton
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.theme.PaddingMedium
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
 * Displays the main replacement screen with multiple replacement-related options.
 *
 * Each option is represented as a clickable card that navigates to a specific replacement flow
 * (organize, process, waiting confirmation, or confirmed).
 *
 * @param onOrganizeClick Callback when the "Organize Replacement" card is clicked.
 * @param onWaitingConfirmationClick Callback when the "Waiting Confirmation" card is clicked.
 * @param onConfirmedClick Callback when the "Confirmed Replacements" card is clicked.
 */
@Composable
fun ReplacementOverviewScreen(
    onOrganizeClick: () -> Unit = {},
    onWaitingConfirmationClick: () -> Unit = {},
    onConfirmedClick: () -> Unit = {}
) {
  val items =
      listOf(
          ButtonItem(
              stringResource(R.string.organize_replacement),
              Icons.Default.GroupAdd,
              ReplacementOverviewTestTags.CARD_ORGANIZE,
              onClick = onOrganizeClick),
          ButtonItem(
              stringResource(R.string.waiting_confirmation_replacement),
              Icons.Default.QuestionAnswer,
              ReplacementOverviewTestTags.CARD_WAITING,
              onClick = onWaitingConfirmationClick),
          ButtonItem(
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
                  MainPageButton(item, onClick = item.onClick)
                  Spacer(modifier = Modifier.height(SpacingMedium))
                }
              }
            }
      })
}

/** Preview of the [ReplacementOverviewScreen] for design inspection in Android Studio. */
@Preview(showBackground = true)
@Composable
fun ReplacementScreenPreview() {
  ReplacementOverviewScreen()
}
