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
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.Red80
import com.android.sample.ui.theme.SpacingMedium

// Assisted by AI

object ReplacementTestTags {
  const val SCREEN = "replacement_screen"
  const val CARD_LIST = "replacement_card_list"

  // Individual cards
  const val CARD_ORGANIZE = "card_organize_replacement"
  const val CARD_PROCESS = "card_replacement_to_process"
  const val CARD_WAITING = "card_replacement_waiting_answers"
  const val CARD_CONFIRMED = "card_replacement_confirmed"
}

data class ReplacementItem(
    val title: String,
    val icon: ImageVector,
    val tag: String,
    val onClick: () -> Unit = {}
)

@Composable
fun ReplacementScreen(
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
              ReplacementTestTags.CARD_ORGANIZE,
              onClick = onOrganizeClick),
          ReplacementItem(
              stringResource(R.string.process_replacement),
              Icons.Default.Work,
              ReplacementTestTags.CARD_PROCESS,
              onClick = onProcessClick),
          ReplacementItem(
              stringResource(R.string.waiting_confirmation_replacement),
              Icons.Default.QuestionAnswer,
              ReplacementTestTags.CARD_WAITING,
              onClick = onWaitingConfirmationClick),
          ReplacementItem(
              stringResource(R.string.confirmed_replacements),
              Icons.Default.CheckCircle,
              ReplacementTestTags.CARD_CONFIRMED,
              onClick = onConfirmedClick))

  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.replacement)) },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(PaddingMedium)
                    .testTag(ReplacementTestTags.SCREEN)) {
              Column(modifier = Modifier.testTag(ReplacementTestTags.CARD_LIST)) {
                items.forEach { item ->
                  ReplacementCard(item, onClick = item.onClick)
                  Spacer(modifier = Modifier.height(SpacingMedium))
                }
              }
            }
      })
}

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

@Preview(showBackground = true)
@Composable
fun ReplacementScreenPreview() {
  ReplacementScreen()
}
