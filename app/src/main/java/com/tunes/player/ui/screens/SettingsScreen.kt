package com.tunes.player.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.tunes.player.playback.LocalPlayback
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tunes.player.utils.AppSettings
import com.tunes.player.viewmodel.MainViewModel

private data class AccentOption(val label: String, val colorInt: Int)

private val accentOptions = listOf(
    AccentOption("Default", AppSettings.ACCENT_NONE),
    AccentOption("Green",   0xFF22C55E.toInt()),
    AccentOption("Teal",    0xFF14B8A6.toInt()),
    AccentOption("Blue",    0xFF3B82F6.toInt()),
    AccentOption("Purple",  0xFFA855F7.toInt()),
    AccentOption("Orange",  0xFFF97316.toInt()),
    AccentOption("Rose",    0xFFF43F5E.toInt()),
    AccentOption("Yellow",  0xFFEAB308.toInt()),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    var biometricEnabled by remember { mutableStateOf(AppSettings.isBiometricEnabled(context)) }
    var checkNewMedia by remember { mutableStateOf(AppSettings.isCheckNewMediaEnabled(context)) }
    var isGridView by remember { mutableStateOf(AppSettings.isGridViewEnabled(context)) }
    var selectedAccent by remember { mutableStateOf(AppSettings.getAccentColor(context)) }
    var crossfadeEnabled by remember { mutableStateOf(AppSettings.isCrossfadeEnabled(context)) }
    var gaplessEnabled by remember { mutableStateOf(AppSettings.isGaplessEnabled(context)) }

    val equalizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.importMusicFolder(it)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            viewModel.importMusicFiles(uris)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Library ──────────────────────────────────────────
            item { SettingsSectionHeader("Library") }

            item {
                ListItem(
                    headlineContent = { Text("Check for new media on launch") },
                    supportingContent = { Text("Notify when new songs are detected") },
                    trailingContent = {
                        Switch(
                            checked = checkNewMedia,
                            onCheckedChange = {
                                checkNewMedia = it
                                AppSettings.setCheckNewMediaEnabled(context, it)
                            }
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Import Music Folder") },
                    supportingContent = { Text("Add an entire folder to your library") },
                    leadingContent = {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable { folderPickerLauncher.launch(null) }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Import Music Files") },
                    supportingContent = { Text("Add individual audio files") },
                    leadingContent = {
                        Icon(Icons.Default.AudioFile, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable {
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Playback ──────────────────────────────────────────
            item { SettingsSectionHeader("Playback") }

            item {
                ListItem(
                    headlineContent = { Text("Crossfade") },
                    supportingContent = { Text("Smoothly fade between tracks (5 seconds)") },
                    trailingContent = {
                        Switch(
                            checked = crossfadeEnabled,
                            onCheckedChange = {
                                crossfadeEnabled = it
                                if (it) {
                                    gaplessEnabled = false
                                    AppSettings.setGaplessEnabled(context, false)
                                }
                                AppSettings.setCrossfadeEnabled(context, it)
                            }
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Gapless Playback") },
                    supportingContent = { Text("Remove silence between tracks") },
                    trailingContent = {
                        Switch(
                            checked = gaplessEnabled,
                            onCheckedChange = {
                                gaplessEnabled = it
                                if (it) {
                                    crossfadeEnabled = false
                                    AppSettings.setCrossfadeEnabled(context, false)
                                }
                                AppSettings.setGaplessEnabled(context, it)
                            }
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Equalizer") },
                    supportingContent = { Text("Adjust sound frequencies") },
                    leadingContent = {
                        Icon(Icons.Default.Equalizer, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable {
                        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, LocalPlayback.audioSessionId)
                            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                        }
                        try {
                            equalizerLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No equalizer found on this device", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Appearance ───────────────────────────────────────
            item { SettingsSectionHeader("Appearance") }

            item {
                ListItem(
                    headlineContent = { Text("Library View") },
                    supportingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            FilterChip(
                                selected = !isGridView,
                                onClick = {
                                    isGridView = false
                                    AppSettings.setGridViewEnabled(context, false)
                                },
                                label = { Text("List") },
                                leadingIcon = {
                                    Icon(Icons.Default.ViewList, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                }
                            )
                            FilterChip(
                                selected = isGridView,
                                onClick = {
                                    isGridView = true
                                    AppSettings.setGridViewEnabled(context, true)
                                },
                                label = { Text("Grid") },
                                leadingIcon = {
                                    Icon(Icons.Default.GridView, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Accent Colour",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(accentOptions) { option ->
                            val displayColor = if (option.colorInt == AppSettings.ACCENT_NONE)
                                MaterialTheme.colorScheme.primary
                            else
                                Color(option.colorInt)
                            val isSelected = selectedAccent == option.colorInt

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(displayColor)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable {
                                        selectedAccent = option.colorInt
                                        viewModel.setAccentColor(option.colorInt)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (displayColor.luminance() > 0.4f)
                                            Color(0xFF1A1C19) else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Privacy ──────────────────────────────────────────
            item { SettingsSectionHeader("Privacy") }

            item {
                ListItem(
                    headlineContent = { Text("Biometric Lock") },
                    supportingContent = { Text("Require authentication to open the app") },
                    trailingContent = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                AppSettings.setBiometricEnabled(context, it)
                            }
                        )
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── About ────────────────────────────────────────────
            item { SettingsSectionHeader("About") }

            item {
                ListItem(
                    headlineContent = { Text("Tunes") },
                    supportingContent = {
                        Column {
                            Text("Dev: jegly")
                            Text("www.jegly.xyz")
                        }
                    },
                    leadingContent = {
                        Icon(Icons.Default.MusicNote, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}
