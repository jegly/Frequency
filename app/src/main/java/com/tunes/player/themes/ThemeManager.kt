package com.tunes.player.themes

import android.content.Context
import com.tunes.player.helper.HomeWalliProvider
import com.tunes.player.utils.AppSettings

object ThemeManager {

    var isDarkModeEnabled = false
        private set
    var isAutoThemeEnabled = false
        private set
    private var mThemeId = 0
    private var mAccentId = 0

    fun init(context: Context) {
        isAutoThemeEnabled = AppSettings.isAutoThemeEnabled(context)
        if (isAutoThemeEnabled) {
            val day = HomeWalliProvider.getTimeOfDay()
            isDarkModeEnabled = (day == HomeWalliProvider.Day.EVENING || day == HomeWalliProvider.Day.NIGHT)
        } else {
            isDarkModeEnabled = AppSettings.isDarkModeEnabled(context)
        }

        // mThemeId = if (isDarkModeEnabled) AppSettings.getSelectedDarkTheme(context) else ThemeStore.LIGHT_THEME_1
        // mAccentId = AppSettings.getSelectedAccentColor(context)
    }

    fun enableDarkMode(context: Context, enable: Boolean) {
        AppSettings.enableDarkMode(context, enable)
    }

    fun enableAutoTheme(context: Context, enable: Boolean) {
        AppSettings.enableAutoTheme(context, enable)
    }
}
