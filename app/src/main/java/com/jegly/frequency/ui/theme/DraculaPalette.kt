package com.jegly.frequency.ui.theme

import androidx.compose.ui.graphics.Color

enum class DraculaAccent(val displayName: String, val key: String) {
    PURPLE("Purple", "purple"),
    PINK("Pink", "pink"),
    CYAN("Cyan", "cyan"),
    GREEN("Green", "green"),
    ORANGE("Orange", "orange"),
    RED("Red", "red"),
    YELLOW("Yellow", "yellow"),
}

data class DraculaBaseColors(
    val text: Color,
    val subtext1: Color,
    val overlay1: Color,
    val overlay0: Color,
    val surface2: Color,
    val surface1: Color,
    val surface0: Color,
    val base: Color,
    val mantle: Color,
    val crust: Color,
)

// Official Dracula dark accent colors; darker adaptations for legibility on a light background.
private val DARK_ACCENT: Map<DraculaAccent, Color> = mapOf(
    DraculaAccent.PURPLE to Color(0xFFbd93f9),
    DraculaAccent.PINK to Color(0xFFff79c6),
    DraculaAccent.CYAN to Color(0xFF8be9fd),
    DraculaAccent.GREEN to Color(0xFF50fa7b),
    DraculaAccent.ORANGE to Color(0xFFffb86c),
    DraculaAccent.RED to Color(0xFFff5555),
    DraculaAccent.YELLOW to Color(0xFFf1fa8c),
)

private val LIGHT_ACCENT: Map<DraculaAccent, Color> = mapOf(
    DraculaAccent.PURPLE to Color(0xFF7b4fef),
    DraculaAccent.PINK to Color(0xFFd9337a),
    DraculaAccent.CYAN to Color(0xFF0090c0),
    DraculaAccent.GREEN to Color(0xFF1c7a3d),
    DraculaAccent.ORANGE to Color(0xFFc55a00),
    DraculaAccent.RED to Color(0xFFcc2222),
    DraculaAccent.YELLOW to Color(0xFFa07800),
)

private val DARK_BASE = DraculaBaseColors(
    text = Color(0xFFf8f8f2),
    subtext1 = Color(0xFFb8bac8),
    overlay1 = Color(0xFF7b8db0),
    overlay0 = Color(0xFF6272a4),
    surface2 = Color(0xFF565761),
    surface1 = Color(0xFF44475a),
    surface0 = Color(0xFF383a48),
    base = Color(0xFF282a36),
    mantle = Color(0xFF21222c),
    crust = Color(0xFF191a21),
)

private val LIGHT_BASE = DraculaBaseColors(
    text = Color(0xFF282a36),
    subtext1 = Color(0xFF414559),
    overlay1 = Color(0xFF6272a4),
    overlay0 = Color(0xFF7b8db0),
    surface2 = Color(0xFFd6d8e7),
    surface1 = Color(0xFFe2e3ed),
    surface0 = Color(0xFFecedfa),
    base = Color(0xFFf8f8f2),
    mantle = Color(0xFFf0f0f9),
    crust = Color(0xFFe8e8f5),
)

// Hue-adjacent secondary and tertiary accent pairings.
private val SECONDARY_ACCENT: Map<DraculaAccent, DraculaAccent> = mapOf(
    DraculaAccent.PURPLE to DraculaAccent.PINK,
    DraculaAccent.PINK to DraculaAccent.PURPLE,
    DraculaAccent.CYAN to DraculaAccent.GREEN,
    DraculaAccent.GREEN to DraculaAccent.CYAN,
    DraculaAccent.ORANGE to DraculaAccent.YELLOW,
    DraculaAccent.RED to DraculaAccent.ORANGE,
    DraculaAccent.YELLOW to DraculaAccent.ORANGE,
)

private val TERTIARY_ACCENT: Map<DraculaAccent, DraculaAccent> = mapOf(
    DraculaAccent.PURPLE to DraculaAccent.CYAN,
    DraculaAccent.PINK to DraculaAccent.ORANGE,
    DraculaAccent.CYAN to DraculaAccent.PURPLE,
    DraculaAccent.GREEN to DraculaAccent.YELLOW,
    DraculaAccent.ORANGE to DraculaAccent.RED,
    DraculaAccent.RED to DraculaAccent.PINK,
    DraculaAccent.YELLOW to DraculaAccent.GREEN,
)

fun draculaAccentColor(dark: Boolean, accent: DraculaAccent): Color =
    (if (dark) DARK_ACCENT else LIGHT_ACCENT)[accent] ?: Color(0xFFbd93f9)

fun draculaBaseColors(dark: Boolean): DraculaBaseColors = if (dark) DARK_BASE else LIGHT_BASE

fun draculaSecondaryAccent(accent: DraculaAccent): DraculaAccent =
    SECONDARY_ACCENT[accent] ?: DraculaAccent.PINK

fun draculaTertiaryAccent(accent: DraculaAccent): DraculaAccent =
    TERTIARY_ACCENT[accent] ?: DraculaAccent.CYAN

fun draculaAccentFromKey(key: String): DraculaAccent =
    DraculaAccent.entries.firstOrNull { it.key == key } ?: DraculaAccent.PURPLE
