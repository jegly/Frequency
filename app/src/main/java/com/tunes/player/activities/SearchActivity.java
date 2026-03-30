package com.tunes.player.activities;

import android.graphics.Color;
import android.media.session.MediaController;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunes.player.R;
import com.tunes.player.adapters.SearchAdapter;
import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.interfaces.ItemClickListener;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends MediaSessionActivity implements ItemClickListener.Simple, AsyncTaskCallback.Simple {

    private List<MusicModel> mSearchResult;
    private final List<String> pendingUpdates = new ArrayList<>();
    private SearchAdapter adapter;
    private TextView tv;
    private TrackManager tm;
    private final Handler mHandler = new Handler();
    private String mQuery = "";
    private RecyclerView rv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViewById(R.id.search_activity_close_btn).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        });

        tv = findViewById(R.id.result_empty);
        setUpSearchView();
        setRecyclerView();
        tm = TrackManager.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    private void setUpSearchView() {
        SearchView sv = findViewById(R.id.search_view);
        View v = sv.findViewById(androidx.appcompat.R.id.search_plate);
        if (v != null) {
            v.setBackgroundColor(Color.parseColor("#00000000"));
        }
        sv.setIconified(false);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(mQuery))
                    searchSuggestions(query);
                sv.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSuggestions(newText);
                return true;
            }
        });
    }

    private void searchSuggestions(String query) {
        mQuery = query;
        pendingUpdates.add(mQuery);
        if (pendingUpdates.size() == 1)
            new AsyncSearchTask(query, this).execute();
    }

    @Override
    public void onTaskComplete(List<MusicModel> list) {
        if (pendingUpdates.isEmpty()) return;
        pendingUpdates.remove(0);
        this.mSearchResult = list;

        if (mSearchResult.isEmpty()) tv.setVisibility(View.VISIBLE);
        else tv.setVisibility(View.GONE);

        adapter.updateItems(list);

        if (!pendingUpdates.isEmpty()) {
            searchSuggestions(pendingUpdates.get(pendingUpdates.size() - 1));
            pendingUpdates.clear();
        }
    }

    private void setRecyclerView() {
        rv = findViewById(R.id.search_rv);
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv.setItemAnimator(new DefaultItemAnimator());
        adapter = new SearchAdapter(getLayoutInflater(), this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        if (mSearchResult != null && pos >= 0 && pos < mSearchResult.size()) {
            tm.buildDataList(mSearchResult, pos);
            playMedia();
        }
    }

    @Override
    public void onOptionsClick(View v, int position) {
        if (mSearchResult == null || position < 0 || position >= mSearchResult.size()) return;
        PopupMenu pm = new PopupMenu(this, v);
        pm.getMenuInflater().inflate(R.menu.item_overflow__menu, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.id_play_next) {
                tm.playNext(mSearchResult.get(position));
            } else if (itemId == R.id.id_add_queue) {
                tm.addToActiveQueue(mSearchResult.get(position));
            } else if (itemId == R.id.id_add_playlist) {
                // Add to playlist logic
            } else if (itemId == R.id.info) {
                createDialog(position);
            }
            return true;
        });
        pm.show();
    }

    private void createDialog(int pos) {
        if (mSearchResult == null || pos < 0 || pos >= mSearchResult.size()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        MusicModel md = mSearchResult.get(pos);
        builder.setTitle(md.getSongName());
        String s = getString(R.string.album_head) + " " + md.getAlbum() + "\n" + getString(R.string.artist_head) + " " + md.getArtist();
        builder.setMessage(s);
        builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onMediaServiceConnected(MediaController controller) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pendingUpdates.clear();
        disconnectFromMediaSession();
        if (rv != null) rv.setAdapter(null);
    }

    static class AsyncSearchTask extends AsyncTask<Void, Void, List<MusicModel>> {

        private final List<MusicModel> listToReturn = new ArrayList<>();
        private final AsyncTaskCallback.Simple mCallback;
        private String mText;

        AsyncSearchTask(String q, AsyncTaskCallback.Simple callback) {
            mText = q;
            this.mCallback = callback;
        }

        private List<MusicModel> filter() {
            List<MusicModel> listToWorkOn = TrackManager.getInstance().getMainList();
            listToReturn.clear();
            if (mText != null && !mText.isEmpty() && null != listToWorkOn) {
                mText = mText.toLowerCase();
                for (MusicModel musicModel : listToWorkOn) {
                    String name = musicModel.getSongName();
                    String album = musicModel.getAlbum();
                    String artist = musicModel.getArtist();
                    if ((name != null && name.toLowerCase().contains(mText)) ||
                            (album != null && album.toLowerCase().contains(mText)) ||
                            (artist != null && artist.toLowerCase().contains(mText))) {
                        listToReturn.add(musicModel);
                    }
                }
            }
            return listToReturn;
        }

        @Override
        protected List<MusicModel> doInBackground(Void... voids) {
            return filter();
        }

        @Override
        protected void onPostExecute(List<MusicModel> musicModels) {
            if (mCallback != null) mCallback.onTaskComplete(musicModels);
        }
    }
}
