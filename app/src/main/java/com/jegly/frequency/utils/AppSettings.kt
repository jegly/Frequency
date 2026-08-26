package com.jegly.frequency.utils

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
    private const val THEME_MODE = "ThemeMode"
    private const val CATPPUCCIN_ACCENT = "CatppuccinAccent"
    private const val CATPPUCCIN_FLAVOR = "CatppuccinFlavor"
    private const val DRACULA_ACCENT = "DraculaAccent"
    private const val PTYXIS_PALETTE = "PtyxisPalette"
    private const val MONOCHROME_ACCENTS = "MonochromeAccents"
    private const val APP_FONT = "AppFont"
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
        getPrefs(context).getInt(SPAN_COUNT_PORTRAIT, 3)

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

    // List view customisation
    private const val LIST_THUMBNAIL_SIZE = "ListThumbnailSize"
    private const val LIST_COMPACT_MODE   = "ListCompactMode"

    fun setListThumbnailSize(context: Context, sizeDp: Int) =
        getPrefs(context).edit().putInt(LIST_THUMBNAIL_SIZE, sizeDp).apply()

    fun getListThumbnailSize(context: Context): Int =
        getPrefs(context).getInt(LIST_THUMBNAIL_SIZE, 50)

    fun setListCompactMode(context: Context, compact: Boolean) =
        getPrefs(context).edit().putBoolean(LIST_COMPACT_MODE, compact).apply()

    fun isListCompactMode(context: Context): Boolean =
        getPrefs(context).getBoolean(LIST_COMPACT_MODE, false)

    // List row vertical padding (dp) — 0 = system default, larger = more spacious
    private const val LIST_ROW_PADDING = "ListRowPadding"
    fun setListRowPadding(context: Context, dp: Int) =
        getPrefs(context).edit().putInt(LIST_ROW_PADDING, dp.coerceIn(-28, 16)).apply()
    fun getListRowPadding(context: Context): Int =
        getPrefs(context).getInt(LIST_ROW_PADDING, 0)

    // Grid view customisation
    private const val GRID_SPACING   = "GridSpacing"
    private const val GRID_SHOW_INFO = "GridShowInfo"

    fun setGridSpacing(context: Context, dp: Int) =
        getPrefs(context).edit().putInt(GRID_SPACING, dp).apply()

    fun getGridSpacing(context: Context): Int =
        getPrefs(context).getInt(GRID_SPACING, 8)

    fun setGridShowInfo(context: Context, show: Boolean) =
        getPrefs(context).edit().putBoolean(GRID_SHOW_INFO, show).apply()

    fun isGridShowInfo(context: Context): Boolean =
        getPrefs(context).getBoolean(GRID_SHOW_INFO, true)

    // Theme mode (system / catppuccin / dracula)
    fun setThemeMode(context: Context, mode: String) =
        getPrefs(context).edit().putString(THEME_MODE, mode).apply()

    fun getThemeMode(context: Context): String =
        getPrefs(context).getString(THEME_MODE, "system") ?: "system"

    fun setCatppuccinAccent(context: Context, key: String) =
        getPrefs(context).edit().putString(CATPPUCCIN_ACCENT, key).apply()

    fun getCatppuccinAccent(context: Context): String =
        getPrefs(context).getString(CATPPUCCIN_ACCENT, "mauve") ?: "mauve"

    fun setDraculaAccent(context: Context, key: String) =
        getPrefs(context).edit().putString(DRACULA_ACCENT, key).apply()

    fun getDraculaAccent(context: Context): String =
        getPrefs(context).getString(DRACULA_ACCENT, "purple") ?: "purple"

    fun setCatppuccinFlavor(context: Context, key: String) =
        getPrefs(context).edit().putString(CATPPUCCIN_FLAVOR, key).apply()

    fun getCatppuccinFlavor(context: Context): String =
        getPrefs(context).getString(CATPPUCCIN_FLAVOR, "mocha") ?: "mocha"

    fun setPtyxisPalette(context: Context, key: String) =
        getPrefs(context).edit().putString(PTYXIS_PALETTE, key).apply()

    fun getPtyxisPalette(context: Context): String =
        getPrefs(context).getString(PTYXIS_PALETTE, "nord") ?: "nord"

    fun setMonochromeAccents(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(MONOCHROME_ACCENTS, enabled).apply()

    fun isMonochromeAccents(context: Context): Boolean =
        getPrefs(context).getBoolean(MONOCHROME_ACCENTS, false)

    fun setAppFont(context: Context, key: String) =
        getPrefs(context).edit().putString(APP_FONT, key).apply()

    fun getAppFont(context: Context): String =
        getPrefs(context).getString(APP_FONT, "dotgothic16") ?: "dotgothic16"

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

    // Auto-remove duplicates
    private const val AUTO_REMOVE_DUPLICATES = "AutoRemoveDuplicates"
    fun setAutoRemoveDuplicates(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(AUTO_REMOVE_DUPLICATES, enabled).apply()
    fun isAutoRemoveDuplicates(context: Context): Boolean = getPrefs(context).getBoolean(AUTO_REMOVE_DUPLICATES, false)

    // Mini-player visibility
    private const val MINI_PLAYER_HIDDEN = "MiniPlayerHidden"
    fun setMiniPlayerHidden(context: Context, hidden: Boolean) =
        getPrefs(context).edit().putBoolean(MINI_PLAYER_HIDDEN, hidden).apply()
    fun isMiniPlayerHidden(context: Context): Boolean =
        getPrefs(context).getBoolean(MINI_PLAYER_HIDDEN, false)

    // Frequency tab independent volume (0.0–1.0, default 1.0)
    private const val SIGNAL_LAB_VOLUME = "SignalLabVolume"
    fun setSignalLabVolume(context: Context, volume: Float) =
        getPrefs(context).edit().putFloat(SIGNAL_LAB_VOLUME, volume.coerceIn(0f, 1f)).apply()
    fun getSignalLabVolume(context: Context): Float =
        getPrefs(context).getFloat(SIGNAL_LAB_VOLUME, 1.0f).coerceIn(0f, 1f)

    // Hide built-in system playlists from the Playlists tab
    private const val HIDE_SYSTEM_PLAYLISTS = "HideSystemPlaylists"
    fun setHideSystemPlaylists(context: Context, hidden: Boolean) =
        getPrefs(context).edit().putBoolean(HIDE_SYSTEM_PLAYLISTS, hidden).apply()
    fun isHideSystemPlaylists(context: Context): Boolean =
        getPrefs(context).getBoolean(HIDE_SYSTEM_PLAYLISTS, false)

    // Playlist categories — stored as one pref per playlist id
    private const val PLAYLIST_CATEGORY_PREFIX = "PlaylistCategory_"
    fun setPlaylistCategory(context: Context, playlistId: Long, category: String?) {
        val key = "$PLAYLIST_CATEGORY_PREFIX$playlistId"
        val editor = getPrefs(context).edit()
        if (category.isNullOrBlank()) editor.remove(key) else editor.putString(key, category)
        editor.apply()
    }

    fun getPlaylistCategory(context: Context, playlistId: Long): String? =
        getPrefs(context).getString("$PLAYLIST_CATEGORY_PREFIX$playlistId", null)

    fun getAllPlaylistCategories(context: Context): Set<String> =
        getPrefs(context).all
            .filterKeys { it.startsWith(PLAYLIST_CATEGORY_PREFIX) }
            .values
            .filterIsInstance<String>()
            .filter { it.isNotBlank() }
            .toSet()

    // Preferred audio output device ID (-1 = system default)
    private const val PREFERRED_AUDIO_DEVICE = "PreferredAudioDeviceId"
    const val AUDIO_DEVICE_SYSTEM = -1
    fun setPreferredAudioDeviceId(context: Context, id: Int) =
        getPrefs(context).edit().putInt(PREFERRED_AUDIO_DEVICE, id).apply()
    fun getPreferredAudioDeviceId(context: Context): Int =
        getPrefs(context).getInt(PREFERRED_AUDIO_DEVICE, AUDIO_DEVICE_SYSTEM)

    // Hidden tracks (removed from library view without deleting from device)
    private const val HIDDEN_TRACKS = "HiddenTracks"

    fun addHiddenTrack(context: Context, path: String) {
        val current = getHiddenTracks(context).toMutableSet()
        current.add(path)
        getPrefs(context).edit().putStringSet(HIDDEN_TRACKS, current).apply()
    }

    fun getHiddenTracks(context: Context): Set<String> =
        getPrefs(context).getStringSet(HIDDEN_TRACKS, emptySet()) ?: emptySet()

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
