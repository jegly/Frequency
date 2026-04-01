package com.tunes.player.helper;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.tunes.player.model.MusicModel;

public class MediaHelper {

    private static final String TAG = "MediaHelper";

    public static MusicModel buildMusicModelFrom(Context context, Intent data){
        Uri uri = data.getData();
        if (uri == null) return null;
        return buildMusicModelFromUri(context, uri);
    }

    public static MusicModel buildMusicModelFromUri(Context context, Uri uri){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try{
            mmr.setDataSource(context, uri);
            String path = uri.toString();
            String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (name == null || name.isEmpty()) {
                name = uri.getLastPathSegment();
            }
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist == null) artist = "Unknown Artist";
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (album == null) album = "Unknown Album";
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = durationStr != null ? Long.parseLong(durationStr) : 0;
            mmr.release();
            
            // MusicModel constructor: int id, String songName, String artist, String album, String songPath, String albumArtUrl, long duration, long dateAdded
            return new MusicModel(-1, name, artist, album, path, null, duration, System.currentTimeMillis() / 1000L);
        } catch(Exception e){
            Log.e(TAG, "buildMusicModelFromUri error", e);
            try {
                mmr.release();
            } catch (Exception ignored) {}
            return null;
        }
    }
}
