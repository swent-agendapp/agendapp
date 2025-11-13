package com.android.sample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize with saved language
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_language", null)
        if (lang != null) {
            val locales = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}