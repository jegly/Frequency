package com.tunes.player.activities;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tunes.player.GlideApp;
import com.tunes.player.R;
import com.tunes.player.model.MusicModel;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.ui.CustomBottomSheet;
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
    private ImageButton mFavBtn;
    private ImageButton mRepeatBtn;
    private ImageButton mShuffleBtn;
    private ImageButton mLoopBtn;
    private ImageButton mPlaylistAddBtn;
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
        mLoopBtn = findViewById(R.id.activity_np_btn_loop);
        mPlaylistAddBtn = findViewById(R.id.activity_np_playlist_add_btn);

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
        if (mLoopBtn != null) updateLoopBtn();
        
        if (mPlaylistAddBtn != null) {
            mPlaylistAddBtn.setOnClickListener(v -> showPlaylistSelectionDialog());
        }
    }

    private void showPlaylistSelectionDialog() {
        MusicModel currentTrack = tm.getActiveQueueItem();
        if (currentTrack == null) return;

        List<String> playlistTitles = PlaylistStorageManager.getPlaylistTitles(this);
        final List<String> customPlaylists = new ArrayList<>();
        final List<Integer> originalIndices = new ArrayList<>();
        
        for (int i = 2; i < playlistTitles.size(); i++) {
            customPlaylists.add(playlistTitles.get(i));
            originalIndices.add(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Playlist");
        
        List<String> options = new ArrayList<>(customPlaylists);
        options.add(0, "+ Create New Playlist");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.md_theme_onSurface));
                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            if (which == 0) {
                openCreatePlaylistDialog(currentTrack);
            } else {
                int playlistIndex = originalIndices.get(which - 1);
                addTrackToPlaylist(currentTrack, playlistIndex, customPlaylists.get(which - 1));
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openCreatePlaylistDialog(MusicModel trackToAdd) {
        View layout = View.inflate(this, R.layout.dialog_create_playlist, null);
        BottomSheetDialog sheetDialog = new CustomBottomSheet(this);
        sheetDialog.setContentView(layout);
        sheetDialog.show();

        TextView header = layout.findViewById(R.id.header);
        header.setText(R.string.create_playlist);

        TextInputLayout til = layout.findViewById(R.id.edit_text_container);
        til.setHint(getString(R.string.create_playlist_hint));

        TextInputEditText et = layout.findViewById(R.id.text_input_field);

        Button create = layout.findViewById(R.id.confirm_btn);
        create.setOnClickListener(v -> {
            String name = et.getText() != null ? et.getText().toString().trim() : "";
            if (!name.isEmpty()) {
                List<String> titles = PlaylistStorageManager.getPlaylistTitles(this);
                titles.add(name);
                PlaylistStorageManager.savePlaylistTitles(this, titles);
                
                // Now add the track to this new playlist
                int newPlaylistIndex = titles.size() - 1;
                addTrackToPlaylist(trackToAdd, newPlaylistIndex, name);
                
                sheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter playlist's name", Toast.LENGTH_SHORT).show();
            }
        });

        Button cancel = layout.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(v -> sheetDialog.dismiss());
    }

    private void addTrackToPlaylist(MusicModel track, int playlistIndex, String playlistName) {
        int dataIndex = playlistIndex - 2;
        List<MusicModel> tracks = PlaylistStorageManager.getPlaylistTrackAtPosition(this, dataIndex);
        
        for (MusicModel m : tracks) {
            if (m.getSongPath() != null && m.getSongPath().equals(track.getSongPath())) {
                Toast.makeText(this, "Track already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        tracks.add(track);
        PlaylistStorageManager.updatePlaylistTracks(this, tracks, dataIndex);
        Toast.makeText(this, "Added to " + playlistName, Toast.LENGTH_SHORT).show();
    }

    private void updateRepeatBtn() {
        if (mRepeatBtn == null) return;
        boolean isRepeatOnce = tm.isCurrentTrackInRepeatMode();
        mRepeatBtn.setImageResource(R.drawable.ic_repeat_one);
        
        int colorAttr = isRepeatOnce ? R.attr.focusedDrawableColor : R.attr.unfocusedDrawableColor;
        int color = getThemeColor(colorAttr);
        mRepeatBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        mRepeatBtn.setOnClickListener(v -> {
            boolean b = tm.isCurrentTrackInRepeatMode();
            tm.repeatCurrentTrack(!b);
            updateRepeatBtn();
            Toast.makeText(this, !b ? R.string.repeat_enabled : R.string.repeat_disabled, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLoopBtn() {
        if (mLoopBtn == null) return;
        boolean isLoop = tm.isInfiniteLoopEnabled();
        mLoopBtn.setImageResource(R.drawable.ic_repeat);
        
        int colorAttr = isLoop ? R.attr.focusedDrawableColor : R.attr.unfocusedDrawableColor;
        int color = getThemeColor(colorAttr);
        mLoopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        mLoopBtn.setOnClickListener(v -> {
            boolean b = tm.isInfiniteLoopEnabled();
            tm.setInfiniteLoop(!b);
            updateLoopBtn();
            Toast.makeText(this, !b ? R.string.loop_enabled : R.string.loop_disabled, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateShuffleBtn() {
        if (mShuffleBtn == null) return;
        mShuffleBtn.setImageResource(R.drawable.ic_shuffle);
        
        boolean isShuffle = tm.isShuffleEnabled();
        int colorAttr = isShuffle ? R.attr.focusedDrawableColor : R.attr.unfocusedDrawableColor;
        int color = getThemeColor(colorAttr);
        mShuffleBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        
        mShuffleBtn.setOnClickListener(v -> {
            boolean b = tm.isShuffleEnabled();
            tm.setShuffle(!b);
            updateShuffleBtn();
            Toast.makeText(this, !b ? R.string.shuffle_enabled : R.string.shuffle_disabled, Toast.LENGTH_SHORT).show();
        });
    }

    private int getThemeColor(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
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
