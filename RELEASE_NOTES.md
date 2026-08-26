# Frequency v0.2.0 (versionCode 4)

## New features

- **Custom presets** — save the entire current Signal Lab session (frequency, waveform, mode,
  noise, filter, ADSR, isochronic settings) as a named preset for one-tap recall later. Manage
  them from the new "My Presets" section of the preset browser (apply / delete).
- **Export session to audio file** — render the current Signal Lab session to a WAV file (any
  duration) and save it to `Music/Frequency/`. Runs in the background with a progress dialog and
  cancel button.
- **Home screen widget** — shows the current track's title/artist with prev / play-pause / next
  controls, kept live as playback changes.
- **Isochronic tone mode** — was already implemented but undocumented; now called out properly in
  the README (rhythmic on/off pulsing, hard-gated or sine-smoothed, layers on top of any mode).

## Themes

- Catppuccin: now all 4 flavors (Latte/Frappé/Macchiato/Mocha) x 14 accents (was 1 flavor).
- Dracula: now light + dark variants (was dark-only), same 7 accents.
- New "Light" theme — a fixed, wallpaper-independent bright scheme.
- New "Ptyxis" theme — 44 terminal-inspired color palettes (11 curated + 33 extended) in one
  picker.
- New monochrome-accent toggle for Catppuccin/Dracula.
- New app-wide font picker — 13 selectable font families (default stays DotGothic16, so existing
  installs look unchanged until a new font is picked).

## Security fix

- The Biometric Lock setting previously only gated the in-app UI — the underlying media session
  service was still reachable by any other app on the device (bypassing the lock to read
  now-playing info and control playback). It now also denies non-app callers while the lock is on.

## Platform

- `minSdk` lowered from 35 to 33 (Android 13+), widening device support. Existing
  version-gated code paths already had correct fallbacks for this range, so behavior on 33/34
  devices is unchanged from what already worked on lower API levels within those gates.

## Upgrade notes

- No user data migrations needed; all new settings default to previous behavior.
- The app's package was already mid-rename from `com.tunes.player` to `com.jegly.frequency` in
  this branch — that rename is unrelated to this release and predates it.
