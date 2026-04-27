package com.tunes.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tunes.player.model.PlaylistModel
import com.tunes.player.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(viewModel: MainViewModel, onPlaylistClick: (PlaylistModel) -> Unit) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isPlaylistLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistModel?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists") },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist")
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
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        onRename = if (!playlist.isSystemPlaylist) {
                            {
                                selectedPlaylist = playlist
                                showRenameDialog = true
                            }
                        } else null,
                        onDelete = if (!playlist.isSystemPlaylist) {
                            {
                                selectedPlaylist = playlist
                                showDeleteDialog = true
                            }
                        } else null
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                scope.launch {
                    viewModel.createPlaylist(name)
                    showCreateDialog = false
                }
            }
        )
    }

    if (showRenameDialog && selectedPlaylist != null) {
        RenamePlaylistDialog(
            currentName = selectedPlaylist!!.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                scope.launch {
                    viewModel.renamePlaylist(selectedPlaylist!!.id, newName)
                    showRenameDialog = false
                    selectedPlaylist = null
                }
            }
        )
    }

    if (showDeleteDialog && selectedPlaylist != null) {
        DeletePlaylistDialog(
            playlistName = selectedPlaylist!!.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                scope.launch {
                    viewModel.deletePlaylist(selectedPlaylist!!.id)
                    showDeleteDialog = false
                    selectedPlaylist = null
                }
            }
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: PlaylistModel,
    onClick: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (playlist.isSystemPlaylist) {
                    when (playlist.name) {
                        "Playing Queue" -> Icons.AutoMirrored.Filled.QueueMusic
                        "Favorite" -> Icons.Default.Favorite
                        "Recently Played" -> Icons.Default.History
                        else -> Icons.AutoMirrored.Filled.PlaylistPlay
                    }
                } else Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (!playlist.isSystemPlaylist) {
                var showMenu by remember { mutableStateOf(false) }
                
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    onRename?.let {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                onRename()
                                showMenu = false
                            }
                        )
                    }
                    onDelete?.let {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text("Playlist Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onConfirm(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Playlist") },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text("New Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank() && playlistName != currentName) {
                        onConfirm(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank() && playlistName != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeletePlaylistDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlist") },
        text = {
            Text("Are you sure you want to delete the playlist \"$playlistName\"? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
