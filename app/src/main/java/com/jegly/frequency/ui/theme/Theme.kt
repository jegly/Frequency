package com.jegly.frequency.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TunesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "system",
    catppuccinFlavor: String = "mocha",
    catppuccinAccent: String = "mauve",
    draculaAccent: String = "purple",
    ptyxisPalette: String = DEFAULT_PTYXIS_KEY,
    monochromeAccents: Boolean = false,
    appFont: String = "dotgothic16",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val flavor = catppuccinFlavorFromKey(catppuccinFlavor)

    val isEffectivelyDark = when (themeMode) {
        "light"      -> false
        "catppuccin" -> flavor != CatppuccinFlavor.LATTE
        "ptyxis"     -> true // every Ptyxis palette is a dark scheme
        else         -> darkTheme // "system" and "dracula" follow the device/manual dark toggle
    }

    val colorScheme = when (themeMode) {
        "catppuccin" -> buildCatppuccinColorScheme(
            flavor = flavor,
            accent = catppuccinAccentFromKey(catppuccinAccent),
            monochrome = monochromeAccents
        )
        "dracula" -> buildDraculaColorScheme(
            accent = draculaAccentFromKey(draculaAccent),
            dark = isEffectivelyDark,
            monochrome = monochromeAccents
        )
        "light"  -> fixedLightColorScheme
        "ptyxis" -> buildPtyxisAnyColorScheme(ptyxisPalette)
        else     -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isEffectivelyDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isEffectivelyDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = buildAppTypography(appFontFromKey(appFont).family),
        content = content
    )
}
