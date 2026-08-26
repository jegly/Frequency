package com.jegly.frequency.audio

import com.jegly.frequency.model.ToneSessionParams
import java.io.RandomAccessFile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sin

/**
 * Offline WAV renderer for a frozen [ToneSessionParams] snapshot. Mirrors [ToneGenerator]'s
 * synthesis math (waveform, mode, noise, filter, isochronic gate, ADSR/fade envelope) but runs
 * standalone — it does not touch the live playback thread, so exporting can't regress it.
 */
object ToneExporter {

    private const val SAMPLE_RATE = 44100
    private const val CHUNK = 4096
    private const val FADE_SAMPLES = 4410 // 100 ms
    private const val AMPLITUDE = 0.40f

    /**
     * Renders [durationSeconds] of audio for [params] into [outFile] as 16-bit PCM stereo WAV.
     * Calls [onProgress] with 0f..1f periodically. Returns false if cancelled via [isCancelled].
     */
    fun renderToWav(
        params: ToneSessionParams,
        durationSeconds: Int,
        outFile: java.io.File,
        onProgress: (Float) -> Unit,
        isCancelled: () -> Boolean
    ): Boolean {
        val totalSamples = durationSeconds.toLong() * SAMPLE_RATE
        val rng = java.util.Random()

        RandomAccessFile(outFile, "rw").use { raf ->
            raf.setLength(0)
            writeWavHeaderPlaceholder(raf)

            var phaseL = 0.0
            var phaseR = 0.0
            var phase2 = 0.0
            var n = 0L

            var sweepPos = 0.0
            var sweepDir = 1.0
            var endLfoPhase = 0.0
            var isoPhase = 0.0
            var svfLowL = 0.0; var svfBandL = 0.0
            var svfLowR = 0.0; var svfBandR = 0.0
            var pinkB0 = 0.0; var pinkB1 = 0.0; var pinkB2 = 0.0
            var pinkB3 = 0.0; var pinkB4 = 0.0; var pinkB5 = 0.0; var pinkB6 = 0.0
            var brownLast = 0.0

            val amp = AMPLITUDE * params.volume.toDouble()
            val anyNoise = params.whiteNoiseOn || params.pinkNoiseOn || params.brownNoiseOn
            val isoInc = 2.0 * PI * params.isochronicRate / SAMPLE_RATE
            val fCutoff = params.filterCutoff.coerceAtMost(SAMPLE_RATE * 0.45)
            val svfF = 2.0 * sin(PI * fCutoff / SAMPLE_RATE)
            val svfQ = (1.0 - params.filterResonance).coerceAtLeast(0.02)
            val aAtkSamp = (params.adsrAttack * SAMPLE_RATE).toInt().coerceAtLeast(1)
            val aDecSamp = (params.adsrDecay * SAMPLE_RATE).toInt().coerceAtLeast(1)
            val aSusLvl = params.adsrSustain

            val incL = 2.0 * PI * params.frequency / SAMPLE_RATE
            val incR = if (params.mode == ToneGenerator.PlayMode.BINAURAL)
                2.0 * PI * (params.frequency + params.beatFreq) / SAMPLE_RATE else incL
            val inc2 = 2.0 * PI * params.mixFreq / SAMPLE_RATE
            val sweepBaseInc = params.sweepSpeed * 2.0 / SAMPLE_RATE

            val pcmBuf = ByteArray(CHUNK * 4) // 2 channels x 2 bytes
            var written = 0L
            var lastProgressReport = 0L

            while (written < totalSamples) {
                if (isCancelled()) return false
                val samplesThisChunk = minOf(CHUNK.toLong(), totalSamples - written).toInt()

                for (i in 0 until samplesThisChunk) {
                    val env = if (params.adsrOn) {
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
                    val playTone = params.toneEnabled

                    when (params.mode) {
                        ToneGenerator.PlayMode.NORMAL -> {
                            val s = if (playTone) (rawSample(phaseL, params.waveform) * env * amp).toFloat() else 0f
                            l = s; r = s
                            phaseL = (phaseL + incL) % (2.0 * PI)
                        }
                        ToneGenerator.PlayMode.BINAURAL -> {
                            l = if (playTone) (rawSample(phaseL, params.waveform) * env * amp).toFloat() else 0f
                            r = if (playTone) (rawSample(phaseR, params.waveform) * env * amp).toFloat() else 0f
                            phaseL = (phaseL + incL) % (2.0 * PI)
                            phaseR = (phaseR + incR) % (2.0 * PI)
                        }
                        ToneGenerator.PlayMode.MIX -> {
                            val s = if (playTone)
                                ((rawSample(phaseL, params.waveform) + rawSample(phase2, params.waveform)) * 0.5 * env * amp).toFloat()
                            else 0f
                            l = s; r = s
                            phaseL = (phaseL + incL) % (2.0 * PI)
                            phase2 = (phase2 + inc2) % (2.0 * PI)
                        }
                        ToneGenerator.PlayMode.SWEEP -> {
                            val jitter = (rng.nextDouble() - 0.5) * params.sweepRandom * sweepBaseInc * 8.0
                            sweepPos += sweepDir * sweepBaseInc + jitter
                            if (sweepPos >= 1.0) { sweepPos = 2.0 - sweepPos; sweepDir = -1.0 }
                            if (sweepPos <= 0.0) { sweepPos = -sweepPos; sweepDir = 1.0 }
                            sweepPos = sweepPos.coerceIn(0.0, 1.0)

                            val instEnd = if (params.endLfoOn) {
                                val range = abs(params.sweepEnd - params.sweepStart) * params.endLfoDepth
                                (params.sweepEnd + sin(endLfoPhase) * range).coerceIn(0.5, 22_000.0)
                            } else params.sweepEnd
                            endLfoPhase = (endLfoPhase + params.endLfoSpeed * 2.0 * PI / SAMPLE_RATE) % (2.0 * PI)

                            val instFreq = params.sweepStart + sweepPos * (instEnd - params.sweepStart)
                            val sweepInc = 2.0 * PI * instFreq / SAMPLE_RATE
                            val s = if (playTone) (rawSample(phaseL, params.waveform) * env * amp).toFloat() else 0f
                            l = s; r = s
                            phaseL = (phaseL + sweepInc) % (2.0 * PI)
                        }
                    }

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
                        if (params.whiteNoiseOn) mix += white * 0.5
                        if (params.pinkNoiseOn) mix += pink
                        if (params.brownNoiseOn) mix += brown
                        val activeCount = (if (params.whiteNoiseOn) 1 else 0) +
                            (if (params.pinkNoiseOn) 1 else 0) + (if (params.brownNoiseOn) 1 else 0)
                        if (activeCount > 1) mix /= activeCount

                        if (playTone) {
                            val nSample = (mix * env * amp * 0.5).toFloat()
                            l += nSample; r += nSample
                        } else {
                            val nSample = (mix * env * amp).toFloat()
                            l = nSample; r = nSample
                        }
                    }

                    if (params.filterOn) {
                        svfLowL += svfF * svfBandL
                        val highL = l.toDouble() - svfLowL - svfQ * svfBandL
                        svfBandL += svfF * highL
                        l = svfLowL.toFloat()
                        svfLowR += svfF * svfBandR
                        val highR = r.toDouble() - svfLowR - svfQ * svfBandR
                        svfBandR += svfF * highR
                        r = svfLowR.toFloat()
                    }

                    val gate = if (params.isochronicOn) {
                        val g = if (params.isochronicSmooth) {
                            (sin(isoPhase) * 0.5 + 0.5).coerceAtLeast(0.0)
                        } else {
                            if (sin(isoPhase) >= 0.0) 1.0 else 0.0
                        }
                        isoPhase = (isoPhase + isoInc) % (2.0 * PI)
                        g.toFloat()
                    } else 1.0f

                    val outL = (l * gate).coerceIn(-1f, 1f)
                    val outR = (r * gate).coerceIn(-1f, 1f)
                    val pcmL = (outL * Short.MAX_VALUE).toInt().toShort()
                    val pcmR = (outR * Short.MAX_VALUE).toInt().toShort()
                    val idx = i * 4
                    pcmBuf[idx] = (pcmL.toInt() and 0xFF).toByte()
                    pcmBuf[idx + 1] = ((pcmL.toInt() shr 8) and 0xFF).toByte()
                    pcmBuf[idx + 2] = (pcmR.toInt() and 0xFF).toByte()
                    pcmBuf[idx + 3] = ((pcmR.toInt() shr 8) and 0xFF).toByte()
                    n++
                }

                raf.write(pcmBuf, 0, samplesThisChunk * 4)
                written += samplesThisChunk

                if (written - lastProgressReport >= SAMPLE_RATE / 4) {
                    onProgress((written.toFloat() / totalSamples).coerceIn(0f, 1f))
                    lastProgressReport = written
                }
            }

            patchWavHeader(raf, written)
        }

        onProgress(1f)
        return true
    }

    private fun writeWavHeaderPlaceholder(raf: RandomAccessFile) {
        raf.write(ByteArray(44))
    }

    private fun patchWavHeader(raf: RandomAccessFile, totalSamples: Long) {
        val dataBytes = totalSamples * 4 // 2 channels x 2 bytes
        val byteRate = SAMPLE_RATE * 2 * 2
        val blockAlign = 2 * 2

        raf.seek(0)
        raf.write("RIFF".toByteArray())
        raf.write(intLE((36 + dataBytes).toInt()))
        raf.write("WAVE".toByteArray())
        raf.write("fmt ".toByteArray())
        raf.write(intLE(16))
        raf.write(shortLE(1)) // PCM
        raf.write(shortLE(2)) // stereo
        raf.write(intLE(SAMPLE_RATE))
        raf.write(intLE(byteRate))
        raf.write(shortLE(blockAlign))
        raf.write(shortLE(16)) // bits per sample
        raf.write("data".toByteArray())
        raf.write(intLE(dataBytes.toInt()))
    }

    private fun intLE(v: Int): ByteArray = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
        ((v shr 16) and 0xFF).toByte(),
        ((v shr 24) and 0xFF).toByte()
    )

    private fun shortLE(v: Int): ByteArray = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte()
    )

    private fun rawSample(phase: Double, wf: ToneGenerator.Waveform): Double = when (wf) {
        ToneGenerator.Waveform.SINE -> sin(phase)
        ToneGenerator.Waveform.SQUARE -> if (sin(phase) >= 0.0) 1.0 else -1.0
        ToneGenerator.Waveform.TRIANGLE -> 2.0 / PI * asin(sin(phase))
        ToneGenerator.Waveform.SAWTOOTH -> 2.0 * (phase / (2.0 * PI)) - 1.0
    }
}
