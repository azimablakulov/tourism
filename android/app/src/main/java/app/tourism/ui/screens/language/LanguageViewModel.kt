package app.tourism.ui.screens.language

import androidx.lifecycle.ViewModel
import app.tourism.data.prefs.Language
import app.tourism.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _languages = MutableStateFlow(userPreferences.languages)
    val languages = _languages.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(userPreferences.getLanguage())
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun updateLanguage(value: Language) {
        _selectedLanguage.value = value
        userPreferences.setLanguage(value.code)
    }
}