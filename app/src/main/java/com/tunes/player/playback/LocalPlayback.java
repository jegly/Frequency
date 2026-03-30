package com.tunes.player.playback;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.io.IOException;

public class LocalPlayback implements
        Playback,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {

    private final IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private Context mContext;
    private AudioManager mAudioManager;
    private Playback.Callback mPlaybackCallback;
    private MediaPlayer mp;
    private int resumePosition = -1;
    private boolean isBecomingNoisyReceiverRegistered = false;
    private int mCurrentState;
    private TelephonyManager mTelephonyManager;
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (mPlaybackCallback != null) {
                    mPlaybackCallback.onFocusChanged(false);
                }
            }
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private Object mTelephonyCallback; // Using Object to handle different API versions
    private TrackManager mTrackManager;
    private int mMediaId = -99;
    private Handler mHandler;
    private boolean mDelayedPlayback = false;
    private int mPlaybackState = PlaybackState.STATE_NONE;
    private AudioFocusRequest mAudioFocusRequest = null;

    public LocalPlayback(Context context, TrackManager trackManager, Handler handler) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mTrackManager = trackManager;
        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        this.mHandler = handler;
        if (mHandler == null) {
            mHandler = new Handler();
        }
    }

    @Override
    public void setCallback(Callback callback) {
        mPlaybackCallback = callback;
    }

    private void initMediaPlayer() {
        if (mp != null) {
            try {
                mp.reset();
                mp.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mp = null;
        }
        mp = new MediaPlayer();
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        
        MusicModel activeItem = mTrackManager.getActiveQueueItem();
        if (activeItem == null || activeItem.getSongPath() == null) {
            if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackCompletion();
            return;
        }

        try {
            mp.setDataSource(mContext, Uri.parse(activeItem.getSongPath()));
            mp.prepareAsync();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Music file error", Toast.LENGTH_LONG).show();
            if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackCompletion();
        }
        mHandler.post(() -> mTrackManager.addToHistory(mContext));
    }

    @Override
    public void onPlay(MusicModel md, boolean mediaHasChanged) {
        if (tryGetAudioFocus()) {
            mDelayedPlayback = false;
            if (!mediaHasChanged) {
                if (mp != null) {
                    play();
                } else {
                    initMediaPlayer();
                }
            } else {
                onStop(false);
                initMediaPlayer();
                if (md != null) mMediaId = md.getId();
            }
            registerBecomingNoisyReceiver();
            registerTelephonyListener();
        } else if (mDelayedPlayback && md != null) {
            mMediaId = md.getId();
        }
    }

    private void play() {
        if (mp != null) {
            try {
                if (resumePosition != -1) {
                    mp.seekTo(resumePosition);
                    resumePosition = -1;
                }
                mp.start();
                mPlaybackState = PlaybackState.STATE_PLAYING;
                if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                initMediaPlayer();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onStop(false);
        if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackCompletion();
    }

    @Override
    public void onPause() {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            resumePosition = mp.getCurrentPosition();
        }
        mPlaybackState = PlaybackState.STATE_PAUSED;
        if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
    }

    @Override
    public void onSeekTo(long position) {
        if (mp != null) {
            mp.seekTo((int) position);
            if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
        }
    }

    @Override
    public void onStop(boolean abandonAudioFocus) {
        if (abandonAudioFocus) abandonAudioFocus();

        if (mp != null) {
            try {
                if (mp.isPlaying()) mp.stop();
                mp.reset();
                mp.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mp = null;
        }
        
        if (isBecomingNoisyReceiverRegistered) {
            try {
                mContext.unregisterReceiver(becomingNoisyReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isBecomingNoisyReceiverRegistered = false;
        }
        
        unregisterTelephonyListener();

        if (abandonAudioFocus) {
            mPlaybackState = PlaybackState.STATE_STOPPED;
            if (mPlaybackCallback != null) mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
        }
    }

    @Override
    public int getActiveMediaId() {
        return mMediaId;
    }

    @Override
    public boolean isPlaying() {
        if (null != mp) {
            try {
                return mp.isPlaying();
            } catch (IllegalStateException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public long getCurrentStreamingPosition() {
        if (null == mp) return 0;
        try {
            return mp.getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    @Override
    public int getPlaybackState() {
        return mPlaybackState;
    }

    private boolean tryGetAudioFocus() {
        int r;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (null == mAudioFocusRequest) {
                AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(mPlaybackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this, mHandler)
                        .build();
            }
            r = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            if (r == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) mDelayedPlayback = true;
        } else {
            r = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest != null)
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        else {
            mAudioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                mCurrentState = AudioManager.AUDIOFOCUS_GAIN;
                break;
        }
        if (mp != null) configurePlayerState();
        else if (mDelayedPlayback && mPlaybackCallback != null) mPlaybackCallback.onFocusChanged(true);
    }

    private void configurePlayerState() {
        if (mCurrentState == AudioManager.AUDIOFOCUS_LOSS || mCurrentState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (mPlaybackCallback != null) mPlaybackCallback.onFocusChanged(false);
        } else if (mCurrentState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (mp != null) mp.setVolume(0.2f, 0.2f);
        } else if (mCurrentState == AudioManager.AUDIOFOCUS_GAIN) {
            if (mp != null) mp.setVolume(1.0f, 1.0f);
            if (mPlaybackCallback != null) mPlaybackCallback.onFocusChanged(true);
        }
    }

    private void registerBecomingNoisyReceiver() {
        if (!isBecomingNoisyReceiverRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(becomingNoisyReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                mContext.registerReceiver(becomingNoisyReceiver, filter);
            }
            isBecomingNoisyReceiverRegistered = true;
        }
    }

    private void registerTelephonyListener() {
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyManager == null) return;

        // On Android 12+ (API 31), we must have READ_PHONE_STATE to listen for call states
        // If we don't have it, we skip registration to avoid the SecurityException.
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mTelephonyCallback == null) {
                mTelephonyCallback = new CallStateCallback();
            }
            mTelephonyManager.registerTelephonyCallback(mContext.getMainExecutor(), (TelephonyCallback) mTelephonyCallback);
        } else {
            if (mPhoneStateListener == null) {
                mPhoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        handleCallState(state);
                    }
                };
            }
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void unregisterTelephonyListener() {
        if (mTelephonyManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mTelephonyCallback != null) {
                mTelephonyManager.unregisterTelephonyCallback((TelephonyCallback) mTelephonyCallback);
            }
        } else {
            if (mPhoneStateListener != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }

    private void handleCallState(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (mPlaybackCallback != null) mPlaybackCallback.onFocusChanged(false);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (mPlaybackCallback != null) mPlaybackCallback.onFocusChanged(true);
                break;
        }
    }

    private class CallStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            handleCallState(state);
        }
    }
}
