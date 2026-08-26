package com.jegly.frequency.ui.screens

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.model.PlaylistModel
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.viewmodel.MainViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class SortOrder { TITLE, ARTIST, DURATION, DATE_ADDED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val hasNewTracks by viewModel.hasNewTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    var isGridView by remember { mutableStateOf(AppSettings.isGridViewEnabled(context)) }
    var spanCount by remember { mutableIntStateOf(AppSettings.getPortraitGridSpanCount(context)) }
    var thumbnailSizeDp by remember { mutableIntStateOf(AppSettings.getListThumbnailSize(context)) }
    var listCompact by remember { mutableStateOf(AppSettings.isListCompactMode(context)) }
    var listRowPadding by remember { mutableIntStateOf(AppSettings.getListRowPadding(context)) }
    var gridSpacing by remember { mutableIntStateOf(AppSettings.getGridSpacing(context)) }
    var gridShowInfo by remember { mutableStateOf(AppSettings.isGridShowInfo(context)) }
    var showViewOptions by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedTrack by remember { mutableStateOf<MusicModel?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var pendingTrackForNewPlaylist by remember { mutableStateOf<MusicModel?>(null) }
    val scope = rememberCoroutineScope()

    var trackPendingDelete by remember { mutableStateOf<MusicModel?>(null) }

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
            bulkPendingDelete.forEach { viewModel.removeTrackFromLibrary(it) }
        }
        bulkPendingDelete = emptyList()
        selectedIds.clear()
    }

    fun clearSelection() { selectedIds.clear() }
    fun toggleSelect(id: Long) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            trackPendingDelete?.let { viewModel.removeTrackFromLibrary(it) }
        }
        trackPendingDelete = null
    }

    val displayedTracks = remember(allTracks, searchQuery, sortOrder) {
        val base = if (searchQuery.isBlank()) allTracks else {
            val q = searchQuery.trim().lowercase()
            allTracks.filter {
                it.songName.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
        when (sortOrder) {
            SortOrder.TITLE -> base.sortedBy { it.songName.lowercase() }
            SortOrder.ARTIST -> base.sortedBy { it.artist.lowercase() }
            SortOrder.DURATION -> base.sortedBy { it.duration }
            SortOrder.DATE_ADDED -> base.sortedByDescending { it.dateAdded }
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
                    title = { Text("Library") },
                    actions = {
                        IconButton(onClick = { showViewOptions = true }) {
                            Icon(
                                if (isGridView) Icons.Default.GridView else Icons.Default.ViewList,
                                contentDescription = "View options"
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                listOf(
                                    SortOrder.TITLE to "Title",
                                    SortOrder.ARTIST to "Artist",
                                    SortOrder.DURATION to "Duration",
                                    SortOrder.DATE_ADDED to "Date Added"
                                ).forEach { (order, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { sortOrder = order; showSortMenu = false },
                                        leadingIcon = {
                                            if (sortOrder == order) Icon(Icons.Default.Check, null)
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${displayedTracks.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(spanCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = gridSpacing.dp, end = gridSpacing.dp,
                        top = gridSpacing.dp, bottom = 96.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing.dp),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing.dp)
                ) {
                    if (hasNewTracks) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(spanCount) }) {
                            NewMediaBanner(onDismiss = { viewModel.dismissNewTracksNotice() })
                        }
                    }
                    itemsIndexed(displayedTracks) { index, track ->
                        GridTrackItem(
                            track = track,
                            isPlaying = track.id == currentTrack?.id,
                            selected = selectedIds.contains(track.id),
                            onClick = {
                                if (selectionMode) toggleSelect(track.id)
                                else viewModel.playTrack(displayedTracks, index)
                            },
                            onLongClick = { toggleSelect(track.id) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (hasNewTracks) {
                        item { NewMediaBanner(onDismiss = { viewModel.dismissNewTracksNotice() }) }
                    }
                    itemsIndexed(displayedTracks) { index, track ->
                        LibraryTrackItem(
                            track = track,
                            isPlaying = track.id == currentTrack?.id,
                            selected = selectedIds.contains(track.id),
                            onTrackClick = {
                                if (selectionMode) toggleSelect(track.id)
                                else viewModel.playTrack(displayedTracks, index)
                            },
                            onLongPress = { toggleSelect(track.id) },
                            onMenuClick = { selectedTrack = track; showMenu = true },
                            thumbnailSizeDp = thumbnailSizeDp,
                            compact = listCompact,
                            rowPaddingDp = listRowPadding
                        )
                    }
                }
            }
        }

        if (showMenu && selectedTrack != null) {
            TrackMenuBottomSheet(
                track = selectedTrack!!,
                onDismiss = { showMenu = false },
                onPlayNext = { },
                onAddToQueue = { },
                onAddToPlaylist = { showMenu = false; showPlaylistDialog = true },
                onRemoveFromLibrary = {
                    showMenu = false
                    selectedTrack?.let { viewModel.hideTrack(it) }
                },
                onDeleteFromDevice = {
                    showMenu = false
                    selectedTrack?.let { track ->
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
                }
            )
        }

        if (showViewOptions) {
            ViewOptionsSheet(
                isGridView = isGridView,
                spanCount = spanCount,
                thumbnailSizeDp = thumbnailSizeDp,
                listCompact = listCompact,
                listRowPadding = listRowPadding,
                gridSpacing = gridSpacing,
                gridShowInfo = gridShowInfo,
                onViewChange = { newGrid ->
                    isGridView = newGrid
                    AppSettings.setGridViewEnabled(context, newGrid)
                },
                onSpanCountChange = { newCount ->
                    spanCount = newCount
                    AppSettings.savePortraitGridSpanCount(context, newCount)
                },
                onThumbnailSizeChange = { sz ->
                    thumbnailSizeDp = sz
                    AppSettings.setListThumbnailSize(context, sz)
                },
                onListCompactChange = { compact ->
                    listCompact = compact
                    AppSettings.setListCompactMode(context, compact)
                },
                onListRowPaddingChange = { p ->
                    listRowPadding = p
                    AppSettings.setListRowPadding(context, p)
                },
                onGridSpacingChange = { sp ->
                    gridSpacing = sp
                    AppSettings.setGridSpacing(context, sp)
                },
                onGridShowInfoChange = { show ->
                    gridShowInfo = show
                    AppSettings.setGridShowInfo(context, show)
                },
                onDismiss = { showViewOptions = false }
            )
        }

        if (showPlaylistDialog && selectedTrack != null) {
            PlaylistSelectionDialog(
                playlists = playlists.filter { !it.isSystemPlaylist || it.id == 1L },
                onDismiss = { showPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    scope.launch {
                        viewModel.addTrackToPlaylist(playlist.id, selectedTrack!!)
                        showPlaylistDialog = false
                    }
                },
                onCreatePlaylist = {
                    pendingTrackForNewPlaylist = selectedTrack
                    showPlaylistDialog = false
                    showCreatePlaylistDialog = true
                }
            )
        }

        // ── Bulk: add to playlist ─────────────────────────────────────────────
        if (showBulkPlaylistDialog) {
            val selectedTracks = displayedTracks.filter { it.id in selectedIds }
            PlaylistSelectionDialog(
                playlists = playlists.filter { !it.isSystemPlaylist || it.id == 1L },
                onDismiss = { showBulkPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    scope.launch {
                        selectedTracks.forEach { viewModel.addTrackToPlaylist(playlist.id, it) }
                        showBulkPlaylistDialog = false
                        clearSelection()
                    }
                },
                onCreatePlaylist = {
                    pendingTrackForNewPlaylist = null
                    showBulkPlaylistDialog = false
                    showCreatePlaylistDialog = true
                }
            )
        }

        if (showCreatePlaylistDialog) {
            var newPlaylistName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                scope.launch {
                                    val id = viewModel.createPlaylist(newPlaylistName.trim())
                                    pendingTrackForNewPlaylist?.let { viewModel.addTrackToPlaylist(id, it) }
                                    if (pendingTrackForNewPlaylist == null) {
                                        val bulk = displayedTracks.filter { it.id in selectedIds }
                                        bulk.forEach { viewModel.addTrackToPlaylist(id, it) }
                                        clearSelection()
                                    }
                                    pendingTrackForNewPlaylist = null
                                    showCreatePlaylistDialog = false
                                }
                            }
                        },
                        enabled = newPlaylistName.isNotBlank()
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
                }
            )
        }

        // ── Bulk: delete (hide-from-library vs delete-from-device) ────────────
        if (showBulkDeleteDialog) {
            val selectedTracks = displayedTracks.filter { it.id in selectedIds }
            AlertDialog(
                onDismissRequest = { showBulkDeleteDialog = false },
                title = { Text("Delete ${selectedTracks.size} track${if (selectedTracks.size == 1) "" else "s"}") },
                text = { Text("Hide them from your library, or permanently delete the files from your device?") },
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
                        TextButton(onClick = {
                            selectedTracks.forEach { viewModel.hideTrack(it) }
                            showBulkDeleteDialog = false
                            clearSelection()
                        }) { Text("Hide from library") }
                        TextButton(onClick = { showBulkDeleteDialog = false }) { Text("Cancel") }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun GridTrackItem(
    track: MusicModel,
    isPlaying: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(width = if (selected) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    Icon(
                        Icons.Default.Equalizer,
                        contentDescription = "Now playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    track.songName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                track.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NewMediaBanner(onDismiss: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LibraryAdd, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text("New music found on your device",
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryTrackItem(
    track: MusicModel,
    isPlaying: Boolean = false,
    selected: Boolean = false,
    onTrackClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onMenuClick: () -> Unit,
    thumbnailSizeDp: Int = 50,
    compact: Boolean = false,
    rowPaddingDp: Int = 0
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val baseHeight = if (compact) 56.dp else 72.dp
    val rowHeight = (baseHeight + rowPaddingDp.dp).coerceAtLeast(28.dp)
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    Icon(
                        Icons.Default.Equalizer,
                        contentDescription = "Now playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    track.songName,
                    maxLines = 1,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        supportingContent = if (!compact) { { Text(track.artist, maxLines = 1) } } else null,
        leadingContent = if (thumbnailSizeDp <= 0) null else { {
            GlideImage(
                model = track.albumArtUrl ?: "",
                contentDescription = null,
                modifier = Modifier.size(thumbnailSizeDp.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        } },
        trailingContent = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.MoreVert, "More") }
        },
        colors = ListItemDefaults.colors(containerColor = bg),
        modifier = Modifier
            .combinedClickable(onClick = onTrackClick, onLongClick = onLongPress)
            .height(rowHeight)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMenuBottomSheet(
    track: MusicModel,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
    onDeleteFromDevice: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(track.songName, style = MaterialTheme.typography.titleLarge)
            Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            ListItem(headlineContent = { Text("Play Next") },
                modifier = Modifier.clickable { onPlayNext(); onDismiss() })
            ListItem(headlineContent = { Text("Add to Queue") },
                modifier = Modifier.clickable { onAddToQueue(); onDismiss() })
            ListItem(headlineContent = { Text("Add to Playlist") },
                modifier = Modifier.clickable { onAddToPlaylist() })
            ListItem(
                headlineContent = { Text("Remove from Library") },
                supportingContent = { Text("Hides this track; won't delete the file") },
                leadingContent = { Icon(Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onRemoveFromLibrary(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Delete from Device") },
                supportingContent = { Text("Permanently deletes the file") },
                leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { onDeleteFromDevice(); onDismiss() }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlaylistSelectionDialog(
    playlists: List<PlaylistModel>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistModel) -> Unit,
    onCreatePlaylist: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onCreatePlaylist?.invoke() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("New Playlist…", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                itemsIndexed(playlists) { _, playlist ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onPlaylistSelected(playlist) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (playlist.isSystemPlaylist && playlist.name == "Favorite")
                                Icons.Default.Favorite else Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewOptionsSheet(
    isGridView: Boolean,
    spanCount: Int,
    thumbnailSizeDp: Int,
    listCompact: Boolean,
    listRowPadding: Int,
    gridSpacing: Int,
    gridShowInfo: Boolean,
    onViewChange: (Boolean) -> Unit,
    onSpanCountChange: (Int) -> Unit,
    onThumbnailSizeChange: (Int) -> Unit,
    onListCompactChange: (Boolean) -> Unit,
    onListRowPaddingChange: (Int) -> Unit,
    onGridSpacingChange: (Int) -> Unit,
    onGridShowInfoChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            Text("View", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = !isGridView,
                    onClick = { onViewChange(false) },
                    label = { Text("List") },
                    leadingIcon = { Icon(Icons.Default.ViewList, null, Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = isGridView,
                    onClick = { onViewChange(true) },
                    label = { Text("Grid") },
                    leadingIcon = { Icon(Icons.Default.GridView, null, Modifier.size(18.dp)) }
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            if (!isGridView) {
                // ── List options ──────────────────────────────────
                SliderRow(
                    label = "Thumbnail size",
                    value = thumbnailSizeDp.toFloat(),
                    display = if (thumbnailSizeDp == 0) "Hidden" else "${thumbnailSizeDp}dp",
                    range = 0f..72f,
                    onValueChange = { onThumbnailSizeChange(it.roundToInt()) }
                )
                Spacer(Modifier.height(12.dp))
                ToggleRow(
                    label = "Hide artist subtitle",
                    description = "Show only the track title",
                    checked = listCompact,
                    onCheckedChange = onListCompactChange
                )
                Spacer(Modifier.height(12.dp))
                SliderRow(
                    label = "Row density",
                    value = listRowPadding.toFloat(),
                    display = when {
                        listRowPadding <= -20 -> "Tiny"
                        listRowPadding <= -12 -> "Ultra-compact"
                        listRowPadding < 0    -> "Compact"
                        listRowPadding == 0   -> "Default"
                        listRowPadding < 8    -> "Roomy"
                        else                  -> "Spacious"
                    },
                    range = -28f..16f,
                    steps = 21,
                    onValueChange = { onListRowPaddingChange(it.roundToInt()) }
                )
            } else {
                // ── Grid options ──────────────────────────────────
                SliderRow(
                    label = "Columns",
                    value = spanCount.toFloat(),
                    display = "$spanCount",
                    range = 2f..6f,
                    steps = 3,
                    onValueChange = { onSpanCountChange(it.roundToInt()) }
                )
                Spacer(Modifier.height(12.dp))
                SliderRow(
                    label = "Item spacing",
                    value = gridSpacing.toFloat(),
                    display = "${gridSpacing}dp",
                    range = 2f..20f,
                    onValueChange = { onGridSpacingChange(it.roundToInt()) }
                )
                Spacer(Modifier.height(12.dp))
                ToggleRow(
                    label = "Show track info",
                    description = "Title and artist overlay on grid items",
                    checked = gridShowInfo,
                    onCheckedChange = onGridShowInfoChange
                )
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    display: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(display, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary)
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        steps = steps,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
