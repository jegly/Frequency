package com.tunes.player.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.tunes.player.TMS;
import com.tunes.player.R;
import com.tunes.player.activities.MainActivity;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import static com.tunes.player.playback.PlaybackManager.METADATA_ALBUM_ART;

public class MediaNotificationManager extends BroadcastReceiver {

    public static final String TAG = "MediaNotificationMan";

    private static final String ACTION_PAUSE = "com.tunes.player.playback.pause";
    private static final String ACTION_PLAY = "com.tunes.player.playback.play";
    private static final String ACTION_PREV = "com.tunes.player.playback.prev";
    private static final String ACTION_NEXT = "com.tunes.player.playback.next";
    private static final String ACTION_STOP = "com.tunes.player.playback.delete";

    private static final String CHANNEL_ID = "com.tunes.player.MUSIC_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private final NotificationManager mNotificationManager;
    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mStopIntent;
    private PendingIntent pi;
    private final TMS mService;
    private MediaSession.Token mSessionToken;
    private MediaController mController;
    private MediaController.TransportControls mTransportControls;
    public boolean mStarted;
    private boolean mInitialised = false;
    private PlaybackState mPlaybackState;
    private MediaMetadata mMetadata;
    private final MediaController.Callback mCb = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            if (null != state) {
                int mState = state.getState();
                if (mPlaybackState != null && mPlaybackState.getState() != mState && mState != PlaybackState.STATE_STOPPED) {
                    mPlaybackState = state;
                    updateNotification();
                }
                mPlaybackState = state;
            }
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            if (null != metadata) {
                mMetadata = metadata;
                updateNotification();
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            updateSessionToken();
            try {
                updateSessionToken();
            } catch (Exception e) {
                Log.e(TAG, "could not connect media controller", e);
            }
        }
    };

    public MediaNotificationManager(TMS service) {
        this.mService = service;
        updateSessionToken();
        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        if(null != mNotificationManager) mNotificationManager.cancelAll();
    }

    private void updateSessionToken() {
        MediaSession.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaController(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    private void updateNotification() {
        if (mPlaybackState == null) return;
        Notification notification = createNotification();
        if (notification == null) return;
        
        if (mPlaybackState.getState() == PlaybackState.STATE_PLAYING) {
            mService.startForeground(NOTIFICATION_ID, notification);
        } else if (mPlaybackState.getState() == PlaybackState.STATE_PAUSED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Using literal value 2 for STOP_FOREGROUND_DETACH to avoid symbol resolution issues
                mService.stopForeground(2); 
            } else {
                mService.stopForeground(false);
            }
        }
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void initialisePendingIntents() {
        String pkg = mService.getPackageName();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PLAY).setPackage(pkg), flags);
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PAUSE).setPackage(pkg), flags);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_STOP).setPackage(pkg), flags);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_NEXT).setPackage(pkg), flags);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PREV).setPackage(pkg), flags);
        if (mNotificationManager != null) mNotificationManager.cancelAll();
        
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        pi = PendingIntent.getActivity(mService, REQUEST_CODE, new Intent(mService, MainActivity.class), piFlags);
        mInitialised = true;
    }

    public void startNotification() {
        if (!mInitialised) {
            initialisePendingIntents();
        }
        if (!mStarted) {
            if (mController != null) {
                mMetadata = mController.getMetadata();
                mPlaybackState = mController.getPlaybackState();
            }
            // The notification must be updated after setting started to true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel();
            Notification notification = createNotification();
            if (notification != null) {
                if (mController != null) mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mService.registerReceiver(this, filter, Context.RECEIVER_EXPORTED);
                } else {
                    mService.registerReceiver(this, filter);
                }
                
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    mService.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification() {
        MusicModel md = TrackManager.getInstance().getActiveQueueItem();
        if (md != null && mPlaybackState != null) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
            notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(MediaSessionCompat.Token.fromToken(mSessionToken))
                    .setCancelButtonIntent(mStopIntent)
                    .setShowCancelButton(true)
                    .setShowActionsInCompactView(0, 1, 2))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(md.getSongName())
                    .setContentText(md.getArtist())
                    .addAction(R.drawable.ic_round_skip_previous_noti, "Skip prev", mPreviousIntent)
                    .addAction(setButton(), "Play Pause Button", getActionIntent())
                    .addAction(R.drawable.ic_round_skip_next_noti, "Skip Next", mNextIntent)
                    .addAction(R.drawable.ic_close_noti, "Stop Self", mStopIntent)
                    .setContentIntent(pi)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setDeleteIntent(mStopIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            if (mMetadata != null) {
                notificationBuilder.setLargeIcon(mMetadata.getBitmap(METADATA_ALBUM_ART));
            }
            
            return notificationBuilder.build();
        }
        return null;
    }

    private int setButton() {
        if (mPlaybackState == null) return R.drawable.ic_round_play_noti;
        return mPlaybackState.getState() == PlaybackState.STATE_PLAYING ?
                R.drawable.ic_round_pause_noti : R.drawable.ic_round_play_noti;
    }

    private PendingIntent getActionIntent() {
        if (mPlaybackState == null) return mPlayIntent;
        return mPlaybackState.getState() == PlaybackState.STATE_PLAYING ?
                mPauseIntent : mPlayIntent;
    }

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            if (mController != null) mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && mTransportControls != null) {
            switch (action) {
                case ACTION_PLAY:
                    mTransportControls.play();
                    break;
                case ACTION_PAUSE:
                    mTransportControls.pause();
                    break;
                case ACTION_STOP:
                    mTransportControls.stop();
                    break;
                case ACTION_NEXT:
                    mTransportControls.skipToNext();
                    break;
                case ACTION_PREV:
                    mTransportControls.skipToPrevious();
                    break;
            }
        }
    }
}
