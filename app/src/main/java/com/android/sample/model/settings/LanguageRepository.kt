package com.android.sample.settings.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class LanguageRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getCurrentLanguage(): String =
        prefs.getString("app_language", Locale.getDefault().language) ?: "en"

    suspend fun saveLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
    }

    fun updateAppLocale(languageCode: String) {
        // Use proper language tag format
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}