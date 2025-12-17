package com.android.sample

import android.app.Application
import com.android.sample.data.global.providers.BoxProvider
import com.android.sample.model.network.NetworkStatusRepositoryProvider

class MainApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Initialize Box database for local data storage
    BoxProvider.init(context = this)

    // Initialize NetworkStatusRepository
    NetworkStatusRepositoryProvider.init(context = this)
  }
}
