package com.sendaurjc.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val DARK_MODE_KEY = "dark_mode"
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(DARK_MODE_KEY, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(DARK_MODE_KEY, enabled).apply()
    }
}
