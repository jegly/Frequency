package com.jegly.frequency.ui.screens

import android.media.session.PlaybackState
import android.text.format.DateUtils
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.platform.LocalContext
import com.jegly.frequency.utils.AppSettings
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.jegly.frequency.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NowPlayingScreen(viewModel: MainViewModel) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val isPlaying = playbackState == PlaybackState.STATE_PLAYING
    val duration = currentTrack?.duration ?: 0L

    var playbackSpeed by remember { mutableStateOf(AppSettings.getPlaybackSpeed(context)) }

    val trackManager = com.jegly.frequency.singleton.TrackManager.instance
    var isShuffleEnabled by remember { mutableStateOf(trackManager.isShuffleEnabled) }
    var isRepeatCurrentTrack by remember { mutableStateOf(trackManager.repeatCurrentTrack) }
    var isInfiniteLoopEnabled by remember { mutableStateOf(trackManager.isInfiniteLoopEnabled) }

    fun updatePlaybackStates() {
        isShuffleEnabled = trackManager.isShuffleEnabled
        isRepeatCurrentTrack = trackManager.repeatCurrentTrack
        isInfiniteLoopEnabled = trackManager.isInfiniteLoopEnabled
    }

    var showSpeedControl by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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

        Spacer(modifier = Modifier.height(40.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        // Menu Button
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waveform Seek Bar
        WaveformSeekBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { viewModel.seekTo(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                DateUtils.formatElapsedTime(currentPosition / 1000),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                DateUtils.formatElapsedTime(duration / 1000),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Playback Controls
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
                    if (isRepeatCurrentTrack) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeatCurrentTrack || isInfiniteLoopEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        // Speed control
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSpeedControl = !showSpeedControl }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Speed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${((playbackSpeed * 100).roundToInt() / 100f)}×",
                style = MaterialTheme.typography.labelMedium,
                color = if (playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                if (showSpeedControl) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showSpeedControl) {
            Slider(
                value = playbackSpeed,
                onValueChange = { playbackSpeed = it },
                onValueChangeFinished = { viewModel.setPlaybackSpeed(playbackSpeed) },
                valueRange = 0.5f..2.0f,
                steps = 5,
                modifier = Modifier.fillMaxWidth()
            )
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
            onClick = { showMenu = false; showPlaylistDialog = true },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Create New Playlist") },
            onClick = { showMenu = false; showCreatePlaylistDialog = true },
            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
        )
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPlaylistDialog = false; showCreatePlaylistDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("New Playlist…", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
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
                                imageVector = if (playlist.isSystemPlaylist && playlist.name == "Favorite")
                                    Icons.Default.Favorite
                                else Icons.AutoMirrored.Filled.PlaylistPlay,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }

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
                                currentTrack?.let { track ->
                                    val newPlaylist = viewModel.playlists.value.find { it.name == playlistName.trim() }
                                    newPlaylist?.let { viewModel.addTrackToPlaylist(it.id, track) }
                                }
                                showCreatePlaylistDialog = false
                            }
                        }
                    },
                    enabled = playlistName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WaveformSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    // Deterministic waveform heights — looks like real audio
    val waveHeights = remember {
        (0 until 52).map { i ->
            val t = i.toFloat() / 52f
            val h = abs(sin(t * 14.3f)) * 0.45f +
                    abs(sin(t * 6.7f + 0.8f)) * 0.3f +
                    abs(sin(t * 22.1f + 1.5f)) * 0.1f +
                    0.12f
            h.coerceIn(0.12f, 0.95f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "waveformProgress"
    )

    Canvas(
        modifier = modifier
            .height(52.dp)
            .pointerInput(duration) {
                detectTapGestures { offset ->
                    val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((ratio * duration).toLong())
                }
            }
            .pointerInput(duration) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeek((ratio * duration).toLong())
                }
            }
    ) {
        val barCount = waveHeights.size
        val gap = 3.5f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount

        waveHeights.forEachIndexed { i, heightFraction ->
            val x = i * (barWidth + gap)
            val barH = size.height * heightFraction
            val topY = (size.height - barH) / 2f
            val isPlayed = i.toFloat() / barCount <= animatedProgress

            drawRoundRect(
                color = if (isPlayed) primaryColor else trackColor,
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}
