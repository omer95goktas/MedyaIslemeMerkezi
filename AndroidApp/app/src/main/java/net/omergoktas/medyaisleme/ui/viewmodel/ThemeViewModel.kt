package net.omergoktas.medyaisleme.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.omergoktas.medyaisleme.data.model.ThemeMode

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("medya_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getInitialTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun getInitialTheme(): ThemeMode {
        val savedName = prefs.getString("selected_theme", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("selected_theme", mode.name).apply()
    }
}
