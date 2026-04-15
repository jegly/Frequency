package com.tunes.player.utils

import android.content.Context
import android.util.Log

object AppSettings {
    private const val TAG = "AppSettings"
    private const val PREF_NAME = "TunesSettings"
    private const val SPAN_COUNT_PORTRAIT = "PortraitGridCount"
    private const val SPAN_COUNT_LANDSCAPE = "LandscapeGridCount"
    private const val UI_MODE_AUTO = "AutoTheme"
    private const val UI_THEME_DARK = "DarkMode"
    private const val BIOMETRIC_ENABLED = "BiometricEnabled"

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun savePortraitGridSpanCount(context: Context, count: Int) {
        getPrefs(context).edit().putInt(SPAN_COUNT_PORTRAIT, count).apply()
    }

    fun getPortraitGridSpanCount(context: Context): Int {
        return getPrefs(context).getInt(SPAN_COUNT_PORTRAIT, 2)
    }

    fun saveLandscapeGridSpanCount(context: Context, count: Int) {
        getPrefs(context).edit().putInt(SPAN_COUNT_LANDSCAPE, count).apply()
    }

    fun getLandscapeGridSpanCount(context: Context): Int {
        return getPrefs(context).getInt(SPAN_COUNT_LANDSCAPE, 4)
    }

    fun enableDarkMode(context: Context, state: Boolean) {
        getPrefs(context).edit().putBoolean(UI_THEME_DARK, state).apply()
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(UI_THEME_DARK, false)
    }

    fun enableAutoTheme(context: Context, state: Boolean) {
        getPrefs(context).edit().putBoolean(UI_MODE_AUTO, state).apply()
    }

    fun isAutoThemeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(UI_MODE_AUTO, false)
    }
    
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(BIOMETRIC_ENABLED, false)
    }
}
