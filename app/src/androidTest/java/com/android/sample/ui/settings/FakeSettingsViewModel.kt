package com.android.sample.ui.settings

import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkStatusRepositoryProvider

// Fake ViewModel for testing Settings screen
class FakeSettingsViewModel(
    networkStatusRepository: NetworkStatusRepository = NetworkStatusRepositoryProvider.repository
) : SettingsViewModel(networkStatusRepository = networkStatusRepository)
