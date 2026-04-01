package com.tunes.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.tunes.player.playback.LocalPlayback;
import com.tunes.player.playback.MediaNotificationManager;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.singleton.TrackManager;

import java.util.List;

public class TMS extends MediaBrowserService implements PlaybackManager.PlaybackServiceCallback {

    private static final String TAG = "TMS";
    private static final String CHANNEL_ID = "com.tunes.player.STARTUP_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;
    
    private MediaSession mMediaSession;
    private MediaNotificationManager mNotificationManager;
    private boolean isServiceRunning = false;
    private HandlerThread mServiceThread = null;
    private Handler mWorkerHandler;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (!clientPackageName.equals(getPackageName())) {
            return null;
        }
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSession(getApplicationContext(), TAG);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(getApplicationContext(), MediaButtonReceiver.class);
        PendingIntent mbrIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, flags);
        mMediaSession.setMediaButtonReceiver(mbrIntent);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        setSessionToken(mMediaSession.getSessionToken());

        mServiceThread = new HandlerThread("TMS-Worker", Thread.NORM_PRIORITY);
        mServiceThread.start();
        mWorkerHandler = new Handler(mServiceThread.getLooper());
        mWorkerHandler.post(this::runOnServiceThread);
    }

    private void runOnServiceThread() {
        TrackManager mTrackManager = TrackManager.getInstance();
        LocalPlayback playback = new LocalPlayback(this, mTrackManager, mWorkerHandler);
        PlaybackManager mPlaybackManager = new PlaybackManager(
                getApplicationContext(), playback, mTrackManager, this);

        mMediaSession.setCallback(mPlaybackManager.getSessionCallbacks(), mWorkerHandler);
        mNotificationManager = new MediaNotificationManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && mMediaSession != null) {
            MediaButtonReceiver.handleIntent(
                    MediaSessionCompat.fromMediaSession(this, mMediaSession), intent);
        }
        
        // Fix for ANR: Ensure startForeground is called within 5 seconds of startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createStartupChannel();
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Tunes")
                    .setContentText("Initializing...")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build();
            startForeground(NOTIFICATION_ID, notification);
        }
        
        isServiceRunning = true;
        return START_NOT_STICKY;
    }

    private void createStartupChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Tunes Service", NotificationManager.IMPORTANCE_MIN);
                nm.createNotificationChannel(channel);
            }
        }
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
        stopForeground(true);
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
