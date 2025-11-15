package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.components.BottomNavigationButtons

@Composable
fun AddOrganizationScreen(
    addOrganizationViewModel: AddOrganizationViewModel = viewModel(),
    organizationViewModel: OrganizationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
) {

  val uiState by addOrganizationViewModel.uiState.collectAsState()

  Scaffold { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
      // Organization Name Text Field
      ValidatingTextField(
          value = uiState.name.orEmpty(),
          onValueChange = { it -> addOrganizationViewModel.updateName(name = it) },
          label = stringResource(R.string.organization_name_label),
          isError = !addOrganizationViewModel.isValidOrganizationName(),
          errorMessage = stringResource(R.string.name_empty_error))

      // Bottom Navigation Buttons
      BottomNavigationButtons(
          onNext = {
            val organizationName = uiState.name
            if (organizationName != null) {
              organizationViewModel.addOrganizationFromName(organizationName)
            }
            onFinish()
          },
          onBack = onNavigateBack,
          canGoBack = true,
          backButtonText = stringResource(R.string.cancel),
          canGoNext = addOrganizationViewModel.isValidOrganizationName(),
          nextButtonText = stringResource(R.string.create),
          backButtonTestTag = "",
          nextButtonTestTag = "",
      )
    }
  }
}
