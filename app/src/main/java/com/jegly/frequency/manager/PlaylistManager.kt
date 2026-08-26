package com.jegly.frequency.manager

import android.content.Context
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.model.PlaylistModel
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.utils.PlaylistStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PlaylistManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PlaylistManager? = null
        
        fun getInstance(): PlaylistManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaylistManager().also { INSTANCE = it }
            }
        }
    }
    
    private val _playlists = MutableStateFlow<List<PlaylistModel>>(emptyList())
    val playlists: StateFlow<List<PlaylistModel>> = _playlists.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var nextPlaylistId: Long = 3 // Start after system playlists (0, 1, 2)
    
    suspend fun loadPlaylists(context: Context) = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val titles = PlaylistStorageManager.getPlaylistTitles(context)
            val playlistModels = mutableListOf<PlaylistModel>()
            
            // Add system playlists
            playlistModels.add(PlaylistModel(id = 0, name = "Playing Queue", isSystemPlaylist = true))
            playlistModels.add(PlaylistModel(id = 1, name = "Favorite", isSystemPlaylist = true))
            playlistModels.add(PlaylistModel(id = -1, name = "Recently Played", isSystemPlaylist = true))
            
            // Add custom playlists
            titles.forEachIndexed { index, title ->
                if (index >= 2) { // Skip system playlists
                    val id = index.toLong()
                    playlistModels.add(PlaylistModel(
                        id = id,
                        name = title,
                        isSystemPlaylist = false,
                        category = AppSettings.getPlaylistCategory(context, id)
                    ))
                }
            }

            _playlists.value = playlistModels
            nextPlaylistId = (playlistModels.maxOfOrNull { it.id } ?: 2) + 1
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createPlaylist(context: Context, name: String): Long = withContext(Dispatchers.IO) {
        val newPlaylist = PlaylistModel(
            id = nextPlaylistId++,
            name = name,
            isSystemPlaylist = false
        )
        
        val currentPlaylists = _playlists.value.toMutableList()
        currentPlaylists.add(newPlaylist)
        _playlists.value = currentPlaylists
        
        // Update storage
        val titles = currentPlaylists.filter { !it.isSystemPlaylist }.map { it.name }
        PlaylistStorageManager.savePlaylistTitles(context, titles)
        
        newPlaylist.id
    }
    
    suspend fun renamePlaylist(context: Context, playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        if (playlistId < 2) return@withContext // Cannot rename system playlists
        
        val currentPlaylists = _playlists.value.toMutableList()
        val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (playlistIndex >= 0) {
            currentPlaylists[playlistIndex] = currentPlaylists[playlistIndex].copy(name = newName)
            _playlists.value = currentPlaylists
            
            // Update storage
            val titles = currentPlaylists.filter { !it.isSystemPlaylist }.map { it.name }
            PlaylistStorageManager.savePlaylistTitles(context, titles)
        }
    }
    
    suspend fun deletePlaylist(context: Context, playlistId: Long) = withContext(Dispatchers.IO) {
        if (playlistId < 2) return@withContext // Cannot delete system playlists
        
        val currentPlaylists = _playlists.value.toMutableList()
        val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (playlistIndex >= 0) {
            currentPlaylists.removeAt(playlistIndex)
            _playlists.value = currentPlaylists
            
            // Update storage
            val titles = currentPlaylists.filter { !it.isSystemPlaylist }.map { it.name }
            PlaylistStorageManager.savePlaylistTitles(context, titles)
            
            // Remove playlist tracks
            PlaylistStorageManager.dropPlaylistCardDataAt(context, playlistId.toInt())
        }
    }
    
    suspend fun addTrackToPlaylist(context: Context, playlistId: Long, track: MusicModel) = withContext(Dispatchers.IO) {
        val currentTracks = getPlaylistTracks(context, playlistId).toMutableList()
        if (!currentTracks.any { it.id == track.id }) {
            currentTracks.add(track)
            updatePlaylistTracks(context, playlistId, currentTracks)
        }
    }
    
    suspend fun removeTrackFromPlaylist(context: Context, playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        val currentTracks = getPlaylistTracks(context, playlistId).toMutableList()
        currentTracks.removeAll { it.id == trackId }
        updatePlaylistTracks(context, playlistId, currentTracks)
    }
    
    suspend fun getPlaylistTracks(context: Context, playlistId: Long): List<MusicModel> = withContext(Dispatchers.IO) {
        when (playlistId) {
            -1L -> PlaylistStorageManager.getRecentTracks(context)
            0L -> emptyList()
            1L -> PlaylistStorageManager.getFavorite(context)
            else -> PlaylistStorageManager.getPlaylistTrackAtPosition(context, playlistId.toInt())
        }
    }

    suspend fun reorderPlaylistTracks(context: Context, playlistId: Long, from: Int, to: Int) = withContext(Dispatchers.IO) {
        if (playlistId < 2) return@withContext
        val tracks = getPlaylistTracks(context, playlistId).toMutableList()
        if (from !in tracks.indices || to !in tracks.indices) return@withContext
        val item = tracks.removeAt(from)
        tracks.add(to, item)
        updatePlaylistTracks(context, playlistId, tracks)
    }
    
    suspend fun setPlaylistCategory(context: Context, playlistId: Long, category: String?) = withContext(Dispatchers.IO) {
        AppSettings.setPlaylistCategory(context, playlistId, category)
        val current = _playlists.value.toMutableList()
        val idx = current.indexOfFirst { it.id == playlistId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(category = category)
            _playlists.value = current
        }
    }

    fun getAllCategories(context: Context): List<String> =
        AppSettings.getAllPlaylistCategories(context).sorted()

    private suspend fun updatePlaylistTracks(context: Context, playlistId: Long, tracks: List<MusicModel>) = withContext(Dispatchers.IO) {
        if (playlistId == 1L) {
            PlaylistStorageManager.saveFavorite(context, tracks)
        } else {
            PlaylistStorageManager.updatePlaylistTracks(context, tracks, playlistId.toInt())
        }
    }
}
