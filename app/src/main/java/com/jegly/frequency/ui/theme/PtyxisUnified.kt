package com.jegly.frequency.ui.theme

import androidx.compose.material3.ColorScheme

/** One flat list of all 44 Ptyxis palettes (11 curated + 33 extended) for a single picker. */
data class PtyxisEntry(val key: String, val displayName: String)

val allPtyxisPalettes: List<PtyxisEntry> =
    PtyxisPalette.entries.map { PtyxisEntry(it.key, it.displayName) } +
        PtyxisPaletteExtended.entries.map { PtyxisEntry(it.key, it.displayName) }

const val DEFAULT_PTYXIS_KEY = "nord"

/** Resolves a palette key across both enums and builds its ColorScheme, falling back to Nord. */
fun buildPtyxisAnyColorScheme(key: String): ColorScheme {
    ptyxisPaletteFromKey(key)?.let { return buildPtyxisColorScheme(it) }
    ptyxisPaletteExtendedFromKey(key)?.let { return buildPtyxisExtendedColorScheme(it) }
    return buildPtyxisColorScheme(PtyxisPalette.NORD)
}
