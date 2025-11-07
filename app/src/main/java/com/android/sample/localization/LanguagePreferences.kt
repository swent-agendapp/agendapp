package com.android.sample.localization

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.android.sample.R
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

/** Represents a language that can be selected by the user. */
data class LanguageOption(
    val languageTag: String,
    val displayName: String,
    val isSystemDefault: Boolean = false,
)

/**
 * Handles reading and persisting the preferred application language. It relies on the
 * locale-config XML resource to ensure the list of available languages stays in sync with the
 * languages shipped in the APK.
 */
class LanguagePreferences(private val context: Context) {

  private val preferences: SharedPreferences =
      context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  /** Returns the preferred language stored by the user, or an empty string if none is stored. */
  fun getPreferredLanguageTag(): String = preferences.getString(KEY_LANGUAGE_TAG, "") ?: ""

  /** Persists the provided language tag. Passing an empty tag clears the preference. */
  fun persistPreferredLanguage(languageTag: String) {
    if (languageTag.isBlank()) {
      preferences.edit().remove(KEY_LANGUAGE_TAG).apply()
    } else {
      preferences.edit().putString(KEY_LANGUAGE_TAG, languageTag).apply()
    }
  }

  /** Applies the provided language immediately using [AppCompatDelegate]. */
  fun applyLanguage(languageTag: String) {
    val localeList =
        if (languageTag.isBlank()) {
          LocaleListCompat.getEmptyLocaleList()
        } else {
          LocaleListCompat.forLanguageTags(languageTag)
        }

    AppCompatDelegate.setApplicationLocales(localeList)

    if (localeList.isEmpty) {
      Locale.setDefault(resolveSystemLocale())
    } else {
      Locale.setDefault(localeList[0])
    }

    updateContextResources(localeList)
  }

  /** Reads the stored language (if any) and applies it. */
  fun applyStoredLanguage() {
    applyLanguage(getPreferredLanguageTag())
  }

  private fun updateContextResources(localeList: LocaleListCompat) {
    val resources = context.resources
    val configuration = resources.configuration

    if (localeList.isEmpty) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.setLocales(Resources.getSystem().configuration.locales)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(resolveSystemLocale())
      } else {
        @Suppress("DEPRECATION") configuration.locale = resolveSystemLocale()
      }
    } else {
      val primaryLocale = localeList[0]
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localesArray = Array(localeList.size()) { index -> localeList[index] }
        configuration.setLocales(LocaleList(*localesArray))
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(primaryLocale)
      } else {
        @Suppress("DEPRECATION") configuration.locale = primaryLocale
      }
    }

    @Suppress("DEPRECATION")
    resources.updateConfiguration(configuration, resources.displayMetrics)
  }

  private fun resolveSystemLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      Resources.getSystem().configuration.locales[0]
    } else {
      @Suppress("DEPRECATION") Resources.getSystem().configuration.locale
    }
  }

  /** Returns the list of languages exposed to the user. */
  fun getSupportedLanguages(): List<LanguageOption> {
    val languageOptions =
        parseLocalesFromConfig()
            .mapNotNull { tag ->
              val locale = Locale.forLanguageTag(tag)
              locale.takeIf { it.language.isNotBlank() }
            }
            .distinctBy { it.language }
            .map { locale ->
              val displayName =
                  locale
                      .getDisplayName(locale)
                      .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
              LanguageOption(languageTag = locale.toLanguageTag(), displayName = displayName)
            }
            .sortedBy { option -> option.displayName.lowercase(Locale.getDefault()) }
    val followSystem =
        LanguageOption(
            languageTag = "",
            displayName = context.getString(R.string.language_follow_system),
            isSystemDefault = true,
        )
    return listOf(followSystem) + languageOptions
  }

  private fun parseLocalesFromConfig(): List<String> {
    val parser = context.resources.getXml(R.xml.locales_config)
    val locales = mutableListOf<String>()
    try {
      var eventType = parser.eventType
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == TAG_LOCALE) {
          val tag = parser.getAttributeValue(XML_NAMESPACE_ANDROID, ATTRIBUTE_NAME)
          if (!tag.isNullOrBlank()) {
            locales += tag
          }
        }
        eventType = parser.next()
      }
    } finally {
      parser.close()
    }
    return if (locales.isEmpty()) DEFAULT_LANGUAGE_TAGS else locales
  }

  companion object {
    private const val PREFS_NAME = "language_preferences"
    private const val KEY_LANGUAGE_TAG = "language_tag"
    private const val TAG_LOCALE = "locale"
    private const val ATTRIBUTE_NAME = "name"
    private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    private val DEFAULT_LANGUAGE_TAGS = listOf("en", "fr", "de")
  }
}
