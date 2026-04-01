package com.tunes.player.loaders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.tunes.player.interfaces.ArtistDataFetchCompletionCallback;
import com.tunes.player.model.ArtistModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Replaced deprecated AsyncTask with ExecutorService + main-thread Handler. */
public class ArtistFetcher {

    private static final String TAG = "ArtistFetcher";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private final ArtistDataFetchCompletionCallback mCallback;
    private final ContentResolver mContentResolver;
    private final String mSort;

    public ArtistFetcher(ContentResolver resolver, ArtistDataFetchCompletionCallback callback, SORT sort) {
        this.mCallback        = callback;
        this.mContentResolver = resolver;
        if (sort == SORT.TITLE_ASC)
            mSort = MediaStore.Audio.Artists.ARTIST + " ASC";
        else if (sort == SORT.NUM_OF_TRACKS_DESC)
            mSort = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " DESC";
        else
            mSort = null;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<ArtistModel> result = fetchArtists();
            sMainHandler.post(() -> {
                if (mCallback != null) mCallback.onTaskComplete(result);
            });
        });
    }

    private List<ArtistModel> fetchArtists() {
        List<ArtistModel> data = new ArrayList<>();
        String[] col = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        };
        try (Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, col, null, null, mSort)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol         = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
                int artistCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
                int albumCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
                int trackCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
                do {
                    int    id         = cursor.getInt(idCol);
                    String artist     = cursor.getString(artistCol);
                    int    numAlbums  = cursor.getInt(albumCountCol);
                    int    numTracks  = cursor.getInt(trackCountCol);
                    data.add(new ArtistModel(id, artist, numAlbums, numTracks));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchArtists error", e);
        }
        return data;
    }

    public enum SORT { TITLE_ASC, NUM_OF_TRACKS_DESC }
}
