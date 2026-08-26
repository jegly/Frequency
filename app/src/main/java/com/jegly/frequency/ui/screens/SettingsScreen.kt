package com.jegly.frequency.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.jegly.frequency.playback.LocalPlayback
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jegly.frequency.ui.theme.AppFont
import com.jegly.frequency.ui.theme.CatppuccinAccent
import com.jegly.frequency.ui.theme.CatppuccinFlavor
import com.jegly.frequency.ui.theme.DraculaAccent
import com.jegly.frequency.ui.theme.allPtyxisPalettes
import com.jegly.frequency.ui.theme.buildPtyxisAnyColorScheme
import com.jegly.frequency.ui.theme.catppuccinAccentColor
import com.jegly.frequency.ui.theme.catppuccinFlavorFromKey
import com.jegly.frequency.ui.theme.draculaAccentColor
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.utils.AudioOutputs
import com.jegly.frequency.viewmodel.MainViewModel
import com.jegly.frequency.ToneService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    var biometricEnabled by remember { mutableStateOf(AppSettings.isBiometricEnabled(context)) }
    var checkNewMedia by remember { mutableStateOf(AppSettings.isCheckNewMediaEnabled(context)) }
    var isGridView by remember { mutableStateOf(AppSettings.isGridViewEnabled(context)) }
    var crossfadeEnabled by remember { mutableStateOf(AppSettings.isCrossfadeEnabled(context)) }
    var gaplessEnabled by remember { mutableStateOf(AppSettings.isGaplessEnabled(context)) }
    var autoRemoveDuplicates by remember { mutableStateOf(AppSettings.isAutoRemoveDuplicates(context)) }
    val miniPlayerHidden by viewModel.miniPlayerHidden.collectAsState()
    var preferredDeviceId by remember { mutableIntStateOf(AppSettings.getPreferredAudioDeviceId(context)) }
    var audioPickerOpen   by remember { mutableStateOf(false) }

    val themeMode by viewModel.themeMode.collectAsState()
    val catppuccinAccent by viewModel.catppuccinAccent.collectAsState()
    val catppuccinFlavor by viewModel.catppuccinFlavor.collectAsState()
    val draculaAccent by viewModel.draculaAccent.collectAsState()
    val ptyxisPalette by viewModel.ptyxisPalette.collectAsState()
    val monochromeAccents by viewModel.monochromeAccents.collectAsState()
    val appFont by viewModel.appFont.collectAsState()

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
            contentPadding = PaddingValues(bottom = 96.dp)
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

            item {
                ListItem(
                    headlineContent = { Text("Auto-remove duplicates") },
                    supportingContent = { Text("Remove duplicate tracks when loading library") },
                    trailingContent = {
                        Switch(
                            checked = autoRemoveDuplicates,
                            onCheckedChange = {
                                autoRemoveDuplicates = it
                                AppSettings.setAutoRemoveDuplicates(context, it)
                            }
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Remove duplicates now") },
                    supportingContent = { Text("Deduplicate current library by title + artist") },
                    leadingContent = {
                        Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable { viewModel.removeDuplicates() }
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
                    headlineContent = { Text("Hide mini-player") },
                    supportingContent = { Text("Hide the floating bar above the nav bar") },
                    trailingContent = {
                        Switch(
                            checked = miniPlayerHidden,
                            onCheckedChange = { viewModel.setMiniPlayerHidden(it) }
                        )
                    }
                )
            }

            item {
                val currentLabel = remember(preferredDeviceId) {
                    if (preferredDeviceId == AppSettings.AUDIO_DEVICE_SYSTEM) "System default"
                    else AudioOutputs.listOutputs(context).firstOrNull { it.id == preferredDeviceId }?.name
                        ?: "System default"
                }
                ListItem(
                    headlineContent = { Text("Audio output") },
                    supportingContent = { Text(currentLabel) },
                    leadingContent = {
                        Icon(Icons.Default.Speaker, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable { audioPickerOpen = true }
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
                        "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val themeOptions = listOf(
                        "system" to "System", "light" to "Light", "catppuccin" to "Catppuccin",
                        "dracula" to "Dracula", "ptyxis" to "Ptyxis"
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { i, (key, label) ->
                            SegmentedButton(
                                selected = themeMode == key,
                                onClick  = { viewModel.setThemeMode(key) },
                                shape    = SegmentedButtonDefaults.itemShape(i, themeOptions.size)
                            ) { Text(label, maxLines = 1) }
                        }
                    }

                    if (themeMode == "catppuccin") {
                        Spacer(modifier = Modifier.height(12.dp))
                        val flavor = catppuccinFlavorFromKey(catppuccinFlavor)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CatppuccinFlavor.entries) { f ->
                                FilterChip(
                                    selected = f == flavor,
                                    onClick  = { viewModel.setCatppuccinFlavor(f.key) },
                                    label    = { Text(f.displayName) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(CatppuccinAccent.entries) { accent ->
                                AccentSwatch(
                                    color = catppuccinAccentColor(flavor, accent),
                                    isSelected = accent.key == catppuccinAccent,
                                    onClick = { viewModel.setCatppuccinAccent(accent.key) }
                                )
                            }
                        }
                    }

                    if (themeMode == "dracula") {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(DraculaAccent.entries) { accent ->
                                AccentSwatch(
                                    color = draculaAccentColor(dark = true, accent = accent),
                                    isSelected = accent.key == draculaAccent,
                                    onClick = { viewModel.setDraculaAccent(accent.key) }
                                )
                            }
                        }
                    }

                    if (themeMode == "catppuccin" || themeMode == "dracula") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monochrome accents", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = monochromeAccents,
                                onCheckedChange = { viewModel.setMonochromeAccents(it) }
                            )
                        }
                    }

                    if (themeMode == "ptyxis") {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(allPtyxisPalettes, key = { it.key }) { entry ->
                                val isSelected = entry.key == ptyxisPalette
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setPtyxisPalette(entry.key) }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(buildPtyxisAnyColorScheme(entry.key).primary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(entry.displayName, style = MaterialTheme.typography.bodyMedium)
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Check, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Font",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AppFont.entries) { font ->
                            FilterChip(
                                selected = font.key == appFont,
                                onClick  = { viewModel.setAppFont(font.key) },
                                label    = { Text(font.displayName) }
                            )
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
                    headlineContent = { Text("Frequency") },
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

    if (audioPickerOpen) {
        AlertDialog(
            onDismissRequest = { audioPickerOpen = false },
            title = { Text("Audio output") },
            text = {
                Column {
                    val devices = AudioOutputs.listOutputs(context)
                    val options = listOf(AppSettings.AUDIO_DEVICE_SYSTEM to "System default") +
                        devices.map { it.id to it.name }
                    options.forEach { (id, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferredDeviceId = id
                                    AppSettings.setPreferredAudioDeviceId(context, id)
                                    context.startService(
                                        Intent(context, ToneService::class.java).apply {
                                            action = ToneService.ACTION_SET_DEVICE
                                        }
                                    )
                                    audioPickerOpen = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = id == preferredDeviceId,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { audioPickerOpen = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun AccentSwatch(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
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
