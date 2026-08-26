package com.jegly.frequency.model

import com.jegly.frequency.audio.ToneGenerator

/** Full snapshot of a Signal Lab session — everything needed to recreate what's playing. */
data class ToneSessionParams(
    val frequency: Double,
    val waveform: ToneGenerator.Waveform,
    val mode: ToneGenerator.PlayMode,
    val beatFreq: Double,
    val mixFreq: Double,
    val sweepStart: Double,
    val sweepEnd: Double,
    val sweepSpeed: Double,
    val sweepRandom: Double,
    val endLfoOn: Boolean,
    val endLfoSpeed: Double,
    val endLfoDepth: Double,
    val volume: Float,
    val whiteNoiseOn: Boolean,
    val pinkNoiseOn: Boolean,
    val brownNoiseOn: Boolean,
    val isochronicOn: Boolean,
    val isochronicRate: Double,
    val isochronicSmooth: Boolean,
    val toneEnabled: Boolean,
    val filterOn: Boolean,
    val filterCutoff: Double,
    val filterResonance: Double,
    val adsrOn: Boolean,
    val adsrAttack: Double,
    val adsrDecay: Double,
    val adsrSustain: Double,
    val adsrRelease: Double
)
