
<p align="center">
  <img src="Freq_banner.png" alt="Frequency Banner" />
</p>

Android music player with a built-in tone generator / synthesizer.

## Features

### Music Player
- Plays local audio files (MP3, FLAC, OGG, WAV, etc.)
- Library browser, user playlists with drag-to-reorder
- Crossfade, gapless playback, speed control
- Equalizer, search and sort
- Mini-player with persistent now-playing screen
- Home screen widget with prev/play-pause/next controls

### Themes
- System, Light, Catppuccin (4 flavors x 14 accents), Dracula (light/dark, 7 accents),
  and Ptyxis (44 terminal-inspired palettes)
- Monochrome accent option for Catppuccin/Dracula
- 13 selectable app-wide fonts

### Signal Lab
Monophonic software synthesizer for brainwave entrainment, meditation, and sound exploration.

**Waveforms** — Sine, Square, Triangle, Sawtooth

**Modes**
- **Normal** — single frequency, mono
- **Binaural** — left and right channels at slightly different frequencies; the brain synthesises the beat
- **Mix** — two frequencies summed
- **Sweep** — LFO-driven frequency sweep between a start and end point, with randomness and a second end-point LFO
- **Isochronic** — rhythmic on/off pulsing at a set rate (hard-gated or sine-smoothed), layers on top of any mode

**Random** — auto-switches to a random preset frequency every 2 seconds – 30 minutes (stacks with Normal, Binaural, Mix)

**Presets**
- 70+ built-in presets (brainwave bands, solfeggio frequencies)
- Save your own presets — captures the full session (frequency, waveform, mode, noise, filter, ADSR, isochronic settings) for one-tap recall later

**Export**
- Render the current session to a WAV file (any duration) and save it to Music/Frequency/

**Other**
- Timer: auto-stop after 5 – 90 minutes
- Background playback via foreground service
- Frequency range: 0.5 Hz – 22 kHz

## Requirements

- Android 13.0+
- `READ_MEDIA_AUDIO` permission for music library

## Build

```
./gradlew assembleRelease
```

## License

GPL-3.0 — see [LICENSE](LICENSE)
