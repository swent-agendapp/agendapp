package com.android.sample.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.android.sample.R
import com.android.sample.localization.LanguageOption

object LanguageSelectionSectionTestTags {
  const val ROOT = "language_selection_section"
  fun option(languageTag: String): String = "language_option_$languageTag"
}

@Composable
fun LanguageSelectionSection(
    modifier: Modifier = Modifier,
    options: List<LanguageOption>,
    selectedLanguageTag: String,
    onLanguageSelected: (LanguageOption) -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth().semantics { testTag = LanguageSelectionSectionTestTags.ROOT }) {
    Text(text = stringResource(R.string.language_screen_title))
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)) {
          items(options) { option ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .selectable(
                            selected = option.languageTag == selectedLanguageTag,
                            onClick = { onLanguageSelected(option) })
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag(LanguageSelectionSectionTestTags.option(option.languageTag)),
                verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                      selected = option.languageTag == selectedLanguageTag,
                      onClick = { onLanguageSelected(option) })
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(text = option.displayName)
                }
          }
        }
  }
}
