package com.tunes.player.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaController;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.tunes.player.R;
import com.tunes.player.activities.MainActivity;
import com.tunes.player.activities.SearchActivity;
import com.tunes.player.activities.SettingsActivity;
import com.tunes.player.helper.MediaHelper;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PICK_MUSIC = 1600;
    private TrackManager tm;
    private MediaController.TransportControls mTransportControl;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.home);
            toolbar.setNavigationIcon(R.drawable.ic_settings);

            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                if (getContext() != null) {
                    startActivity(new Intent(getContext(), SettingsActivity.class));
                }
            });
        }

        startPostponedEnterTransition();

        View folderBtn = view.findViewById(R.id.ic_folder);
        if (folderBtn != null) {
            folderBtn.setOnClickListener(v -> pickMedia());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search && getContext() != null)
            startActivity(new Intent(getContext(), SearchActivity.class));
        return true;
    }

    private void pickMedia(){
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, PICK_MUSIC);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && getContext() != null) {
            if (requestCode == PICK_MUSIC) {
                MusicModel md = MediaHelper.buildMusicModelFrom(getContext(), data);
                if(md != null){
                    List<MusicModel> singlePickedItemList = new ArrayList<>();
                    singlePickedItemList.add(md);
                    tm.buildDataList(singlePickedItemList, 0);
                    play();
                }
            }
        }
    }

    private void play() {
        if (mTransportControl != null) {
            mTransportControl.play();
        } else if (getActivity() != null) {
            MediaController controller = getActivity().getMediaController();
            if (controller != null) {
                mTransportControl = controller.getTransportControls();
                if (mTransportControl != null) mTransportControl.play();
            }
        }
    }
}
