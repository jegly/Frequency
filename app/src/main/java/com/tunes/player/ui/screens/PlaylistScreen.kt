package com.tunes.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tunes.player.model.MusicModel
import com.tunes.player.viewmodel.MainViewModel
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
                                onTrackClick = { viewModel.playTrack(tracksList.toList(), index) },
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
                                onDragCancel = { draggedIndex = null; dragOffsetY = 0f }
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
                                onTrackClick = { viewModel.playTrack(tracksList.toList(), index) },
                                onDragStart = {},
                                onDrag = {},
                                onDragEnd = {},
                                onDragCancel = {}
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
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun DraggableTrackItem(
    track: MusicModel,
    showHandle: Boolean,
    onTrackClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    ListItem(
        headlineContent = { Text(track.songName, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        leadingContent = {
            GlideImage(
                model = track.albumArtUrl ?: "",
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        },
        trailingContent = {
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
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onTrackClick() }
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
