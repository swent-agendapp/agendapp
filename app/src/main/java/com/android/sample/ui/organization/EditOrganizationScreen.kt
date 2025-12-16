package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingMedium

object EditOrganizationTestTags {
  const val ROOT = "editOrganizationScreenRoot"
  const val NEW_ORGANIZATION_NAME_TEXT_FIELD = "newOrganizationNameTextField"
  const val EDIT_BUTTON = "editButton"
  const val BACK_BUTTON = "backButton"
}

@Composable
fun EditOrganizationScreen(
    editOrganizationViewModel: EditOrganizationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
) {
  LaunchedEffect(Unit) { editOrganizationViewModel.loadOrganizationData() }
  val uiState by editOrganizationViewModel.uiState.collectAsState()

  var nameTouched by remember { mutableStateOf(false) }

  Scaffold(modifier = Modifier.fillMaxHeight().testTag(EditOrganizationTestTags.ROOT)) {
      innerPadding ->
    Box(
        modifier = Modifier.padding(innerPadding).padding(PaddingMedium).fillMaxHeight(),
        contentAlignment = Alignment.Center) {
          Column(modifier = Modifier.padding(innerPadding)) {
            // Organization Name Text Field
            ValidatingTextField(
                value = uiState.name,
                onValueChange = { newName -> editOrganizationViewModel.updateName(name = newName) },
                label = stringResource(R.string.organization_name_label),
                isError = !editOrganizationViewModel.isValidOrganizationName() && nameTouched,
                errorMessage = stringResource(R.string.name_empty_error),
                testTag = EditOrganizationTestTags.NEW_ORGANIZATION_NAME_TEXT_FIELD,
                onFocusChange = { focusState -> if (focusState.isFocused) nameTouched = true })

            // Bottom Navigation Buttons
            BottomNavigationButtons(
                onNext = {
                  if (editOrganizationViewModel.isValidOrganizationName()) {
                    editOrganizationViewModel.editOrganization()
                  }
                  onFinish()
                },
                onBack = onNavigateBack,
                canGoBack = true,
                backButtonText = stringResource(R.string.cancel),
                canGoNext = editOrganizationViewModel.isValidOrganizationName(),
                nextButtonText = stringResource(R.string.common_save),
                backButtonTestTag = EditOrganizationTestTags.BACK_BUTTON,
                nextButtonTestTag = EditOrganizationTestTags.EDIT_BUTTON,
            )
          }
        }
  }
}
