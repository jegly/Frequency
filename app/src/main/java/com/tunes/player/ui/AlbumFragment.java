package com.tunes.player.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunes.player.R;
import com.tunes.player.activities.DetailsActivity;
import com.tunes.player.activities.MainActivity;
import com.tunes.player.activities.SearchActivity;
import com.tunes.player.activities.SettingsActivity;
import com.tunes.player.adapters.AlbumsAdapter;
import com.tunes.player.interfaces.ItemClickListener;
import com.tunes.player.model.AlbumModel;
import com.tunes.player.loaders.AlbumFetcher;
import com.tunes.player.utils.AppSettings;

import java.util.List;

public class AlbumFragment extends Fragment implements ItemClickListener.SingleEvent {

    private List<AlbumModel> mList;
    private AlbumsAdapter adapter;
    private int spanCount;
    private int currentConfig;
    private GridLayoutManager layoutManager;
    private enum ID {
        PORTRAIT,
        LANDSCAPE
    }

    public AlbumFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        setHasOptionsMenu(true);
        currentConfig = getResources().getConfiguration().orientation;
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar t = view.findViewById(R.id.toolbar);
        t.setTitle(R.string.albums);
        if (getActivity() != null)
            ((MainActivity) getActivity()).setSupportActionBar(t);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            spanCount = AppSettings.getLandscapeGridSpanCount(getContext());
        else
            spanCount = AppSettings.getPortraitGridSpanCount(getContext());

        new Handler().postDelayed(() -> setRv(view), 310);
        startPostponedEnterTransition();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_album_artist_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_action_search) {
            startActivity(new Intent(getContext(), SearchActivity.class));
        } else if (itemId == R.id.menu_action_setting) {
            startActivity(new Intent(getContext(), SettingsActivity.class));
        } else if (itemId == R.id.two) {
            updateGridSize(ID.PORTRAIT, 2);
        } else if (itemId == R.id.three) {
            updateGridSize(ID.PORTRAIT, 3);
        } else if (itemId == R.id.four) {
            updateGridSize(ID.PORTRAIT, 4);
        } else if (itemId == R.id.l_four) {
            updateGridSize(ID.LANDSCAPE, 4);
        } else if (itemId == R.id.l_five) {
            updateGridSize(ID.LANDSCAPE, 5);
        } else if (itemId == R.id.l_six) {
            updateGridSize(ID.LANDSCAPE, 6);
        }
        return true;
    }

    private void updateGridSize(ID id, int spanCount){
        if(id == ID.PORTRAIT) {
           AppSettings.savePortraitGridSpanCount(getContext(), spanCount);
            if(currentConfig == Configuration.ORIENTATION_PORTRAIT)
                layoutManager.setSpanCount(spanCount);
        }
        else {
            AppSettings.saveLandscapeGridSpanCount(getContext(), spanCount);
            if(currentConfig == Configuration.ORIENTATION_LANDSCAPE)
                layoutManager.setSpanCount(spanCount);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentConfig = Configuration.ORIENTATION_LANDSCAPE;
            spanCount = AppSettings.getLandscapeGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentConfig = Configuration.ORIENTATION_PORTRAIT;
            spanCount = AppSettings.getPortraitGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        }
    }

    private void setRv(View view) {
        if (null != getActivity()) new AlbumFetcher(getActivity().getContentResolver(), list -> {
            if (null != list && list.size() > 0) {
                mList = list;
                RecyclerView rv = view.findViewById(R.id.rv_album_fragment);
                rv.setVisibility(View.VISIBLE);
                layoutManager = new GridLayoutManager(rv.getContext(), spanCount);
                rv.setLayoutManager(layoutManager);
                rv.setHasFixedSize(true);
                adapter = new AlbumsAdapter(list, getLayoutInflater(), this);
                rv.setAdapter(adapter);
            }
        }, AlbumFetcher.SORT.TITLE_ASC).execute();
    }

    @Override
    public void onClickItem(int pos) {
        Intent i = new Intent(getContext(), DetailsActivity.class);
        i.putExtra(DetailsActivity.KEY_ART_URL, mList.get(pos).getAlbumArt());
        i.putExtra(DetailsActivity.KEY_TITLE, mList.get(pos).getAlbumName());
        i.putExtra(DetailsActivity.KEY_ITEM_CATEGORY, DetailsActivity.CATEGORY_ALBUM);
        startActivity(i);
    }
}
