package com.jegly.frequency.model

enum class WaveCategory(val label: String) {
    ALL("All"),
    DELTA("Delta"),
    THETA("Theta"),
    ALPHA("Alpha"),
    BETA("Beta"),
    GAMMA("Gamma"),
    SOLFEGGIO("Solfeggio"),
    HIGH("High Hz")
}

data class FrequencyPreset(
    val name: String,
    val frequency: Double,
    val effect: String,
    val category: WaveCategory
)

object FrequencyPresets {
    val all: List<FrequencyPreset> = listOf(
        // Delta
        FrequencyPreset("Delta 0.5 Hz", 0.5,  "Deep sleep, healing, unconscious mind",   WaveCategory.DELTA),
        FrequencyPreset("Delta 1 Hz",   1.0,  "Deep sleep, healing, unconscious mind",   WaveCategory.DELTA),
        FrequencyPreset("Delta 2 Hz",   2.0,  "Deep sleep, healing, unconscious mind",   WaveCategory.DELTA),
        FrequencyPreset("Delta 3 Hz",   3.0,  "Deep sleep, regeneration",                WaveCategory.DELTA),
        FrequencyPreset("Delta 4 Hz",   4.0,  "Deep sleep, healing",                     WaveCategory.DELTA),

        // Theta
        FrequencyPreset("Theta 4 Hz",    4.0,  "Deep relaxation, creativity",              WaveCategory.THETA),
        FrequencyPreset("Theta 5 Hz",    5.0,  "Deep relaxation, memory consolidation",   WaveCategory.THETA),
        FrequencyPreset("Theta 6 Hz",    6.0,  "Creativity, visualization",               WaveCategory.THETA),
        FrequencyPreset("Schumann 7.83", 7.83, "Earth resonance – Schumann frequency",    WaveCategory.THETA),
        FrequencyPreset("Theta 8 Hz",    8.0,  "Deep relaxation, creativity",             WaveCategory.THETA),

        // Alpha
        FrequencyPreset("Alpha 8 Hz",  8.0,  "Calm alertness, reduced stress",           WaveCategory.ALPHA),
        FrequencyPreset("Alpha 9 Hz",  9.0,  "Calm alertness, mental coordination",      WaveCategory.ALPHA),
        FrequencyPreset("Alpha 10 Hz", 10.0, "Calm alertness, light meditation",         WaveCategory.ALPHA),
        FrequencyPreset("Alpha 11 Hz", 11.0, "Calm alertness, reduced stress",           WaveCategory.ALPHA),
        FrequencyPreset("Alpha 12 Hz", 12.0, "Calm alertness, relaxation",               WaveCategory.ALPHA),
        FrequencyPreset("Alpha 13 Hz", 13.0, "Calm alertness, mental coordination",      WaveCategory.ALPHA),

        // Beta
        FrequencyPreset("Low Beta 14 Hz",  14.0, "Focus, alertness, learning",           WaveCategory.BETA),
        FrequencyPreset("Low Beta 16 Hz",  16.0, "Focus, alertness, learning",           WaveCategory.BETA),
        FrequencyPreset("Low Beta 18 Hz",  18.0, "Focus, alertness, learning",           WaveCategory.BETA),
        FrequencyPreset("High Beta 20 Hz", 20.0, "Active thinking, problem-solving",     WaveCategory.BETA),
        FrequencyPreset("High Beta 25 Hz", 25.0, "Active thinking, stress, focus",       WaveCategory.BETA),
        FrequencyPreset("High Beta 30 Hz", 30.0, "Active thinking, problem-solving",     WaveCategory.BETA),

        // Gamma
        FrequencyPreset("Gamma 30 Hz",  30.0,  "High cognition, memory recall",         WaveCategory.GAMMA),
        FrequencyPreset("Gamma 40 Hz",  40.0,  "Memory recall, peak performance",       WaveCategory.GAMMA),
        FrequencyPreset("Gamma 60 Hz",  60.0,  "High-level cognition, perception",      WaveCategory.GAMMA),
        FrequencyPreset("Gamma 80 Hz",  80.0,  "Peak performance, awareness",           WaveCategory.GAMMA),
        FrequencyPreset("Gamma 100 Hz", 100.0, "Advanced learning, peak performance",   WaveCategory.GAMMA),

        // Solfeggio / Healing
        FrequencyPreset("174 Hz – Grounding",     174.0, "Grounding, security, pain relief",         WaveCategory.SOLFEGGIO),
        FrequencyPreset("285 Hz – Regeneration",  285.0, "Tissue healing, cellular regeneration",    WaveCategory.SOLFEGGIO),
        FrequencyPreset("396 Hz – Fear Release",  396.0, "Liberates fear and guilt",                 WaveCategory.SOLFEGGIO),
        FrequencyPreset("417 Hz – Change",        417.0, "Undo negative patterns, change",           WaveCategory.SOLFEGGIO),
        FrequencyPreset("432 Hz – Harmony",       432.0, "Heart-centered calm, natural tuning",      WaveCategory.SOLFEGGIO),
        FrequencyPreset("440 Hz – Standard A4",   440.0, "Standard concert pitch, neutral",          WaveCategory.SOLFEGGIO),
        FrequencyPreset("444 Hz – Meditation",    444.0, "Spiritual alignment, C5 = 528 Hz",         WaveCategory.SOLFEGGIO),
        FrequencyPreset("528 Hz – Love",          528.0, "DNA repair, transformation, love",         WaveCategory.SOLFEGGIO),
        FrequencyPreset("639 Hz – Connection",    639.0, "Relationships, empathy, harmony",          WaveCategory.SOLFEGGIO),
        FrequencyPreset("741 Hz – Creativity",    741.0, "Expression, detox, intuition",             WaveCategory.SOLFEGGIO),
        FrequencyPreset("839 Hz – Clarity",       839.0, "Advanced audio experimentation",           WaveCategory.SOLFEGGIO),
        FrequencyPreset("852 Hz – Awareness",     852.0, "Return to spiritual order, clarity",       WaveCategory.SOLFEGGIO),
        FrequencyPreset("963 Hz – Enlightenment", 963.0, "Higher consciousness, divine connection",  WaveCategory.SOLFEGGIO),

        // High Frequencies
        FrequencyPreset("100 Hz – Low Focus",      100.0,  "Calm attention, grounding",                WaveCategory.HIGH),
        FrequencyPreset("110 Hz – Mental Clarity", 110.0,  "Mental clarity, focus",                   WaveCategory.HIGH),
        FrequencyPreset("120 Hz – Grounding",      120.0,  "Energy stabilization, mild relaxation",   WaveCategory.HIGH),
        FrequencyPreset("130 Hz – Motivation",     130.0,  "Mild alertness, motivation",              WaveCategory.HIGH),
        FrequencyPreset("140 Hz – Creativity",     140.0,  "Ideation, problem-solving",               WaveCategory.HIGH),
        FrequencyPreset("150 Hz – Alignment",      150.0,  "Body-mind connection",                    WaveCategory.HIGH),
        FrequencyPreset("160 Hz – Calm Focus",     160.0,  "Relaxed concentration",                   WaveCategory.HIGH),
        FrequencyPreset("174 Hz – Healing",        174.0,  "Grounding, security",                     WaveCategory.HIGH),
        FrequencyPreset("180 Hz – Relaxation",     180.0,  "Reduces stress, calms mind",              WaveCategory.HIGH),
        FrequencyPreset("200 Hz – Focus",          200.0,  "Task-oriented thinking",                  WaveCategory.HIGH),
        FrequencyPreset("220 Hz – Harmony",        220.0,  "Balance of mind and body",                WaveCategory.HIGH),
        FrequencyPreset("240 Hz – Stress Relief",  240.0,  "Relaxation, calm energy",                 WaveCategory.HIGH),
        FrequencyPreset("250 Hz – Attention",      250.0,  "Attention enhancement",                   WaveCategory.HIGH),
        FrequencyPreset("285 Hz – Regeneration",   285.0,  "Tissue healing",                          WaveCategory.HIGH),
        FrequencyPreset("300 Hz – Focus",          300.0,  "Mental clarity",                          WaveCategory.HIGH),
        FrequencyPreset("350 Hz – Motivation",     350.0,  "Stimulates action",                       WaveCategory.HIGH),
        FrequencyPreset("396 Hz – Fear Release",   396.0,  "Liberates fear and guilt",                WaveCategory.HIGH),
        FrequencyPreset("400 Hz – Focus & Relax",  400.0,  "Concentration with calm",                 WaveCategory.HIGH),
        FrequencyPreset("417 Hz – Change",         417.0,  "Undo negative patterns",                  WaveCategory.HIGH),
        FrequencyPreset("432 Hz – Harmony",        432.0,  "Heart-centered calm",                     WaveCategory.HIGH),
        FrequencyPreset("440 Hz – Standard",       440.0,  "Standard A4 pitch",                       WaveCategory.HIGH),
        FrequencyPreset("450 Hz – Calm Focus",     450.0,  "Balanced attention",                      WaveCategory.HIGH),
        FrequencyPreset("500 Hz – Mid High",       500.0,  "Focused attention",                       WaveCategory.HIGH),
        FrequencyPreset("528 Hz – Love",           528.0,  "DNA repair, transformation",              WaveCategory.HIGH),
        FrequencyPreset("600 Hz – Activation",     600.0,  "Cognitive stimulation",                   WaveCategory.HIGH),
        FrequencyPreset("639 Hz – Connection",     639.0,  "Relationships, empathy",                  WaveCategory.HIGH),
        FrequencyPreset("700 Hz – Activation",     700.0,  "Subtle alertness",                        WaveCategory.HIGH),
        FrequencyPreset("741 Hz – Creativity",     741.0,  "Expression, intuition",                   WaveCategory.HIGH),
        FrequencyPreset("800 Hz – Calm Energy",    800.0,  "Relaxed focus",                           WaveCategory.HIGH),
        FrequencyPreset("839 Hz – Clarity",        839.0,  "Advanced experimentation",                WaveCategory.HIGH),
        FrequencyPreset("852 Hz – Awareness",      852.0,  "Spiritual order",                         WaveCategory.HIGH),
        FrequencyPreset("900 Hz – Mental Clarity", 900.0,  "Thought organization",                    WaveCategory.HIGH),
        FrequencyPreset("963 Hz – Enlightenment",  963.0,  "Higher consciousness",                    WaveCategory.HIGH),
        FrequencyPreset("1000 Hz – Peak",         1000.0,  "Peak alertness, advanced meditation",     WaveCategory.HIGH),
        FrequencyPreset("2000 Hz",                2000.0,  "Cognitive sharpening",                    WaveCategory.HIGH),
        FrequencyPreset("5000 Hz",                5000.0,  "Advanced audio experiments",              WaveCategory.HIGH),
        FrequencyPreset("10000 Hz",              10000.0,  "High-frequency awareness",                WaveCategory.HIGH),
    )

    fun forCategory(cat: WaveCategory): List<FrequencyPreset> =
        if (cat == WaveCategory.ALL) all else all.filter { it.category == cat }
}
