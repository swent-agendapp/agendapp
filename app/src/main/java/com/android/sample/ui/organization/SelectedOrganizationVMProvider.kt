package com.android.sample.ui.organization

/** Provides a single instance of [SelectedOrganizationViewModel] in the app. */
object SelectedOrganizationVMProvider {
  private val _viewModel: SelectedOrganizationViewModel by lazy { SelectedOrganizationViewModel() }

  var viewModel: SelectedOrganizationViewModel = _viewModel
}
