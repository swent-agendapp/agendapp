package com.android.sample.ui.replacement.organize.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightExtraHeavy

// Assisted by AI

/**
 * Final step of the **Organize Replacement** workflow, allowing the admin to choose when the
 * replacement should be processed.
 *
 * The screen displays:
 * - A confirmation message asking whether the request should be processed immediately
 * - Two action buttons:
 *     - **Process now**
 *     - **Process later**
 *
 * ### UX behavior
 * - Tapping **Process now** triggers `onProcessNow()`
 * - Tapping **Process later** triggers `onProcessLater()`
 *
 * This composable does **not** handle the processing or navigation logic itself — it only exposes
 * callbacks. The parent `ReplacementOrganizeScreen` owns the navigation state and workflow.
 *
 * @param onProcessNow Called when the admin chooses to process the replacement request immediately.
 * @param onProcessLater Called when the admin postpones the processing to a later time.
 */
@Composable
fun SelectProcessMomentScreen(
    onProcessNow: () -> Unit = {},
    onProcessLater: () -> Unit = {},
) {
  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.organize_replacement)) },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(WeightExtraHeavy).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.select_process_moment),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT))
                  }

              Column(
                  modifier = Modifier.weight(WeightExtraHeavy).padding(vertical = PaddingLarge),
                  verticalArrangement = Arrangement.spacedBy(PaddingMedium),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(
                        onClick = onProcessNow,
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON),
                        shape = RoundedCornerShape(CornerRadiusLarge)) {
                          Text(
                              text = stringResource(R.string.process_now),
                              modifier =
                                  Modifier.padding(
                                      vertical = PaddingLarge, horizontal = PaddingMedium),
                              style = MaterialTheme.typography.titleMedium)
                        }

                    OutlinedButton(
                        onClick = onProcessLater,
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON),
                        shape = RoundedCornerShape(CornerRadiusLarge)) {
                          Text(
                              text = stringResource(R.string.process_later),
                              modifier =
                                  Modifier.padding(
                                      vertical = PaddingLarge, horizontal = PaddingMedium),
                              style = MaterialTheme.typography.titleMedium)
                        }
                  }
            }
      })
}

@Preview(showBackground = true)
@Composable
fun SelectProcessMomentScreenPreview() {
  SelectProcessMomentScreen()
}
