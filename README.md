
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

## Release signing

Release APKs use hybrid signing (APK Signature Scheme v3.2): the original app key plus an EC P-384 key and an ML-DSA-87 post-quantum key. Android 17 and newer verify the hybrid block, where the ML-DSA-87 key is the signer; earlier Android versions verify the original key as before.

| File | SHA-256 |
| --- | --- |
| `Frequency-0.2.3-PQC-signed.apk` | `78f7bc0909f5950a8e251ada38a924872e14d892470947b9ab13bc1ec150bab2` |

Verify a download:

```bash
sha256sum Frequency-0.2.3-PQC-signed.apk
apksigner verify -v --print-certs Frequency-0.2.3-PQC-signed.apk   # build-tools 37.0.0 on JDK 25 shows all three keys
```

Signing certificate SHA-256:

- ML-DSA-87 (post-quantum): `5f99de5b3195753d4fe8ddabebd2fb27356b2109d6fbb2471fcbbdf21a9e76a6`
- EC P-384 (hybrid classical): `fa8f4a07e6a4f2b7dc6e5ab31fc05592b1414ab9436d1e2e39b35ce3bb2e33a2`
- Original key: `98d324d4106a368c62729a0a24d9ac9a6b47f8ac4c6585348531f0ee4eb6a04c`

## License

GPL-3.0 — see [LICENSE](LICENSE)
