package com.tunes.player.utils

import android.content.Context
import android.content.SharedPreferences

object UserInfo {

    private const val PREFS_FILE = "user_prefs"
    private const val KEY_NAME = "UserName"
    private const val KEY_AVATAR = "AvatarUri"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    fun saveUserName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name).apply()
    }

    fun getUserName(context: Context): String {
        return prefs(context).getString(KEY_NAME, "User") ?: "User"
    }

    fun saveUserProfilePic(context: Context, path: String) {
        prefs(context).edit().putString(KEY_AVATAR, path).apply()
    }

    fun getUserProfilePic(context: Context): String {
        return prefs(context).getString(KEY_AVATAR, "") ?: ""
    }
}
