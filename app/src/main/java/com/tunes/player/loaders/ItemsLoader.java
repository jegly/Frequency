package com.tunes.player.loaders;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tunes.player.activities.DetailsActivity;
import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Replaced deprecated AsyncTask with ExecutorService + main-thread Handler. */
public class ItemsLoader {

    private static final String TAG = "ItemsLoader";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private final AsyncTaskCallback.Simple mCallback;
    private final String mTitle;
    private final int mChoice;

    public ItemsLoader(AsyncTaskCallback.Simple callback, String itemToSearch, int choice) {
        this.mCallback = callback;
        this.mTitle    = itemToSearch;
        this.mChoice   = choice;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<MusicModel> result = filter();
            sMainHandler.post(() -> {
                if (mCallback != null) mCallback.onTaskComplete(result);
            });
        });
    }

    private List<MusicModel> filter() {
        List<MusicModel> source = TrackManager.getInstance().getMainList();
        List<MusicModel> out = new ArrayList<>();
        if (source == null) return out;

        for (MusicModel md : source) {
            if (mChoice == DetailsActivity.CATEGORY_ALBUM) {
                if (md.getAlbum() != null && md.getAlbum().equals(mTitle)) out.add(md);
            } else if (mChoice == DetailsActivity.CATEGORY_ARTIST) {
                if (md.getArtist() != null && md.getArtist().equals(mTitle)) out.add(md);
            }
        }

        if (out.isEmpty()) Log.d(TAG, "No items found for: " + mTitle);
        return out;
    }
}
