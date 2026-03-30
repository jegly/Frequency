package com.tunes.player.activities;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.tunes.player.GlideApp;
import com.tunes.player.R;
import com.tunes.player.model.MusicModel;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends MediaSessionActivity {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private TextView startTime;
    private TextView endTime;
    private SeekBar seekBar;
    private final Runnable mUpdateProgressTask = this::updateProgress;
    private ImageButton mPlayPause;
    private MediaController mController;
    private TextView artistAlbums;
    private PlaybackState mState;
    private TextView toolbarSongTitle;
    private int progress = 0;
    private boolean isFav = false;
    private boolean mFavListModified = false;
    private List<MusicModel> favourites = null;
    private ImageView mFavBtn;
    private ImageView mRepeatBtn;
    private ImageView mShuffleBtn;
    private TrackManager tm;
    private boolean animateSeekBar = false;

    private ScheduledFuture<?> mScheduleFuture;

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            updateMetaData(metadata);
            setFavoriteButtonState();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_now_playing);
        tm = TrackManager.getInstance();
        
        startTime = findViewById(R.id.activity_np_start_time);
        endTime = findViewById(R.id.activity_np_end_time);
        seekBar = findViewById(R.id.activity_np_seekBar);
        mPlayPause = findViewById(R.id.activity_np_play_pause_btn);
        artistAlbums = findViewById(R.id.activity_np_album_artist_name);
        toolbarSongTitle = findViewById(R.id.activity_np_song_title);
        mFavBtn = findViewById(R.id.activity_np_favourite_btn);
        mRepeatBtn = findViewById(R.id.activity_np_btn_repeat);
        mShuffleBtn = findViewById(R.id.activity_np_btn_shuffle);

        findViewById(R.id.activity_np_close_btn).setOnClickListener(v -> finishAfterTransition());

        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (startTime != null) startTime.setText(DateUtils.formatElapsedTime(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    stopSeekBarUpdate();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mController != null) {
                        progress = seekBar.getProgress();
                        mController.getTransportControls().seekTo(progress * 1000L);
                        if (mState != null && mState.getState() == PlaybackState.STATE_PLAYING) {
                            scheduleSeekBarUpdate();
                        }
                    }
                }
            });
        }
        initButtons();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) animateSeekBar = true;
    }

    private void initButtons() {
        View nextBtn = findViewById(R.id.activity_np_skip_next_btn);
        if (nextBtn != null) {
            nextBtn.setOnClickListener(v -> {
                if (mController != null) mController.getTransportControls().skipToNext();
            });
        }
        
        View prevBtn = findViewById(R.id.activity_np_skip_prev_btn);
        if (prevBtn != null) {
            prevBtn.setOnClickListener(v -> {
                if (mController != null) mController.getTransportControls().skipToPrevious();
            });
        }

        if (mFavBtn != null) {
            mFavBtn.setOnClickListener(v -> {
                if (isFav) removeFromFavorite();
                else addToFavorite();
            });
            setFavoriteButtonState();
        }
        
        if (mRepeatBtn != null) updateRepeatBtn();
        if (mShuffleBtn != null) updateShuffleBtn();
    }

    private void updateRepeatBtn() {
        if (mRepeatBtn == null) return;
        mRepeatBtn.setImageResource(tm.isCurrentTrackInRepeatMode() ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);

        mRepeatBtn.setOnClickListener(v -> {
            boolean b = tm.isCurrentTrackInRepeatMode();
            mRepeatBtn.setImageResource(b ? R.drawable.ic_repeat : R.drawable.ic_repeat_one);
            tm.repeatCurrentTrack(!b);
            Toast.makeText(this, b ? R.string.repeat_disabled : R.string.repeat_enabled, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateShuffleBtn() {
        if (mShuffleBtn == null) return;
        
        // Since ic_shuffle is missing, we use ic_playlist as a placeholder 
        // and use tinting to show active state
        mShuffleBtn.setImageResource(R.drawable.ic_playlist);
        
        boolean isShuffle = tm.isShuffleEnabled();
        int color = ContextCompat.getColor(this, isShuffle ? R.color.md_theme_primary : R.color.md_theme_onSurface);
        mShuffleBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        
        mShuffleBtn.setOnClickListener(v -> {
            boolean b = tm.isShuffleEnabled();
            tm.setShuffle(!b);
            int newColor = ContextCompat.getColor(this, !b ? R.color.md_theme_primary : R.color.md_theme_onSurface);
            mShuffleBtn.setColorFilter(newColor, PorterDuff.Mode.SRC_IN);
            Toast.makeText(this, !b ? R.string.shuffle_enabled : R.string.shuffle_disabled, Toast.LENGTH_SHORT).show();
        });
    }

    private void setFavoriteButtonState() {
        if (mFavBtn == null) return;
        MusicModel current = tm.getActiveQueueItem();
        if (current == null) return;

        if (null == favourites) favourites = PlaylistStorageManager.getFavorite(this);
        boolean contains = false;
        if (null != favourites) {
            for (MusicModel md : favourites) {
                if (md.getSongName() != null && md.getSongName().equals(current.getSongName())) {
                    contains = true;
                    break;
                }
            }
        }
        
        isFav = contains;
        mFavBtn.setImageResource(isFav ? R.drawable.ic_favorite_active : R.drawable.ic_favorite);
    }

    private void addToFavorite() {
        MusicModel current = tm.getActiveQueueItem();
        if (current == null) return;
        
        if (null == favourites) favourites = new ArrayList<>();
        favourites.add(current);
        isFav = true;
        mFavListModified = true;
        if (mFavBtn != null) mFavBtn.setImageResource(R.drawable.ic_favorite_active);
        Toast.makeText(this, R.string.added_to_fav, Toast.LENGTH_SHORT).show();
    }

    private void removeFromFavorite() {
        MusicModel current = tm.getActiveQueueItem();
        if (current == null) return;
        
        String title = current.getSongName();
        if (title == null || favourites == null) return;
        
        for (MusicModel md : favourites) {
            if (title.equals(md.getSongName())) {
                favourites.remove(md);
                break;
            }
        }
        isFav = false;
        mFavListModified = true;
        if (mFavBtn != null) mFavBtn.setImageResource(R.drawable.ic_favorite);
        Toast.makeText(this, R.string.removed_from_fav, Toast.LENGTH_SHORT).show();
    }

    private void updateMetaData(MediaMetadata metadata) {
        if (metadata != null) {
            stopSeekBarUpdate();
            ImageView albumArt = findViewById(R.id.activity_np_album_art);
            if (albumArt != null) {
                GlideApp.with(this)
                        .load(metadata.getBitmap(PlaybackManager.METADATA_ALBUM_ART))
                        .error(R.drawable.np_album_art)
                        .transform(new CircleCrop())
                        .into(albumArt);
            }

            long sec = metadata.getLong(PlaybackManager.METADATA_DURATION_KEY) / 1000;
            if (seekBar != null) {
                progress = 0;
                seekBar.setProgress(progress);
                seekBar.setMax((int) sec);
            }
            if (endTime != null) endTime.setText(DateUtils.formatElapsedTime(sec));

            if (artistAlbums != null) {
                artistAlbums.setText(metadata.getString(PlaybackManager.METADATA_ARTIST_KEY));
            }

            if (toolbarSongTitle != null) {
                toolbarSongTitle.setText(metadata.getText(PlaybackManager.METADATA_TITLE_KEY));
                toolbarSongTitle.setSelected(true);
            }
        }
    }

    private void updatePlaybackState(PlaybackState state) {
        if (state != null) {
            mState = state;
            switch (state.getState()) {
                case PlaybackState.STATE_PLAYING:
                    scheduleSeekBarUpdate();
                    if (mPlayPause != null) mPlayPause.setImageResource(R.drawable.ic_round_pause);
                    break;

                case PlaybackState.STATE_PAUSED:
                    stopSeekBarUpdate();
                    if (mPlayPause != null) mPlayPause.setImageResource(R.drawable.ic_round_play);
                    break;
                    
                case PlaybackState.STATE_STOPPED:
                    stopSeekBarUpdate();
                    progress = 0;
                    if (seekBar != null) seekBar.setProgress(progress);
                    if (mPlayPause != null) mPlayPause.setImageResource(R.drawable.ic_round_play);
                    break;
            }
        }
    }


    @Override
    public void onMediaServiceConnected(MediaController controller) {
        mController = controller;
        mController.registerCallback(mCallback);
        updateMetaData(mController.getMetadata());
        mState = mController.getPlaybackState();
        
        if (null != mState) {
            progress = (int) (mState.getPosition() / 1000);
            setSeekBarProgress();

            if (mPlayPause != null) {
                mPlayPause.setOnClickListener(v -> {
                    if (mState == null || mController == null) return;
                    if (mState.getState() == PlaybackState.STATE_PLAYING) {
                        mController.getTransportControls().pause();
                        Drawable d = ContextCompat.getDrawable(this, R.drawable.pause_to_play);
                        mPlayPause.setImageDrawable(d);
                        if (d instanceof AnimatedVectorDrawable) ((AnimatedVectorDrawable) d).start();
                    } else {
                        mController.getTransportControls().play();
                        Drawable d = ContextCompat.getDrawable(this, R.drawable.play_to_pause_linear_out_slow_in);
                        mPlayPause.setImageDrawable(d);
                        if (d instanceof AnimatedVectorDrawable) ((AnimatedVectorDrawable) d).start();
                    }
                });
            }
        }
        updatePlaybackState(mController.getPlaybackState());
    }

    private void scheduleSeekBarUpdate() {
        if (seekBar != null && seekBar.getProgress() == seekBar.getMax()) {
            progress = 0;
            seekBar.setProgress(progress);
        }
        if (!mExecutorService.isShutdown()) {
            if (null != mScheduleFuture) mScheduleFuture.cancel(true);
            mScheduleFuture = mExecutorService.scheduleWithFixedDelay(() -> mHandler.post(mUpdateProgressTask), 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekBarUpdate() {
        if (mScheduleFuture != null) mScheduleFuture.cancel(true);
    }

    private void updateProgress() {
        progress++;
        setSeekBarProgress();
    }

    @SuppressLint("NewApi")
    private void setSeekBarProgress() {
        if (seekBar == null) return;
        if (animateSeekBar) seekBar.setProgress(progress, true);
        else seekBar.setProgress(progress);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFavListModified) PlaylistStorageManager.saveFavorite(this, favourites);
    }

    @Override
    protected void onDestroy() {
        if (mController != null) mController.unregisterCallback(mCallback);
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) mExecutorService.shutdown();
        disconnectFromMediaSession();
        super.onDestroy();
    }
}
