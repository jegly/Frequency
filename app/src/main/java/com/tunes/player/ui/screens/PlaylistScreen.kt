package com.tunes.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tunes.player.model.MusicModel
import com.tunes.player.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: MainViewModel,
    playlistName: String,
    onBack: () -> Unit
) {
    var tracks by remember { mutableStateOf<List<MusicModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddTracksDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val allTracks by viewModel.allTracks.collectAsState()

    // Find playlist ID by name (this is a simplified approach)
    val playlists by viewModel.playlists.collectAsState()
    val playlist = playlists.find { it.name == playlistName }
    val playlistId = playlist?.id ?: 0L

    LaunchedEffect(playlistId) {
        isLoading = true
        tracks = viewModel.getPlaylistTracks(playlistId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlistName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (playlistId >= 2) { // Only allow editing custom playlists
                        IconButton(onClick = { showAddTracksDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tracks")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tracks in this playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (playlistId >= 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddTracksDialog = true }) {
                            Text("Add Tracks")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                itemsIndexed(tracks) { index, track ->
                    PlaylistTrackItem(
                        track = track,
                        onTrackClick = { viewModel.playTrack(tracks, index) },
                        onMenuClick = { /* Show menu with remove option */ },
                        canRemove = playlistId >= 2 // Only allow removing from custom playlists
                    )
                }
            }
        }
    }

    if (showAddTracksDialog) {
        AddTracksToPlaylistDialog(
            allTracks = allTracks,
            currentTracks = tracks,
            onDismiss = { showAddTracksDialog = false },
            onAddTracks = { selectedTracks ->
                scope.launch {
                    selectedTracks.forEach { track ->
                        viewModel.addTrackToPlaylist(playlistId, track)
                    }
                    // Refresh the tracks list
                    tracks = viewModel.getPlaylistTracks(playlistId)
                    showAddTracksDialog = false
                }
            }
        )
    }
}

@Composable
fun PlaylistTrackItem(
    track: MusicModel,
    onTrackClick: () -> Unit,
    onMenuClick: () -> Unit,
    canRemove: Boolean
) {
    if (canRemove) {
        LibraryTrackItem(
            track = track,
            onTrackClick = onTrackClick,
            onMenuClick = onMenuClick
        )
    } else {
        LibraryTrackItem(
            track = track,
            onTrackClick = onTrackClick,
            onMenuClick = { }
        )
    }
}

@Composable
fun AddTracksToPlaylistDialog(
    allTracks: List<MusicModel>,
    currentTracks: List<MusicModel>,
    onDismiss: () -> Unit,
    onAddTracks: (List<MusicModel>) -> Unit
) {
    var selectedTracks by remember { mutableStateOf<Set<MusicModel>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tracks to Playlist") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                val availableTracks = allTracks.filter { track ->
                    currentTracks.none { it.id == track.id }
                }
                
                itemsIndexed(availableTracks) { _, track ->
                    val isSelected = selectedTracks.contains(track)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                selectedTracks = if (isSelected) {
                                    selectedTracks - track
                                } else {
                                    selectedTracks + track
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedTracks = if (checked) {
                                    selectedTracks + track
                                } else {
                                    selectedTracks - track
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.songName, maxLines = 1)
                            Text(track.artist, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAddTracks(selectedTracks.toList()) },
                enabled = selectedTracks.isNotEmpty()
            ) {
                Text("Add ${selectedTracks.size} Track${if (selectedTracks.size != 1) "s" else ""}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
