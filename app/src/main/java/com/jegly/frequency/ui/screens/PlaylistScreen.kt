package com.jegly.frequency.ui.screens

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.viewmodel.MainViewModel
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: MainViewModel,
    playlistName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tracksList = remember { mutableStateListOf<MusicModel>() }
    var isLoading by remember { mutableStateOf(true) }
    var showAddTracksDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val allTracks by viewModel.allTracks.collectAsState()

    val playlists by viewModel.playlists.collectAsState()
    val playlist = playlists.find { it.name == playlistName }
    val playlistId = playlist?.id ?: 0L
    val canModify = playlistId >= 2L

    LaunchedEffect(playlistId) {
        isLoading = true
        tracksList.clear()
        tracksList.addAll(viewModel.getPlaylistTracks(playlistId))
        isLoading = false
    }

    // Drag-to-reorder state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var itemHeightPx by remember { mutableStateOf(72f) }

    // ── Multi-select state ────────────────────────────────────────────────────
    val selectedIds = remember { mutableStateListOf<Long>() }
    val selectionMode = selectedIds.isNotEmpty()
    var showBulkPlaylistDialog by remember { mutableStateOf(false) }
    var showBulkDeleteDialog   by remember { mutableStateOf(false) }
    var bulkPendingDelete      by remember { mutableStateOf<List<MusicModel>>(emptyList()) }

    val bulkDeleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            bulkPendingDelete.forEach { track ->
                viewModel.removeTrackFromLibrary(track)
                tracksList.remove(track)
            }
        }
        bulkPendingDelete = emptyList()
        selectedIds.clear()
    }

    fun clearSelection() { selectedIds.clear() }
    fun toggleSelect(id: Long) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
    }

    var trackPendingDelete by remember { mutableStateOf<MusicModel?>(null) }
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            trackPendingDelete?.let { track ->
                viewModel.removeTrackFromLibrary(track)
                tracksList.remove(track)
            }
        }
        trackPendingDelete = null
    }

    val requestDelete: (MusicModel) -> Unit = { track ->
        trackPendingDelete = track
        try {
            val uri = Uri.parse(track.songPath)
            val intentSender = MediaStore.createDeleteRequest(
                context.contentResolver, listOf(uri)
            ).intentSender
            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } catch (e: Exception) {
            trackPendingDelete = null
        }
    }

    fun targetIndex(srcIndex: Int): Int =
        (srcIndex + (dragOffsetY / itemHeightPx).roundToInt()).coerceIn(0, (tracksList.size - 1).coerceAtLeast(0))

    fun itemDisplacement(index: Int): Float {
        val src = draggedIndex ?: return 0f
        val target = targetIndex(src)
        return when {
            index == src -> dragOffsetY
            src < target && index in (src + 1)..target -> -itemHeightPx
            src > target && index in target until src -> itemHeightPx
            else -> 0f
        }
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showBulkPlaylistDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist")
                        }
                        IconButton(onClick = { showBulkDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(playlistName) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (canModify) {
                            IconButton(onClick = { showAddTracksDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Tracks")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (tracksList.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.playTrack(tracksList.toList(), Random.nextInt(tracksList.size)) },
                    icon = { Icon(Icons.Default.Casino, contentDescription = null) },
                    text = { Text("Random") }
                )
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            tracksList.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("No tracks in this playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (canModify) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showAddTracksDialog = true }) { Text("Add Tracks") }
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(tracksList, key = { _, track -> track.id }) { index, track ->
                    val isDragged = draggedIndex == index
                    val displacement = itemDisplacement(index)

                    if (canModify) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    val removedTrack = track
                                    tracksList.remove(removedTrack)
                                    scope.launch {
                                        viewModel.removeTrackFromPlaylist(playlistId, removedTrack.id)
                                    }
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = Color.White)
                                }
                            },
                            modifier = Modifier
                                .zIndex(if (isDragged) 1f else 0f)
                                .graphicsLayer { translationY = displacement }
                                .onGloballyPositioned { if (index == 0) itemHeightPx = it.size.height.toFloat() }
                        ) {
                            DraggableTrackItem(
                                track = track,
                                showHandle = true,
                                selected = selectedIds.contains(track.id),
                                onTrackClick = {
                                    if (selectionMode) toggleSelect(track.id)
                                    else viewModel.playTrack(tracksList.toList(), index)
                                },
                                onLongPress = { toggleSelect(track.id) },
                                onDragStart = { draggedIndex = index; dragOffsetY = 0f },
                                onDrag = { dy -> dragOffsetY += dy },
                                onDragEnd = {
                                    val src = draggedIndex ?: return@DraggableTrackItem
                                    val target = targetIndex(src)
                                    if (target != src) {
                                        val item = tracksList.removeAt(src)
                                        tracksList.add(target, item)
                                        scope.launch { viewModel.reorderPlaylistTracks(playlistId, src, target) }
                                    }
                                    draggedIndex = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = { draggedIndex = null; dragOffsetY = 0f },
                                onRemoveFromLibrary = { viewModel.hideTrack(track); tracksList.remove(track) },
                                onDeleteFromDevice = { requestDelete(track) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragged) 1f else 0f)
                                .graphicsLayer { translationY = displacement }
                                .onGloballyPositioned { if (index == 0) itemHeightPx = it.size.height.toFloat() }
                        ) {
                            DraggableTrackItem(
                                track = track,
                                showHandle = false,
                                selected = selectedIds.contains(track.id),
                                onTrackClick = {
                                    if (selectionMode) toggleSelect(track.id)
                                    else viewModel.playTrack(tracksList.toList(), index)
                                },
                                onLongPress = { toggleSelect(track.id) },
                                onDragStart = {},
                                onDrag = {},
                                onDragEnd = {},
                                onDragCancel = {},
                                onRemoveFromLibrary = { viewModel.hideTrack(track); tracksList.remove(track) },
                                onDeleteFromDevice = { requestDelete(track) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTracksDialog) {
        AddTracksToPlaylistDialog(
            allTracks = allTracks,
            currentTracks = tracksList.toList(),
            onDismiss = { showAddTracksDialog = false },
            onAddTracks = { selected ->
                scope.launch {
                    selected.forEach { viewModel.addTrackToPlaylist(playlistId, it) }
                    tracksList.clear()
                    tracksList.addAll(viewModel.getPlaylistTracks(playlistId))
                    showAddTracksDialog = false
                }
            }
        )
    }

    // ── Bulk: add to another playlist ─────────────────────────────────────────
    if (showBulkPlaylistDialog) {
        val selectedTracks = tracksList.filter { it.id in selectedIds }
        PlaylistSelectionDialog(
            playlists = playlists.filter { !it.isSystemPlaylist || it.id == 1L },
            onDismiss = { showBulkPlaylistDialog = false },
            onPlaylistSelected = { target ->
                scope.launch {
                    selectedTracks.forEach { viewModel.addTrackToPlaylist(target.id, it) }
                    showBulkPlaylistDialog = false
                    clearSelection()
                }
            }
        )
    }

    // ── Bulk: remove-from-playlist OR delete-from-device ──────────────────────
    if (showBulkDeleteDialog) {
        val selectedTracks = tracksList.filter { it.id in selectedIds }
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Remove ${selectedTracks.size} track${if (selectedTracks.size == 1) "" else "s"}") },
            text = { Text("Remove from this playlist only, or permanently delete the files from your device?") },
            confirmButton = {
                TextButton(onClick = {
                    showBulkDeleteDialog = false
                    bulkPendingDelete = selectedTracks
                    try {
                        val uris = selectedTracks.map { Uri.parse(it.songPath) }
                        val intentSender = MediaStore.createDeleteRequest(
                            context.contentResolver, uris
                        ).intentSender
                        bulkDeleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    } catch (e: Exception) {
                        bulkPendingDelete = emptyList()
                    }
                }) { Text("Delete from device", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                Row {
                    if (canModify) {
                        TextButton(onClick = {
                            scope.launch {
                                selectedTracks.forEach {
                                    viewModel.removeTrackFromPlaylist(playlistId, it.id)
                                    tracksList.remove(it)
                                }
                                showBulkDeleteDialog = false
                                clearSelection()
                            }
                        }) { Text("Remove from playlist") }
                    }
                    TextButton(onClick = { showBulkDeleteDialog = false }) { Text("Cancel") }
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun DraggableTrackItem(
    track: MusicModel,
    showHandle: Boolean,
    selected: Boolean = false,
    onTrackClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onDragStart: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
    onDeleteFromDevice: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
             else MaterialTheme.colorScheme.surface
    ListItem(
        headlineContent = { Text(track.songName, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        colors = ListItemDefaults.colors(containerColor = bg),
        leadingContent = {
            GlideImage(
                model = track.albumArtUrl ?: "",
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    var showItemMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showItemMenu = true }) {
                        Icon(Icons.Default.MoreVert, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Remove from Library") },
                            onClick = { showItemMenu = false; onRemoveFromLibrary() },
                            leadingIcon = { Icon(Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete from Device") },
                            onClick = { showItemMenu = false; onDeleteFromDevice() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
                if (showHandle) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { onDragStart() },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        onDrag(amount.y)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragCancel() }
                                )
                            }
                    )
                }
            }
        },
        modifier = Modifier
            .combinedClickable(onClick = onTrackClick, onLongClick = onLongPress)
    )
}

@Composable
fun AddTracksToPlaylistDialog(
    allTracks: List<MusicModel>,
    currentTracks: List<MusicModel>,
    onDismiss: () -> Unit,
    onAddTracks: (List<MusicModel>) -> Unit
) {
    var selectedTracks by remember { mutableStateOf<Set<MusicModel>>(emptySet()) }
    val available = remember(allTracks, currentTracks) {
        allTracks.filter { track -> currentTracks.none { it.id == track.id } }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tracks to Playlist") },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                itemsIndexed(available) { _, track ->
                    val isSelected = selectedTracks.contains(track)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTracks = if (isSelected) selectedTracks - track else selectedTracks + track
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = { checked ->
                            selectedTracks = if (checked) selectedTracks + track else selectedTracks - track
                        })
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
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
            ) { Text("Add ${selectedTracks.size} Track${if (selectedTracks.size != 1) "s" else ""}") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
