package com.tunes.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.service.media.MediaBrowserService;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.tunes.player.playback.LocalPlayback;
import com.tunes.player.playback.MediaNotificationManager;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.singleton.TrackManager;

import java.util.List;

public class TMS extends MediaBrowserService implements PlaybackManager.PlaybackServiceCallback {

    private static final String TAG = "PMS";
    private MediaSession mMediaSession;
    private MediaNotificationManager mNotificationManager;
    private boolean isServiceRunning = false;
    private HandlerThread mServiceThread = null;
    private Handler mWorkerHandler;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(getString(R.string.app_name), /* Name visible in Android Auto*/null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceThread = new HandlerThread("PMS Thread", Thread.NORM_PRIORITY);
        mServiceThread.start();
        mWorkerHandler = new Handler(mServiceThread.getLooper());
        mWorkerHandler.post(this::runOnServiceThread);
    }

    private void runOnServiceThread() {
        TrackManager mTrackManager = TrackManager.getInstance();
        LocalPlayback playback = new LocalPlayback(this, mTrackManager, mWorkerHandler);
        PlaybackManager mPlaybackManager = new PlaybackManager(this.getApplicationContext(), playback, mTrackManager, this/*, mWorkerHandler*/);

        mMediaSession = new MediaSession(this.getApplicationContext(), TAG);
        setSessionToken(mMediaSession.getSessionToken());
        mMediaSession.setCallback(mPlaybackManager.getSessionCallbacks(), mWorkerHandler);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(getApplicationContext(), MediaButtonReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent mbrIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, flags);
        mMediaSession.setMediaButtonReceiver(mbrIntent);
        mNotificationManager = new MediaNotificationManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && mMediaSession != null) {
            MediaButtonReceiver.handleIntent(MediaSessionCompat.fromMediaSession(this, mMediaSession), intent);
        }
        isServiceRunning = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mMediaSession != null) {
            mMediaSession.release();
        }
        if (mServiceThread != null) {
            mServiceThread.quit();
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mMediaSession != null && !mMediaSession.isActive()) stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onPlaybackStart() {
        if (mMediaSession != null) {
            mMediaSession.setActive(true);
        }
        if (!isServiceRunning) {
            Intent serviceIntent = new Intent(this.getApplicationContext(), TMS.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    @Override
    public void onPlaybackStopped() {
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
        isServiceRunning = false;
    }

    @Override
    public void onStartNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.startNotification();
        }
    }

    @Override
    public void onStopNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.stopNotification();
        }
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState newState) {
        if (mMediaSession != null) {
            mMediaSession.setPlaybackState(newState);
        }
    }

    @Override
    public void onMetaDataChanged(MediaMetadata newMetaData) {
        if (mMediaSession != null) {
            mMediaSession.setMetadata(newMetaData);
        }
    }
}
