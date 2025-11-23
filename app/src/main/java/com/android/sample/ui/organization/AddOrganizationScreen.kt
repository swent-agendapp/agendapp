package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.android.sample.model.organization.SelectedOrganizationRepository
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingMedium

object AddOrganizationScreenTestTags {
  const val ROOT = "addOrganizationScreenRoot"
  const val ORGANIZATION_NAME_TEXT_FIELD = "organizationNameTextField"
  const val CREATE_BUTTON = "createButton"
  const val BACK_BUTTON = "backButton"
}

@Composable
fun AddOrganizationScreen(
    addOrganizationViewModel: AddOrganizationViewModel = viewModel(),
    organizationViewModel: OrganizationViewModel = viewModel(),
    selectedOrganizationRepository: SelectedOrganizationRepository = SelectedOrganizationRepository,
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
) {

  val uiState by addOrganizationViewModel.uiState.collectAsState()

  var nameTouched by remember { mutableStateOf(false) }

  Scaffold(modifier = Modifier.fillMaxHeight().testTag(AddOrganizationScreenTestTags.ROOT)) {
      innerPadding ->
    Box(
        modifier = Modifier.padding(innerPadding).padding(PaddingMedium).fillMaxHeight(),
        contentAlignment = Alignment.Center) {
          Column(modifier = Modifier.padding(innerPadding)) {
            // Organization Name Text Field
            ValidatingTextField(
                value = uiState.name.orEmpty(),
                onValueChange = { newName -> addOrganizationViewModel.updateName(name = newName) },
                label = stringResource(R.string.organization_name_label),
                isError = !addOrganizationViewModel.isValidOrganizationName() && nameTouched,
                errorMessage = stringResource(R.string.name_empty_error),
                testTag = AddOrganizationScreenTestTags.ORGANIZATION_NAME_TEXT_FIELD,
                onFocusChange = { focusState -> if (focusState.isFocused) nameTouched = true })

            // Bottom Navigation Buttons
            BottomNavigationButtons(
                onNext = {
                  val organizationName = uiState.name
                  if (organizationName != null) {
                    organizationViewModel.addOrganizationFromName(organizationName)
                  }
                  selectedOrganizationRepository.clearSelection()
                  onFinish()
                },
                onBack = onNavigateBack,
                canGoBack = true,
                backButtonText = stringResource(R.string.cancel),
                canGoNext = addOrganizationViewModel.isValidOrganizationName(),
                nextButtonText = stringResource(R.string.create),
                backButtonTestTag = AddOrganizationScreenTestTags.BACK_BUTTON,
                nextButtonTestTag = AddOrganizationScreenTestTags.CREATE_BUTTON,
            )
          }
        }
  }
}
