package com.tunes.player.loaders

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import com.tunes.player.model.MusicModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackFetcherFromStorage(private val contentResolver: ContentResolver) {

    enum class Sort {
        TITLE_ASC,
        DATE_ADDED_DESC
    }

    suspend fun fetchTracks(sort: Sort = Sort.TITLE_ASC): List<MusicModel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MusicModel>()
        // Use Audio URI specifically if we only want audio
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val sortOrder = if (sort == Sort.DATE_ADDED_DESC) {
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        } else {
            "${MediaStore.Audio.Media.TITLE} ASC"
        }

        try {
            contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris
                        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        .toString()

                    var title = cursor.getString(titleCol)
                    if (title.isNullOrEmpty()) title = cursor.getString(nameCol)
                    if (title.isNullOrEmpty()) title = "Unknown"

                    var artist = if (artistCol != -1) cursor.getString(artistCol) else null
                    if (artist.isNullOrEmpty() || artist == "<unknown>") artist = "Unknown Artist"

                    var album = if (albumCol != -1) cursor.getString(albumCol) else null
                    if (album.isNullOrEmpty() || album == "<unknown>") album = "Unknown Album"

                    val duration = cursor.getLong(durCol)
                    val dateAdded = cursor.getLong(dateCol)

                    list.add(
                        MusicModel(
                            id, title, artist, album,
                            contentUri, null, duration, dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TrackFetcher", "Error fetching tracks", e)
        }
        list
    }
}
