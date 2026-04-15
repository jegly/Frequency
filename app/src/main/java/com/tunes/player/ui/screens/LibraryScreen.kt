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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tunes.player.model.MusicModel
import com.tunes.player.model.PlaylistModel
import com.tunes.player.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: MainViewModel) {
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var selectedTrack by remember { mutableStateOf<MusicModel?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    Text(
                        text = "${allTracks.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            itemsIndexed(allTracks) { index, track ->
                LibraryTrackItem(
                    track = track,
                    onTrackClick = { viewModel.playTrack(allTracks, index) },
                    onMenuClick = {
                        selectedTrack = track
                        showMenu = true
                    }
                )
            }
        }

        if (showMenu && selectedTrack != null) {
            TrackMenuBottomSheet(
                track = selectedTrack!!,
                onDismiss = { showMenu = false },
                onPlayNext = { /* TODO */ },
                onAddToQueue = { /* TODO */ },
                onAddToPlaylist = { 
                    showMenu = false
                    showPlaylistDialog = true
                }
            )
        }

        if (showPlaylistDialog && selectedTrack != null) {
            PlaylistSelectionDialog(
                playlists = playlists.filter { !it.isSystemPlaylist || it.id == 1L }, // Allow Favorite playlist
                onDismiss = { showPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    scope.launch {
                        viewModel.addTrackToPlaylist(playlist.id, selectedTrack!!)
                        showPlaylistDialog = false
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryTrackItem(
    track: MusicModel,
    onTrackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(track.songName, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        leadingContent = {
            GlideImage(
                model = track.albumArtUrl ?: "",
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        },
        trailingContent = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
        modifier = Modifier.clickable { onTrackClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMenuBottomSheet(
    track: MusicModel,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(track.songName, style = MaterialTheme.typography.titleLarge)
            Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                headlineContent = { Text("Play Next") },
                modifier = Modifier.clickable { onPlayNext(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Add to Queue") },
                modifier = Modifier.clickable { onAddToQueue(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Add to Playlist") },
                modifier = Modifier.clickable { onAddToPlaylist(); onDismiss() }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlaylistSelectionDialog(
    playlists: List<PlaylistModel>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistModel) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                itemsIndexed(playlists) { _, playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaylistSelected(playlist) }
                            .padding(12.dp),
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
                            tint = MaterialTheme.colorScheme.primary
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
