package com.jegly.frequency.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.singleton.TrackManager
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.utils.AudioOutputs

class LocalPlayback(
    private val context: Context,
    private val trackManager: TrackManager,
    private val handler: Handler? = null
) : Playback,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        @Volatile var audioSessionId: Int = 0
        private const val CROSSFADE_DURATION_MS = 5000L
        private const val CROSSFADE_STEPS = 50
    }

    private val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var playbackCallback: Playback.Callback? = null
    private var mediaPlayer: MediaPlayer? = null
    private var resumePosition = -1
    private var isBecomingNoisyReceiverRegistered = false
    private var currentState: Int = 0

    // Gapless / crossfade
    private var nextMediaPlayer: MediaPlayer? = null
    private var nextTrackPrepared = false
    private var isCrossfading = false
    private var scheduleRunnable: Runnable? = null

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                playbackCallback?.onFocusChanged(false)
            }
        }
    }

    private var mediaId: Long = -99L
    private val workerHandler = handler ?: Handler(Looper.getMainLooper())
    private var delayedPlayback = false
    private var playbackState = PlaybackState.STATE_NONE
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun setCallback(callback: Playback.Callback) {
        playbackCallback = callback
    }

    private fun applyPreferredDevice(mp: MediaPlayer) {
        val device = AudioOutputs.findById(context, AppSettings.getPreferredAudioDeviceId(context))
        try { mp.setPreferredDevice(device) } catch (e: Exception) {
            Log.w("LocalPlayback", "setPreferredDevice failed", e)
        }
    }

    fun refreshPreferredDevice() {
        mediaPlayer?.let { applyPreferredDevice(it) }
        nextMediaPlayer?.let { applyPreferredDevice(it) }
    }

    private fun initMediaPlayer() {
        cancelPendingTransitions()
        mediaPlayer?.let {
            try { it.release() } catch (e: Exception) {
                Log.e("LocalPlayback", "Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener(this@LocalPlayback)
            setOnCompletionListener(this@LocalPlayback)
            applyPreferredDevice(this)
        }
        audioSessionId = mediaPlayer?.audioSessionId ?: 0

        val activeItem = trackManager.getActiveQueueItem()
        if (activeItem?.songPath == null) {
            playbackCallback?.onPlaybackCompletion()
            return
        }

        try {
            mediaPlayer?.setDataSource(context, Uri.parse(activeItem.songPath))
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e("LocalPlayback", "Playback error", e)
            Toast.makeText(context, "Music file error", Toast.LENGTH_LONG).show()
            playbackCallback?.onPlaybackCompletion()
        }
        workerHandler.post { trackManager.addToHistory(context) }
    }

    override fun onPlay(md: MusicModel?, mediaHasChanged: Boolean) {
        if (tryGetAudioFocus()) {
            delayedPlayback = false
            if (!mediaHasChanged) {
                if (mediaPlayer != null) play() else initMediaPlayer()
            } else {
                onStop(false)
                initMediaPlayer()
                md?.let { mediaId = it.id }
            }
            registerBecomingNoisyReceiver()
        } else if (delayedPlayback && md != null) {
            mediaId = md.id
        }
    }

    private fun play() {
        mediaPlayer?.let {
            try {
                if (resumePosition != -1) {
                    it.seekTo(resumePosition)
                    resumePosition = -1
                }
                it.start()
                val savedSpeed = AppSettings.getPlaybackSpeed(context)
                if (savedSpeed != 1.0f) {
                    try { it.playbackParams = it.playbackParams.setSpeed(savedSpeed) } catch (e: Exception) {}
                }
                playbackState = PlaybackState.STATE_PLAYING
                playbackCallback?.onPlaybackStateChanged(playbackState)
                scheduleCrossfadeOrGapless()
            } catch (e: IllegalStateException) {
                Log.e("LocalPlayback", "Playback error", e)
                initMediaPlayer()
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        play()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (isCrossfading && mp == mediaPlayer) {
            // Current player finished during crossfade; next is already playing
            try { mp.release() } catch (e: Exception) {}
            mediaPlayer = nextMediaPlayer
            nextMediaPlayer = null
            isCrossfading = false
            nextTrackPrepared = false
            audioSessionId = mediaPlayer?.audioSessionId ?: 0
            playbackCallback?.onTrackChangedSeamlessly()
            scheduleCrossfadeOrGapless()
            return
        }

        val crossfadeOn = AppSettings.isCrossfadeEnabled(context)
        val gaplessOn = AppSettings.isGaplessEnabled(context)

        if (gaplessOn && !crossfadeOn && nextTrackPrepared && nextMediaPlayer != null && mp == mediaPlayer) {
            // Gapless: swap players instantly with no gap
            try { mp.release() } catch (e: Exception) {}
            mediaPlayer = nextMediaPlayer!!
            mediaPlayer?.setOnCompletionListener(this)
            nextMediaPlayer = null
            nextTrackPrepared = false
            audioSessionId = mediaPlayer?.audioSessionId ?: 0
            mediaPlayer?.start()
            playbackState = PlaybackState.STATE_PLAYING
            playbackCallback?.onPlaybackStateChanged(playbackState)
            playbackCallback?.onTrackChangedSeamlessly()
            scheduleCrossfadeOrGapless()
            return
        }

        // Normal completion
        cancelPendingTransitions()
        onStop(false)
        playbackCallback?.onPlaybackCompletion()
    }

    override fun onPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
            }
        }
        playbackState = PlaybackState.STATE_PAUSED
        playbackCallback?.onPlaybackStateChanged(playbackState)
    }

    override fun onSeekTo(position: Long) {
        mediaPlayer?.let {
            it.seekTo(position.toInt())
            playbackCallback?.onPlaybackStateChanged(playbackState)
        }
    }

    override fun onStop(abandonAudioFocus: Boolean) {
        cancelPendingTransitions()
        if (abandonAudioFocus) abandonAudioFocus()

        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                Log.e("LocalPlayback", "Error stopping MediaPlayer", e)
            }
            mediaPlayer = null
        }

        if (isBecomingNoisyReceiverRegistered) {
            try { context.unregisterReceiver(becomingNoisyReceiver) } catch (e: Exception) {
                Log.e("LocalPlayback", "Unregister error", e)
            }
            isBecomingNoisyReceiverRegistered = false
        }

        playbackState = PlaybackState.STATE_STOPPED
        playbackCallback?.onPlaybackStateChanged(playbackState)

        if (abandonAudioFocus) abandonAudioFocus()
    }

    fun resetPlaybackState() {
        mediaPlayer?.let {
            try { if (it.isPlaying) it.stop(); it.release() } catch (e: Exception) {}
            mediaPlayer = null
        }
        playbackState = PlaybackState.STATE_STOPPED
        playbackCallback?.onPlaybackStateChanged(playbackState)
        resumePosition = -1
        mediaId = -99L
    }

    override fun getActiveMediaId(): Long = mediaId
    override fun isPlaying(): Boolean = try { mediaPlayer?.isPlaying ?: false } catch (e: IllegalStateException) { false }
    override fun getCurrentStreamingPosition(): Long = try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (e: IllegalStateException) { 0L }
    override fun getPlaybackState(): Int = playbackState

    override fun setPlaybackSpeed(speed: Float) {
        try {
            val mp = mediaPlayer ?: return
            mp.playbackParams = mp.playbackParams.setSpeed(speed)
        } catch (e: Exception) {
            Log.e("LocalPlayback", "setPlaybackSpeed failed", e)
        }
    }

    // ── Gapless / Crossfade ───────────────────────────────────────

    private fun peekNextTrack(): MusicModel? {
        val list = trackManager.activeList.value
        val nextIndex = trackManager.getActiveIndex() + 1
        return if (nextIndex < list.size) list[nextIndex] else null
    }

    private fun cancelPendingTransitions() {
        isCrossfading = false
        nextTrackPrepared = false
        scheduleRunnable?.let { workerHandler.removeCallbacks(it) }
        scheduleRunnable = null
        nextMediaPlayer?.let {
            try { if (it.isPlaying) it.stop(); it.release() } catch (e: Exception) {}
        }
        nextMediaPlayer = null
    }

    private fun scheduleCrossfadeOrGapless() {
        val crossfadeEnabled = AppSettings.isCrossfadeEnabled(context)
        val gaplessEnabled = AppSettings.isGaplessEnabled(context)
        if (!crossfadeEnabled && !gaplessEnabled) return
        if (peekNextTrack() == null) return

        val prepareAheadMs = if (crossfadeEnabled) CROSSFADE_DURATION_MS + 1500L else 3000L

        val runnable = object : Runnable {
            override fun run() {
                if (isCrossfading || nextTrackPrepared || nextMediaPlayer != null) return
                val remaining = try {
                    val mp = mediaPlayer ?: return
                    (mp.duration - mp.currentPosition).toLong()
                } catch (e: Exception) { return }

                when {
                    remaining in 1..prepareAheadMs -> prepareNextTrack(startCrossfadeWhenReady = crossfadeEnabled)
                    remaining > prepareAheadMs -> workerHandler.postDelayed(this, 500)
                }
            }
        }
        scheduleRunnable = runnable
        workerHandler.postDelayed(runnable, 1000)
    }

    private fun prepareNextTrack(startCrossfadeWhenReady: Boolean) {
        val next = peekNextTrack() ?: return
        val np = MediaPlayer()
        np.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        applyPreferredDevice(np)
        np.setOnPreparedListener { mp ->
            nextTrackPrepared = true
            if (startCrossfadeWhenReady && !isCrossfading) {
                mp.setVolume(0f, 0f)
                mp.start()
                startCrossfadeAnimation()
            }
        }
        np.setOnCompletionListener(this)
        try {
            np.setDataSource(context, Uri.parse(next.songPath))
            np.prepareAsync()
        } catch (e: Exception) {
            Log.e("LocalPlayback", "Failed to prepare next track", e)
            try { np.release() } catch (ex: Exception) {}
            return
        }
        nextMediaPlayer = np
    }

    private fun startCrossfadeAnimation() {
        isCrossfading = true
        val stepDelay = CROSSFADE_DURATION_MS / CROSSFADE_STEPS
        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                if (!isCrossfading) return
                step++
                val progress = step.toFloat() / CROSSFADE_STEPS
                try {
                    mediaPlayer?.setVolume(1f - progress, 1f - progress)
                    nextMediaPlayer?.setVolume(progress, progress)
                } catch (e: Exception) {}
                if (step < CROSSFADE_STEPS) workerHandler.postDelayed(this, stepDelay)
                // onCompletion on the old player finalizes the swap
            }
        }
        workerHandler.postDelayed(runnable, stepDelay)
    }

    // ── Audio focus ───────────────────────────────────────────────

    private fun tryGetAudioFocus(): Boolean {
        val r: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, workerHandler)
                    .build()
            }
            r = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (r == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) delayedPlayback = true
        } else {
            @Suppress("DEPRECATION")
            r = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        currentState = focusChange
        if (mediaPlayer != null) configurePlayerState()
        else if (delayedPlayback) playbackCallback?.onFocusChanged(true)
    }

    private fun configurePlayerState() {
        when (currentState) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                playbackCallback?.onFocusChanged(false)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                mediaPlayer?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                playbackCallback?.onFocusChanged(true)
            }
        }
    }

    private fun registerBecomingNoisyReceiver() {
        if (!isBecomingNoisyReceiverRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(becomingNoisyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(becomingNoisyReceiver, filter)
            }
            isBecomingNoisyReceiverRegistered = true
        }
    }
}
