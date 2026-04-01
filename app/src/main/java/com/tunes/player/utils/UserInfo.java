package com.tunes.player.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores user display name and avatar URI in a single SharedPreferences file,
 * rather than one file per key.
 */
public class UserInfo {

    private static final String PREFS_FILE  = "user_prefs";
    private static final String KEY_NAME    = "UserName";
    private static final String KEY_AVATAR  = "AvatarUri";

    private UserInfo() {}

    public static void saveUserName(Context context, String name) {
        prefs(context).edit().putString(KEY_NAME, name).apply();
    }

    public static String getUserName(Context context) {
        return prefs(context).getString(KEY_NAME, "User");
    }

    public static void saveUserProfilePic(Context context, String path) {
        prefs(context).edit().putString(KEY_AVATAR, path).apply();
    }

    public static String getUserProfilePic(Context context) {
        return prefs(context).getString(KEY_AVATAR, "");
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }
}
