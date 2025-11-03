package com.android.sample.ui.calendar.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.sample.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    label: String,
    initialInstant: Instant? = null,
    onDateSelected: (LocalDate) -> Unit
) {
  var selectedDate by remember { mutableStateOf(initialInstant?.toEpochMilli()) }
  var showModal by remember { mutableStateOf(false) }

  OutlinedTextField(
      value = selectedDate?.let { convertMillisToDate(it) } ?: "",
      onValueChange = {},
      label = { Text(label) },
      placeholder = { Text(stringResource(R.string.date_picker_placeholder)) },
      trailingIcon = {
        Icon(
            Icons.Default.DateRange,
            contentDescription = stringResource(R.string.date_picker_select_date_content_description))
      },
      modifier =
          modifier.fillMaxWidth().pointerInput(selectedDate) {
            awaitEachGesture {
              awaitFirstDown(pass = PointerEventPass.Initial)
              val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
              if (upEvent != null) {
                showModal = true
              }
            }
          },
      shape = RoundedCornerShape(12.dp),
      readOnly = true)

  if (showModal) {
    DatePickerModal(
        onDateSelected = { selectedDateMillis ->
          selectedDate = selectedDateMillis
          selectedDateMillis?.let { millis ->
            val localDate =
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            onDateSelected(localDate)
          }
        },
        onDismiss = { showModal = false })
  }
}

fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val datePickerState = rememberDatePickerState()

  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = {
              onDateSelected(datePickerState.selectedDateMillis)
              onDismiss()
            }) {
              Text(stringResource(android.R.string.ok))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
      }) {
        DatePicker(state = datePickerState)
      }
}
