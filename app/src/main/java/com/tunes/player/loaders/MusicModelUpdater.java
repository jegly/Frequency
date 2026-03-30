package com.tunes.player.loaders;

import android.os.AsyncTask;

import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MusicModelUpdater extends AsyncTask<Void, Void, List<MusicModel>> {

    private List<MusicModel> mOldList;
    private final AsyncTaskCallback.UpdateCompletion mCallback;
    private boolean modified = false;

    public MusicModelUpdater(List<MusicModel> oldList, AsyncTaskCallback.UpdateCompletion callback) {
        // Create a copy to avoid ConcurrentModificationException if the original list is being modified elsewhere
        mOldList = oldList != null ? new ArrayList<>(oldList) : null;
        mCallback = callback;
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        List<MusicModel> master = TrackManager.getInstance().getMainList();
        if (master != null && !master.isEmpty() && mOldList != null) {
            Iterator<MusicModel> iterator = mOldList.iterator();
            while (iterator.hasNext()) {
                MusicModel oldMd = iterator.next();
                String title = oldMd.getSongName();
                boolean found = false;

                for (MusicModel masterMd : master) {
                    if (masterMd.getSongName() != null && masterMd.getSongName().equals(title)) {
                        found = true;
                        // Update fields if they have changed (e.g., file moved or re-scanned)
                        if (masterMd.getSongPath() != null && !masterMd.getSongPath().equals(oldMd.getSongPath())) {
                            oldMd.setSongPath(masterMd.getSongPath());
                            oldMd.setAlbumArtUrl(masterMd.getAlbumArtUrl());
                            modified = true;
                        }
                        break;
                    }
                }

                if (!found) {
                    iterator.remove();
                    modified = true;
                }
            }
        } else if (mOldList != null) {
            // If master list is empty, then all old tracks are technically "gone" from the library view
            mOldList.clear();
            modified = true;
        }
        return mOldList;
    }

    @Override
    protected void onPostExecute(List<MusicModel> musicModels) {
        if (mCallback != null) {
            mCallback.onUpdateComplete(musicModels, modified);
        }
    }
}
