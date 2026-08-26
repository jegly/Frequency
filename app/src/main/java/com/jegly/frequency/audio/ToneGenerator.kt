package com.jegly.frequency.audio

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.*

class ToneGenerator {

    enum class Waveform(val label: String) {
        SINE("Sine"), SQUARE("Square"), TRIANGLE("Triangle"), SAWTOOTH("Sawtooth")
    }

    enum class PlayMode(val label: String) {
        NORMAL("Normal"), BINAURAL("Binaural"), MIX("Mix"), SWEEP("Sweep")
    }

    companion object {
        private const val SAMPLE_RATE  = 44100
        private const val CHUNK        = 1024
        private const val FADE_SAMPLES = 4410  // 100 ms
        private const val AMPLITUDE    = 0.40f
        const val SCOPE_SIZE           = 512   // most-recent samples available to the UI scope
    }

    @Volatile private var targetFreq:        Double   = 440.0
    @Volatile private var activeWaveform:    Waveform = Waveform.SINE
    @Volatile private var activeMode:        PlayMode = PlayMode.NORMAL
    @Volatile private var activeBeatFreq:    Double   = 4.0
    @Volatile private var activeMixFreq:     Double   = 528.0
    @Volatile private var activeSweepStart:  Double   = 200.0
    @Volatile private var activeSweepEnd:    Double   = 1000.0
    @Volatile private var activeSweepSpeed:  Double   = 0.1    // LFO Hz — full cycle per 1/speed seconds
    @Volatile private var activeSweepRandom: Double   = 0.3    // 0 = smooth, 1 = wild
    @Volatile private var endLfoEnabled:     Boolean  = false
    @Volatile private var endLfoSpeed:       Double   = 0.2    // Hz of end-freq oscillation
    @Volatile private var endLfoDepth:       Double   = 0.3    // 0–1 fraction of (end-start) range
    @Volatile private var activeVolume:      Float    = 1.0f   // user-controlled gain (0–1)
    @Volatile private var whiteNoiseOn:      Boolean  = false
    @Volatile private var pinkNoiseOn:       Boolean  = false
    @Volatile private var brownNoiseOn:      Boolean  = false
    @Volatile private var isochronicOn:      Boolean  = false
    @Volatile private var isochronicRate:    Double   = 7.83   // Schumann resonance as default
    @Volatile private var isochronicSmooth:  Boolean  = false  // false = hard square, true = sine gate
    @Volatile private var toneEnabled:       Boolean  = true   // tap selected waveform to silence tone
    // Low-pass filter (state-variable)
    @Volatile private var filterOn:          Boolean  = false
    @Volatile private var filterCutoff:      Double   = 2000.0  // Hz
    @Volatile private var filterResonance:   Double   = 0.3     // 0 = none, 1 = strong (self-osc threshold)
    // ADSR envelope (replaces the simple 100ms fade when on)
    @Volatile private var adsrOn:            Boolean  = false
    @Volatile private var adsrAttack:        Double   = 0.05    // seconds
    @Volatile private var adsrDecay:         Double   = 0.20
    @Volatile private var adsrSustain:       Double   = 0.80    // 0–1
    @Volatile private var adsrRelease:       Double   = 0.30
    @Volatile private var running = false

    private var audioTrack: AudioTrack? = null
    private var thread: Thread? = null

    // Live scope: ring buffer of the last SCOPE_SIZE mono samples produced
    private val scopeBuffer = FloatArray(SCOPE_SIZE)
    @Volatile private var scopeWriteIndex = 0

    /** Copy the most recent samples into [out] in oldest-to-newest order.
     *  [out] must be at least SCOPE_SIZE long. Read is best-effort, not locked. */
    fun fillScopeSnapshot(out: FloatArray) {
        if (out.size < SCOPE_SIZE) return
        val idx = scopeWriteIndex
        System.arraycopy(scopeBuffer, idx, out, 0, SCOPE_SIZE - idx)
        System.arraycopy(scopeBuffer, 0, out, SCOPE_SIZE - idx, idx)
    }

    fun start(
        frequency:    Double,
        waveform:     Waveform,
        mode:         PlayMode = PlayMode.NORMAL,
        beatFreq:     Double   = 4.0,
        mixFreq:      Double   = 528.0,
        sweepStart:    Double   = 200.0,
        sweepEnd:      Double   = 1000.0,
        sweepSpeed:    Double   = 0.1,
        sweepRandom:   Double   = 0.3,
        endLfoOn:      Boolean  = false,
        endLfoSpd:     Double   = 0.2,
        endLfoDep:     Double   = 0.3,
        volume:        Float    = 1.0f,
        whiteOn:       Boolean  = false,
        pinkOn:        Boolean  = false,
        brownOn:       Boolean  = false,
        isoOn:         Boolean  = false,
        isoRate:       Double   = 7.83,
        isoSmooth:     Boolean  = false,
        toneOn:        Boolean  = true,
        filtOn:        Boolean  = false,
        filtCutoff:    Double   = 2000.0,
        filtRes:       Double   = 0.3,
        adsrEnable:    Boolean  = false,
        adsrA:         Double   = 0.05,
        adsrD:         Double   = 0.20,
        adsrS:         Double   = 0.80,
        adsrR:         Double   = 0.30,
        preferredDevice: AudioDeviceInfo? = null
    ) {
        if (running) return
        targetFreq        = frequency.coerceIn(0.5, 22_000.0)
        activeWaveform    = waveform
        activeMode        = mode
        activeBeatFreq    = beatFreq.coerceIn(0.1, 100.0)
        activeMixFreq     = mixFreq.coerceIn(0.5, 22_000.0)
        activeSweepStart  = sweepStart.coerceIn(0.5, 22_000.0)
        activeSweepEnd    = sweepEnd.coerceIn(0.5, 22_000.0)
        activeSweepSpeed  = sweepSpeed.coerceIn(0.005, 5.0)
        activeSweepRandom = sweepRandom.coerceIn(0.0, 1.0)
        endLfoEnabled     = endLfoOn
        endLfoSpeed       = endLfoSpd.coerceIn(0.005, 5.0)
        endLfoDepth       = endLfoDep.coerceIn(0.0, 1.0)
        activeVolume      = volume.coerceIn(0f, 1f)
        whiteNoiseOn      = whiteOn
        pinkNoiseOn       = pinkOn
        brownNoiseOn      = brownOn
        isochronicOn      = isoOn
        isochronicRate    = isoRate.coerceIn(0.5, 40.0)
        isochronicSmooth  = isoSmooth
        toneEnabled       = toneOn
        filterOn          = filtOn
        filterCutoff      = filtCutoff.coerceIn(20.0, 20_000.0)
        filterResonance   = filtRes.coerceIn(0.0, 0.99)
        adsrOn            = adsrEnable
        adsrAttack        = adsrA.coerceIn(0.0, 5.0)
        adsrDecay         = adsrD.coerceIn(0.0, 5.0)
        adsrSustain       = adsrS.coerceIn(0.0, 1.0)
        adsrRelease       = adsrR.coerceIn(0.0, 5.0)
        running           = true

        val minBuf   = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT
        )
        val bufBytes = maxOf(minBuf, CHUNK * Float.SIZE_BYTES * 16)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .build()
            )
            .setBufferSizeInBytes(bufBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        if (preferredDevice != null) track.setPreferredDevice(preferredDevice)
        track.play()

        thread = Thread({
            val rng    = java.util.Random()
            val buf    = FloatArray(CHUNK * 2)
            var phaseL = 0.0
            var phaseR = 0.0
            var phase2 = 0.0
            var n      = 0L

            // Sweep state
            var sweepPos    = 0.0
            var sweepDir    = 1.0
            var endLfoPhase = 0.0
            // Isochronic gate state
            var isoPhase    = 0.0
            // Filter state (state-variable, stereo)
            var svfLowL = 0.0; var svfBandL = 0.0
            var svfLowR = 0.0; var svfBandR = 0.0
            // Pink noise state (Paul Kellet's algorithm)
            var pinkB0 = 0.0; var pinkB1 = 0.0; var pinkB2 = 0.0
            var pinkB3 = 0.0; var pinkB4 = 0.0; var pinkB5 = 0.0; var pinkB6 = 0.0
            // Brown noise state
            var brownLast = 0.0

            while (running) {
                val freq   = targetFreq
                val wf     = activeWaveform
                val mode   = activeMode
                val beat   = activeBeatFreq
                val mFreq  = activeMixFreq
                val sStart    = activeSweepStart
                val sEnd      = activeSweepEnd
                val sSpeed    = activeSweepSpeed
                val sRand     = activeSweepRandom
                val eLfoOn    = endLfoEnabled
                val eLfoSpeed = endLfoSpeed
                val eLfoDepth = endLfoDepth
                val vol       = activeVolume.toDouble()
                val amp       = AMPLITUDE * vol
                val noiseW    = whiteNoiseOn
                val noiseP    = pinkNoiseOn
                val noiseB    = brownNoiseOn
                val anyNoise  = noiseW || noiseP || noiseB
                val isoOn     = isochronicOn
                val isoRate   = isochronicRate
                val isoSmooth = isochronicSmooth
                val isoInc    = 2.0 * PI * isoRate / SAMPLE_RATE
                // Filter coefficients (recomputed per chunk)
                val fOn       = filterOn
                val fCutoff   = filterCutoff.coerceAtMost(SAMPLE_RATE * 0.45)
                val svfF      = 2.0 * sin(PI * fCutoff / SAMPLE_RATE)
                val svfQ      = (1.0 - filterResonance).coerceAtLeast(0.02)
                // ADSR
                val aOn       = adsrOn
                val aAtkSamp  = (adsrAttack * SAMPLE_RATE).toInt().coerceAtLeast(1)
                val aDecSamp  = (adsrDecay * SAMPLE_RATE).toInt().coerceAtLeast(1)
                val aSusLvl   = adsrSustain

                // Pre-compute increments for non-SWEEP modes (constant per chunk)
                val incL = 2.0 * PI * freq / SAMPLE_RATE
                val incR = if (mode == PlayMode.BINAURAL) 2.0 * PI * (freq + beat) / SAMPLE_RATE else incL
                val inc2 = 2.0 * PI * mFreq / SAMPLE_RATE
                // SWEEP: position moves 2*speed/sampleRate per sample (triangle wave)
                val sweepBaseInc = sSpeed * 2.0 / SAMPLE_RATE

                for (i in 0 until CHUNK) {
                    val env = if (aOn) {
                        when {
                            n < aAtkSamp -> n.toDouble() / aAtkSamp
                            n < aAtkSamp + aDecSamp ->
                                1.0 + (aSusLvl - 1.0) * (n - aAtkSamp).toDouble() / aDecSamp
                            else -> aSusLvl
                        }
                    } else {
                        if (n < FADE_SAMPLES) n.toDouble() / FADE_SAMPLES else 1.0
                    }
                    var l: Float
                    var r: Float

                    // Tone plays only when toneEnabled is true (toggled via the waveform buttons).
                    val playTone = toneEnabled

                    when (mode) {
                        PlayMode.NORMAL -> {
                            val s = if (playTone) (rawSample(phaseL, wf) * env * amp).toFloat() else 0f
                            l = s; r = s
                            phaseL = (phaseL + incL) % (2.0 * PI)
                        }
                        PlayMode.BINAURAL -> {
                            l = if (playTone) (rawSample(phaseL, wf) * env * amp).toFloat() else 0f
                            r = if (playTone) (rawSample(phaseR, wf) * env * amp).toFloat() else 0f
                            phaseL = (phaseL + incL) % (2.0 * PI)
                            phaseR = (phaseR + incR) % (2.0 * PI)
                        }
                        PlayMode.MIX -> {
                            val s = if (playTone) ((rawSample(phaseL, wf) + rawSample(phase2, wf)) * 0.5 * env * amp).toFloat() else 0f
                            l = s; r = s
                            phaseL = (phaseL + incL) % (2.0 * PI)
                            phase2 = (phase2 + inc2) % (2.0 * PI)
                        }
                        PlayMode.SWEEP -> {
                            // Advance sweep position: deterministic component + organic jitter
                            val jitter = (rng.nextDouble() - 0.5) * sRand * sweepBaseInc * 8.0
                            sweepPos += sweepDir * sweepBaseInc + jitter

                            // Bounce at boundaries — reflect so it never escapes [0,1]
                            if (sweepPos >= 1.0) { sweepPos = 2.0 - sweepPos; sweepDir = -1.0 }
                            if (sweepPos <= 0.0) { sweepPos = -sweepPos; sweepDir = 1.0 }
                            sweepPos = sweepPos.coerceIn(0.0, 1.0)

                            // End-freq LFO: sine wave that rides the endpoint up and down
                            val instEnd = if (eLfoOn) {
                                val range = abs(sEnd - sStart) * eLfoDepth
                                (sEnd + sin(endLfoPhase) * range).coerceIn(0.5, 22_000.0)
                            } else sEnd
                            endLfoPhase = (endLfoPhase + eLfoSpeed * 2.0 * PI / SAMPLE_RATE) % (2.0 * PI)

                            val instFreq = sStart + sweepPos * (instEnd - sStart)
                            val sweepInc = 2.0 * PI * instFreq / SAMPLE_RATE
                            val s = if (playTone) (rawSample(phaseL, wf) * env * amp).toFloat() else 0f
                            l = s; r = s
                            phaseL = (phaseL + sweepInc) % (2.0 * PI)
                        }
                    }

                    // Noise — layered with the tone in Binaural mode, otherwise replaces it.
                    if (anyNoise) {
                        val white = rng.nextDouble() * 2.0 - 1.0
                        pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                        pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                        pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                        pinkB3 = 0.86650 * pinkB3 + white * 0.3104856
                        pinkB4 = 0.55000 * pinkB4 + white * 0.5329522
                        pinkB5 = -0.7616 * pinkB5 - white * 0.0168980
                        val pink = (pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + pinkB6 + white * 0.5362) / 10.0
                        pinkB6 = white * 0.115926
                        brownLast = (brownLast + 0.02 * white) / 1.02
                        val brown = (brownLast * 3.5).coerceIn(-1.0, 1.0)

                        var mix = 0.0
                        if (noiseW) mix += white * 0.5
                        if (noiseP) mix += pink
                        if (noiseB) mix += brown
                        val activeCount = (if (noiseW) 1 else 0) + (if (noiseP) 1 else 0) + (if (noiseB) 1 else 0)
                        if (activeCount > 1) mix /= activeCount

                        if (playTone) {
                            // Tone + noise — tone stays full, noise at 0.5 to leave headroom for the sum
                            val nSample = (mix * env * amp * 0.5).toFloat()
                            l += nSample
                            r += nSample
                        } else {
                            // Noise-only — full level
                            val nSample = (mix * env * amp).toFloat()
                            l = nSample; r = nSample
                        }
                    }

                    // ── Low-pass filter (state-variable, stereo) ─────────────
                    if (fOn) {
                        svfLowL += svfF * svfBandL
                        val highL = l.toDouble() - svfLowL - svfQ * svfBandL
                        svfBandL += svfF * highL
                        l = svfLowL.toFloat()
                        svfLowR += svfF * svfBandR
                        val highR = r.toDouble() - svfLowR - svfQ * svfBandR
                        svfBandR += svfF * highR
                        r = svfLowR.toFloat()
                    }

                    val gate = if (isoOn) {
                        val g = if (isoSmooth) {
                            (sin(isoPhase) * 0.5 + 0.5).coerceAtLeast(0.0)
                        } else {
                            if (sin(isoPhase) >= 0.0) 1.0 else 0.0
                        }
                        isoPhase = (isoPhase + isoInc) % (2.0 * PI)
                        g.toFloat()
                    } else 1.0f
                    val outL = l * gate
                    val outR = r * gate
                    buf[i * 2]     = outL
                    buf[i * 2 + 1] = outR
                    // Live scope: mono mix into ring buffer
                    val si = scopeWriteIndex
                    scopeBuffer[si] = (outL + outR) * 0.5f
                    scopeWriteIndex = (si + 1) % SCOPE_SIZE
                    n++
                }
                track.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
            }

            // Fade-out
            val fadeOut  = FloatArray(FADE_SAMPLES * 2)
            val fadeWf   = activeWaveform
            val fadeFreq = targetFreq
            val fadeInc  = 2.0 * PI * fadeFreq / SAMPLE_RATE
            val fadeAmp  = AMPLITUDE * activeVolume.toDouble()
            val fadeAnyNoise = whiteNoiseOn || pinkNoiseOn || brownNoiseOn
            for (i in 0 until FADE_SAMPLES) {
                val fade = 1.0 - i.toDouble() / FADE_SAMPLES
                val s = if (fadeAnyNoise) {
                    (rng.nextDouble() * 2.0 - 1.0) * 0.5 * fade * fadeAmp
                } else {
                    rawSample(phaseL, fadeWf) * fade * fadeAmp
                }.toFloat()
                fadeOut[i * 2]     = s
                fadeOut[i * 2 + 1] = s
                if (!fadeAnyNoise) phaseL = (phaseL + fadeInc) % (2.0 * PI)
            }
            track.write(fadeOut, 0, fadeOut.size, AudioTrack.WRITE_BLOCKING)
            track.stop()
            track.release()
            audioTrack = null
        }, "ToneGenerator")

        thread?.priority = Thread.MAX_PRIORITY
        thread?.isDaemon = true
        thread?.start()
    }

    fun stop()                      { running         = false }
    fun setFrequency(hz: Double)    { targetFreq      = hz.coerceIn(0.5, 22_000.0) }
    fun setWaveform(wf: Waveform)   { activeWaveform  = wf }
    fun setPlayMode(mode: PlayMode) { activeMode      = mode }
    fun setBeatFreq(hz: Double)     { activeBeatFreq  = hz.coerceIn(0.1, 100.0) }
    fun setMixFreq(hz: Double)      { activeMixFreq   = hz.coerceIn(0.5, 22_000.0) }
    fun setSweepStart(hz: Double)      { activeSweepStart  = hz.coerceIn(0.5, 22_000.0) }
    fun setSweepEnd(hz: Double)        { activeSweepEnd    = hz.coerceIn(0.5, 22_000.0) }
    fun setSweepSpeed(hz: Double)      { activeSweepSpeed  = hz.coerceIn(0.005, 5.0) }
    fun setSweepRandom(v: Double)      { activeSweepRandom = v.coerceIn(0.0, 1.0) }
    fun setEndLfoEnabled(on: Boolean)  { endLfoEnabled     = on }
    fun setEndLfoSpeed(hz: Double)     { endLfoSpeed       = hz.coerceIn(0.005, 5.0) }
    fun setEndLfoDepth(v: Double)      { endLfoDepth       = v.coerceIn(0.0, 1.0) }
    fun setVolume(v: Float)            { activeVolume      = v.coerceIn(0f, 1f) }
    fun setNoiseToggles(white: Boolean, pink: Boolean, brown: Boolean) {
        whiteNoiseOn = white; pinkNoiseOn = pink; brownNoiseOn = brown
    }
    fun setIsochronic(on: Boolean, rate: Double, smooth: Boolean) {
        isochronicOn     = on
        isochronicRate   = rate.coerceIn(0.5, 40.0)
        isochronicSmooth = smooth
    }
    fun setToneEnabled(on: Boolean) { toneEnabled = on }
    fun setFilter(on: Boolean, cutoff: Double, resonance: Double) {
        filterOn        = on
        filterCutoff    = cutoff.coerceIn(20.0, 20_000.0)
        filterResonance = resonance.coerceIn(0.0, 0.99)
    }
    fun setAdsr(on: Boolean, a: Double, d: Double, s: Double, r: Double) {
        adsrOn      = on
        adsrAttack  = a.coerceIn(0.0, 5.0)
        adsrDecay   = d.coerceIn(0.0, 5.0)
        adsrSustain = s.coerceIn(0.0, 1.0)
        adsrRelease = r.coerceIn(0.0, 5.0)
    }
    fun setPreferredDevice(device: AudioDeviceInfo?) {
        audioTrack?.setPreferredDevice(device)
    }

    val isRunning   get() = running
    val currentFreq get() = targetFreq
    val currentMode get() = activeMode

    private fun rawSample(phase: Double, wf: Waveform): Double = when (wf) {
        Waveform.SINE     -> sin(phase)
        Waveform.SQUARE   -> if (sin(phase) >= 0.0) 1.0 else -1.0
        Waveform.TRIANGLE -> 2.0 / PI * asin(sin(phase))
        Waveform.SAWTOOTH -> 2.0 * (phase / (2.0 * PI)) - 1.0
    }
}
