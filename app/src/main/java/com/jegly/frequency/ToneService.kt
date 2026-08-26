package com.jegly.frequency

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jegly.frequency.activities.MainActivity
import com.jegly.frequency.audio.ToneGenerator
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.utils.AudioOutputs

class ToneService : Service() {

    companion object {
        const val ACTION_START          = "com.jegly.frequency.TONE_START"
        const val ACTION_STOP           = "com.jegly.frequency.TONE_STOP"
        const val ACTION_SET_FREQ       = "com.jegly.frequency.TONE_SET_FREQ"
        const val ACTION_SET_WAVEFORM   = "com.jegly.frequency.TONE_SET_WAVEFORM"
        const val ACTION_SET_MODE       = "com.jegly.frequency.TONE_SET_MODE"
        const val ACTION_SET_BEAT_FREQ  = "com.jegly.frequency.TONE_SET_BEAT_FREQ"
        const val ACTION_SET_MIX_FREQ   = "com.jegly.frequency.TONE_SET_MIX_FREQ"
        const val ACTION_UPDATE_SWEEP   = "com.jegly.frequency.TONE_UPDATE_SWEEP"
        const val ACTION_SET_VOLUME     = "com.jegly.frequency.TONE_SET_VOLUME"
        const val ACTION_SET_NOISE      = "com.jegly.frequency.TONE_SET_NOISE"
        const val ACTION_SET_ISOCHRONIC = "com.jegly.frequency.TONE_SET_ISOCHRONIC"
        const val ACTION_SET_DEVICE     = "com.jegly.frequency.TONE_SET_DEVICE"
        const val ACTION_SET_TONE_ON    = "com.jegly.frequency.TONE_SET_TONE_ON"
        const val ACTION_SET_FILTER     = "com.jegly.frequency.TONE_SET_FILTER"
        const val ACTION_SET_ADSR       = "com.jegly.frequency.TONE_SET_ADSR"

        const val EXTRA_FREQ          = "freq"
        const val EXTRA_WAVEFORM      = "waveform"
        const val EXTRA_MODE          = "mode"
        const val EXTRA_BEAT_FREQ     = "beat_freq"
        const val EXTRA_MIX_FREQ      = "mix_freq"
        const val EXTRA_SWEEP_START    = "sweep_start"
        const val EXTRA_SWEEP_END      = "sweep_end"
        const val EXTRA_SWEEP_SPEED    = "sweep_speed"
        const val EXTRA_SWEEP_RANDOM   = "sweep_random"
        const val EXTRA_END_LFO_ON     = "end_lfo_on"
        const val EXTRA_END_LFO_SPEED  = "end_lfo_speed"
        const val EXTRA_END_LFO_DEPTH  = "end_lfo_depth"
        const val EXTRA_VOLUME         = "volume"
        const val EXTRA_NOISE_WHITE    = "noise_white"
        const val EXTRA_NOISE_PINK     = "noise_pink"
        const val EXTRA_NOISE_BROWN    = "noise_brown"
        const val EXTRA_ISO_ON         = "iso_on"
        const val EXTRA_ISO_RATE       = "iso_rate"
        const val EXTRA_ISO_SMOOTH     = "iso_smooth"
        const val EXTRA_TONE_ON        = "tone_on"
        const val EXTRA_FILTER_ON      = "filter_on"
        const val EXTRA_FILTER_CUTOFF  = "filter_cutoff"
        const val EXTRA_FILTER_RES     = "filter_res"
        const val EXTRA_ADSR_ON        = "adsr_on"
        const val EXTRA_ADSR_A         = "adsr_a"
        const val EXTRA_ADSR_D         = "adsr_d"
        const val EXTRA_ADSR_S         = "adsr_s"
        const val EXTRA_ADSR_R         = "adsr_r"

        private const val CHANNEL_ID      = "tone_generator_channel"
        private const val NOTIFICATION_ID = 99

        val generator = ToneGenerator()
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val freq     = intent.getDoubleExtra(EXTRA_FREQ, 440.0)
                val wf       = ToneGenerator.Waveform.valueOf(
                    intent.getStringExtra(EXTRA_WAVEFORM) ?: ToneGenerator.Waveform.SINE.name)
                val mode     = ToneGenerator.PlayMode.valueOf(
                    intent.getStringExtra(EXTRA_MODE) ?: ToneGenerator.PlayMode.NORMAL.name)
                generator.start(
                    frequency   = freq,
                    waveform    = wf,
                    mode        = mode,
                    beatFreq    = intent.getDoubleExtra(EXTRA_BEAT_FREQ, 4.0),
                    mixFreq     = intent.getDoubleExtra(EXTRA_MIX_FREQ, 528.0),
                    sweepStart  = intent.getDoubleExtra(EXTRA_SWEEP_START,  200.0),
                    sweepEnd    = intent.getDoubleExtra(EXTRA_SWEEP_END,   1000.0),
                    sweepSpeed  = intent.getDoubleExtra(EXTRA_SWEEP_SPEED,   0.1),
                    sweepRandom = intent.getDoubleExtra(EXTRA_SWEEP_RANDOM,  0.3),
                    endLfoOn    = intent.getBooleanExtra(EXTRA_END_LFO_ON,  false),
                    endLfoSpd   = intent.getDoubleExtra(EXTRA_END_LFO_SPEED, 0.2),
                    endLfoDep   = intent.getDoubleExtra(EXTRA_END_LFO_DEPTH, 0.3),
                    volume      = intent.getFloatExtra(EXTRA_VOLUME, 1.0f),
                    whiteOn     = intent.getBooleanExtra(EXTRA_NOISE_WHITE, false),
                    pinkOn      = intent.getBooleanExtra(EXTRA_NOISE_PINK,  false),
                    brownOn     = intent.getBooleanExtra(EXTRA_NOISE_BROWN, false),
                    isoOn       = intent.getBooleanExtra(EXTRA_ISO_ON,     false),
                    isoRate     = intent.getDoubleExtra(EXTRA_ISO_RATE,    7.83),
                    isoSmooth   = intent.getBooleanExtra(EXTRA_ISO_SMOOTH, false),
                    toneOn      = intent.getBooleanExtra(EXTRA_TONE_ON, true),
                    filtOn      = intent.getBooleanExtra(EXTRA_FILTER_ON, false),
                    filtCutoff  = intent.getDoubleExtra(EXTRA_FILTER_CUTOFF, 2000.0),
                    filtRes     = intent.getDoubleExtra(EXTRA_FILTER_RES, 0.3),
                    adsrEnable  = intent.getBooleanExtra(EXTRA_ADSR_ON, false),
                    adsrA       = intent.getDoubleExtra(EXTRA_ADSR_A, 0.05),
                    adsrD       = intent.getDoubleExtra(EXTRA_ADSR_D, 0.20),
                    adsrS       = intent.getDoubleExtra(EXTRA_ADSR_S, 0.80),
                    adsrR       = intent.getDoubleExtra(EXTRA_ADSR_R, 0.30),
                    preferredDevice = AudioOutputs.findById(this, AppSettings.getPreferredAudioDeviceId(this))
                )
                startForeground(NOTIFICATION_ID, buildNotification(freq, generator.currentMode))
            }
            ACTION_STOP -> {
                generator.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SET_FREQ -> {
                val freq = intent.getDoubleExtra(EXTRA_FREQ, generator.currentFreq)
                generator.setFrequency(freq)
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(freq, generator.currentMode))
            }
            ACTION_SET_WAVEFORM -> {
                val name = intent.getStringExtra(EXTRA_WAVEFORM) ?: return START_NOT_STICKY
                generator.setWaveform(ToneGenerator.Waveform.valueOf(name))
            }
            ACTION_SET_MODE -> {
                val name = intent.getStringExtra(EXTRA_MODE) ?: return START_NOT_STICKY
                val mode = ToneGenerator.PlayMode.valueOf(name)
                generator.setPlayMode(mode)
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(generator.currentFreq, mode))
            }
            ACTION_SET_BEAT_FREQ -> {
                generator.setBeatFreq(intent.getDoubleExtra(EXTRA_BEAT_FREQ, 4.0))
            }
            ACTION_SET_MIX_FREQ -> {
                generator.setMixFreq(intent.getDoubleExtra(EXTRA_MIX_FREQ, 528.0))
            }
            ACTION_SET_VOLUME -> {
                generator.setVolume(intent.getFloatExtra(EXTRA_VOLUME, 1.0f))
            }
            ACTION_SET_NOISE -> {
                generator.setNoiseToggles(
                    white = intent.getBooleanExtra(EXTRA_NOISE_WHITE, false),
                    pink  = intent.getBooleanExtra(EXTRA_NOISE_PINK,  false),
                    brown = intent.getBooleanExtra(EXTRA_NOISE_BROWN, false)
                )
            }
            ACTION_SET_ISOCHRONIC -> {
                generator.setIsochronic(
                    on     = intent.getBooleanExtra(EXTRA_ISO_ON,     false),
                    rate   = intent.getDoubleExtra(EXTRA_ISO_RATE,    7.83),
                    smooth = intent.getBooleanExtra(EXTRA_ISO_SMOOTH, false)
                )
            }
            ACTION_SET_DEVICE -> {
                generator.setPreferredDevice(
                    AudioOutputs.findById(this, AppSettings.getPreferredAudioDeviceId(this))
                )
            }
            ACTION_SET_TONE_ON -> {
                generator.setToneEnabled(intent.getBooleanExtra(EXTRA_TONE_ON, true))
            }
            ACTION_SET_FILTER -> {
                generator.setFilter(
                    on        = intent.getBooleanExtra(EXTRA_FILTER_ON,    false),
                    cutoff    = intent.getDoubleExtra(EXTRA_FILTER_CUTOFF, 2000.0),
                    resonance = intent.getDoubleExtra(EXTRA_FILTER_RES,    0.3)
                )
            }
            ACTION_SET_ADSR -> {
                generator.setAdsr(
                    on = intent.getBooleanExtra(EXTRA_ADSR_ON, false),
                    a  = intent.getDoubleExtra(EXTRA_ADSR_A, 0.05),
                    d  = intent.getDoubleExtra(EXTRA_ADSR_D, 0.20),
                    s  = intent.getDoubleExtra(EXTRA_ADSR_S, 0.80),
                    r  = intent.getDoubleExtra(EXTRA_ADSR_R, 0.30)
                )
            }
            ACTION_UPDATE_SWEEP -> {
                generator.setSweepStart(intent.getDoubleExtra(EXTRA_SWEEP_START,   200.0))
                generator.setSweepEnd(intent.getDoubleExtra(EXTRA_SWEEP_END,      1000.0))
                generator.setSweepSpeed(intent.getDoubleExtra(EXTRA_SWEEP_SPEED,    0.1))
                generator.setSweepRandom(intent.getDoubleExtra(EXTRA_SWEEP_RANDOM,  0.3))
                generator.setEndLfoEnabled(intent.getBooleanExtra(EXTRA_END_LFO_ON, false))
                generator.setEndLfoSpeed(intent.getDoubleExtra(EXTRA_END_LFO_SPEED, 0.2))
                generator.setEndLfoDepth(intent.getDoubleExtra(EXTRA_END_LFO_DEPTH, 0.3))
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (generator.isRunning) generator.stop()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Frequency", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Background tone playback" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(freq: Double, mode: ToneGenerator.PlayMode): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, ToneService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        val modeTag = if (mode != ToneGenerator.PlayMode.NORMAL) " · ${mode.label}" else ""
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Frequency$modeTag")
            .setContentText("Playing ${formatHz(freq)}")
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun formatHz(hz: Double) = when {
        hz < 10    -> "%.2f Hz".format(hz)
        hz < 100   -> "%.1f Hz".format(hz)
        hz < 1000  -> "%.0f Hz".format(hz)
        hz < 10000 -> "%.2f kHz".format(hz / 1000.0)
        else       -> "%.1f kHz".format(hz / 1000.0)
    }
}
