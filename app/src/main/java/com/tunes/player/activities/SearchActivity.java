package com.tunes.player.activities;

import android.graphics.Color;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Replaced inner AsyncSearchTask with ExecutorService + main-thread Handler. */
public class SearchActivity extends MediaSessionActivity
        implements ItemClickListener.Simple, AsyncTaskCallback.Simple {

    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler  = new Handler(Looper.getMainLooper());

    private List<MusicModel> mSearchResult;
    private final List<String> pendingUpdates = new ArrayList<>();
    private SearchAdapter adapter;
    private TextView tv;
    private TrackManager tm;
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
        if (v != null) v.setBackgroundColor(Color.parseColor("#00000000"));
        sv.setIconified(false);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if (!query.equals(mQuery)) searchSuggestions(query);
                sv.clearFocus();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                searchSuggestions(newText);
                return true;
            }
        });
    }

    private void searchSuggestions(String query) {
        mQuery = query;
        pendingUpdates.add(mQuery);
        if (pendingUpdates.size() == 1) runSearch(query);
    }

    private void runSearch(String query) {
        sExecutor.execute(() -> {
            List<MusicModel> results = filter(query);
            sMainHandler.post(() -> onTaskComplete(results));
        });
    }

    @Override
    public void onTaskComplete(List<MusicModel> list) {
        if (pendingUpdates.isEmpty()) return;
        pendingUpdates.remove(0);
        mSearchResult = list;

        tv.setVisibility(mSearchResult.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateItems(list);

        if (!pendingUpdates.isEmpty()) {
            String next = pendingUpdates.get(pendingUpdates.size() - 1);
            pendingUpdates.clear();
            searchSuggestions(next);
        }
    }

    private List<MusicModel> filter(String text) {
        List<MusicModel> out = new ArrayList<>();
        List<MusicModel> source = TrackManager.getInstance().getMainList();
        if (text == null || text.isEmpty() || source == null) return out;
        String q = text.toLowerCase();
        for (MusicModel m : source) {
            String name   = m.getSongName();
            String album  = m.getAlbum();
            String artist = m.getArtist();
            if ((name   != null && name.toLowerCase().contains(q))   ||
                (album  != null && album.toLowerCase().contains(q))  ||
                (artist != null && artist.toLowerCase().contains(q))) {
                out.add(m);
            }
        }
        return out;
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
            if      (itemId == R.id.id_play_next)   tm.playNext(mSearchResult.get(position));
            else if (itemId == R.id.id_add_queue)   tm.addToActiveQueue(mSearchResult.get(position));
            else if (itemId == R.id.info)            createDialog(position);
            return true;
        });
        pm.show();
    }

    private void createDialog(int pos) {
        if (mSearchResult == null || pos < 0 || pos >= mSearchResult.size()) return;
        MusicModel md = mSearchResult.get(pos);
        String msg = getString(R.string.album_head)  + " " + md.getAlbum()  + "\n"
                   + getString(R.string.artist_head) + " " + md.getArtist();
        new AlertDialog.Builder(this)
                .setTitle(md.getSongName())
                .setMessage(msg)
                .setPositiveButton("Done", (d, w) -> d.dismiss())
                .create().show();
    }

    @Override
    public void onMediaServiceConnected(MediaController controller) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pendingUpdates.clear();
        disconnectFromMediaSession();
        if (rv != null) rv.setAdapter(null);
    }
}
