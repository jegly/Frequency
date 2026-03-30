package com.tunes.player.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tunes.player.themes.ThemeManager;

public abstract class PMBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeManager.getThemeToApply());
        getTheme().applyStyle(ThemeManager.getAccentToApply(), true);
        super.onCreate(savedInstanceState);
    }
}
