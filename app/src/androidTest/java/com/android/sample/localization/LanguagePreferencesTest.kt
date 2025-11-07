package com.android.sample.localization

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.android.sample.R
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class LanguagePreferencesTest {

  private lateinit var context: Context
  private lateinit var languagePreferences: LanguagePreferences

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    languagePreferences = LanguagePreferences(context)
    languagePreferences.persistPreferredLanguage("")
    languagePreferences.applyLanguage("")
  }

  @After
  fun tearDown() {
    languagePreferences.persistPreferredLanguage("")
    languagePreferences.applyLanguage("")
  }

  @Test
  fun supportedLanguages_includeConfiguredLocales() {
    val options = languagePreferences.getSupportedLanguages()

    assertThat(options.map { it.languageTag })
        .containsAtLeast("", "en", "fr", "de")
  }

  @Test
  fun applyLanguage_updatesApplicationLocales() {
    languagePreferences.applyLanguage("fr")

    assertThat(AppCompatDelegate.getApplicationLocales().toLanguageTags()).isEqualTo("fr")

    languagePreferences.applyLanguage("")

    assertThat(AppCompatDelegate.getApplicationLocales().isEmpty).isTrue()
  }

  @Test
  fun persistPreferredLanguage_savesAndClearsTag() {
    languagePreferences.persistPreferredLanguage("de")

    assertThat(languagePreferences.getPreferredLanguageTag()).isEqualTo("de")

    languagePreferences.persistPreferredLanguage("")

    assertThat(languagePreferences.getPreferredLanguageTag()).isEmpty()
  }

  @Test
  fun applyLanguage_updatesResourcesFromSelectedLocale() {
    languagePreferences.applyLanguage("fr")

    val localizedLabel =
        ApplicationProvider.getApplicationContext<Context>()
            .resources
            .getString(R.string.settings_language_button)

    assertThat(localizedLabel).isEqualTo("Langue")
  }
}
