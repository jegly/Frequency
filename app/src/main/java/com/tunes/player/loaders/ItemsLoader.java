package com.tunes.player.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.tunes.player.activities.DetailsActivity;
import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;

public class ItemsLoader extends AsyncTask<Void, Void, List<MusicModel>> {

    private AsyncTaskCallback.Simple mCallback;
    private String title;
    private int choice;


    public ItemsLoader(AsyncTaskCallback.Simple mCallback, String itemToSearch, int choice) {
        this.mCallback = mCallback;
        this.title = itemToSearch;
        this.choice = choice;
    }

    @Override
    protected void onPostExecute(List<MusicModel> itemsModels) {
        mCallback.onTaskComplete(itemsModels);
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        List<MusicModel> listToWorkOn = TrackManager.getInstance().getMainList();
        List<MusicModel> listToReturn = null;
        if (null != listToWorkOn) {
            listToReturn = new ArrayList<>();
            if (choice == DetailsActivity.CATEGORY_ALBUM) {
                for (MusicModel md : listToWorkOn) {
                    if (md.getAlbum().contains(title) || md.getAlbum().equals(title))
                        listToReturn.add(md);
                }
            } else if (choice == DetailsActivity.CATEGORY_ARTIST) {
                for (MusicModel md : listToWorkOn)
                    if (/*md.getArtist().contains(title) ||*/ md.getArtist().equals(title))
                        listToReturn.add(md);

            }
            if (listToReturn.size() == 0)
                Log.e("ItemsLoader", "Zero item found");
        }
        return listToReturn;
    }
}
