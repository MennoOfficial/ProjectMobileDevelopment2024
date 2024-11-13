package com.example.lendlyapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val THEME_PREFS = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    }

    fun isDarkMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun toggleTheme(context: Context) {
        val isDarkMode = isDarkMode(context)
        getPreferences(context).edit().putBoolean(KEY_DARK_MODE, !isDarkMode).apply()
        
        if (!isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun applyTheme(context: Context) {
        if (isDarkMode(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
} 