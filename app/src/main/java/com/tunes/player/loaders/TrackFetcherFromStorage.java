package com.tunes.player.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.tunes.player.model.MusicModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fetches audio tracks from MediaStore.
 * Replaced deprecated AsyncTask with ExecutorService + main-thread Handler.
 * Uses content URIs (not deprecated DATA column) for media access.
 */
public class TrackFetcherFromStorage {

    private static final String TAG = "TrackFetcherFromStorage";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public interface TaskDelegate {
        void onTaskCompleted(List<MusicModel> list);
    }

    public enum Sort {
        TITLE_ASC,
        DATE_ADDED_DESC
    }

    private final ContentResolver mContentResolver;
    private final TaskDelegate mDelegate;
    private final Sort mSort;

    public TrackFetcherFromStorage(ContentResolver resolver, TaskDelegate delegate, Sort sort) {
        this.mContentResolver = resolver;
        this.mDelegate        = delegate;
        this.mSort            = sort;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<MusicModel> result = fetchTracks();
            sMainHandler.post(() -> {
                if (mDelegate != null) mDelegate.onTaskCompleted(result);
            });
        });
    }

    private List<MusicModel> fetchTracks() {
        List<MusicModel> list = new ArrayList<>();

        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.ARTIST,
                MediaStore.Files.FileColumns.ALBUM,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Filter to audio MIME types and common extensions only.
        String selection =
                MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'audio/%' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.mp3' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.m4a' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.flac' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.wav' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.ogg' OR " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%.aac'";

        String sortOrder = (mSort == Sort.DATE_ADDED_DESC)
                ? MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                : MediaStore.Files.FileColumns.TITLE + " ASC";

        try (Cursor cursor = mContentResolver.query(uri, projection, selection, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol      = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int titleCol   = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int artistCol  = cursor.getColumnIndex(MediaStore.Files.FileColumns.ARTIST);
                int albumCol   = cursor.getColumnIndex(MediaStore.Files.FileColumns.ALBUM);
                int durCol     = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION);
                int dateCol    = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
                int nameCol    = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

                do {
                    long id = cursor.getLong(idCol);

                    // Build a content URI — avoids the deprecated DATA/file-path column.
                    String contentUri = ContentUris
                            .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                            .toString();

                    String title = cursor.getString(titleCol);
                    if (title == null || title.isEmpty()) title = cursor.getString(nameCol);
                    if (title == null || title.isEmpty()) title = "Unknown";

                    String artist = (artistCol != -1) ? cursor.getString(artistCol) : null;
                    if (artist == null || artist.isEmpty() || artist.equals("<unknown>"))
                        artist = "Unknown Artist";

                    String album = (albumCol != -1) ? cursor.getString(albumCol) : null;
                    if (album == null || album.isEmpty() || album.equals("<unknown>"))
                        album = "Unknown Album";

                    long duration  = cursor.getLong(durCol);
                    long dateAdded = cursor.getLong(dateCol);

                    list.add(new MusicModel((int) id, title, artist, album,
                            contentUri, null, duration, dateAdded));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchTracks error", e);
        }

        return list;
    }
}
