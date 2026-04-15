package com.tunes.player.loaders

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.tunes.player.model.ArtistModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtistFetcher(private val contentResolver: ContentResolver) {

    enum class Sort { TITLE_ASC, NUM_OF_TRACKS_DESC }

    suspend fun fetchArtists(sort: Sort = Sort.TITLE_ASC): List<ArtistModel> = withContext(Dispatchers.IO) {
        val data = mutableListOf<ArtistModel>()
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        val sortOrder = when (sort) {
            Sort.TITLE_ASC -> "${MediaStore.Audio.Artists.ARTIST} ASC"
            Sort.NUM_OF_TRACKS_DESC -> "${MediaStore.Audio.Artists.NUMBER_OF_TRACKS} DESC"
        }

        try {
            contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                val albumCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                val trackCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idCol)
                    val artist = cursor.getString(artistCol)
                    val numAlbums = cursor.getInt(albumCountCol)
                    val numTracks = cursor.getInt(trackCountCol)
                    data.add(ArtistModel(id, artist, numAlbums, numTracks))
                }
            }
        } catch (e: Exception) {
            Log.e("ArtistFetcher", "fetchArtists error", e)
        }
        data
    }
}
