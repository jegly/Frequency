package com.tunes.player.themes;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0004J\u0016\u0010\u0010\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0004J\u000e\u0010\u0011\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u001e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0006R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/tunes/player/themes/ThemeManager;", "", "()V", "<set-?>", "", "isAutoThemeEnabled", "()Z", "isDarkModeEnabled", "mAccentId", "", "mThemeId", "enableAutoTheme", "", "context", "Landroid/content/Context;", "enable", "enableDarkMode", "init", "app_release"})
public final class ThemeManager {
    private static boolean isDarkModeEnabled = false;
    private static boolean isAutoThemeEnabled = false;
    private static int mThemeId = 0;
    private static int mAccentId = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.themes.ThemeManager INSTANCE = null;
    
    private ThemeManager() {
        super();
    }
    
    public final boolean isDarkModeEnabled() {
        return false;
    }
    
    public final boolean isAutoThemeEnabled() {
        return false;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void enableDarkMode(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enable) {
    }
    
    public final void enableAutoTheme(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enable) {
    }
}