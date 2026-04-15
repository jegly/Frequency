package com.tunes.player

import android.app.Application
import com.tunes.player.themes.ThemeManager

class TunesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(applicationContext)
        // Glide initialization if needed
    }
}
