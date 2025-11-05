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
import com.android.sample.R
import com.android.sample.ui.theme.CornerRadiusLarge
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

// Assisted by AI

/**
 * Displays a read-only text field that opens a modal date picker when tapped.
 *
 * This composable shows a formatted date in an `OutlinedTextField`. When the user taps on the
 * field, a `DatePickerDialog` is presented, allowing the user to select a date. Once a date is
 * selected, the field updates and the selected value is passed upward through the `onDateSelected`
 * callback.
 *
 * @param modifier Optional [Modifier] to customize the layout or behavior of the text field.
 * @param label The label displayed in the input field (e.g., `"Start date"`).
 * @param initialInstant Initial date to display in the input field. Defaults to the current date if
 *   none is provided.
 * @param onDateSelected Callback invoked when a date is confirmed in the modal. Exposes the
 *   selected [LocalDate] to the caller for state management or persistence (e.g., ViewModel
 *   update).
 *
 * Example usage:
 * ```
 * DatePickerFieldToModal(
 *     label = "Start Date",
 *     initialInstant = uiState.startInstant,
 *     onDateSelected = { newDate ->
 *         viewModel.onStartDateChanged(newDate)
 *     }
 * )
 * ```
 */
@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    label: String = "label",
    initialInstant: Instant? = null,
    onDateSelected: (LocalDate) -> Unit
) {
  var selectedDate by remember { mutableStateOf(initialInstant?.toEpochMilli()) }
  var showModal by remember { mutableStateOf(false) }

  OutlinedTextField(
      value = selectedDate?.let { convertMillisToDate(it) } ?: "",
      onValueChange = {},
      label = { Text(label) },
      placeholder = { Text("DD/MM/YYYY") },
      trailingIcon = {
        Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.select_date))
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
      shape = RoundedCornerShape(CornerRadiusLarge),
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

/**
 * Converts UNIX timestamp milliseconds to a human-readable date string formatted as `dd/MM/yyyy`.
 *
 * @param millis The date expressed as a UNIX timestamp (milliseconds since epoch).
 * @return Formatted date string (e.g., `"01/12/2025"`).
 */
fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return formatter.format(Date(millis))
}

/**
 * Displays a modal date picker using Material3's [DatePickerDialog].
 *
 * This dialog allows the user to scroll through calendar months and select a date. The dialog
 * exposes callbacks for date confirmation and dismissal. The dialog is controlled externally
 * (typically through state in the parent composable).
 *
 * @param onDateSelected Callback invoked when the user confirms a date selection. The selected date
 *   is provided as the UNIX timestamp (in milliseconds), or `null` if no date was selected.
 * @param onDismiss Callback invoked when the dialog is dismissed without confirmation.
 *
 * This composable does not manage UI state (such as dialog visibility); the parent is responsible
 * for controlling when the dialog is shown.
 */
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
              Text(stringResource(R.string.ok))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
      }) {
        DatePicker(state = datePickerState)
      }
}
