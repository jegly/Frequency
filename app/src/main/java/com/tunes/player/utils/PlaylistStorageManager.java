package com.tunes.player.utils;

import android.content.Context;
import android.util.Log;

import com.tunes.player.R;
import com.tunes.player.model.MusicModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores playlist, history, favourites and imported-track data as JSON inside
 * the app's private files directory.  JSON replaces the previous Java-serialization
 * approach (which had no type-filter and could deserialise unexpected classes).
 */
public class PlaylistStorageManager {

    private static final String TAG = "PlaylistStorageManager";

    private static final String FOLDER_HISTORY       = "history";
    private static final String PLAYLIST_DATA_FILE   = "Playlist_Tracks_Data.json";
    private static final String PLAYLIST_TITLES_FILE = "Playlist_Names.json";
    private static final String FAVORITE_TRACKS_FILE = "FavoriteTracks.json";
    private static final String IMPORTED_TRACKS_FILE = "ImportedTracks.json";

    private PlaylistStorageManager() {}

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    public static void addToRecentTracks(Context context, MusicModel md) {
        new Thread(() -> {
            File dir = new File(context.getFilesDir(), FOLDER_HISTORY);
            if (!dir.exists()) dir.mkdirs();
            String safeName = md.getSongName()
                    .replaceAll("[\\\\/:*?\"<>|]", "_") + ".json";
            File f = new File(dir, safeName);
            try {
                writeJson(f, musicModelToJson(md).toString());
            } catch (JSONException | IOException e) {
                Log.e(TAG, "addToRecentTracks failed", e);
            }
        }).start();
    }

    public static List<MusicModel> getRecentTracks(Context context) {
        File dir = new File(context.getFilesDir(), FOLDER_HISTORY);
        File[] files = dir.listFiles();
        List<MusicModel> list = new ArrayList<>();
        if (files == null) return list;

        Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        int count = 0;
        for (File f : files) {
            if (count >= 50) break;
            try {
                String json = readFile(f);
                if (json != null) {
                    list.add(musicModelFromJson(new JSONObject(json)));
                    count++;
                }
            } catch (JSONException | IOException e) {
                Log.e(TAG, "getRecentTracks error reading " + f.getName(), e);
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Favourites
    // -------------------------------------------------------------------------

    public static void saveFavorite(Context context, List<MusicModel> list) {
        saveTrackList(context, FAVORITE_TRACKS_FILE, list);
    }

    public static List<MusicModel> getFavorite(Context context) {
        return loadTrackList(context, FAVORITE_TRACKS_FILE);
    }

    // -------------------------------------------------------------------------
    // Imported tracks
    // -------------------------------------------------------------------------

    public static void saveImportedTracks(Context context, List<MusicModel> list) {
        new Thread(() -> {
            synchronized (IMPORTED_TRACKS_FILE) {
                List<MusicModel> current = loadTrackList(context, IMPORTED_TRACKS_FILE);
                boolean modified = false;
                for (MusicModel m : list) {
                    boolean exists = false;
                    for (MusicModel c : current) {
                        if (c.getSongPath() != null && c.getSongPath().equals(m.getSongPath())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        current.add(m);
                        modified = true;
                    }
                }
                if (modified) saveTrackListSync(context, IMPORTED_TRACKS_FILE, current);
            }
        }).start();
    }

    public static List<MusicModel> getImportedTracks(Context context) {
        return loadTrackList(context, IMPORTED_TRACKS_FILE);
    }

    // -------------------------------------------------------------------------
    // Playlist titles
    // -------------------------------------------------------------------------

    public static void savePlaylistTitles(Context context, List<String> titles) {
        new Thread(() -> {
            JSONArray arr = new JSONArray();
            for (String t : titles) arr.put(t);
            File f = new File(context.getFilesDir(), PLAYLIST_TITLES_FILE);
            try {
                writeJson(f, arr.toString());
            } catch (IOException e) {
                Log.e(TAG, "savePlaylistTitles failed", e);
            }
        }).start();
    }

    public static List<String> getPlaylistTitles(Context context) {
        List<String> list = new ArrayList<>();
        File f = new File(context.getFilesDir(), PLAYLIST_TITLES_FILE);
        if (!f.exists()) {
            list.add(context.getString(R.string.playlist_current_queue));
            list.add(context.getString(R.string.favorite_playlist));
            return list;
        }
        try {
            String json = readFile(f);
            if (json != null) {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
            }
        } catch (JSONException | IOException e) {
            Log.e(TAG, "getPlaylistTitles failed", e);
        }
        if (list.isEmpty()) {
            list.add(context.getString(R.string.playlist_current_queue));
            list.add(context.getString(R.string.favorite_playlist));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Playlist tracks
    // -------------------------------------------------------------------------

    public static void updatePlaylistTracks(Context context, List<MusicModel> updatedList, int index) {
        new Thread(() -> {
            synchronized (PLAYLIST_DATA_FILE) {
                List<List<MusicModel>> all = loadAllPlaylistTracks(context);
                while (all.size() <= index) all.add(new ArrayList<>());
                all.set(index, updatedList != null ? new ArrayList<>(updatedList) : new ArrayList<>());
                saveAllPlaylistTracks(context, all);
            }
        }).start();
    }

    public static List<MusicModel> getPlaylistTrackAtPosition(Context context, int pos) {
        List<List<MusicModel>> all = loadAllPlaylistTracks(context);
        if (pos >= 0 && pos < all.size()) return new ArrayList<>(all.get(pos));
        return new ArrayList<>();
    }

    public static void dropPlaylistCardDataAt(Context context, int pos) {
        new Thread(() -> {
            synchronized (PLAYLIST_DATA_FILE) {
                int customIndex = pos - 2;
                if (customIndex < 0) return;
                List<List<MusicModel>> all = loadAllPlaylistTracks(context);
                if (customIndex < all.size()) {
                    all.remove(customIndex);
                    saveAllPlaylistTracks(context, all);
                }
            }
        }).start();
    }

    public static void dropAllPlaylistData(Context context) {
        new Thread(() -> {
            new File(context.getFilesDir(), PLAYLIST_TITLES_FILE).delete();
            new File(context.getFilesDir(), PLAYLIST_DATA_FILE).delete();
        }).start();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void saveTrackList(Context context, String fileName, List<MusicModel> list) {
        new Thread(() -> saveTrackListSync(context, fileName, list)).start();
    }

    private static synchronized void saveTrackListSync(Context context, String fileName, List<MusicModel> list) {
        File f = new File(context.getFilesDir(), fileName);
        if (list == null || list.isEmpty()) {
            if (f.exists() && f.delete()) Log.d(TAG, fileName + " deleted");
            return;
        }
        JSONArray arr = new JSONArray();
        for (MusicModel m : list) {
            try { arr.put(musicModelToJson(m)); } catch (JSONException e) {
                Log.e(TAG, "saveTrackListSync serialise error", e);
            }
        }
        try {
            writeJson(f, arr.toString());
        } catch (IOException e) {
            Log.e(TAG, "saveTrackListSync write error for " + fileName, e);
        }
    }

    private static synchronized List<MusicModel> loadTrackList(Context context, String fileName) {
        List<MusicModel> list = new ArrayList<>();
        File f = new File(context.getFilesDir(), fileName);
        if (!f.exists()) return list;
        try {
            String json = readFile(f);
            if (json == null) return list;
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(musicModelFromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException | IOException e) {
            Log.e(TAG, "loadTrackList error for " + fileName, e);
        }
        return list;
    }

    private static synchronized void saveAllPlaylistTracks(Context context, List<List<MusicModel>> listOfLists) {
        File f = new File(context.getFilesDir(), PLAYLIST_DATA_FILE);
        JSONArray outer = new JSONArray();
        for (List<MusicModel> inner : listOfLists) {
            JSONArray arr = new JSONArray();
            for (MusicModel m : inner) {
                try { arr.put(musicModelToJson(m)); } catch (JSONException e) {
                    Log.e(TAG, "saveAllPlaylistTracks serialise error", e);
                }
            }
            outer.put(arr);
        }
        try {
            writeJson(f, outer.toString());
        } catch (IOException e) {
            Log.e(TAG, "saveAllPlaylistTracks write error", e);
        }
    }

    private static synchronized List<List<MusicModel>> loadAllPlaylistTracks(Context context) {
        List<List<MusicModel>> result = new ArrayList<>();
        File f = new File(context.getFilesDir(), PLAYLIST_DATA_FILE);
        if (!f.exists()) return result;
        try {
            String json = readFile(f);
            if (json == null) return result;
            JSONArray outer = new JSONArray(json);
            for (int i = 0; i < outer.length(); i++) {
                JSONArray inner = outer.getJSONArray(i);
                List<MusicModel> list = new ArrayList<>();
                for (int j = 0; j < inner.length(); j++) {
                    list.add(musicModelFromJson(inner.getJSONObject(j)));
                }
                result.add(list);
            }
        } catch (JSONException | IOException e) {
            Log.e(TAG, "loadAllPlaylistTracks error", e);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // JSON <-> MusicModel
    // -------------------------------------------------------------------------

    private static JSONObject musicModelToJson(MusicModel m) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id",        m.getId());
        o.put("name",      m.getSongName());
        o.put("artist",    m.getArtist());
        o.put("album",     m.getAlbum());
        o.put("path",      m.getSongPath()    != null ? m.getSongPath()    : "");
        o.put("art",       m.getAlbumArtUrl() != null ? m.getAlbumArtUrl() : "");
        o.put("duration",  m.getDuration());
        o.put("dateAdded", m.getDateAdded());
        return o;
    }

    private static MusicModel musicModelFromJson(JSONObject o) throws JSONException {
        return new MusicModel(
                o.getInt("id"),
                o.optString("name",    ""),
                o.optString("artist",  ""),
                o.optString("album",   ""),
                o.optString("path",    null),
                o.optString("art",     null),
                o.optLong("duration",  0),
                o.optLong("dateAdded", 0)
        );
    }

    // -------------------------------------------------------------------------
    // File I/O
    // -------------------------------------------------------------------------

    private static void writeJson(File file, String json) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, false))) {
            w.write(json);
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
