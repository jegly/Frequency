package com.tunes.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.tunes.player.model.MusicModel
import com.tunes.player.model.PlaylistModel
import com.tunes.player.utils.AppSettings
import com.tunes.player.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private enum class SortOrder { TITLE, ARTIST, DURATION, DATE_ADDED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val hasNewTracks by viewModel.hasNewTracks.collectAsState()
    val isGridView = remember { AppSettings.isGridViewEnabled(context) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedTrack by remember { mutableStateOf<MusicModel?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            TopAppBar(
                title = { Text("Library") },
                actions = {
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
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasNewTracks) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            NewMediaBanner(onDismiss = { viewModel.dismissNewTracksNotice() })
                        }
                    }
                    itemsIndexed(displayedTracks) { index, track ->
                        GridTrackItem(track) { viewModel.playTrack(displayedTracks, index) }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (hasNewTracks) {
                        item { NewMediaBanner(onDismiss = { viewModel.dismissNewTracksNotice() }) }
                    }
                    itemsIndexed(displayedTracks) { index, track ->
                        LibraryTrackItem(
                            track = track,
                            onTrackClick = { viewModel.playTrack(displayedTracks, index) },
                            onMenuClick = { selectedTrack = track; showMenu = true }
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
                onAddToPlaylist = { showMenu = false; showPlaylistDialog = true }
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
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GridTrackItem(track: MusicModel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        GlideImage(
            model = track.albumArtUrl ?: "",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Text(track.songName, style = MaterialTheme.typography.labelLarge,
                    color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.artist, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryTrackItem(track: MusicModel, onTrackClick: () -> Unit, onMenuClick: () -> Unit) {
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
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.MoreVert, "More") }
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
            Spacer(Modifier.height(16.dp))
            ListItem(headlineContent = { Text("Play Next") },
                modifier = Modifier.clickable { onPlayNext(); onDismiss() })
            ListItem(headlineContent = { Text("Add to Queue") },
                modifier = Modifier.clickable { onAddToQueue(); onDismiss() })
            ListItem(headlineContent = { Text("Add to Playlist") },
                modifier = Modifier.clickable { onAddToPlaylist() })
            Spacer(Modifier.height(32.dp))
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
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
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
