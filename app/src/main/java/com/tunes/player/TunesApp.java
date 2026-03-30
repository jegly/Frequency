package com.tunes.player;

import android.app.Application;

import com.tunes.player.themes.ThemeManager;

public class TunesApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.init(getApplicationContext());
        int radius = getResources().getDimensionPixelSize(R.dimen.rounding_radius_default);
        int small = getResources().getDimensionPixelSize(R.dimen.rounding_radius_small);
        GlideConstantArtifacts.init(new int[]{radius, small});
    }
}
