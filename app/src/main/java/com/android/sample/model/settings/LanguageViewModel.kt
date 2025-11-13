package com.android.sample.settings.language

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LanguageRepository(application.applicationContext)

    private val _selectedLanguage = MutableStateFlow(repository.getCurrentLanguage())
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    fun onLanguageSelected(languageCode: String) {
        _selectedLanguage.value = languageCode
    }

    fun applyLanguage(context: Context) {
        viewModelScope.launch {
            repository.saveLanguage(_selectedLanguage.value)
            repository.updateAppLocale(_selectedLanguage.value)

            // Use Intent flags to properly restart the app
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)

            // Finish current activity if it's an Activity context
            if (context is Activity) {
                context.finish()
            }
        }
    }

    private fun restartActivity(context: Context) {
        // Let AppCompat handle the locale change and activity recreation
        // The system will automatically recreate the activity when locale changes
        if (context is androidx.appcompat.app.AppCompatActivity) {
            context.recreate()
        }
    }
}