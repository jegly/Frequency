package com.tunes.player.playback

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
import com.tunes.player.model.MusicModel
import com.tunes.player.singleton.TrackManager

class LocalPlayback(
    private val context: Context,
    private val trackManager: TrackManager,
    private val handler: Handler? = null
) : Playback,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    AudioManager.OnAudioFocusChangeListener {

    private val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var playbackCallback: Playback.Callback? = null
    private var mediaPlayer: MediaPlayer? = null
    private var resumePosition = -1
    private var isBecomingNoisyReceiverRegistered = false
    private var currentState: Int = 0
    
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

    private fun initMediaPlayer() {
        mediaPlayer?.let {
            try {
                it.release()
            } catch (e: Exception) {
                Log.e("LocalPlayback", "Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener(this@LocalPlayback)
            setOnCompletionListener(this@LocalPlayback)
        }

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
                if (mediaPlayer != null) {
                    play()
                } else {
                    initMediaPlayer()
                }
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
                playbackState = PlaybackState.STATE_PLAYING
                playbackCallback?.onPlaybackStateChanged(playbackState)
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
            try {
                context.unregisterReceiver(becomingNoisyReceiver)
            } catch (e: Exception) {
                Log.e("LocalPlayback", "Playback error", e)
            }
            isBecomingNoisyReceiverRegistered = false
        }

        // Always set state to STOPPED when stopping playback
        playbackState = PlaybackState.STATE_STOPPED
        playbackCallback?.onPlaybackStateChanged(playbackState)
        
        if (abandonAudioFocus) {
            abandonAudioFocus()
        }
    }
    
    // Add method to force reset playback state
    fun resetPlaybackState() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                Log.e("LocalPlayback", "Error resetting MediaPlayer", e)
            }
            mediaPlayer = null
        }
        
        playbackState = PlaybackState.STATE_STOPPED
        playbackCallback?.onPlaybackStateChanged(playbackState)
        resumePosition = -1
        mediaId = -99L
    }

    override fun getActiveMediaId(): Long = mediaId

    override fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: IllegalStateException) {
            false
        }
    }

    override fun getCurrentStreamingPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: IllegalStateException) {
            0L
        }
    }

    override fun getPlaybackState(): Int = playbackState

    private fun tryGetAudioFocus(): Boolean {
        val r: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, workerHandler)
                    .build()
            }
            r = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (r == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) delayedPlayback = true
        } else {
            r = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
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
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                playbackCallback?.onFocusChanged(false)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
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
