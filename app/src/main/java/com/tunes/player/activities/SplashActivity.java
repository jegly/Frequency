package com.tunes.player.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tunes.player.R;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.loaders.TrackFetcherFromStorage;
import com.tunes.player.utils.PlaylistStorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends PMBActivity implements TrackFetcherFromStorage.TaskDelegate {

    private static final int REQUEST_CODE = 69;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadingIndicator = findViewById(R.id.loading_indicator);
        // Hide status text as per user request to not show "Found X songs"
        View statusText = findViewById(R.id.status_text);
        if (statusText != null) statusText.setVisibility(View.GONE);

        File historyDir = new File(getFilesDir(), "history");
        if (!historyDir.exists()) historyDir.mkdirs();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAndRequestPermissions, 500);
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            startMusicLoader();
        } else {
            showPermissionExplanationDialog(permissionsNeeded.toArray(new String[0]));
        }
    }

    private void showPermissionExplanationDialog(String[] permissions) {
        new AlertDialog.Builder(this)
                .setTitle("Music Access Required")
                .setMessage("Tunes needs permission to access your audio files and media to build your music library from all folders.")
                .setPositiveButton("Grant Access", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean anyGranted = false;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        anyGranted = true;
                        break;
                    }
                }
            }

            if (anyGranted) {
                startMusicLoader();
            } else {
                showSettingsDialog();
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("Music access was denied. You can enable it in Settings to use the app.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onTaskCompleted(List<MusicModel> list) {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);

        new Thread(() -> {
            TrackManager tm = TrackManager.getInstance();
            tm.setMainList(list);
            List<MusicModel> imported = PlaylistStorageManager.getImportedTracks(this);
            tm.addImportedTracks(this, imported);

            new Handler(Looper.getMainLooper()).postDelayed(this::startHomeActivity, 500);
        }).start();
    }

    private void startHomeActivity() {
        if (!isFinishing()) {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }

    private void startMusicLoader() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        TrackFetcherFromStorage ml = new TrackFetcherFromStorage(getContentResolver(), this, TrackFetcherFromStorage.Sort.TITLE_ASC);
        ml.execute();
    }
}
