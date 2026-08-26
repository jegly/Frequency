package com.jegly.frequency.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jegly.frequency.ToneService
import com.jegly.frequency.audio.ToneGenerator
import com.jegly.frequency.model.CustomPreset
import com.jegly.frequency.model.FrequencyPreset
import com.jegly.frequency.model.FrequencyPresets
import com.jegly.frequency.model.WaveCategory
import com.jegly.frequency.viewmodel.ToneGeneratorViewModel
import kotlin.math.*
import kotlin.math.roundToInt

private val TIMER_OPTIONS = listOf(
    0 to "Off", 300 to "5m", 600 to "10m", 900 to "15m",
    1200 to "20m", 1800 to "30m", 2700 to "45m", 3600 to "60m", 5400 to "90m"
)

private val EXPORT_DURATION_OPTIONS = listOf(
    30 to "30s", 60 to "1m", 300 to "5m", 600 to "10m", 1800 to "30m", 3600 to "60m"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyScreen() {
    // Activity-scoped so the ViewModel (and its state) survives navigation away from this tab
    val vm: ToneGeneratorViewModel = viewModel(LocalContext.current as ComponentActivity)

    val frequency      by vm.frequency.collectAsState()
    val waveform       by vm.waveform.collectAsState()
    val isPlaying      by vm.isPlaying.collectAsState()
    val selectedPreset by vm.selectedPreset.collectAsState()
    val selectedCat    by vm.selectedCategory.collectAsState()
    val playMode       by vm.playMode.collectAsState()
    val beatFrequency  by vm.beatFrequency.collectAsState()
    val mixFrequency   by vm.mixFrequency.collectAsState()
    val timerSeconds    by vm.timerSeconds.collectAsState()
    val timerRemaining  by vm.timerRemaining.collectAsState()
    val randomMode      by vm.randomMode.collectAsState()
    val randomInterval  by vm.randomInterval.collectAsState()
    val sweepStartFreq  by vm.sweepStartFreq.collectAsState()
    val sweepEndFreq    by vm.sweepEndFreq.collectAsState()
    val sweepSpeed      by vm.sweepSpeed.collectAsState()
    val sweepRandomness by vm.sweepRandomness.collectAsState()
    val endLfoEnabled   by vm.endLfoEnabled.collectAsState()
    val endLfoSpeed     by vm.endLfoSpeed.collectAsState()
    val endLfoDepth     by vm.endLfoDepth.collectAsState()
    val volume          by vm.volume.collectAsState()
    val toneEnabled     by vm.toneEnabled.collectAsState()
    val whiteNoiseOn    by vm.whiteNoiseOn.collectAsState()
    val pinkNoiseOn     by vm.pinkNoiseOn.collectAsState()
    val brownNoiseOn    by vm.brownNoiseOn.collectAsState()
    val isochronicOn     by vm.isochronicOn.collectAsState()
    val isochronicRate   by vm.isochronicRate.collectAsState()
    val isochronicSmooth by vm.isochronicSmooth.collectAsState()
    val filterOn         by vm.filterOn.collectAsState()
    val filterCutoff     by vm.filterCutoff.collectAsState()
    val filterResonance  by vm.filterResonance.collectAsState()
    val adsrOn           by vm.adsrOn.collectAsState()
    val adsrAttack       by vm.adsrAttack.collectAsState()
    val adsrDecay        by vm.adsrDecay.collectAsState()
    val adsrSustain      by vm.adsrSustain.collectAsState()
    val adsrRelease      by vm.adsrRelease.collectAsState()
    val customPresets    by vm.customPresets.collectAsState()
    val isExporting      by vm.isExporting.collectAsState()
    val exportProgress   by vm.exportProgress.collectAsState()
    val exportMessage    by vm.exportMessage.collectAsState()

    var showPresets by remember { mutableStateOf(false) }
    var showCustomPresets by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportNameInput by remember { mutableStateOf("Frequency Session") }
    var exportDurationSeconds by remember { mutableIntStateOf(300) }
    var exportStarted by remember { mutableStateOf(false) }
    LaunchedEffect(isExporting) {
        if (exportStarted && !isExporting) {
            showExportDialog = false
            exportStarted = false
        }
    }

    val context = LocalContext.current
    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearExportMessage()
        }
    }
    var manualInput by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf(false) }
    var mixInput    by remember { mutableStateOf("") }
    var mixError    by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val logMin    = ln(0.5)
    val freqCeil  = if (randomMode) 10_000.0 else 22_000.0
    val logMax    = ln(freqCeil)
    val sliderPos = ((ln(frequency.coerceIn(0.5, freqCeil)) - logMin) / (logMax - logMin)).toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frequency") },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Save current settings as preset"
                        )
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Export session as audio file"
                        )
                    }
                    IconButton(onClick = { showPresets = !showPresets }) {
                        Icon(
                            imageVector = if (showPresets) Icons.Default.KeyboardArrowDown
                                          else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (showPresets) "Hide presets" else "Browse presets"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Collapsible preset browser ────────────────────────────────────
            AnimatedVisibility(
                visible = showPresets,
                enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = showCustomPresets,
                                onClick  = { showCustomPresets = true },
                                label    = { Text("My Presets") }
                            )
                        }
                        items(WaveCategory.entries) { cat ->
                            FilterChip(
                                selected = !showCustomPresets && cat == selectedCat,
                                onClick  = { showCustomPresets = false; vm.setCategory(cat) },
                                label    = { Text(cat.label) }
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showCustomPresets) {
                            if (customPresets.isEmpty()) {
                                item {
                                    Text(
                                        "No saved presets yet — tap the bookmark icon above to save your current settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                            items(customPresets, key = { it.id }) { preset ->
                                CustomPresetRow(
                                    preset  = preset,
                                    onClick = { vm.applyCustomPreset(preset); showPresets = false },
                                    onDelete = { vm.deleteCustomPreset(preset) }
                                )
                            }
                        } else items(
                            FrequencyPresets.forCategory(selectedCat),
                            key = { "${it.category}${it.frequency}${it.name}" }
                        ) { preset ->
                            PresetRow(
                                preset   = preset,
                                selected = preset == selectedPreset,
                                onClick  = { vm.selectPreset(preset); showPresets = false }
                            )
                        }
                    }
                }
            }

            // ── Controls panel (fills remaining screen, scrollable) ───────────
            Surface(
                modifier        = Modifier.fillMaxWidth().weight(1f),
                tonalElevation  = 6.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Real-time scope showing what the engine is actually outputting
                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        modifier  = Modifier.fillMaxWidth().height(56.dp)
                    )

                    // ── Frequency + mode controls (always shown; noise mixes on top) ──
                    AnimatedVisibility(
                        visible = true,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) { Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                    // Frequency display
                    Text(
                        text  = formatHz(frequency),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Manual Hz input
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it; manualError = false },
                        label = { Text("Enter Hz manually") },
                        placeholder = { Text(formatHz(frequency)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            val hz = manualInput.toDoubleOrNull()
                            if (hz != null && hz in 0.5..freqCeil) {
                                vm.setFrequency(hz); manualInput = ""; focusManager.clearFocus()
                            } else { manualError = true }
                        }),
                        isError = manualError,
                        supportingText = if (manualError) {
                            { Text("Enter a value between 0.5 and ${if (randomMode) "10 000" else "22 000"} Hz") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Preset info
                    if (selectedPreset != null) {
                        Text(
                            text     = selectedPreset!!.name,
                            style    = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text      = selectedPreset!!.effect,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines  = 2,
                            overflow  = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── Mode selector ─────────────────────────────────────────
                    Text(
                        "Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    // Audio mode — single select (controls how sound is rendered)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            ToneGenerator.PlayMode.NORMAL   to "Normal",
                            ToneGenerator.PlayMode.BINAURAL to "Binaural",
                            ToneGenerator.PlayMode.MIX      to "Mix",
                            ToneGenerator.PlayMode.SWEEP    to "Sweep"
                        ).forEach { (mode, label) ->
                            val isSweep = mode == ToneGenerator.PlayMode.SWEEP
                            FilterChip(
                                selected = playMode == mode,
                                onClick  = {
                                    vm.setPlayMode(mode)
                                    if (isSweep && randomMode) vm.setRandomMode(false)
                                },
                                label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                enabled  = if (isSweep) !randomMode else true,
                                leadingIcon = if (isSweep) {
                                    { Icon(Icons.Default.GraphicEq, null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                    // Random — independent overlay, stacks on top of any audio mode except Sweep
                    FilterChip(
                        selected = randomMode,
                        onClick  = {
                            val next = !randomMode
                            vm.setRandomMode(next)
                            if (next && playMode == ToneGenerator.PlayMode.SWEEP) vm.setPlayMode(ToneGenerator.PlayMode.NORMAL)
                        },
                        label    = { Text("Random", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = playMode != ToneGenerator.PlayMode.SWEEP,
                        leadingIcon = { Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(14.dp)) }
                    )

                    // ── Binaural controls ─────────────────────────────────────
                    AnimatedVisibility(
                        visible = playMode == ToneGenerator.PlayMode.BINAURAL,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Beat frequency",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${formatHz(beatFrequency)}  ${beatLabel(beatFrequency)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = ((beatFrequency - 0.5) / (40.0 - 0.5)).toFloat(),
                                onValueChange = { vm.setBeatFrequency(0.5 + it * (40.0 - 0.5)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0.5 Hz  δ", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("40 Hz  γ", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // L / R labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    "L: ${formatHz(frequency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "R: ${formatHz(frequency + beatFrequency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Beat: ${formatHz(beatFrequency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // ── Mix controls ──────────────────────────────────────────
                    AnimatedVisibility(
                        visible = playMode == ToneGenerator.PlayMode.MIX,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Second frequency",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = mixInput,
                                onValueChange = { mixInput = it; mixError = false },
                                label = { Text("Hz") },
                                placeholder = { Text(formatHz(mixFrequency)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction    = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    val hz = mixInput.toDoubleOrNull()
                                    if (hz != null && hz in 0.5..22_000.0) {
                                        vm.setMixFrequency(hz); mixInput = ""; focusManager.clearFocus()
                                    } else { mixError = true }
                                }),
                                isError = mixError,
                                supportingText = if (mixError) {
                                    { Text("Enter a value between 0.5 and 22 000 Hz") }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Mix freq log-scale slider
                            val mixLogMin = ln(0.5)
                            val mixLogMax = ln(22_000.0)
                            val mixPos = ((ln(mixFrequency.coerceIn(0.5, 22_000.0)) - mixLogMin) / (mixLogMax - mixLogMin)).toFloat()
                            Slider(
                                value = mixPos,
                                onValueChange = { pos ->
                                    vm.setMixFrequency(exp(mixLogMin + pos * (mixLogMax - mixLogMin)))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Freq 1: ${formatHz(frequency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("Freq 2: ${formatHz(mixFrequency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    // ── Random controls ───────────────────────────────────────
                    AnimatedVisibility(
                        visible = randomMode,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Switch every",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatInterval(randomInterval),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            val rLogMin = ln(2.0)
                            val rLogMax = ln(1800.0)
                            val rPos = ((ln(randomInterval.toDouble().coerceIn(2.0, 1800.0)) - rLogMin) / (rLogMax - rLogMin)).toFloat()
                            Slider(
                                value = rPos,
                                onValueChange = { pos ->
                                    val secs = exp(rLogMin + pos * (rLogMax - rLogMin))
                                        .roundToInt().coerceIn(2, 1800)
                                    vm.setRandomInterval(secs)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2s", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("30m", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isPlaying) {
                                Text(
                                    "Randomly cycling through presets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // ── Sweep controls ────────────────────────────────────────
                    AnimatedVisibility(
                        visible = playMode == ToneGenerator.PlayMode.SWEEP,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val sweepLogMin = ln(0.5)
                            val sweepLogMax = ln(22_000.0)

                            // Start frequency
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Start",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatHz(sweepStartFreq),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = ((ln(sweepStartFreq.coerceIn(0.5, 22_000.0)) - sweepLogMin) / (sweepLogMax - sweepLogMin)).toFloat(),
                                onValueChange = { pos ->
                                    vm.setSweepStartFreq(exp(sweepLogMin + pos * (sweepLogMax - sweepLogMin)))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // End frequency
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("End",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatHz(sweepEndFreq),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = ((ln(sweepEndFreq.coerceIn(0.5, 22_000.0)) - sweepLogMin) / (sweepLogMax - sweepLogMin)).toFloat(),
                                onValueChange = { pos ->
                                    vm.setSweepEndFreq(exp(sweepLogMin + pos * (sweepLogMax - sweepLogMin)))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // End-freq LFO — automatically moves the end point
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Sweep end automatically",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Switch(
                                        checked = endLfoEnabled,
                                        onCheckedChange = { vm.setEndLfoEnabled(it) }
                                    )
                                }
                                AnimatedVisibility(
                                    visible = endLfoEnabled,
                                    enter   = expandVertically() + fadeIn(),
                                    exit    = shrinkVertically() + fadeOut()
                                ) {
                                    val eLfoLogMin = ln(0.005)
                                    val eLfoLogMax = ln(5.0)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        // Speed
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Speed",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatSweepPeriod(endLfoSpeed),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = ((ln(endLfoSpeed.coerceIn(0.005, 5.0)) - eLfoLogMin) / (eLfoLogMax - eLfoLogMin)).toFloat(),
                                            onValueChange = { pos ->
                                                vm.setEndLfoSpeed(exp(eLfoLogMin + pos * (eLfoLogMax - eLfoLogMin)))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        // Depth
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Depth",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(depthLabel(endLfoDepth),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = endLfoDepth.toFloat(),
                                            onValueChange = { vm.setEndLfoDepth(it.toDouble()) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Subtle", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Full range", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                            // Speed
                            val spdLogMin = ln(0.005)
                            val spdLogMax = ln(5.0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Speed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatSweepPeriod(sweepSpeed),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = ((ln(sweepSpeed.coerceIn(0.005, 5.0)) - spdLogMin) / (spdLogMax - spdLogMin)).toFloat(),
                                onValueChange = { pos ->
                                    vm.setSweepSpeed(exp(spdLogMin + pos * (spdLogMax - spdLogMin)))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Very slow", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Very fast", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                            // Randomness
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Randomness",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(randomnessLabel(sweepRandomness),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = sweepRandomness.toFloat(),
                                onValueChange = { vm.setSweepRandomness(it.toDouble()) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Smooth", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Wild", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── Frequency slider (hidden in Sweep mode — sweep has its own range pickers)
                    AnimatedVisibility(
                        visible = playMode != ToneGenerator.PlayMode.SWEEP,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(
                                value = sliderPos,
                                onValueChange = { pos ->
                                    vm.setFrequency(exp(logMin + pos * (logMax - logMin)))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0.5 Hz", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (randomMode) "10 kHz" else "22 kHz", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    }} // end AnimatedVisibility for frequency+mode controls

                    // ── Waveform selector ─────────────────────────────────────
                    // Oscillators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToneGenerator.Waveform.entries.forEach { wf ->
                            val selected = wf == waveform && toneEnabled
                            OutlinedButton(
                                onClick  = { vm.setWaveform(wf) },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    contentColor   = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border         = if (selected) ButtonDefaults.outlinedButtonBorder else null,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(wf.label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    // Noise mix — independent toggles, mixable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Noise",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp)
                        )
                        listOf(
                            Triple("White", whiteNoiseOn, vm::setWhiteNoise),
                            Triple("Pink",  pinkNoiseOn,  vm::setPinkNoise),
                            Triple("Brown", brownNoiseOn, vm::setBrownNoise)
                        ).forEach { (label, on, toggle) ->
                            OutlinedButton(
                                onClick  = { toggle(!on) },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (on) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    contentColor   = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border         = if (on) ButtonDefaults.outlinedButtonBorder else null,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── Isochronic ────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Isochronic",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = isochronicOn,
                            onCheckedChange = { vm.setIsochronicOn(it) }
                        )
                    }
                    AnimatedVisibility(
                        visible = isochronicOn,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Pulse rate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "%.2f Hz".format(isochronicRate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = isochronicRate.toFloat(),
                                onValueChange = { vm.setIsochronicRate(it.toDouble()) },
                                valueRange = 0.5f..40f
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Smooth pulses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = isochronicSmooth,
                                    onCheckedChange = { vm.setIsochronicSmooth(it) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── Low-pass filter ───────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Filter (low-pass)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = filterOn,
                            onCheckedChange = { vm.setFilterOn(it) }
                        )
                    }
                    AnimatedVisibility(
                        visible = filterOn,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cutoff", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatHz(filterCutoff),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            // Log-scale slider 20Hz..20kHz
                            val logMinF = ln(20.0)
                            val logMaxF = ln(20_000.0)
                            val pos = ((ln(filterCutoff.coerceIn(20.0, 20_000.0)) - logMinF) / (logMaxF - logMinF)).toFloat()
                            Slider(
                                value = pos,
                                onValueChange = { p ->
                                    val hz = exp(logMinF + p * (logMaxF - logMinF))
                                    vm.setFilterCutoff(hz)
                                },
                                valueRange = 0f..1f
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Resonance", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(filterResonance * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = filterResonance.toFloat(),
                                onValueChange = { vm.setFilterResonance(it.toDouble()) },
                                valueRange = 0f..0.99f
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── ADSR envelope ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "ADSR envelope",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = adsrOn,
                            onCheckedChange = { vm.setAdsrOn(it) }
                        )
                    }
                    AnimatedVisibility(
                        visible = adsrOn,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AdsrSliderRow("Attack",  adsrAttack,  0.0..5.0, "s",  vm::setAdsrAttack)
                            AdsrSliderRow("Decay",   adsrDecay,   0.0..5.0, "s",  vm::setAdsrDecay)
                            AdsrSliderRow("Sustain", adsrSustain, 0.0..1.0, "%",  vm::setAdsrSustain)
                            AdsrSliderRow("Release", adsrRelease, 0.0..5.0, "s",  vm::setAdsrRelease)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // ── Timer ─────────────────────────────────────────────────
                    Text(
                        "Auto-stop timer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TIMER_OPTIONS) { (secs, label) ->
                            FilterChip(
                                selected = secs == timerSeconds,
                                onClick  = { vm.setTimer(secs) },
                                label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    // Countdown display
                    AnimatedVisibility(
                        visible = timerRemaining > 0,
                        enter   = fadeIn(),
                        exit    = fadeOut()
                    ) {
                        Text(
                            text  = "Stops in ${formatCountdown(timerRemaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Volume (independent of system music volume) ───────────
                    Text(
                        "Volume",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (volume == 0f) Icons.AutoMirrored.Filled.VolumeOff
                                          else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = volume,
                            onValueChange = { vm.setVolume(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            "${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.widthIn(min = 36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Play / Stop ───────────────────────────────────────────
                    FilledIconButton(
                        onClick  = { vm.togglePlayStop() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector        = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            modifier           = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save preset") },
            text = {
                OutlinedTextField(
                    value = saveNameInput,
                    onValueChange = { saveNameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.saveCurrentAsPreset(saveNameInput)
                        saveNameInput = ""
                        showSaveDialog = false
                    },
                    enabled = saveNameInput.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = { Text(if (isExporting) "Exporting…" else "Export as audio file") },
            text = {
                if (isExporting) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${(exportProgress * 100).roundToInt()}%")
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = exportNameInput,
                            onValueChange = { exportNameInput = it },
                            label = { Text("File name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Duration", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(EXPORT_DURATION_OPTIONS) { (secs, label) ->
                                FilterChip(
                                    selected = exportDurationSeconds == secs,
                                    onClick  = { exportDurationSeconds = secs },
                                    label    = { Text(label) }
                                )
                            }
                        }
                        Text(
                            "Saved as WAV to Music/Frequency/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (isExporting) {
                    TextButton(onClick = { vm.cancelExport() }) { Text("Cancel export") }
                } else {
                    TextButton(
                        onClick = {
                            exportStarted = true
                            vm.exportSession(exportDurationSeconds, exportNameInput)
                        },
                        enabled = exportNameInput.isNotBlank()
                    ) { Text("Export") }
                }
            },
            dismissButton = {
                if (!isExporting) {
                    TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
                }
            }
        )
    }
}

// ── Preset row ────────────────────────────────────────────────────────────────

@Composable
private fun PresetRow(preset: FrequencyPreset, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
             else MaterialTheme.colorScheme.surfaceContainerLow
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
             else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = formatHz(preset.frequency),
            style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(80.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = preset.name, style = MaterialTheme.typography.bodyMedium, color = fg,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = preset.effect, style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CustomPresetRow(preset: CustomPreset, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = formatHz(preset.params.frequency),
            style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = preset.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete preset",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ── Waveform visualizer (real-time oscilloscope) ─────────────────────────────

@Composable
private fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier:  Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.outlineVariant

    // Stable scratch buffer the engine writes its latest samples into.
    val samples = remember { FloatArray(ToneGenerator.SCOPE_SIZE) }
    // Tick state — read inside the Canvas so the draw subscribes to it.
    val tick = remember { mutableIntStateOf(0) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            withFrameNanos {
                ToneService.generator.fillScopeSnapshot(samples)
            }
            tick.intValue++
        }
    }

    Canvas(modifier = modifier) {
        // Read tick inside the draw block so the snapshot system invalidates per frame
        @Suppress("UNUSED_EXPRESSION") tick.intValue

        val w   = size.width
        val h   = size.height
        val mid = h / 2f
        val amp = h * 0.45f

        // Zero-line
        drawLine(
            color = surfaceColor,
            start = Offset(0f, mid),
            end   = Offset(w, mid),
            strokeWidth = 1.dp.toPx()
        )

        if (!isPlaying) return@Canvas

        val n = samples.size
        // Auto-gain — make small signals visible. Find peak this frame.
        var peak = 0.001f
        for (s in samples) {
            val a = if (s < 0f) -s else s
            if (a > peak) peak = a
        }
        val scale = (1f / peak) * 0.9f

        val path = Path()
        for (i in 0 until n) {
            val x = i / (n - 1f) * w
            val y = mid - samples[i] * scale * amp
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun AdsrSliderRow(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    unit: String,
    onChange: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (unit == "%") "${(value * 100).toInt()}%"
            else "%.2f%s".format(value, unit),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.toDouble()) },
        valueRange = range.start.toFloat()..range.endInclusive.toFloat()
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatHz(hz: Double): String = when {
    hz < 10    -> "%.2f Hz".format(hz)
    hz < 100   -> "%.1f Hz".format(hz)
    hz < 1000  -> "%.0f Hz".format(hz)
    hz < 10000 -> "%.2f kHz".format(hz / 1000.0)
    else       -> "%.1f kHz".format(hz / 1000.0)
}

private fun formatInterval(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m == 0 -> "${s}s"
        s == 0 -> "${m}m"
        else   -> "${m}m ${s}s"
    }
}

private fun formatCountdown(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun beatLabel(hz: Double): String = when {
    hz < 4  -> "δ Delta"
    hz < 8  -> "θ Theta"
    hz < 14 -> "α Alpha"
    hz < 30 -> "β Beta"
    else    -> "γ Gamma"
}

// One-way time at this LFO speed (start→end or end→start)
private fun formatSweepPeriod(hz: Double): String {
    val oneway = 1.0 / (2.0 * hz)
    return when {
        oneway >= 60 -> "${(oneway / 60).roundToInt()}m one-way"
        oneway >= 2  -> "${oneway.roundToInt()}s one-way"
        else         -> "%.1fs one-way".format(oneway)
    }
}

private fun depthLabel(v: Double): String = when {
    v < 0.15 -> "Subtle"
    v < 0.35 -> "Light"
    v < 0.55 -> "Moderate"
    v < 0.75 -> "Deep"
    else     -> "Full range"
}

private fun randomnessLabel(v: Double): String = when {
    v < 0.15 -> "Smooth"
    v < 0.35 -> "Gentle"
    v < 0.55 -> "Organic"
    v < 0.75 -> "Chaotic"
    else     -> "Wild"
}

