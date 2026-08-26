package com.jegly.frequency

import android.app.Application
import com.jegly.frequency.themes.ThemeManager

class TunesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(applicationContext)
        // Glide initialization if needed
    }
}
