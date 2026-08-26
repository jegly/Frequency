package com.jegly.frequency.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// Builds a complete Material 3 ColorScheme entirely from the Catppuccin palette.
// Latte is the only light flavor; Frappe/Macchiato/Mocha are dark.
// Primary/secondary/tertiary are three distinct Catppuccin accents chosen by hue proximity
// (or collapsed to one when [monochrome] is set).
fun buildCatppuccinColorScheme(
    flavor: CatppuccinFlavor,
    accent: CatppuccinAccent,
    monochrome: Boolean
): ColorScheme {
    val dark = flavor != CatppuccinFlavor.LATTE
    val inverseFlavor = if (dark) CatppuccinFlavor.LATTE else CatppuccinFlavor.MOCHA

    val b = catppuccinBaseColors(flavor)
    val bi = catppuccinBaseColors(inverseFlavor)

    val primary = catppuccinAccentColor(flavor, accent)
    val secondary = if (monochrome) primary
                    else catppuccinAccentColor(flavor, catppuccinSecondaryAccent(accent))
    val tertiary = if (monochrome) primary
                   else catppuccinAccentColor(flavor, catppuccinTertiaryAccent(accent))

    fun onColor(c: Color) = if (c.luminance() < 0.35f) Color.White else b.base
    fun container(c: Color) = lerp(b.base, c, 0.22f)
    fun onContainer(c: Color) = lerp(b.text, c, 0.20f)

    val errorColor = catppuccinAccentColor(flavor, CatppuccinAccent.RED)
    val inversePrimary = catppuccinAccentColor(inverseFlavor, accent)

    return if (dark) darkColorScheme(
        primary = primary, onPrimary = onColor(primary),
        primaryContainer = container(primary), onPrimaryContainer = onContainer(primary),
        secondary = secondary, onSecondary = onColor(secondary),
        secondaryContainer = container(secondary), onSecondaryContainer = onContainer(secondary),
        tertiary = tertiary, onTertiary = onColor(tertiary),
        tertiaryContainer = container(tertiary), onTertiaryContainer = onContainer(tertiary),
        error = errorColor, onError = onColor(errorColor),
        errorContainer = container(errorColor), onErrorContainer = onContainer(errorColor),
        background = b.base, onBackground = b.text, surface = b.base, onSurface = b.text,
        surfaceVariant = b.surface0, onSurfaceVariant = b.subtext1,
        outline = b.overlay1, outlineVariant = b.surface2,
        scrim = b.crust, inverseSurface = bi.base, inverseOnSurface = bi.text,
        inversePrimary = inversePrimary,
        surfaceDim = b.mantle, surfaceBright = b.surface1,
        surfaceContainerLowest = b.crust, surfaceContainerLow = b.mantle,
        surfaceContainer = b.surface0, surfaceContainerHigh = b.surface1,
        surfaceContainerHighest = b.surface2
    ) else lightColorScheme(
        primary = primary, onPrimary = onColor(primary),
        primaryContainer = container(primary), onPrimaryContainer = onContainer(primary),
        secondary = secondary, onSecondary = onColor(secondary),
        secondaryContainer = container(secondary), onSecondaryContainer = onContainer(secondary),
        tertiary = tertiary, onTertiary = onColor(tertiary),
        tertiaryContainer = container(tertiary), onTertiaryContainer = onContainer(tertiary),
        error = errorColor, onError = onColor(errorColor),
        errorContainer = container(errorColor), onErrorContainer = onContainer(errorColor),
        background = b.base, onBackground = b.text, surface = b.base, onSurface = b.text,
        surfaceVariant = b.surface0, onSurfaceVariant = b.subtext1,
        outline = b.overlay1, outlineVariant = b.surface2,
        scrim = b.crust, inverseSurface = bi.base, inverseOnSurface = bi.text,
        inversePrimary = inversePrimary,
        surfaceDim = b.mantle, surfaceBright = b.surface1,
        surfaceContainerLowest = b.crust, surfaceContainerLow = b.mantle,
        surfaceContainer = b.surface0, surfaceContainerHigh = b.surface1,
        surfaceContainerHighest = b.surface2
    )
}

// Builds a complete Material 3 ColorScheme from the Dracula palette.
// [dark] picks the official dark surfaces or a Dracula-adapted light base.
fun buildDraculaColorScheme(
    accent: DraculaAccent,
    dark: Boolean,
    monochrome: Boolean
): ColorScheme {
    val b = draculaBaseColors(dark)
    val bi = draculaBaseColors(!dark)

    val primary = draculaAccentColor(dark, accent)
    val secondary = if (monochrome) primary
                    else draculaAccentColor(dark, draculaSecondaryAccent(accent))
    val tertiary = if (monochrome) primary
                   else draculaAccentColor(dark, draculaTertiaryAccent(accent))

    fun onColor(c: Color) = if (c.luminance() < 0.35f) Color.White else b.base
    fun container(c: Color) = lerp(b.base, c, 0.22f)
    fun onContainer(c: Color) = lerp(b.text, c, 0.20f)

    val errorColor = draculaAccentColor(dark, DraculaAccent.RED)

    return if (dark) darkColorScheme(
        primary = primary, onPrimary = onColor(primary),
        primaryContainer = container(primary), onPrimaryContainer = onContainer(primary),
        secondary = secondary, onSecondary = onColor(secondary),
        secondaryContainer = container(secondary), onSecondaryContainer = onContainer(secondary),
        tertiary = tertiary, onTertiary = onColor(tertiary),
        tertiaryContainer = container(tertiary), onTertiaryContainer = onContainer(tertiary),
        error = errorColor, onError = onColor(errorColor),
        errorContainer = container(errorColor), onErrorContainer = onContainer(errorColor),
        background = b.base, onBackground = b.text, surface = b.base, onSurface = b.text,
        surfaceVariant = b.surface0, onSurfaceVariant = b.subtext1,
        outline = b.overlay1, outlineVariant = b.surface2,
        scrim = b.crust, inverseSurface = bi.base, inverseOnSurface = bi.text,
        inversePrimary = draculaAccentColor(!dark, accent),
        surfaceDim = b.mantle, surfaceBright = b.surface1,
        surfaceContainerLowest = b.crust, surfaceContainerLow = b.mantle,
        surfaceContainer = b.surface0, surfaceContainerHigh = b.surface1,
        surfaceContainerHighest = b.surface2
    ) else lightColorScheme(
        primary = primary, onPrimary = onColor(primary),
        primaryContainer = container(primary), onPrimaryContainer = onContainer(primary),
        secondary = secondary, onSecondary = onColor(secondary),
        secondaryContainer = container(secondary), onSecondaryContainer = onContainer(secondary),
        tertiary = tertiary, onTertiary = onColor(tertiary),
        tertiaryContainer = container(tertiary), onTertiaryContainer = onContainer(tertiary),
        error = errorColor, onError = onColor(errorColor),
        errorContainer = container(errorColor), onErrorContainer = onContainer(errorColor),
        background = b.base, onBackground = b.text, surface = b.base, onSurface = b.text,
        surfaceVariant = b.surface0, onSurfaceVariant = b.subtext1,
        outline = b.overlay1, outlineVariant = b.surface2,
        scrim = b.crust, inverseSurface = bi.base, inverseOnSurface = bi.text,
        inversePrimary = draculaAccentColor(!dark, accent),
        surfaceDim = b.mantle, surfaceBright = b.surface1,
        surfaceContainerLowest = b.crust, surfaceContainerLow = b.mantle,
        surfaceContainer = b.surface0, surfaceContainerHigh = b.surface1,
        surfaceContainerHighest = b.surface2
    )
}

// A fixed, genuinely-bright light scheme (CustomTheme "light") — wallpaper-independent, unlike
// the dynamic system light scheme which tints toward the wallpaper and rarely goes crisp white.
val fixedLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF2F5DA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF565E71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131C2B),
    tertiary = Color(0xFF6E5676),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6D9FF),
    onTertiaryContainer = Color(0xFF271430),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF75777F),
    outlineVariant = Color(0xFFC5C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFACC7FF),
    surfaceDim = Color(0xFFDAD9DD),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFF1F3F5),
    surfaceContainerHigh = Color(0xFFEBEDF0),
    surfaceContainerHighest = Color(0xFFE5E8EB),
)
