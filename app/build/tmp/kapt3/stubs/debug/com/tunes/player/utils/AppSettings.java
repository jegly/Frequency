package com.tunes.player.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0011\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0014\u001a\u00020\u00132\u0006\u0010\r\u001a\u00020\u000eJ\u0018\u0010\u0015\u001a\n \u0017*\u0004\u0018\u00010\u00160\u00162\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u000e\u0010\u0018\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0019\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u001a\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u001b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u0013J\u0016\u0010\u001d\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u0013J\u0016\u0010\u001e\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/tunes/player/utils/AppSettings;", "", "()V", "BIOMETRIC_ENABLED", "", "PREF_NAME", "SPAN_COUNT_LANDSCAPE", "SPAN_COUNT_PORTRAIT", "TAG", "UI_MODE_AUTO", "UI_THEME_DARK", "enableAutoTheme", "", "context", "Landroid/content/Context;", "state", "", "enableDarkMode", "getLandscapeGridSpanCount", "", "getPortraitGridSpanCount", "getPrefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "isAutoThemeEnabled", "isBiometricEnabled", "isDarkModeEnabled", "saveLandscapeGridSpanCount", "count", "savePortraitGridSpanCount", "setBiometricEnabled", "enabled", "app_debug"})
public final class AppSettings {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AppSettings";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREF_NAME = "TunesSettings";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SPAN_COUNT_PORTRAIT = "PortraitGridCount";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SPAN_COUNT_LANDSCAPE = "LandscapeGridCount";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UI_MODE_AUTO = "AutoTheme";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UI_THEME_DARK = "DarkMode";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BIOMETRIC_ENABLED = "BiometricEnabled";
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.utils.AppSettings INSTANCE = null;
    
    private AppSettings() {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs(android.content.Context context) {
        return null;
    }
    
    public final void savePortraitGridSpanCount(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int count) {
    }
    
    public final int getPortraitGridSpanCount(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void saveLandscapeGridSpanCount(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int count) {
    }
    
    public final int getLandscapeGridSpanCount(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void enableDarkMode(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean state) {
    }
    
    public final boolean isDarkModeEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void enableAutoTheme(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean state) {
    }
    
    public final boolean isAutoThemeEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setBiometricEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enabled) {
    }
    
    public final boolean isBiometricEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
}