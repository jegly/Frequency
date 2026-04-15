package com.tunes.player.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tJ\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\b\u001a\u00020\tH\u0002J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\u0004J\u0016\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/tunes/player/utils/UserInfo;", "", "()V", "KEY_AVATAR", "", "KEY_NAME", "PREFS_FILE", "getUserName", "context", "Landroid/content/Context;", "getUserProfilePic", "prefs", "Landroid/content/SharedPreferences;", "saveUserName", "", "name", "saveUserProfilePic", "path", "app_release"})
public final class UserInfo {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_FILE = "user_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_NAME = "UserName";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_AVATAR = "AvatarUri";
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.utils.UserInfo INSTANCE = null;
    
    private UserInfo() {
        super();
    }
    
    private final android.content.SharedPreferences prefs(android.content.Context context) {
        return null;
    }
    
    public final void saveUserName(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUserName(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void saveUserProfilePic(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String path) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUserProfilePic(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
}