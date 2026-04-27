package com.tunes.player.utils

import android.content.Context

object AppSettings {
    private const val PREF_NAME = "TunesSettings"

    // Keys
    private const val SPAN_COUNT_PORTRAIT = "PortraitGridCount"
    private const val SPAN_COUNT_LANDSCAPE = "LandscapeGridCount"
    private const val UI_MODE_AUTO = "AutoTheme"
    private const val UI_THEME_DARK = "DarkMode"
    private const val BIOMETRIC_ENABLED = "BiometricEnabled"
    private const val CHECK_NEW_MEDIA = "CheckNewMedia"
    private const val LAST_TRACK_COUNT = "LastTrackCount"
    private const val GRID_VIEW_ENABLED = "GridViewEnabled"
    private const val ACCENT_COLOR = "AccentColor"
    private const val CUSTOM_MUSIC_FOLDERS = "CustomMusicFolders"
    private const val CROSSFADE_ENABLED = "CrossfadeEnabled"
    private const val GAPLESS_ENABLED = "GaplessEnabled"
    private const val PLAYBACK_SPEED = "PlaybackSpeed"

    const val ACCENT_NONE = -1

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Grid / span
    fun savePortraitGridSpanCount(context: Context, count: Int) =
        getPrefs(context).edit().putInt(SPAN_COUNT_PORTRAIT, count).apply()

    fun getPortraitGridSpanCount(context: Context): Int =
        getPrefs(context).getInt(SPAN_COUNT_PORTRAIT, 2)

    fun saveLandscapeGridSpanCount(context: Context, count: Int) =
        getPrefs(context).edit().putInt(SPAN_COUNT_LANDSCAPE, count).apply()

    fun getLandscapeGridSpanCount(context: Context): Int =
        getPrefs(context).getInt(SPAN_COUNT_LANDSCAPE, 4)

    // Theme
    fun enableDarkMode(context: Context, state: Boolean) =
        getPrefs(context).edit().putBoolean(UI_THEME_DARK, state).apply()

    fun isDarkModeEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(UI_THEME_DARK, false)

    fun enableAutoTheme(context: Context, state: Boolean) =
        getPrefs(context).edit().putBoolean(UI_MODE_AUTO, state).apply()

    fun isAutoThemeEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(UI_MODE_AUTO, false)

    // Biometric
    fun setBiometricEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(BIOMETRIC_ENABLED, enabled).apply()

    fun isBiometricEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(BIOMETRIC_ENABLED, false)

    // Check for new media on launch
    fun setCheckNewMediaEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(CHECK_NEW_MEDIA, enabled).apply()

    fun isCheckNewMediaEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(CHECK_NEW_MEDIA, true)

    // Last track count (for detecting new media)
    fun saveLastTrackCount(context: Context, count: Int) =
        getPrefs(context).edit().putInt(LAST_TRACK_COUNT, count).apply()

    fun getLastTrackCount(context: Context): Int =
        getPrefs(context).getInt(LAST_TRACK_COUNT, 0)

    // Library view mode
    fun setGridViewEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(GRID_VIEW_ENABLED, enabled).apply()

    fun isGridViewEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(GRID_VIEW_ENABLED, false)

    // Accent colour (-1 = use dynamic/default)
    fun setAccentColor(context: Context, color: Int) =
        getPrefs(context).edit().putInt(ACCENT_COLOR, color).apply()

    fun getAccentColor(context: Context): Int =
        getPrefs(context).getInt(ACCENT_COLOR, ACCENT_NONE)

    // Playback speed
    fun setPlaybackSpeed(context: Context, speed: Float) =
        getPrefs(context).edit().putFloat(PLAYBACK_SPEED, speed).apply()

    fun getPlaybackSpeed(context: Context): Float =
        getPrefs(context).getFloat(PLAYBACK_SPEED, 1.0f)

    // Crossfade
    fun setCrossfadeEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(CROSSFADE_ENABLED, enabled).apply()

    fun isCrossfadeEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(CROSSFADE_ENABLED, false)

    // Gapless playback
    fun setGaplessEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(GAPLESS_ENABLED, enabled).apply()

    fun isGaplessEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(GAPLESS_ENABLED, false)

    // Custom music folders (persisted SAF URI strings)
    fun addCustomMusicFolder(context: Context, uriString: String) {
        val current = getCustomMusicFolders(context).toMutableSet()
        current.add(uriString)
        getPrefs(context).edit().putStringSet(CUSTOM_MUSIC_FOLDERS, current).apply()
    }

    fun removeCustomMusicFolder(context: Context, uriString: String) {
        val current = getCustomMusicFolders(context).toMutableSet()
        current.remove(uriString)
        getPrefs(context).edit().putStringSet(CUSTOM_MUSIC_FOLDERS, current).apply()
    }

    fun getCustomMusicFolders(context: Context): Set<String> =
        getPrefs(context).getStringSet(CUSTOM_MUSIC_FOLDERS, emptySet()) ?: emptySet()
}
