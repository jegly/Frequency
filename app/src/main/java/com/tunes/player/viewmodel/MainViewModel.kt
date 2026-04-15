package com.tunes.player.viewmodel

import android.app.Application
import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunes.player.TMS
import com.tunes.player.loaders.TrackFetcherFromStorage
import com.tunes.player.manager.PlaylistManager
import com.tunes.player.model.MusicModel
import com.tunes.player.model.PlaylistModel
import com.tunes.player.singleton.TrackManager
import com.tunes.player.utils.PlaylistStorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val trackManager = TrackManager.instance
    private val fetcher = TrackFetcherFromStorage(application.contentResolver)
    private val playlistManager = PlaylistManager.getInstance()

    private val _allTracks = MutableStateFlow<List<MusicModel>>(emptyList())
    val allTracks: StateFlow<List<MusicModel>> = _allTracks.asStateFlow()

    private val _recentTracks = MutableStateFlow<List<MusicModel>>(emptyList())
    val recentTracks: StateFlow<List<MusicModel>> = _recentTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.STATE_NONE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    val currentTrack = trackManager.currentTrack
    val activeQueue = trackManager.activeList
    
    val playlists = playlistManager.playlists
    val isPlaylistLoading = playlistManager.isLoading

    private var mediaBrowser: MediaBrowser? = null
    private var mediaController: MediaController? = null

    private val connectionCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser?.let {
                mediaController = MediaController(getApplication(), it.sessionToken)
                mediaController?.registerCallback(object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        _playbackState.value = state?.state ?: PlaybackState.STATE_NONE
                    }
                })
                
                // Force refresh state immediately after connection and ensure synchronization
                mediaController?.playbackState?.let { state ->
                    _playbackState.value = state.state
                }
                
                // Also refresh current track and position to ensure full synchronization
                mediaController?.metadata?.let { metadata ->
                    metadata.description.mediaId?.let { mediaId ->
                        val trackId = mediaId.toLongOrNull()
                        if (trackId != null) {
                            // Current track is already updated via trackManager.currentTrack property
                            // No need to manually set it here
                        }
                    }
                }
                
                // Update position based on actual playback state
                if (mediaController?.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    startPositionUpdater()
                }
            }
        }
    }

    init {
        loadLibrary()
        loadPlaylists()
        connectToService()
        startPositionUpdater()
    }

    private fun connectToService() {
        mediaBrowser = MediaBrowser(
            getApplication(),
            ComponentName(getApplication(), TMS::class.java),
            connectionCallback,
            null
        )
        mediaBrowser?.connect()
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                if (_playbackState.value == PlaybackState.STATE_PLAYING) {
                    _currentPosition.value = mediaController?.playbackState?.position ?: 0L
                }
                delay(1000)
            }
        }
    }

    fun loadLibrary() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tracks = fetcher.fetchTracks()
                _allTracks.value = tracks
                trackManager.setMainList(tracks)
                _recentTracks.value = PlaylistStorageManager.getRecentTracks(getApplication())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playTrack(list: List<MusicModel>, index: Int) {
        // Stop any existing playback before starting new track
        if (_playbackState.value == PlaybackState.STATE_PLAYING) {
            mediaController?.transportControls?.pause()
        }
        
        trackManager.buildDataList(list, index)
        mediaController?.transportControls?.play()
    }

    fun togglePlayPause() {
        // Get the actual current state from media controller to ensure synchronization
        val currentState = mediaController?.playbackState?.state ?: _playbackState.value
        
        if (currentState == PlaybackState.STATE_PLAYING) {
            mediaController?.transportControls?.pause()
        } else {
            mediaController?.transportControls?.play()
        }
    }
    
    // Force refresh playback state - call when app resumes
    fun refreshPlaybackState() {
        mediaController?.let { controller ->
            // Force sync playback state
            controller.playbackState?.let { state ->
                _playbackState.value = state.state
            }
            
            // Force sync current track
            controller.metadata?.let { metadata ->
                metadata.description.mediaId?.let { mediaId ->
                    val trackId = mediaId.toLongOrNull()
                    if (trackId != null) {
                        // Current track is already updated via trackManager.currentTrack property
                        // No need to manually set it here
                    }
                }
            }
            
            // Ensure position updater is running if playing
            if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                startPositionUpdater()
            }
        }
    }

    fun seekTo(pos: Long) {
        mediaController?.transportControls?.seekTo(pos)
    }

    fun skipToNext() = mediaController?.transportControls?.skipToNext()
    fun skipToPrevious() = mediaController?.transportControls?.skipToPrevious()

    fun toggleShuffle() {
        trackManager.isShuffleEnabled = !trackManager.isShuffleEnabled
    }

    fun toggleRepeat() {
        if (!trackManager.repeatCurrentTrack && !trackManager.isInfiniteLoopEnabled) {
            trackManager.repeatCurrentTrack = true
        } else if (trackManager.repeatCurrentTrack) {
            trackManager.repeatCurrentTrack = false
            trackManager.isInfiniteLoopEnabled = true
        } else {
            trackManager.isInfiniteLoopEnabled = false
        }
    }

    // Playlist methods
    fun loadPlaylists() {
        viewModelScope.launch {
            playlistManager.loadPlaylists(getApplication())
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistManager.createPlaylist(getApplication(), name)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistManager.renamePlaylist(getApplication(), playlistId, newName)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistManager.deletePlaylist(getApplication(), playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: MusicModel) {
        playlistManager.addTrackToPlaylist(getApplication(), playlistId, track)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistManager.removeTrackFromPlaylist(getApplication(), playlistId, trackId)
    }

    suspend fun getPlaylistTracks(playlistId: Long): List<MusicModel> {
        return playlistManager.getPlaylistTracks(getApplication(), playlistId)
    }

    override fun onCleared() {
        super.onCleared()
        mediaBrowser?.disconnect()
    }
}
