package com.jegly.frequency.helper

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.jegly.frequency.model.MusicModel

object MediaHelper {

    private const val TAG = "MediaHelper"

    fun buildMusicModelFrom(context: Context, data: Intent): MusicModel? {
        val uri = data.data ?: return null
        return buildMusicModelFromUri(context, uri)
    }

    fun buildMusicModelFromUri(context: Context, uri: Uri): MusicModel? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(context, uri)
            val path = uri.toString()
            var name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            if (name.isNullOrEmpty()) {
                name = uri.lastPathSegment ?: "Unknown"
            }
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            
            MusicModel(
                id = -1,
                songName = name,
                artist = artist,
                album = album,
                songPath = path,
                albumArtUrl = null,
                duration = duration,
                dateAdded = System.currentTimeMillis() / 1000L
            )
        } catch (e: Exception) {
            Log.e(TAG, "buildMusicModelFromUri error", e)
            null
        } finally {
            try {
                mmr.release()
            } catch (ignored: Exception) {
            }
        }
    }
}
