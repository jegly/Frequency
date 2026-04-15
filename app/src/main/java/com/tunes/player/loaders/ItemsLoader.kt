package com.tunes.player.loaders

import android.util.Log
import com.tunes.player.model.MusicModel
import com.tunes.player.singleton.TrackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemsLoader(private val title: String, private val category: Int) {

    companion object {
        const val CATEGORY_ALBUM = 1
        const val CATEGORY_ARTIST = 2
    }

    suspend fun loadItems(): List<MusicModel> = withContext(Dispatchers.IO) {
        val source = TrackManager.instance.getMainList()
        val out = mutableListOf<MusicModel>()

        for (md in source) {
            when (category) {
                CATEGORY_ALBUM -> if (md.album == title) out.add(md)
                CATEGORY_ARTIST -> if (md.artist == title) out.add(md)
            }
        }

        if (out.isEmpty()) Log.d("ItemsLoader", "No items found for: $title")
        out
    }
}
