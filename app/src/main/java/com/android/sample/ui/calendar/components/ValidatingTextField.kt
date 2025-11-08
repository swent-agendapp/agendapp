package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.theme.CornerRadiusLarge

/**
 * A reusable text input field that supports validation feedback and focus awareness.
 *
 * This composable wraps Material3's [OutlinedTextField], adding:
 * - Optional validation logic (`isError` + `errorMessage`)
 * - `onFocusChange` callback to detect when the user enters or leaves the field
 * - `testTag` support for UI testing
 *
 * It is intended to be used in forms where input validation and state lifting to a ViewModel are
 * required (e.g., event creation with title and description fields).
 *
 * @param modifier Optional [Modifier] to customize layout or behavior of the field.
 * @param value Current text value displayed inside the field.
 * @param onValueChange Lambda invoked on every user input change. State should be stored externally
 *   (typically in a ViewModel or screen-level state).
 * @param label Text displayed above the input field (floating label).
 * @param testTag Tag used by UI tests (Compose Test Rule).
 * @param isError If `true`, the field will render in an error state and the `errorMessage` will be
 *   shown.
 * @param errorMessage Text shown below the field when `isError` is true.
 * @param singleLine Limits text to one line when true. Defaults to true.
 * @param minLines Minimum height of the text field when multi-line is enabled.
 * @param placeholder Placeholder text displayed when the field is empty.
 * @param onFocusChange Callback triggered when the field gains or loses focus.
 *
 * Example:
 * ```
 * ValidatingTextField(
 *     value = uiState.title,
 *     onValueChange = viewModel::setTitle,
 *     label = "Event title",
 *     isError = viewModel.titleIsBlank(),
 *     errorMessage = "Title cannot be empty",
 *     testTag = AddEventTestTags.TITLE_TEXT_FIELD,
 * )
 * ```
 */
@Composable
fun ValidatingTextField(
    modifier: Modifier = Modifier,
    value: String = "value",
    onValueChange: (String) -> Unit = {},
    label: String = "label",
    testTag: String = "testTag",
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String = "",
    onFocusChange: (FocusState) -> Unit = {}
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      singleLine = singleLine,
      modifier = modifier.fillMaxWidth().testTag(testTag).onFocusChanged(onFocusChange),
      minLines = minLines,
      isError = isError,
      placeholder = { Text(placeholder) },
      supportingText = {
        if (isError) {
          Text(text = errorMessage, modifier = Modifier.testTag(AddEventTestTags.ERROR_MESSAGE))
        }
      },
      shape = RoundedCornerShape(CornerRadiusLarge),
  )
}

@Preview(showBackground = true)
@Composable
fun ValidatingTextFieldPreview() {
  ValidatingTextField()
}
