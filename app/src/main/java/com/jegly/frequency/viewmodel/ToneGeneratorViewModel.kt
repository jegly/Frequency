package com.jegly.frequency.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.frequency.ToneService
import com.jegly.frequency.audio.ToneGenerator
import com.jegly.frequency.model.CustomPreset
import com.jegly.frequency.model.FrequencyPreset
import com.jegly.frequency.model.FrequencyPresets
import com.jegly.frequency.model.ToneSessionParams
import com.jegly.frequency.model.WaveCategory
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.utils.CustomPresetStorageManager
import com.jegly.frequency.utils.ToneExportManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.abs

class ToneGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val _frequency = MutableStateFlow(432.0)
    val frequency: StateFlow<Double> = _frequency.asStateFlow()

    private val _waveform = MutableStateFlow(ToneGenerator.Waveform.SINE)
    val waveform: StateFlow<ToneGenerator.Waveform> = _waveform.asStateFlow()

    private val _toneEnabled = MutableStateFlow(false)
    val toneEnabled: StateFlow<Boolean> = _toneEnabled.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedPreset = MutableStateFlow<FrequencyPreset?>(
        FrequencyPresets.all.find { it.category == WaveCategory.SOLFEGGIO && it.frequency == 432.0 }
    )
    val selectedPreset: StateFlow<FrequencyPreset?> = _selectedPreset.asStateFlow()

    private val _selectedCategory = MutableStateFlow(WaveCategory.ALL)
    val selectedCategory: StateFlow<WaveCategory> = _selectedCategory.asStateFlow()

    // ── Mode ─────────────────────────────────────────────────────────────────
    private val _playMode = MutableStateFlow(ToneGenerator.PlayMode.NORMAL)
    val playMode: StateFlow<ToneGenerator.PlayMode> = _playMode.asStateFlow()

    // ── Binaural ──────────────────────────────────────────────────────────────
    private val _beatFrequency = MutableStateFlow(4.0)
    val beatFrequency: StateFlow<Double> = _beatFrequency.asStateFlow()

    // ── Mix ───────────────────────────────────────────────────────────────────
    private val _mixFrequency = MutableStateFlow(528.0)
    val mixFrequency: StateFlow<Double> = _mixFrequency.asStateFlow()

    // ── Sweep ─────────────────────────────────────────────────────────────────
    private val _sweepStartFreq  = MutableStateFlow(200.0)
    val sweepStartFreq: StateFlow<Double> = _sweepStartFreq.asStateFlow()

    private val _sweepEndFreq    = MutableStateFlow(1000.0)
    val sweepEndFreq: StateFlow<Double> = _sweepEndFreq.asStateFlow()

    private val _sweepSpeed      = MutableStateFlow(0.1)   // LFO Hz
    val sweepSpeed: StateFlow<Double> = _sweepSpeed.asStateFlow()

    private val _sweepRandomness = MutableStateFlow(0.3)   // 0 smooth → 1 wild
    val sweepRandomness: StateFlow<Double> = _sweepRandomness.asStateFlow()

    private val _endLfoEnabled = MutableStateFlow(false)
    val endLfoEnabled: StateFlow<Boolean> = _endLfoEnabled.asStateFlow()

    private val _endLfoSpeed = MutableStateFlow(0.2)
    val endLfoSpeed: StateFlow<Double> = _endLfoSpeed.asStateFlow()

    private val _endLfoDepth = MutableStateFlow(0.3)
    val endLfoDepth: StateFlow<Double> = _endLfoDepth.asStateFlow()

    // ── Volume (independent of system music volume) ───────────────────────────
    private val _volume = MutableStateFlow(AppSettings.getSignalLabVolume(application))
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // ── Noise mix (each toggle independent, mix when multiple are on) ─────────
    private val _whiteNoiseOn = MutableStateFlow(false)
    val whiteNoiseOn: StateFlow<Boolean> = _whiteNoiseOn.asStateFlow()

    private val _pinkNoiseOn = MutableStateFlow(false)
    val pinkNoiseOn: StateFlow<Boolean> = _pinkNoiseOn.asStateFlow()

    private val _brownNoiseOn = MutableStateFlow(false)
    val brownNoiseOn: StateFlow<Boolean> = _brownNoiseOn.asStateFlow()

    // ── Isochronic gating ─────────────────────────────────────────────────────
    private val _isochronicOn = MutableStateFlow(false)
    val isochronicOn: StateFlow<Boolean> = _isochronicOn.asStateFlow()

    private val _isochronicRate = MutableStateFlow(7.83)
    val isochronicRate: StateFlow<Double> = _isochronicRate.asStateFlow()

    private val _isochronicSmooth = MutableStateFlow(false)
    val isochronicSmooth: StateFlow<Boolean> = _isochronicSmooth.asStateFlow()

    // ── Low-pass filter ───────────────────────────────────────────────────────
    private val _filterOn = MutableStateFlow(false)
    val filterOn: StateFlow<Boolean> = _filterOn.asStateFlow()

    private val _filterCutoff = MutableStateFlow(2000.0)
    val filterCutoff: StateFlow<Double> = _filterCutoff.asStateFlow()

    private val _filterResonance = MutableStateFlow(0.3)
    val filterResonance: StateFlow<Double> = _filterResonance.asStateFlow()

    // ── ADSR envelope ─────────────────────────────────────────────────────────
    private val _adsrOn = MutableStateFlow(false)
    val adsrOn: StateFlow<Boolean> = _adsrOn.asStateFlow()

    private val _adsrAttack = MutableStateFlow(0.05)
    val adsrAttack: StateFlow<Double> = _adsrAttack.asStateFlow()

    private val _adsrDecay = MutableStateFlow(0.20)
    val adsrDecay: StateFlow<Double> = _adsrDecay.asStateFlow()

    private val _adsrSustain = MutableStateFlow(0.80)
    val adsrSustain: StateFlow<Double> = _adsrSustain.asStateFlow()

    private val _adsrRelease = MutableStateFlow(0.30)
    val adsrRelease: StateFlow<Double> = _adsrRelease.asStateFlow()

    // ── Timer ─────────────────────────────────────────────────────────────────
    private val _timerSeconds  = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerRemaining = MutableStateFlow(0)
    val timerRemaining: StateFlow<Int> = _timerRemaining.asStateFlow()

    private var timerJob: Job? = null

    // ── Random mode ───────────────────────────────────────────────────────────
    private val _randomMode     = MutableStateFlow(false)
    val randomMode: StateFlow<Boolean> = _randomMode.asStateFlow()

    private val _randomInterval = MutableStateFlow(10)
    val randomInterval: StateFlow<Int> = _randomInterval.asStateFlow()

    private var randomJob: Job? = null

    // ── Custom presets ────────────────────────────────────────────────────────
    private val _customPresets = MutableStateFlow<List<CustomPreset>>(emptyList())
    val customPresets: StateFlow<List<CustomPreset>> = _customPresets.asStateFlow()

    // ── Export to audio file ──────────────────────────────────────────────────
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    @Volatile private var exportCancelRequested = false

    init {
        if (ToneService.generator.isRunning) {
            _isPlaying.value = true
            _frequency.value = ToneService.generator.currentFreq
            _playMode.value  = ToneService.generator.currentMode
        }
        viewModelScope.launch {
            _customPresets.value = CustomPresetStorageManager.getCustomPresets(getApplication())
        }
    }

    // ── Preset / frequency ────────────────────────────────────────────────────

    fun selectPreset(preset: FrequencyPreset) {
        _selectedPreset.value = preset
        _frequency.value      = preset.frequency
        if (_isPlaying.value) send(ToneService.ACTION_SET_FREQ) {
            putExtra(ToneService.EXTRA_FREQ, preset.frequency)
        }
    }

    fun setFrequency(hz: Double) {
        _frequency.value = hz
        val closest = FrequencyPresets.all.minByOrNull { abs(it.frequency - hz) }
        _selectedPreset.value = if (closest != null && abs(closest.frequency - hz) < 1.5) closest else null
        if (_isPlaying.value) send(ToneService.ACTION_SET_FREQ) {
            putExtra(ToneService.EXTRA_FREQ, hz)
        }
    }

    fun setCategory(cat: WaveCategory) { _selectedCategory.value = cat }

    // ── Waveform / mode ───────────────────────────────────────────────────────

    fun setWaveform(wf: ToneGenerator.Waveform) {
        // Re-tapping the active waveform toggles the tone off; tapping any other waveform
        // selects it AND re-enables the tone.
        if (wf == _waveform.value && _toneEnabled.value) {
            setToneEnabled(false)
            return
        }
        _waveform.value = wf
        if (_isPlaying.value) send(ToneService.ACTION_SET_WAVEFORM) {
            putExtra(ToneService.EXTRA_WAVEFORM, wf.name)
        }
        if (!_toneEnabled.value) setToneEnabled(true)
    }

    fun setToneEnabled(on: Boolean) {
        _toneEnabled.value = on
        if (_isPlaying.value) send(ToneService.ACTION_SET_TONE_ON) {
            putExtra(ToneService.EXTRA_TONE_ON, on)
        }
    }

    fun setPlayMode(mode: ToneGenerator.PlayMode) {
        _playMode.value = mode
        if (_isPlaying.value) send(ToneService.ACTION_SET_MODE) {
            putExtra(ToneService.EXTRA_MODE, mode.name)
        }
        // Picking a tone mode is a strong signal the user wants to hear a tone
        if (!_toneEnabled.value) setToneEnabled(true)
    }

    fun setBeatFrequency(hz: Double) {
        _beatFrequency.value = hz
        if (_isPlaying.value) send(ToneService.ACTION_SET_BEAT_FREQ) {
            putExtra(ToneService.EXTRA_BEAT_FREQ, hz)
        }
    }

    fun setMixFrequency(hz: Double) {
        _mixFrequency.value = hz
        if (_isPlaying.value) send(ToneService.ACTION_SET_MIX_FREQ) {
            putExtra(ToneService.EXTRA_MIX_FREQ, hz)
        }
    }

    // ── Sweep ─────────────────────────────────────────────────────────────────

    fun setSweepStartFreq(hz: Double) {
        _sweepStartFreq.value = hz
        sendSweepUpdate()
    }

    fun setSweepEndFreq(hz: Double) {
        _sweepEndFreq.value = hz
        sendSweepUpdate()
    }

    fun setSweepSpeed(hz: Double) {
        _sweepSpeed.value = hz
        sendSweepUpdate()
    }

    fun setSweepRandomness(v: Double) {
        _sweepRandomness.value = v
        sendSweepUpdate()
    }

    fun setEndLfoEnabled(on: Boolean) {
        _endLfoEnabled.value = on
        sendSweepUpdate()
    }

    fun setEndLfoSpeed(hz: Double) {
        _endLfoSpeed.value = hz
        sendSweepUpdate()
    }

    fun setEndLfoDepth(v: Double) {
        _endLfoDepth.value = v
        sendSweepUpdate()
    }

    fun setVolume(v: Float) {
        val clamped = v.coerceIn(0f, 1f)
        _volume.value = clamped
        AppSettings.setSignalLabVolume(getApplication(), clamped)
        if (_isPlaying.value) send(ToneService.ACTION_SET_VOLUME) {
            putExtra(ToneService.EXTRA_VOLUME, clamped)
        }
    }

    fun setWhiteNoise(on: Boolean) { _whiteNoiseOn.value = on; sendNoiseUpdate() }
    fun setPinkNoise(on: Boolean)  { _pinkNoiseOn.value  = on; sendNoiseUpdate() }
    fun setBrownNoise(on: Boolean) { _brownNoiseOn.value = on; sendNoiseUpdate() }

    private fun sendNoiseUpdate() {
        if (!_isPlaying.value) return
        send(ToneService.ACTION_SET_NOISE) {
            putExtra(ToneService.EXTRA_NOISE_WHITE, _whiteNoiseOn.value)
            putExtra(ToneService.EXTRA_NOISE_PINK,  _pinkNoiseOn.value)
            putExtra(ToneService.EXTRA_NOISE_BROWN, _brownNoiseOn.value)
        }
    }

    fun setIsochronicOn(on: Boolean)        { _isochronicOn.value     = on; sendIsoUpdate() }
    fun setIsochronicRate(hz: Double)       { _isochronicRate.value   = hz; sendIsoUpdate() }
    fun setIsochronicSmooth(smooth: Boolean){ _isochronicSmooth.value = smooth; sendIsoUpdate() }

    private fun sendIsoUpdate() {
        if (!_isPlaying.value) return
        send(ToneService.ACTION_SET_ISOCHRONIC) {
            putExtra(ToneService.EXTRA_ISO_ON,     _isochronicOn.value)
            putExtra(ToneService.EXTRA_ISO_RATE,   _isochronicRate.value)
            putExtra(ToneService.EXTRA_ISO_SMOOTH, _isochronicSmooth.value)
        }
    }

    // ── Filter setters ────────────────────────────────────────────────────────
    fun setFilterOn(on: Boolean)       { _filterOn.value = on; sendFilterUpdate() }
    fun setFilterCutoff(hz: Double)    { _filterCutoff.value = hz; sendFilterUpdate() }
    fun setFilterResonance(v: Double)  { _filterResonance.value = v; sendFilterUpdate() }

    private fun sendFilterUpdate() {
        if (!_isPlaying.value) return
        send(ToneService.ACTION_SET_FILTER) {
            putExtra(ToneService.EXTRA_FILTER_ON,     _filterOn.value)
            putExtra(ToneService.EXTRA_FILTER_CUTOFF, _filterCutoff.value)
            putExtra(ToneService.EXTRA_FILTER_RES,    _filterResonance.value)
        }
    }

    // ── ADSR setters ──────────────────────────────────────────────────────────
    fun setAdsrOn(on: Boolean)         { _adsrOn.value = on;       sendAdsrUpdate() }
    fun setAdsrAttack(sec: Double)     { _adsrAttack.value = sec;  sendAdsrUpdate() }
    fun setAdsrDecay(sec: Double)      { _adsrDecay.value = sec;   sendAdsrUpdate() }
    fun setAdsrSustain(level: Double)  { _adsrSustain.value = level; sendAdsrUpdate() }
    fun setAdsrRelease(sec: Double)    { _adsrRelease.value = sec; sendAdsrUpdate() }

    private fun sendAdsrUpdate() {
        if (!_isPlaying.value) return
        send(ToneService.ACTION_SET_ADSR) {
            putExtra(ToneService.EXTRA_ADSR_ON, _adsrOn.value)
            putExtra(ToneService.EXTRA_ADSR_A,  _adsrAttack.value)
            putExtra(ToneService.EXTRA_ADSR_D,  _adsrDecay.value)
            putExtra(ToneService.EXTRA_ADSR_S,  _adsrSustain.value)
            putExtra(ToneService.EXTRA_ADSR_R,  _adsrRelease.value)
        }
    }

    private fun sendSweepUpdate() {
        if (!_isPlaying.value) return
        send(ToneService.ACTION_UPDATE_SWEEP) {
            putExtra(ToneService.EXTRA_SWEEP_START,   _sweepStartFreq.value)
            putExtra(ToneService.EXTRA_SWEEP_END,     _sweepEndFreq.value)
            putExtra(ToneService.EXTRA_SWEEP_SPEED,   _sweepSpeed.value)
            putExtra(ToneService.EXTRA_SWEEP_RANDOM,  _sweepRandomness.value)
            putExtra(ToneService.EXTRA_END_LFO_ON,    _endLfoEnabled.value)
            putExtra(ToneService.EXTRA_END_LFO_SPEED, _endLfoSpeed.value)
            putExtra(ToneService.EXTRA_END_LFO_DEPTH, _endLfoDepth.value)
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    fun setTimer(seconds: Int) {
        timerJob?.cancel()
        _timerSeconds.value   = seconds
        _timerRemaining.value = seconds
        if (seconds > 0 && _isPlaying.value) launchTimer(seconds)
    }

    private fun launchTimer(seconds: Int) {
        timerJob?.cancel()
        _timerRemaining.value = seconds
        timerJob = viewModelScope.launch {
            while (isActive && _timerRemaining.value > 0) {
                delay(1000)
                if (isActive) _timerRemaining.value--
            }
            if (isActive && _isPlaying.value) togglePlayStop()
        }
    }

    // ── Random mode ───────────────────────────────────────────────────────────

    fun setRandomMode(enabled: Boolean) {
        _randomMode.value = enabled
        if (enabled && _isPlaying.value) launchRandom()
        else randomJob?.cancel()
    }

    fun setRandomInterval(seconds: Int) {
        _randomInterval.value = seconds
        if (_randomMode.value && _isPlaying.value) {
            randomJob?.cancel()
            launchRandom()
        }
    }

    private fun launchRandom() {
        randomJob?.cancel()
        randomJob = viewModelScope.launch {
            while (isActive && _randomMode.value) {
                delay(_randomInterval.value * 1000L)
                if (!isActive || !_randomMode.value) break
                setFrequency(FrequencyPresets.all.filter { it.frequency <= 10_000.0 }.random().frequency)
            }
        }
    }

    // ── Play / Stop ───────────────────────────────────────────────────────────

    fun togglePlayStop() {
        if (_isPlaying.value) {
            send(ToneService.ACTION_STOP)
            _isPlaying.value = false
            timerJob?.cancel()
            randomJob?.cancel()
            _timerRemaining.value = 0
        } else {
            // If nothing audible is queued (no tone, no noise), turn the tone on
            val anyNoise = _whiteNoiseOn.value || _pinkNoiseOn.value || _brownNoiseOn.value
            if (!_toneEnabled.value && !anyNoise) _toneEnabled.value = true
            startServiceWithCurrentParams()
            _isPlaying.value = true
            if (_timerSeconds.value > 0) launchTimer(_timerSeconds.value)
            if (_randomMode.value) launchRandom()
        }
    }

    private fun startServiceWithCurrentParams() {
        val ctx = getApplication<Application>()
        ctx.startForegroundService(Intent(ctx, ToneService::class.java).apply {
                action = ToneService.ACTION_START
                putExtra(ToneService.EXTRA_FREQ,         _frequency.value)
                putExtra(ToneService.EXTRA_WAVEFORM,     _waveform.value.name)
                putExtra(ToneService.EXTRA_MODE,         _playMode.value.name)
                putExtra(ToneService.EXTRA_BEAT_FREQ,    _beatFrequency.value)
                putExtra(ToneService.EXTRA_MIX_FREQ,     _mixFrequency.value)
                putExtra(ToneService.EXTRA_SWEEP_START,   _sweepStartFreq.value)
                putExtra(ToneService.EXTRA_SWEEP_END,     _sweepEndFreq.value)
                putExtra(ToneService.EXTRA_SWEEP_SPEED,   _sweepSpeed.value)
                putExtra(ToneService.EXTRA_SWEEP_RANDOM,  _sweepRandomness.value)
                putExtra(ToneService.EXTRA_END_LFO_ON,    _endLfoEnabled.value)
                putExtra(ToneService.EXTRA_END_LFO_SPEED, _endLfoSpeed.value)
                putExtra(ToneService.EXTRA_END_LFO_DEPTH, _endLfoDepth.value)
                putExtra(ToneService.EXTRA_VOLUME,        _volume.value)
                putExtra(ToneService.EXTRA_NOISE_WHITE,   _whiteNoiseOn.value)
                putExtra(ToneService.EXTRA_NOISE_PINK,    _pinkNoiseOn.value)
                putExtra(ToneService.EXTRA_NOISE_BROWN,   _brownNoiseOn.value)
                putExtra(ToneService.EXTRA_ISO_ON,        _isochronicOn.value)
                putExtra(ToneService.EXTRA_ISO_RATE,      _isochronicRate.value)
                putExtra(ToneService.EXTRA_ISO_SMOOTH,    _isochronicSmooth.value)
                putExtra(ToneService.EXTRA_TONE_ON,       _toneEnabled.value)
                putExtra(ToneService.EXTRA_FILTER_ON,     _filterOn.value)
                putExtra(ToneService.EXTRA_FILTER_CUTOFF, _filterCutoff.value)
                putExtra(ToneService.EXTRA_FILTER_RES,    _filterResonance.value)
                putExtra(ToneService.EXTRA_ADSR_ON,       _adsrOn.value)
                putExtra(ToneService.EXTRA_ADSR_A,        _adsrAttack.value)
                putExtra(ToneService.EXTRA_ADSR_D,        _adsrDecay.value)
                putExtra(ToneService.EXTRA_ADSR_S,        _adsrSustain.value)
                putExtra(ToneService.EXTRA_ADSR_R,        _adsrRelease.value)
        })
    }

    // ── Custom presets ────────────────────────────────────────────────────────

    private fun currentSessionParams(): ToneSessionParams = ToneSessionParams(
        frequency = _frequency.value,
        waveform = _waveform.value,
        mode = _playMode.value,
        beatFreq = _beatFrequency.value,
        mixFreq = _mixFrequency.value,
        sweepStart = _sweepStartFreq.value,
        sweepEnd = _sweepEndFreq.value,
        sweepSpeed = _sweepSpeed.value,
        sweepRandom = _sweepRandomness.value,
        endLfoOn = _endLfoEnabled.value,
        endLfoSpeed = _endLfoSpeed.value,
        endLfoDepth = _endLfoDepth.value,
        volume = _volume.value,
        whiteNoiseOn = _whiteNoiseOn.value,
        pinkNoiseOn = _pinkNoiseOn.value,
        brownNoiseOn = _brownNoiseOn.value,
        isochronicOn = _isochronicOn.value,
        isochronicRate = _isochronicRate.value,
        isochronicSmooth = _isochronicSmooth.value,
        toneEnabled = _toneEnabled.value,
        filterOn = _filterOn.value,
        filterCutoff = _filterCutoff.value,
        filterResonance = _filterResonance.value,
        adsrOn = _adsrOn.value,
        adsrAttack = _adsrAttack.value,
        adsrDecay = _adsrDecay.value,
        adsrSustain = _adsrSustain.value,
        adsrRelease = _adsrRelease.value
    )

    fun saveCurrentAsPreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val preset = CustomPreset(id = UUID.randomUUID().toString(), name = trimmed, params = currentSessionParams())
        viewModelScope.launch {
            CustomPresetStorageManager.saveCustomPreset(getApplication(), preset)
            _customPresets.value = _customPresets.value + preset
        }
    }

    fun applyCustomPreset(preset: CustomPreset) {
        val p = preset.params
        _selectedPreset.value = null
        _frequency.value = p.frequency
        _waveform.value = p.waveform
        _playMode.value = p.mode
        _beatFrequency.value = p.beatFreq
        _mixFrequency.value = p.mixFreq
        _sweepStartFreq.value = p.sweepStart
        _sweepEndFreq.value = p.sweepEnd
        _sweepSpeed.value = p.sweepSpeed
        _sweepRandomness.value = p.sweepRandom
        _endLfoEnabled.value = p.endLfoOn
        _endLfoSpeed.value = p.endLfoSpeed
        _endLfoDepth.value = p.endLfoDepth
        _volume.value = p.volume
        AppSettings.setSignalLabVolume(getApplication(), p.volume)
        _whiteNoiseOn.value = p.whiteNoiseOn
        _pinkNoiseOn.value = p.pinkNoiseOn
        _brownNoiseOn.value = p.brownNoiseOn
        _isochronicOn.value = p.isochronicOn
        _isochronicRate.value = p.isochronicRate
        _isochronicSmooth.value = p.isochronicSmooth
        _toneEnabled.value = p.toneEnabled
        _filterOn.value = p.filterOn
        _filterCutoff.value = p.filterCutoff
        _filterResonance.value = p.filterResonance
        _adsrOn.value = p.adsrOn
        _adsrAttack.value = p.adsrAttack
        _adsrDecay.value = p.adsrDecay
        _adsrSustain.value = p.adsrSustain
        _adsrRelease.value = p.adsrRelease

        if (_isPlaying.value) {
            send(ToneService.ACTION_STOP)
            startServiceWithCurrentParams()
        }
    }

    fun deleteCustomPreset(preset: CustomPreset) {
        viewModelScope.launch {
            CustomPresetStorageManager.deleteCustomPreset(getApplication(), preset.id)
            _customPresets.value = _customPresets.value.filterNot { it.id == preset.id }
        }
    }

    // ── Export to audio file ──────────────────────────────────────────────────

    fun exportSession(durationSeconds: Int, fileName: String) {
        if (_isExporting.value) return
        exportCancelRequested = false
        _isExporting.value = true
        _exportProgress.value = 0f
        val params = currentSessionParams()
        viewModelScope.launch {
            val result = ToneExportManager.exportSession(
                context = getApplication(),
                params = params,
                durationSeconds = durationSeconds,
                fileName = fileName,
                onProgress = { p -> _exportProgress.value = p },
                isCancelled = { exportCancelRequested }
            )
            _isExporting.value = false
            _exportMessage.value = when (result) {
                is ToneExportManager.ExportResult.Success -> "Saved to Music/Frequency/$fileName.wav"
                is ToneExportManager.ExportResult.Cancelled -> "Export cancelled"
                is ToneExportManager.ExportResult.Failure -> "Export failed: ${result.message}"
            }
        }
    }

    fun cancelExport() {
        exportCancelRequested = true
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun send(action: String, extras: (Intent.() -> Unit)? = null) {
        val ctx = getApplication<Application>()
        ctx.startService(Intent(ctx, ToneService::class.java).apply {
            this.action = action
            extras?.invoke(this)
        })
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        randomJob?.cancel()
        // Service intentionally NOT stopped — keeps playing in background
    }
}
