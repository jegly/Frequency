package com.tunes.player.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.tunes.player.interfaces.AlbumDataFetchCompletionCallback;
import com.tunes.player.model.AlbumModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Replaced deprecated AsyncTask with ExecutorService + main-thread Handler. */
public class AlbumFetcher {

    private static final String TAG = "AlbumFetcher";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private final AlbumDataFetchCompletionCallback mCallback;
    private final ContentResolver mContentResolver;
    private final String mSort;

    public AlbumFetcher(ContentResolver resolver, AlbumDataFetchCompletionCallback callback, SORT sort) {
        this.mCallback        = callback;
        this.mContentResolver = resolver;
        if (sort == SORT.TITLE_ASC)
            mSort = MediaStore.Audio.Albums.ALBUM + " ASC";
        else if (sort == SORT.DATE_ASC)
            mSort = MediaStore.Audio.Albums.FIRST_YEAR + " ASC";
        else
            mSort = null;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<AlbumModel> result = fetchAlbums();
            sMainHandler.post(() -> {
                if (mCallback != null) mCallback.onTaskComplete(result);
            });
        });
    }

    private List<AlbumModel> fetchAlbums() {
        List<AlbumModel> data = new ArrayList<>();
        String[] col = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };
        try (Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, col, null, null, mSort)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol        = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
                int albumCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
                int songCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
                final Uri artUri = Uri.parse("content://media/external/audio/albumart");
                do {
                    int    id       = cursor.getInt(idCol);
                    String album    = cursor.getString(albumCol);
                    String albumArt = ContentUris.withAppendedId(artUri, id).toString();
                    int    num      = cursor.getInt(songCountCol);
                    data.add(new AlbumModel(id, album, num, albumArt));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchAlbums error", e);
        }
        return data;
    }

    public enum SORT { TITLE_ASC, DATE_ASC }
}
