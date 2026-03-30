package com.tunes.player.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.tunes.player.model.MusicModel;

import java.util.ArrayList;
import java.util.List;

public class TrackFetcherFromStorage extends AsyncTask<Void, Void, List<MusicModel>> {

    public enum Sort {
        TITLE_ASC,
        DATE_ADDED_DESC
    }

    public interface TaskDelegate {
        void onTaskCompleted(List<MusicModel> list);
    }

    private final ContentResolver mContentResolver;
    private final TaskDelegate mDelegate;
    private final Sort mSort;

    public TrackFetcherFromStorage(ContentResolver resolver, TaskDelegate delegate, Sort sort) {
        this.mContentResolver = resolver;
        this.mDelegate = delegate;
        this.mSort = sort;
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        List<MusicModel> list = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND "
                + MediaStore.Audio.Media.DURATION + " > 30000";

        String sortOrder;
        if (mSort == Sort.DATE_ADDED_DESC) {
            sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        } else {
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        }

        Cursor cursor = mContentResolver.query(uri, projection, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int idCol       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int dataCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int albumIdCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int durCol      = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int dateCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);

            do {
                int id          = cursor.getInt(idCol);
                String title    = cursor.getString(titleCol);
                String artist   = cursor.getString(artistCol);
                String album    = cursor.getString(albumCol);
                String path     = cursor.getString(dataCol);
                long albumId    = cursor.getLong(albumIdCol);
                long duration   = cursor.getLong(durCol);
                long dateAdded  = cursor.getLong(dateCol);

                String artUri = "content://media/external/audio/albumart/" + albumId;

                list.add(new MusicModel(id, title, artist, album, path, artUri, duration, dateAdded));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    @Override
    protected void onPostExecute(List<MusicModel> list) {
        if (mDelegate != null) {
            mDelegate.onTaskCompleted(list);
        }
    }
}
