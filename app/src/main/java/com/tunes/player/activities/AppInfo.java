package com.tunes.player.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.tunes.player.BuildConfig;
import com.tunes.player.R;

public class AppInfo extends PMBActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toolbar t = findViewById(R.id.toolbar);
            t.setTitle(R.string.app_name);
            t.setNavigationIcon(R.drawable.ic_back);
            t.setNavigationOnClickListener(v -> finish());
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.app_info_close_btn).setOnClickListener(v -> finish());
        }

        TextView temp = findViewById(R.id.app_version_desc);
        temp.setText(BuildConfig.VERSION_NAME);

        temp = findViewById(R.id.app_build_desc);
        temp.setText(String.valueOf(BuildConfig.VERSION_CODE));
    }
}
