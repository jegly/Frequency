package com.tunes.player.viewmodel

import android.app.Application
import android.content.ComponentName
import android.media.MediaMetadataRetriever
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunes.player.TMS
import com.tunes.player.loaders.TrackFetcherFromStorage
import com.tunes.player.manager.PlaylistManager
import com.tunes.player.model.MusicModel
import com.tunes.player.model.PlaylistModel
import com.tunes.player.singleton.TrackManager
import com.tunes.player.utils.AppSettings
import com.tunes.player.utils.PlaylistStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _hasNewTracks = MutableStateFlow(false)
    val hasNewTracks: StateFlow<Boolean> = _hasNewTracks.asStateFlow()

    private val _accentColor = MutableStateFlow(AppSettings.getAccentColor(application))
    val accentColor: StateFlow<Int> = _accentColor.asStateFlow()

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
                mediaController?.playbackState?.let { state ->
                    _playbackState.value = state.state
                }
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
                delay(500)
            }
        }
    }

    fun loadLibrary() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val systemTracks = fetcher.fetchTracks()

                // Scan any custom imported folders
                val customFolderUris = AppSettings.getCustomMusicFolders(getApplication())
                val importedTracks = customFolderUris.flatMap { uriString ->
                    try {
                        scanDocumentTree(Uri.parse(uriString))
                    } catch (e: Exception) {
                        Log.w("MainViewModel", "Cannot scan folder $uriString", e)
                        emptyList()
                    }
                }

                val merged = (systemTracks + importedTracks).distinctBy { it.songPath }

                if (AppSettings.isCheckNewMediaEnabled(getApplication())) {
                    val lastCount = AppSettings.getLastTrackCount(getApplication())
                    if (lastCount > 0 && merged.size > lastCount) {
                        _hasNewTracks.value = true
                    }
                }
                AppSettings.saveLastTrackCount(getApplication(), merged.size)

                _allTracks.value = merged
                trackManager.setMainList(merged)
                _recentTracks.value = PlaylistStorageManager.getRecentTracks(getApplication())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissNewTracksNotice() {
        _hasNewTracks.value = false
    }

    // Accent color
    fun setAccentColor(colorInt: Int) {
        AppSettings.setAccentColor(getApplication(), colorInt)
        _accentColor.value = colorInt
    }

    // Import a folder via SAF tree URI
    fun importMusicFolder(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                AppSettings.addCustomMusicFolder(getApplication(), uri.toString())
                val imported = scanDocumentTree(uri)
                val merged = (_allTracks.value + imported).distinctBy { it.songPath }
                _allTracks.value = merged
                trackManager.setMainList(merged)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Import individual audio files via SAF content URIs
    fun importMusicFiles(uris: List<Uri>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imported = uris.mapNotNull { uri -> metadataFromUri(uri) }
                val merged = (_allTracks.value + imported).distinctBy { it.songPath }
                _allTracks.value = merged
                trackManager.setMainList(merged)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Play an audio file from an external URI (intent)
    fun playFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val track = metadataFromUri(uri) ?: MusicModel(
                    id = uri.hashCode().toLong(),
                    songName = uri.lastPathSegment ?: "Unknown",
                    artist = "Unknown Artist",
                    album = "Unknown Album",
                    songPath = uri.toString(),
                    albumArtUrl = null,
                    duration = 0L,
                    dateAdded = 0L
                )
                trackManager.buildDataList(listOf(track), 0)
                mediaController?.transportControls?.play()
            } catch (e: Exception) {
                Log.e("MainViewModel", "playFromUri failed", e)
            }
        }
    }

    private suspend fun scanDocumentTree(treeUri: Uri): List<MusicModel> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(getApplication(), treeUri) ?: return@withContext emptyList()
        scanDocumentDir(root)
    }

    private fun scanDocumentDir(dir: DocumentFile): List<MusicModel> {
        val result = mutableListOf<MusicModel>()
        dir.listFiles().forEach { file ->
            when {
                file.isDirectory -> result.addAll(scanDocumentDir(file))
                file.type?.startsWith("audio/") == true -> {
                    metadataFromUri(file.uri)?.let { result.add(it) }
                }
            }
        }
        return result
    }

    private fun metadataFromUri(uri: Uri): MusicModel? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(getApplication(), uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment ?: "Unknown"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it != "<unknown>" } ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it != "<unknown>" } ?: "Unknown Album"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            MusicModel(
                id = uri.hashCode().toLong(),
                songName = title,
                artist = artist,
                album = album,
                songPath = uri.toString(),
                albumArtUrl = null,
                duration = duration,
                dateAdded = 0L
            )
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    fun playTrack(list: List<MusicModel>, index: Int) {
        if (_playbackState.value == PlaybackState.STATE_PLAYING) {
            mediaController?.transportControls?.pause()
        }
        trackManager.buildDataList(list, index)
        mediaController?.transportControls?.play()
    }

    fun togglePlayPause() {
        val currentState = mediaController?.playbackState?.state ?: _playbackState.value
        if (currentState == PlaybackState.STATE_PLAYING) {
            mediaController?.transportControls?.pause()
        } else {
            mediaController?.transportControls?.play()
        }
    }

    fun refreshPlaybackState() {
        mediaController?.let { controller ->
            controller.playbackState?.let { state -> _playbackState.value = state.state }
            if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                startPositionUpdater()
            }
        }
    }

    fun seekTo(pos: Long) { mediaController?.transportControls?.seekTo(pos) }
    fun skipToNext() = mediaController?.transportControls?.skipToNext()
    fun skipToPrevious() = mediaController?.transportControls?.skipToPrevious()

    fun toggleShuffle() { trackManager.isShuffleEnabled = !trackManager.isShuffleEnabled }

    fun toggleRepeat() {
        when {
            !trackManager.repeatCurrentTrack && !trackManager.isInfiniteLoopEnabled ->
                trackManager.repeatCurrentTrack = true
            trackManager.repeatCurrentTrack -> {
                trackManager.repeatCurrentTrack = false
                trackManager.isInfiniteLoopEnabled = true
            }
            else -> trackManager.isInfiniteLoopEnabled = false
        }
    }

    // Playlist methods
    fun loadPlaylists() {
        viewModelScope.launch { playlistManager.loadPlaylists(getApplication()) }
    }

    suspend fun createPlaylist(name: String): Long =
        playlistManager.createPlaylist(getApplication(), name)

    suspend fun renamePlaylist(playlistId: Long, newName: String) =
        playlistManager.renamePlaylist(getApplication(), playlistId, newName)

    suspend fun deletePlaylist(playlistId: Long) =
        playlistManager.deletePlaylist(getApplication(), playlistId)

    suspend fun addTrackToPlaylist(playlistId: Long, track: MusicModel) =
        playlistManager.addTrackToPlaylist(getApplication(), playlistId, track)

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) =
        playlistManager.removeTrackFromPlaylist(getApplication(), playlistId, trackId)

    suspend fun reorderPlaylistTracks(playlistId: Long, from: Int, to: Int) =
        playlistManager.reorderPlaylistTracks(getApplication(), playlistId, from, to)

    suspend fun getPlaylistTracks(playlistId: Long): List<MusicModel> =
        playlistManager.getPlaylistTracks(getApplication(), playlistId)

    fun setPlaybackSpeed(speed: Float) {
        AppSettings.setPlaybackSpeed(getApplication(), speed)
        val bundle = Bundle().apply { putFloat("speed", speed) }
        mediaController?.transportControls?.sendCustomAction(
            com.tunes.player.playback.PlaybackManager.ACTION_SET_SPEED, bundle
        )
    }

    override fun onCleared() {
        super.onCleared()
        mediaBrowser?.disconnect()
    }
}
