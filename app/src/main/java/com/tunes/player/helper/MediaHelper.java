package com.tunes.player.helper;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.tunes.player.model.MusicModel;

public class MediaHelper {

    public static MusicModel buildMusicModelFrom(Context context, Intent data){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Uri uri = data.getData();
        if (uri == null) return null;
        
        try{
            mmr.setDataSource(context, uri);
            String path = uri.toString();
            String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = durationStr != null ? Long.parseLong(durationStr) : 0;
            mmr.release();
            
            // MusicModel constructor: int id, String songName, String artist, String album, String songPath, String albumArtUrl, long duration, long dateAdded
            return new MusicModel(-1, name, artist, album, path, null, duration, System.currentTimeMillis() / 1000L);
        } catch(Exception e){
            e.printStackTrace();
            try {
                mmr.release();
            } catch (Exception ignored) {}
            return null;
        }
    }
}
