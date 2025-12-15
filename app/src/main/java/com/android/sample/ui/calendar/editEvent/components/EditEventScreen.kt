package com.android.sample.ui.calendar.editEvent.components

import StepHeader
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.calendar.components.eventSummaryComponents.ParticipantsSection
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.category.components.CategorySelector
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.BorderWidthThick
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.GeneralPaletteDark
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmallMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy

// Assisted by AI

// Minimum number of lines for the description input field.
private const val DESCRIPTION_MIN_LINES = 6
/**
 * **EditEventScreen**
 *
 * A composable screen that allows users to **edit** the details of an existing calendar event. The
 * screen includes fields for title, description, start/end dates and times, recurrence, and
 * participants. It is fully synchronized with [EditEventViewModel].
 *
 * ### Parameters:
 *
 * @param eventId The unique identifier of the event to be edited.
 * @param editEventViewModel The [EditEventViewModel] managing event state.
 * @param onSave Called when the user saves the changes.
 * @param onCancel Called when the user cancels the editing.
 * @param onEditParticipants Called when the user wants to edit participants.
 * @param skipLoad For testing purposes to skip loading the event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    editEventViewModel: EditEventViewModel = viewModel(),
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    onEditParticipants: () -> Unit = {},
    skipLoad: Boolean = false // For testing purposes to skip loading the event
) {
    val context = LocalContext.current
    val uiState by editEventViewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        if (!skipLoad) editEventViewModel.loadEvent(eventId)
    }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavigationButtons(
                onBack = onCancel,
                backButtonText = stringResource(R.string.common_cancel),
                canGoBack = true,
                backButtonTestTag = EditEventTestTags.CANCEL_BUTTON,
                onNext = {
                    editEventViewModel.saveEditEventChanges()
                    onSave()
                },
                nextButtonText = stringResource(R.string.common_save),
                canGoNext = editEventViewModel.allFieldsValid(),
                nextButtonTestTag = EditEventTestTags.SAVE_BUTTON
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = PaddingExtraLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(SpacingExtraLarge))

            val surfaceVariant =
                if (isSystemInDarkTheme()) GeneralPaletteDark.SurfaceVariant else GeneralPalette.SurfaceVariant
            val onSurfaceVariant =
                if (isSystemInDarkTheme()) GeneralPaletteDark.OnSurfaceVariant else GeneralPalette.OnSurfaceVariant
            val primary =
                if (isSystemInDarkTheme()) GeneralPaletteDark.Primary else GeneralPalette.Primary

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.padding(PaddingSmallMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(SpacingSmall))

                Text(
                    text = stringResource(R.string.edit_event_header_step),
                    style = MaterialTheme.typography.labelLarge,
                    color = onSurfaceVariant
                )
            }

            Spacer(Modifier.height(SpacingLarge))

            // Title
            ValidatingTextField(
                label = stringResource(R.string.edit_event_title_label),
                placeholder = stringResource(R.string.edit_event_title_placeholder),
                testTag = EditEventTestTags.TITLE_FIELD,
                value = uiState.title,
                onValueChange = { editEventViewModel.setTitle(it) },
                isError = uiState.title.isBlank(),
                errorMessage = stringResource(R.string.edit_event_title_error)
            )

            Spacer(Modifier.height(SpacingLarge))

            // Category
            CategorySelector(
                selectedCategory = uiState.category,
                onCategorySelected = { editEventViewModel.setCategory(it) },
                testTag = EditEventTestTags.CATEGORY_SELECTOR
            )

            Spacer(Modifier.height(SpacingLarge))

            // Description
            ValidatingTextField(
                label = stringResource(R.string.edit_event_description_label),
                placeholder = stringResource(R.string.edit_event_description_placeholder),
                testTag = EditEventTestTags.DESCRIPTION_FIELD,
                value = uiState.description,
                onValueChange = { editEventViewModel.setDescription(it) },
                isError = false,
                errorMessage = "",
                singleLine = false,
                minLines = DESCRIPTION_MIN_LINES
            )

            Spacer(Modifier.height(SpacingExtraLarge))

            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabelWithIcon(
                        icon = { Icon(Icons.Outlined.CalendarMonth, null) },
                        label = stringResource(R.string.edit_event_start_date_label)
                    )
                    Spacer(Modifier.height(SpacingSmall))
                    DatePickerFieldToModal(
                        label = "",
                        modifier = Modifier.testTag(EditEventTestTags.START_DATE_FIELD),
                        initialInstant = uiState.startInstant,
                        enabled = true,
                        onDateSelected = { date ->
                            val newStart = DateTimeUtils.instantWithDate(uiState.startInstant, date)
                            editEventViewModel.setStartInstant(newStart)
                            if (uiState.endInstant < newStart) {
                                editEventViewModel.setEndInstant(newStart)
                            }
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FieldLabelWithIcon(
                        icon = { Icon(Icons.Outlined.CalendarMonth, null) },
                        label = stringResource(R.string.edit_event_end_date_label)
                    )
                    Spacer(Modifier.height(SpacingSmall))
                    DatePickerFieldToModal(
                        label = "",
                        modifier = Modifier.testTag(EditEventTestTags.END_DATE_FIELD),
                        initialInstant = uiState.endInstant,
                        enabled = true,
                        onDateSelected = { date ->
                            val newEnd = DateTimeUtils.instantWithDate(uiState.endInstant, date)
                            editEventViewModel.setEndInstant(
                                if (newEnd < uiState.startInstant) uiState.startInstant else newEnd
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(SpacingLarge))

            // Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabelWithIcon(
                        icon = { Icon(Icons.Outlined.AccessTime, null) },
                        label = stringResource(R.string.edit_event_start_time_label)
                    )
                    Spacer(Modifier.height(SpacingSmall))
                    ClickableOutlinedField(
                        value = DateTimeUtils.formatInstantToTime(uiState.startInstant),
                        testTag = EditEventTestTags.START_TIME_BUTTON,
                        onClick = { showStartTimePicker = true }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FieldLabelWithIcon(
                        icon = { Icon(Icons.Outlined.AccessTime, null) },
                        label = stringResource(R.string.edit_event_end_time_label)
                    )
                    Spacer(Modifier.height(SpacingSmall))
                    ClickableOutlinedField(
                        value = DateTimeUtils.formatInstantToTime(uiState.endInstant),
                        testTag = EditEventTestTags.END_TIME_BUTTON,
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            Spacer(Modifier.height(SpacingExtraLarge))

            // Participants
            Card(shape = RoundedCornerShape(CornerRadiusLarge)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                    horizontalAlignment = Alignment.Start
                ) {
                    ParticipantsSection(
                        participantNames = uiState.participants.toList(),
                        showHeader = false
                    )
                    Spacer(Modifier.height(SpacingLarge))
                    SecondaryButton(
                        modifier = Modifier.testTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON),
                        onClick = onEditParticipants,
                        text = stringResource(R.string.edit_event_edit_participants_button)
                    )
                }
            }

            Spacer(Modifier.height(SpacingExtraLarge))
        }
    }

    // Time pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newStart = DateTimeUtils.instantWithTime(uiState.startInstant, hour, minute)
                editEventViewModel.setStartInstant(newStart)
                if (uiState.endInstant < newStart) {
                    editEventViewModel.setEndInstant(newStart)
                }
                showStartTimePicker = false
            },
            DateTimeUtils.getInstantHour(uiState.startInstant),
            DateTimeUtils.getInstantMinute(uiState.startInstant),
            true
        ).show()
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newEnd = DateTimeUtils.instantWithTime(uiState.endInstant, hour, minute)
                editEventViewModel.setEndInstant(
                    if (newEnd < uiState.startInstant) uiState.startInstant else newEnd
                )
                showEndTimePicker = false
            },
            DateTimeUtils.getInstantHour(uiState.endInstant),
            DateTimeUtils.getInstantMinute(uiState.endInstant),
            true
        ).show()
    }
}

@Preview(showBackground = true, name = "Edit Event Screen Preview")
@Composable
fun EditEventScreenPreview() {
  EditEventScreen(eventId = "PREVIEW123", skipLoad = true)
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
private fun ClickableOutlinedField(value: String, testTag: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth().testTag(testTag).clickable { onClick() },
        trailingIcon = { Text(text = "▾", style = MaterialTheme.typography.titleMedium) })
}