package com.android.sample

import android.app.Application
import com.android.sample.data.global.providers.BoxProvider

class MainApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Initialize Box database for local data storage
    BoxProvider.init(context = this)
  }
}
