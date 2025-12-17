package com.android.sample.ui.calendar.addEvent.components

import StepHeader
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.labelRes
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.AlphaLowLow
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.GeneralPaletteDark
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium

/**
 * Second step of event creation flow: select start/end date and time, plus optional recurrence
 * rules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventTimeAndRecurrenceScreen(
    modifier: Modifier = Modifier,
    addEventViewModel: AddEventViewModel = viewModel()
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val context = LocalContext.current

  val recurrenceOptions = RecurrenceStatus.entries.toList()
  val selectedRecurrence = newEventUIState.recurrenceMode

  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = PaddingExtraLarge)
              .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.height(SpacingExtraLarge))
        StepHeader(
            stepText = stringResource(R.string.add_event_step_2_of_2),
            title = stringResource(R.string.add_event_time_title),
            subtitle = stringResource(R.string.add_event_time_subtitle),
            icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) },
            progress = 0.6f)

        Spacer(modifier = Modifier.height(SpacingExtraLarge))

        // Start row: Start Date + Start Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
            verticalAlignment = Alignment.Bottom) {
              Column(modifier = Modifier.weight(WeightMedium)) {
                FieldLabelWithIcon(
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    label = stringResource(R.string.startDatePickerLabel))
                Spacer(Modifier.height(SpacingSmall))
                DatePickerFieldToModal(
                    label = "",
                    modifier = Modifier.fillMaxWidth().testTag(AddEventTestTags.START_DATE_FIELD),
                    initialInstant = newEventUIState.startInstant,
                    enabled = true,
                    onDateSelected = { date ->
                      val newStart =
                          DateTimeUtils.instantWithDate(newEventUIState.startInstant, date)
                      addEventViewModel.setStartInstant(newStart)
                      if (newEventUIState.endInstant < newStart)
                          addEventViewModel.setEndInstant(newStart)
                    })
              }

              Column(modifier = Modifier.weight(WeightLight)) {
                FieldLabelWithIcon(
                    icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) },
                    label = stringResource(R.string.startTime))
                Spacer(Modifier.height(SpacingSmall))
                ClickableOutlinedField(
                    value = DateTimeUtils.formatInstantToTime(newEventUIState.startInstant),
                    testTag = AddEventTestTags.START_TIME_BUTTON,
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.fillMaxWidth())
              }
            }

        Spacer(modifier = Modifier.height(SpacingLarge))

        // End row: End Date + End Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
            verticalAlignment = Alignment.Bottom) {
              Column(modifier = Modifier.weight(WeightMedium)) {
                FieldLabelWithIcon(
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    label = stringResource(R.string.endDatePickerLabel))
                Spacer(Modifier.height(SpacingSmall))
                DatePickerFieldToModal(
                    label = "",
                    modifier = Modifier.fillMaxWidth().testTag(AddEventTestTags.END_DATE_FIELD),
                    initialInstant = newEventUIState.endInstant,
                    enabled = true,
                    onDateSelected = { date ->
                      val candidate =
                          DateTimeUtils.instantWithDate(newEventUIState.endInstant, date)
                      addEventViewModel.setEndInstant(
                          if (candidate < newEventUIState.startInstant) newEventUIState.startInstant
                          else candidate)
                    })
              }

              Column(modifier = Modifier.weight(WeightLight)) {
                FieldLabelWithIcon(
                    icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) },
                    label = stringResource(R.string.endTime))
                Spacer(Modifier.height(SpacingSmall))
                ClickableOutlinedField(
                    value = DateTimeUtils.formatInstantToTime(newEventUIState.endInstant),
                    testTag = AddEventTestTags.END_TIME_BUTTON,
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.fillMaxWidth())
              }
            }

        Spacer(modifier = Modifier.height(SpacingLarge))

        FieldLabelWithIcon(
            icon = { Icon(Icons.Outlined.Repeat, contentDescription = null) },
            label = stringResource(R.string.recurrenceMenuLabel))
        Spacer(Modifier.height(SpacingSmall))

        recurrenceOptions.forEach { option ->
          RecurrenceOptionCard(
              text = stringResource(option.labelRes()),
              selected = option == selectedRecurrence,
              onClick = { addEventViewModel.setRecurrenceMode(option) },
              testTag = AddEventTestTags.recurrenceTag(option))
          Spacer(modifier = Modifier.height(SpacingSmall))
        }

        Spacer(modifier = Modifier.height(SpacingLarge))

        DatePickerFieldToModal(
            label = stringResource(R.string.recurrenceEndPickerLabel),
            modifier = Modifier.testTag(AddEventTestTags.END_RECURRENCE_FIELD),
            onDateSelected = { date ->
              addEventViewModel.setRecurrenceEndTime(
                  DateTimeUtils.instantWithDate(newEventUIState.startInstant, date = date))
            },
            enabled = (selectedRecurrence != RecurrenceStatus.OneTime),
            initialInstant = newEventUIState.recurrenceEndInstant)

        Spacer(modifier = Modifier.height(SpacingExtraLarge))
      }

  if (showStartTimePicker) {
    TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
              val newInstant =
                  DateTimeUtils.instantWithTime(
                      instant = newEventUIState.startInstant, hour = hour, minute = minute)
              addEventViewModel.setStartInstant(newInstant)

              if (newEventUIState.endInstant < newInstant) {
                addEventViewModel.setEndInstant(newInstant)
              }
              showStartTimePicker = false
            },
            DateTimeUtils.getInstantHour(newEventUIState.startInstant),
            DateTimeUtils.getInstantMinute(newEventUIState.startInstant),
            false)
        .show()
  }

  if (showEndTimePicker) {
    TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
              val newInstant =
                  DateTimeUtils.instantWithTime(
                      instant = newEventUIState.endInstant, hour = hour, minute = minute)
              addEventViewModel.setEndInstant(newInstant)
              showEndTimePicker = false
            },
            DateTimeUtils.getInstantHour(newEventUIState.endInstant),
            DateTimeUtils.getInstantMinute(newEventUIState.endInstant),
            false)
        .show()
  }
}

@Composable
fun AddEventTimeAndRecurrenceBottomBar(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val timeIsCoherent by
      remember(newEventUIState) { derivedStateOf { !addEventViewModel.startTimeIsAfterEndTime() } }

  BottomNavigationButtons(
      onNext = onNext,
      onBack = onBack,
      backButtonText = stringResource(R.string.goBack),
      nextButtonText = stringResource(R.string.next),
      canGoNext = timeIsCoherent,
      backButtonTestTag = AddEventTestTags.BACK_BUTTON,
      nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
}

@Composable
private fun FieldLabelWithIcon(icon: @Composable () -> Unit, label: String) {
  val surfaceVariant =
      if (isSystemInDarkTheme()) GeneralPaletteDark.SurfaceVariant
      else GeneralPalette.SurfaceVariant
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(shape = MaterialTheme.shapes.small, color = surfaceVariant) {
      Box(modifier = Modifier.padding(PaddingExtraSmall), contentAlignment = Alignment.Center) {
        icon()
      }
    }
    Spacer(modifier = Modifier.padding(horizontal = SpacingSmall))
    Text(text = label, style = MaterialTheme.typography.labelLarge)
  }
}

@Composable
private fun ClickableOutlinedField(
    value: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxWidth().testTag(testTag).clickable(onClick = onClick)) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun RecurrenceOptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
  val surface = if (isSystemInDarkTheme()) GeneralPaletteDark.Surface else GeneralPalette.Surface
  val secondary =
      if (isSystemInDarkTheme()) GeneralPaletteDark.Secondary else GeneralPalette.Secondary
  val secondarySelected = secondary.copy(alpha = AlphaLowLow)
  val onSurface =
      if (isSystemInDarkTheme()) GeneralPaletteDark.OnSurface else GeneralPalette.OnSurface

  val bg = if (selected) secondarySelected else surface
  val content = onSurface

  OutlinedCard(
      modifier = Modifier.fillMaxWidth().testTag(testTag).clickable { onClick() },
  ) {
    Surface(color = bg) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
          verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(modifier = Modifier.padding(horizontal = SpacingSmall))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = content,
                modifier = Modifier.weight(WeightExtraHeavy))
          }
    }
  }
}
