package com.tunes.player.playback;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;

import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

public class PlaybackManager implements Playback.Callback {

    // Constants used by MediaNotificationManager and other classes to read metadata
    public static final String METADATA_TITLE_KEY    = MediaMetadata.METADATA_KEY_TITLE;
    public static final String METADATA_ARTIST_KEY   = MediaMetadata.METADATA_KEY_ARTIST;
    public static final String METADATA_ALBUM_KEY    = MediaMetadata.METADATA_KEY_ALBUM;
    public static final String METADATA_ALBUM_ART    = MediaMetadata.METADATA_KEY_ALBUM_ART;
    public static final String METADATA_DURATION_KEY = MediaMetadata.METADATA_KEY_DURATION;

    // Direction constants used by TrackManager.canSkipTrack()
    public static final short ACTION_PLAY_NEXT = 1;
    public static final short ACTION_PLAY_PREV = -1;

    public interface PlaybackServiceCallback {
        void onPlaybackStart();
        void onPlaybackStopped();
        void onStartNotification();
        void onStopNotification();
        void onPlaybackStateChanged(PlaybackState newState);
        void onMetaDataChanged(MediaMetadata newMetaData);
    }

    private final Context mContext;
    private final Playback mPlayback;
    private final TrackManager mTrackManager;
    private final PlaybackServiceCallback mServiceCallback;
    private int mLastKnownMediaId = -1;

    public PlaybackManager(Context context, Playback playback,
                           TrackManager trackManager,
                           PlaybackServiceCallback serviceCallback) {
        this.mContext = context;
        this.mPlayback = playback;
        this.mTrackManager = trackManager;
        this.mServiceCallback = serviceCallback;
        mPlayback.setCallback(this);
    }

    // ── MediaSession.Callback ──────────────────────────────────────────────

    public android.media.session.MediaSession.Callback getSessionCallbacks() {
        return new android.media.session.MediaSession.Callback() {

            @Override
            public void onPlay() {
                handlePlay();
            }

            @Override
            public void onPause() {
                handlePause();
            }

            @Override
            public void onStop() {
                handleStop();
            }

            @Override
            public void onSkipToNext() {
                handleSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                handleSkipToPrev();
            }

            @Override
            public void onSeekTo(long pos) {
                mPlayback.onSeekTo(pos);
            }

            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                handlePlay();
            }
        };
    }

    // ── Private handlers ───────────────────────────────────────────────────

    private void handlePlay() {
        MusicModel item = mTrackManager.getActiveQueueItem();
        if (item == null) return;

        boolean mediaChanged = (item.getId() != mLastKnownMediaId);
        mLastKnownMediaId = item.getId();

        mPlayback.onPlay(item, mediaChanged);
        mServiceCallback.onPlaybackStart();
        mServiceCallback.onStartNotification();

        if (mediaChanged) {
            publishMetadata(item);
        }
    }

    private void handlePause() {
        mPlayback.onPause();
        mServiceCallback.onStopNotification();
    }

    private void handleStop() {
        mPlayback.onStop(true);
        mServiceCallback.onPlaybackStopped();
        mServiceCallback.onStopNotification();
    }

    private void handleSkipToNext() {
        if (mTrackManager.canSkipTrack(ACTION_PLAY_NEXT)) {
            handlePlay();
        } else {
            handleStop();
        }
    }

    private void handleSkipToPrev() {
        if (mPlayback.getCurrentStreamingPosition() > 3000) {
            // If more than 3s in, restart current track
            mPlayback.onSeekTo(0);
        } else if (mTrackManager.canSkipTrack(ACTION_PLAY_PREV)) {
            handlePlay();
        }
    }

    private void publishMetadata(MusicModel md) {
        MediaMetadata.Builder builder = new MediaMetadata.Builder()
                .putString(METADATA_TITLE_KEY,  md.getSongName())
                .putString(METADATA_ARTIST_KEY, md.getArtist())
                .putString(METADATA_ALBUM_KEY,  md.getAlbum())
                .putLong(METADATA_DURATION_KEY, md.getDuration());

        mServiceCallback.onMetaDataChanged(builder.build());
    }

    // ── Playback.Callback ──────────────────────────────────────────────────

    @Override
    public void onPlaybackCompletion() {
        handleSkipToNext();
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        PlaybackState.Builder builder = new PlaybackState.Builder();
        long actions = PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_PLAY_PAUSE
                | PlaybackState.ACTION_SKIP_TO_NEXT
                | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                | PlaybackState.ACTION_STOP
                | PlaybackState.ACTION_SEEK_TO;

        builder.setActions(actions)
               .setState(state, mPlayback.getCurrentStreamingPosition(),
                         1.0f, SystemClock.elapsedRealtime());

        mServiceCallback.onPlaybackStateChanged(builder.build());
    }

    @Override
    public void onFocusChanged(boolean resumePlayback) {
        if (resumePlayback) {
            handlePlay();
        } else {
            handlePause();
        }
    }
}
