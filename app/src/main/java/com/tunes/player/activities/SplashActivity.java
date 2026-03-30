package com.tunes.player.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tunes.player.R;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.loaders.TrackFetcherFromStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends PMBActivity implements TrackFetcherFromStorage.TaskDelegate {

    private static final int REQUEST_CODE = 69;
    private boolean mPermissionsRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        File historyDir = new File(getFilesDir(), "history");
        if (!historyDir.exists()) {
            if (!historyDir.mkdirs()) {
                Log.e("SplashActivity", "Error creating history directory");
            }
        }
        
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (mPermissionsRequested) return;
        
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        }

        if (permissionsNeeded.isEmpty()) {
            startMusicLoader();
        } else {
            mPermissionsRequested = true;
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            mPermissionsRequested = false;
            boolean allGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            if (allGranted) {
                startMusicLoader();
            } else {
                Toast.makeText(this, "App needs storage access to find music", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
            }
        }
    }

    @Override
    public void onTaskCompleted(List<MusicModel> list) {
        TrackManager.getInstance().setMainList(list);
        startHomeActivity();
    }

    private void startHomeActivity() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        }, 600);
    }

    private void startMusicLoader() {
        TrackFetcherFromStorage ml = new TrackFetcherFromStorage(getContentResolver(), this, TrackFetcherFromStorage.Sort.TITLE_ASC);
        ml.execute();
    }
}
