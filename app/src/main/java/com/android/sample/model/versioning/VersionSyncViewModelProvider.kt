package com.android.sample.model.versioning

/** Provides a globally accessible instance of [VersionSyncViewModel]. */
object VersionSyncViewModelProvider {
  private var _viewModel: VersionSyncViewModel? = null

  val viewModel: VersionSyncViewModel
    get() = _viewModel ?: error("VersionSyncViewModelProvider not initialized")

  fun init(viewModel: VersionSyncViewModel) {
    _viewModel = viewModel
  }
}
