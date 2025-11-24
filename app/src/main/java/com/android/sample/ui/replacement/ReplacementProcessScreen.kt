package com.android.sample.ui.replacement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.android.sample.R
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.calendar.utils.DateTimeUtils.DATE_FORMAT_PATTERN
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.replacement.components.MemberSelectionList
import com.android.sample.ui.replacement.components.MemberSelectionListOptions
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightVeryHeavy
import java.time.format.DateTimeFormatter

object ProcessReplacementTestTags {
  const val ROOT = "process_replacement_root"
  const val SEARCH_BAR = "process_replacement_search_bar"
  const val MEMBER_LIST = "process_replacement_member_list"
  const val SELECTED_SUMMARY = "process_replacement_selected_summary"
  const val SEND_BUTTON = "process_replacement_send_button"
  private const val MEMBER_PREFIX = "process_replacement_member_"

  fun memberTag(name: String): String = MEMBER_PREFIX + name
}

private val DefaultCandidates =
    listOf("Emilien", "Haobin", "Noa", "Weifeng", "Timael", "Méline", "Nathan")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessReplacementScreen(
    replacementId: String,
    candidates: List<String> = DefaultCandidates,
    onSendRequests: (List<String>) -> Unit = {},
    onBack: () -> Unit = {},
) {
  val replacement =
      remember(replacementId) { getMockReplacements().first { it.id == replacementId } }

  var selectedMembers by remember { mutableStateOf(setOf<String>()) }

  val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  val dateText = replacement.event.startLocalDate.format(dateFormatter)
  val timeText =
      "${replacement.event.startLocalTime.format(timeFormatter)} - " +
          replacement.event.endLocalTime.format(timeFormatter)

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.replacement_process_title),
            onClick = onBack,
        )
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues)
                    .testTag(ProcessReplacementTestTags.ROOT),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
                  shape = RoundedCornerShape(CornerRadiusLarge)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                    ) {
                      Text(
                          text = replacement.event.title,
                          style =
                              MaterialTheme.typography.bodyLarge.copy(
                                  fontWeight = FontWeight.SemiBold),
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                      Spacer(modifier = Modifier.height(SpacingSmall))
                      Text(
                          text = "$dateText • $timeText",
                          style = MaterialTheme.typography.bodyMedium)
                      Spacer(modifier = Modifier.height(SpacingSmall))
                      Text(
                          text =
                              stringResource(
                                  id = R.string.replacement_substituted_label,
                                  replacement.absentUserId),
                          style = MaterialTheme.typography.bodySmall)
                    }
                  }

              Spacer(modifier = Modifier.height(SpacingMedium))

              Card(
                  modifier = Modifier.fillMaxWidth().weight(WeightVeryHeavy),
                  elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
                  shape = RoundedCornerShape(CornerRadiusLarge)) {
                    MemberSelectionList(
                        members = candidates,
                        selectedMembers = selectedMembers,
                        onSelectionChanged = { selectedMembers = it },
                        options =
                            MemberSelectionListOptions(
                                searchTestTag = ProcessReplacementTestTags.SEARCH_BAR,
                                listTestTag = ProcessReplacementTestTags.MEMBER_LIST,
                                summaryTestTag = ProcessReplacementTestTags.SELECTED_SUMMARY,
                                isSingleSelection = false,
                                memberTagBuilder = { member ->
                                  ProcessReplacementTestTags.memberTag(member)
                                },
                            ))
                  }

              Spacer(modifier = Modifier.height(SpacingMedium))

              PrimaryButton(
                  onClick = { onSendRequests(selectedMembers.toList()) },
                  enabled = selectedMembers.isNotEmpty(),
                  text =
                      pluralStringResource(
                          R.plurals.replacement_send_requests_button,
                          selectedMembers.size,
                          selectedMembers.size,
                      ),
                  modifier =
                      Modifier.fillMaxWidth().testTag(ProcessReplacementTestTags.SEND_BUTTON))
            }
      }
}
