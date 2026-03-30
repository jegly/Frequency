package com.tunes.player.ui;

import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tunes.player.R;
import com.tunes.player.activities.NowPlayingActivity;
import com.tunes.player.playback.PlaybackManager;

public class ControlsFragment extends Fragment {

    private TextView tv1;
    private ImageButton playPause;
    private MediaController mController;
    private MediaController.TransportControls mTransportControl;
    private MediaMetadata mMetadata;
    private PlaybackState mState;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateController();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tv1 = v.findViewById(R.id.song_name);
        if (tv1 != null) tv1.setSelected(true);
        
        playPause = v.findViewById(R.id.cf_play_pause_btn);
        ImageButton skipNext = v.findViewById(R.id.cf_skip_next_btn);
        ImageButton skipPrev = v.findViewById(R.id.cf_skip_prev_btn);

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

        v.setOnClickListener(v1 -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
                startActivity(intent);
            }
        });
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
