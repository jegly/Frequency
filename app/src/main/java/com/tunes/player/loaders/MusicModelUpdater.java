package com.tunes.player.loaders;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Synchronises a saved track list against the current library.
 * Replaced AsyncTask with ExecutorService + main-thread Handler.
 * Track matching now uses songPath (falling back to title) to avoid
 * false collisions when multiple files share the same display name.
 */
public class MusicModelUpdater {

    private static final String TAG = "MusicModelUpdater";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private final List<MusicModel> mOldList;
    private final AsyncTaskCallback.UpdateCompletion mCallback;

    public MusicModelUpdater(List<MusicModel> oldList, AsyncTaskCallback.UpdateCompletion callback) {
        mOldList  = oldList != null ? new ArrayList<>(oldList) : null;
        mCallback = callback;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<MusicModel> result = doInBackground();
            sMainHandler.post(() -> {
                if (mCallback != null) mCallback.onUpdateComplete(result, result != mOldList);
            });
        });
    }

    private List<MusicModel> doInBackground() {
        if (mOldList == null) return new ArrayList<>();

        List<MusicModel> master = TrackManager.getInstance().getMainList();
        boolean modified = false;

        if (master == null || master.isEmpty()) {
            mOldList.clear();
            return mOldList;
        }

        Iterator<MusicModel> it = mOldList.iterator();
        while (it.hasNext()) {
            MusicModel old = it.next();
            MusicModel match = findInMaster(master, old);
            if (match == null) {
                it.remove();
                modified = true;
            } else {
                // Update path/art if the file moved or was re-scanned.
                if (match.getSongPath() != null && !match.getSongPath().equals(old.getSongPath())) {
                    old.setSongPath(match.getSongPath());
                    old.setAlbumArtUrl(match.getAlbumArtUrl());
                    modified = true;
                }
            }
        }

        if (!modified) Log.d(TAG, "Library sync: no changes");
        return mOldList;
    }

    /** Match first by path (unique), then fall back to title+artist pair. */
    private MusicModel findInMaster(List<MusicModel> master, MusicModel target) {
        String targetPath = target.getSongPath();
        // Primary: match by file path (most reliable).
        if (targetPath != null && !targetPath.isEmpty()) {
            for (MusicModel m : master) {
                if (targetPath.equals(m.getSongPath())) return m;
            }
        }
        // Fallback: match by title + artist to handle moved files.
        String targetTitle  = target.getSongName();
        String targetArtist = target.getArtist();
        for (MusicModel m : master) {
            if (targetTitle  != null && targetTitle.equals(m.getSongName()) &&
                targetArtist != null && targetArtist.equals(m.getArtist())) {
                return m;
            }
        }
        return null;
    }
}
