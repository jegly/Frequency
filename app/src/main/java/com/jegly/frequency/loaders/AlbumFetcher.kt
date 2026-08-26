package com.jegly.frequency.loaders

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.jegly.frequency.model.AlbumModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumFetcher(private val contentResolver: ContentResolver) {

    enum class Sort { TITLE_ASC, DATE_ASC }

    suspend fun fetchAlbums(sort: Sort = Sort.TITLE_ASC): List<AlbumModel> = withContext(Dispatchers.IO) {
        val data = mutableListOf<AlbumModel>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        val sortOrder = when (sort) {
            Sort.TITLE_ASC -> "${MediaStore.Audio.Albums.ALBUM} ASC"
            Sort.DATE_ASC -> "${MediaStore.Audio.Albums.FIRST_YEAR} ASC"
        }

        try {
            contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val songCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
                val artUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idCol)
                    val album = cursor.getString(albumCol)
                    val albumArt = ContentUris.withAppendedId(artUri, id.toLong()).toString()
                    val num = cursor.getInt(songCountCol)
                    data.add(AlbumModel(id, album, num, albumArt))
                }
            }
        } catch (e: Exception) {
            Log.e("AlbumFetcher", "fetchAlbums error", e)
        }
        data
    }
}
