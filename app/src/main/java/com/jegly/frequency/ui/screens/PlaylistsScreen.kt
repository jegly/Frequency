package com.jegly.frequency.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import com.jegly.frequency.model.PlaylistModel
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private const val UNCATEGORIZED_KEY = "__uncategorized__"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(viewModel: MainViewModel, onPlaylistClick: (PlaylistModel) -> Unit) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isPlaylistLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistModel?>(null) }
    val collapsedCategories = remember { mutableStateMapOf<String, Boolean>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var hideSystemPlaylists by remember { mutableStateOf(AppSettings.isHideSystemPlaylists(context)) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists") },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (hideSystemPlaylists) "Show built-in playlists" else "Hide built-in playlists") },
                                onClick = {
                                    hideSystemPlaylists = !hideSystemPlaylists
                                    AppSettings.setHideSystemPlaylists(context, hideSystemPlaylists)
                                    showOverflowMenu = false
                                }
                            )
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
        } else {
            // System playlists come first un-grouped; custom playlists grouped by category
            val systemPlaylists =
                if (hideSystemPlaylists) emptyList() else playlists.filter { it.isSystemPlaylist }
            val customGrouped   = playlists.filter { !it.isSystemPlaylist }
                .groupBy { it.category ?: UNCATEGORIZED_KEY }
                .toSortedMap(compareBy { if (it == UNCATEGORIZED_KEY) "" else it })

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(systemPlaylists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        onRename = null,
                        onDelete = null,
                        onCategorise = null
                    )
                }

                customGrouped.forEach { (category, items) ->
                    val collapsed = collapsedCategories[category] == true
                    val displayName = if (category == UNCATEGORIZED_KEY) "Uncategorized" else category
                    item(key = "header_$category") {
                        CategoryHeader(
                            name = displayName,
                            count = items.size,
                            collapsed = collapsed,
                            onClick = { collapsedCategories[category] = !collapsed }
                        )
                    }
                    if (!collapsed) {
                        items(items, key = { "pl_${it.id}" }) { playlist ->
                            PlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) },
                                onRename = {
                                    selectedPlaylist = playlist
                                    showRenameDialog = true
                                },
                                onDelete = {
                                    selectedPlaylist = playlist
                                    showDeleteDialog = true
                                },
                                onCategorise = {
                                    selectedPlaylist = playlist
                                    showCategoryDialog = true
                                }
                            )
                        }
                    }
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

    if (showCategoryDialog && selectedPlaylist != null) {
        CategoryPickerDialog(
            current = selectedPlaylist!!.category,
            existing = viewModel.allCategories(),
            onDismiss = { showCategoryDialog = false },
            onPick = { category ->
                scope.launch {
                    viewModel.setPlaylistCategory(selectedPlaylist!!.id, category)
                    showCategoryDialog = false
                    selectedPlaylist = null
                }
            }
        )
    }
}

@Composable
fun CategoryPickerDialog(
    current: String?,
    existing: List<String>,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to category") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(null) }
                        .padding(vertical = 6.dp)
                ) {
                    RadioButton(selected = current == null, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Uncategorized", style = MaterialTheme.typography.bodyMedium)
                }
                existing.forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(cat) }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(selected = current == cat, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(cat, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New category") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { onPick(newName.trim()) },
                            enabled = newName.isNotBlank() && newName.trim() !in existing
                        ) { Text("Create") }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CategoryHeader(name: String, count: Int, collapsed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (collapsed) Icons.Default.ChevronRight else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: PlaylistModel,
    onClick: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onCategorise: (() -> Unit)?
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
                    onCategorise?.let {
                        DropdownMenuItem(
                            text = { Text("Move to category…") },
                            onClick = {
                                onCategorise()
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
