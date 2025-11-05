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
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.addEvent.AddEventTestTags

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
      shape = RoundedCornerShape(12.dp),
  )
}

@Preview(showBackground = true)
@Composable
fun ValidatingTextFieldPreview() {
  ValidatingTextField()
}
