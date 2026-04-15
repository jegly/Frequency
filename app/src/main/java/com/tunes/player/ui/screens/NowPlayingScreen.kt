package com.tunes.player.ui.screens

import android.media.session.PlaybackState
import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tunes.player.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NowPlayingScreen(viewModel: MainViewModel) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val scope = rememberCoroutineScope()
    
    val isPlaying = playbackState == PlaybackState.STATE_PLAYING
    val duration = currentTrack?.duration ?: 0L
    
    // Access shuffle and repeat states from TrackManager
    val trackManager = com.tunes.player.singleton.TrackManager.instance
    
    // Use state variables that update when TrackManager state changes
    var isShuffleEnabled by remember { mutableStateOf(trackManager.isShuffleEnabled) }
    var isRepeatCurrentTrack by remember { mutableStateOf(trackManager.repeatCurrentTrack) }
    var isInfiniteLoopEnabled by remember { mutableStateOf(trackManager.isInfiniteLoopEnabled) }
    
    // Update state when TrackManager state changes (called when buttons are pressed)
    fun updatePlaybackStates() {
        isShuffleEnabled = trackManager.isShuffleEnabled
        isRepeatCurrentTrack = trackManager.repeatCurrentTrack
        isInfiniteLoopEnabled = trackManager.isInfiniteLoopEnabled
    }
    
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Album Art
        GlideImage(
            model = currentTrack?.albumArtUrl ?: "",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraLarge),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Title and Artist
        Text(
            text = currentTrack?.songName ?: "Not Playing",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1
        )
        Text(
            text = currentTrack?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Button
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Seek Bar
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(DateUtils.formatElapsedTime(currentPosition / 1000), style = MaterialTheme.typography.labelMedium)
            Text(DateUtils.formatElapsedTime(duration / 1000), style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                viewModel.toggleShuffle()
                updatePlaybackStates()
            }) {
                Icon(
                    Icons.Default.Shuffle, 
                    contentDescription = "Shuffle",
                    tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = { viewModel.skipToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
            }
            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = { viewModel.skipToNext() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { 
                viewModel.toggleRepeat()
                updatePlaybackStates()
            }) {
                Icon(
                    when {
                        isRepeatCurrentTrack -> Icons.Default.RepeatOne
                        isInfiniteLoopEnabled -> Icons.Default.Repeat
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (isRepeatCurrentTrack || isInfiniteLoopEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
    }

    // Dropdown Menu
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        offset = DpOffset((-8).dp, 8.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Add to Playlist") },
            onClick = {
                showMenu = false
                showPlaylistDialog = true
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Create New Playlist") },
            onClick = {
                showMenu = false
                showCreatePlaylistDialog = true
            },
            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
        )
    }

    // Playlist Selection Dialog
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                Column {
                    playlists.filter { !it.isSystemPlaylist || it.name == "Favorite" }.forEach { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentTrack?.let { track ->
                                        scope.launch {
                                            viewModel.addTrackToPlaylist(playlist.id, track)
                                            showPlaylistDialog = false
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (playlist.isSystemPlaylist) {
                                    when (playlist.name) {
                                        "Favorite" -> Icons.Default.Favorite
                                        else -> Icons.AutoMirrored.Filled.PlaylistPlay
                                    }
                                } else Icons.AutoMirrored.Filled.PlaylistPlay,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            scope.launch {
                                viewModel.createPlaylist(playlistName.trim())
                                // Add current track to the new playlist
                                currentTrack?.let { track ->
                                    val newPlaylist = viewModel.playlists.value.find { it.name == playlistName.trim() }
                                    newPlaylist?.let { playlist ->
                                        viewModel.addTrackToPlaylist(playlist.id, track)
                                    }
                                }
                                showCreatePlaylistDialog = false
                            }
                        }
                    },
                    enabled = playlistName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
