package com.tunes.player.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tunes.player.R;
import com.tunes.player.activities.NowPlayingActivity;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.singleton.TrackManager;

public class ControlsFragment extends Fragment {

    private TextView tv1;
    private ImageButton playPause;
    private ImageButton shuffleBtn;
    private MediaController mController;
    private MediaController.TransportControls mTransportControl;
    private MediaMetadata mMetadata;
    private PlaybackState mState;
    private TrackManager tm;

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            mState = state;
            updateControls();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            mMetadata = metadata;
            updateMetadata();
        }
    };

    public ControlsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tm = TrackManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateController();
        updateShuffleBtn();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tv1 = v.findViewById(R.id.song_name);
        if (tv1 != null) tv1.setSelected(true);
        
        playPause = v.findViewById(R.id.cf_play_pause_btn);
        ImageButton skipNext = v.findViewById(R.id.cf_skip_next_btn);
        ImageButton skipPrev = v.findViewById(R.id.cf_skip_prev_btn);
        shuffleBtn = v.findViewById(R.id.cf_shuffle_btn);

        if (playPause != null) {
            playPause.setOnClickListener(v1 -> {
                if (mTransportControl != null && mState != null) {
                    if (mState.getState() == PlaybackState.STATE_PLAYING)
                        mTransportControl.pause();
                    else
                        mTransportControl.play();
                }
            });
        }

        if (skipNext != null) {
            skipNext.setOnClickListener(v1 -> {
                if (mTransportControl != null) mTransportControl.skipToNext();
            });
        }

        if (skipPrev != null) {
            skipPrev.setOnClickListener(v1 -> {
                if (mTransportControl != null) mTransportControl.skipToPrevious();
            });
        }

        if (shuffleBtn != null) {
            shuffleBtn.setOnClickListener(v1 -> {
                boolean b = tm.isShuffleEnabled();
                tm.setShuffle(!b);
                updateShuffleBtn();
                Toast.makeText(getContext(), !b ? R.string.shuffle_enabled : R.string.shuffle_disabled, Toast.LENGTH_SHORT).show();
            });
        }

        v.setOnClickListener(v1 -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateShuffleBtn() {
        if (shuffleBtn == null || tm == null) return;
        boolean isShuffle = tm.isShuffleEnabled();
        int colorAttr = isShuffle ? R.attr.focusedDrawableColor : R.attr.unfocusedDrawableColor;
        int color = getThemeColor(colorAttr);
        shuffleBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private int getThemeColor(int attr) {
        if (getContext() == null) return 0;
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private void updateControls() {
        if (playPause == null || mState == null)
            return;
        if (mState.getState() == PlaybackState.STATE_PLAYING)
            playPause.setImageResource(R.drawable.ic_round_pause);
        else
            playPause.setImageResource(R.drawable.ic_round_play);
    }

    private void updateMetadata() {
        if (mMetadata != null && tv1 != null) {
            tv1.setText(mMetadata.getText(PlaybackManager.METADATA_TITLE_KEY));
        }
    }

    private void updateController() {
        if (mController == null && getActivity() != null)
            mController = getActivity().getMediaController();
        if (mController != null) {
            mController.registerCallback(mCallback);
            mMetadata = mController.getMetadata();
            mState = mController.getPlaybackState();
            mTransportControl = mController.getTransportControls();
            updateMetadata();
            updateControls();
        } else Log.e("ControlsFragment", "controller is null");
    }

    @Override
    public void onStop() {
        if (mController != null)
            mController.unregisterCallback(mCallback);
        super.onStop();
    }
}
