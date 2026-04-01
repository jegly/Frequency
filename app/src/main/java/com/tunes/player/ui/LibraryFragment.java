package com.tunes.player.ui;

import android.content.Intent;
import android.media.session.MediaController;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tunes.player.R;
import com.tunes.player.activities.MainActivity;
import com.tunes.player.activities.SearchActivity;
import com.tunes.player.activities.SettingsActivity;
import com.tunes.player.activities.TrackPickerActivity;
import com.tunes.player.adapters.LibraryAdapter;
import com.tunes.player.interfaces.LibraryItemClickListener;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.utils.PlaylistStorageManager;

import java.util.List;

public class LibraryFragment extends Fragment implements LibraryItemClickListener {

    private TrackManager tm;
    private List<MusicModel> mList = null;
    private MediaController.TransportControls mTransportControl;
    private TextView mTotalSongsTv;

    public LibraryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);

        if (null != getActivity())
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        mTotalSongsTv = view.findViewById(R.id.tv_total_songs);

        startPostponedEnterTransition();
        setRv(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_library_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_action_search) {
            startActivity(new Intent(getContext(), SearchActivity.class));
        } else if (itemId == R.id.menu_action_setting) {
            startActivity(new Intent(getContext(), SettingsActivity.class));
        }
        return true;
    }

    private void setRv(final View v) {
        mList = tm.getMainList();
        if (null != mList) {
            RecyclerView recyclerView = v.findViewById(R.id.rv_library_fragment);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.VERTICAL, false));
            recyclerView.setHasFixedSize(true);
            LibraryAdapter adapter = new LibraryAdapter(mList, getLayoutInflater(), this);
            recyclerView.setAdapter(adapter);

            if (mTotalSongsTv != null) {
                String countText = mList.size() + " songs";
                mTotalSongsTv.setText(countText);
            }
        } else {
            if (mTotalSongsTv != null) {
                mTotalSongsTv.setText("0 songs");
            }
        }
    }

    @Override
    public void onItemClick(int pos) {
        tm.buildDataList(mList, pos);
        play();
    }

    @Override
    public void onOptionsClick(int pos) {
        showMenuItems(mList.get(pos));
    }

    private void play() {
        if (null == mTransportControl && null != getActivity()) {
            mTransportControl = getActivity().getMediaController().getTransportControls();
            if (mTransportControl != null) mTransportControl.play();
        } else if (mTransportControl != null)
            mTransportControl.play();
    }

    private void showMenuItems(MusicModel md) {
        View view = View.inflate(getContext(), R.layout.library_item_menu, null);
        BottomSheetDialog bottomSheetDialog = new CustomBottomSheet(view.getContext());

        view.findViewById(R.id.track_play_next)
                .setOnClickListener(v -> {
                    tm.playNext(md);
                    Toast.makeText(v.getContext(), getString(R.string.play_next_toast), Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
                });

        view.findViewById(R.id.add_to_queue)
                .setOnClickListener(v -> {
                    tm.addToActiveQueue(md);
                    Toast.makeText(v.getContext(), getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
                });
        
        TextView addToPlaylist = view.findViewById(R.id.add_to_playlist_btn);
        if (addToPlaylist != null) {
            addToPlaylist.setVisibility(View.VISIBLE);
            addToPlaylist.setOnClickListener(v -> {
                showPlaylistSelector(md);
                if (bottomSheetDialog.isShowing()) bottomSheetDialog.dismiss();
            });
        }

        TextView infoBtn = view.findViewById(R.id.track_info_btn);
        if (infoBtn != null) {
            infoBtn.setVisibility(View.VISIBLE);
            infoBtn.setOnClickListener(v -> {
                String info = "Title: " + md.getSongName() + "\nArtist: " + md.getArtist() + "\nAlbum: " + md.getAlbum();
                Toast.makeText(getContext(), info, Toast.LENGTH_LONG).show();
                if (bottomSheetDialog.isShowing()) bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void showPlaylistSelector(MusicModel md) {
        if (getContext() == null) return;
        List<String> titles = PlaylistStorageManager.getPlaylistTitles(getContext());
        // Remove "Playing queue" from the list of target playlists
        if (!titles.isEmpty()) titles.remove(0);

        CharSequence[] items = titles.toArray(new CharSequence[0]);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Select Playlist");
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) { // Favorite
                List<MusicModel> favorites = PlaylistStorageManager.getFavorite(getContext());
                favorites.add(md);
                PlaylistStorageManager.saveFavorite(getContext(), favorites);
            } else { // Custom playlist
                List<MusicModel> playlistTracks = PlaylistStorageManager.getPlaylistTrackAtPosition(getContext(), which - 1);
                playlistTracks.add(md);
                PlaylistStorageManager.updatePlaylistTracks(getContext(), playlistTracks, which - 1);
            }
            Toast.makeText(getContext(), "Added to playlist", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}
