package com.jegly.frequency.playback

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.SystemClock
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.singleton.TrackManager

class PlaybackManager(
    private val context: Context,
    private val playback: Playback,
    private val trackManager: TrackManager,
    private val serviceCallback: PlaybackServiceCallback
) : Playback.Callback {

    companion object {
        const val ACTION_SET_SPEED = "com.jegly.frequency.ACTION_SET_SPEED"
        const val METADATA_TITLE_KEY = MediaMetadata.METADATA_KEY_TITLE
        const val METADATA_ARTIST_KEY = MediaMetadata.METADATA_KEY_ARTIST
        const val METADATA_ALBUM_KEY = MediaMetadata.METADATA_KEY_ALBUM
        const val METADATA_ALBUM_ART = MediaMetadata.METADATA_KEY_ALBUM_ART
        const val METADATA_DURATION_KEY = MediaMetadata.METADATA_KEY_DURATION

        const val ACTION_PLAY_NEXT: Short = 1
        const val ACTION_PLAY_PREV: Short = 2
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStart()
        fun onPlaybackStopped()
        fun onStartNotification()
        fun onStopNotification()
        fun onPlaybackStateChanged(newState: PlaybackState)
        fun onMetaDataChanged(newMetaData: MediaMetadata)
    }

    private var lastKnownPath = ""

    init {
        playback.setCallback(this)
    }

    val sessionCallbacks: MediaSession.Callback
        get() = object : MediaSession.Callback() {
            override fun onPlay() {
                handlePlay()
            }

            override fun onPause() {
                handlePause()
            }

            override fun onStop() {
                handleStop()
            }

            override fun onSkipToNext() {
                handleSkipToNext()
            }

            override fun onSkipToPrevious() {
                handleSkipToPrev()
            }

            override fun onSeekTo(pos: Long) {
                playback.onSeekTo(pos)
            }

            override fun onCustomAction(action: String, extras: Bundle?) {
                if (action == ACTION_SET_SPEED) {
                    val speed = extras?.getFloat("speed", 1.0f) ?: 1.0f
                    playback.setPlaybackSpeed(speed)
                }
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                handlePlay()
            }
        }

    private fun handlePlay() {
        val item = trackManager.getActiveQueueItem() ?: return
        val mediaChanged = item.songPath != lastKnownPath
        lastKnownPath = item.songPath

        playback.onPlay(item, mediaChanged)
        serviceCallback.onPlaybackStart()
        serviceCallback.onStartNotification()

        if (mediaChanged) {
            publishMetadata(item)
        }
    }

    private fun handlePause() {
        playback.onPause()
    }

    private fun handleStop() {
        playback.onStop(true)
        serviceCallback.onPlaybackStopped()
        serviceCallback.onStopNotification()
    }

    private fun handleSkipToNext() {
        if (trackManager.canSkipTrack(ACTION_PLAY_NEXT)) {
            handlePlay()
        } else {
            handleStop()
        }
    }

    private fun handleSkipToPrev() {
        if (playback.getCurrentStreamingPosition() > 3000) {
            playback.onSeekTo(0)
        } else if (trackManager.canSkipTrack(ACTION_PLAY_PREV)) {
            handlePlay()
        }
    }

    private fun publishMetadata(md: MusicModel) {
        val builder = MediaMetadata.Builder()
            .putString(METADATA_TITLE_KEY, md.songName)
            .putString(METADATA_ARTIST_KEY, md.artist)
            .putString(METADATA_ALBUM_KEY, md.album)
            .putLong(METADATA_DURATION_KEY, md.duration)

        serviceCallback.onMetaDataChanged(builder.build())
    }

    override fun onPlaybackCompletion() {
        handleSkipToNext()
    }

    override fun onTrackChangedSeamlessly() {
        if (trackManager.canSkipTrack(ACTION_PLAY_NEXT)) {
            val item = trackManager.getActiveQueueItem() ?: return
            lastKnownPath = item.songPath
            publishMetadata(item)
            serviceCallback.onStartNotification()
        } else {
            handleStop()
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        val actions = (PlaybackState.ACTION_PLAY
                or PlaybackState.ACTION_PAUSE
                or PlaybackState.ACTION_PLAY_PAUSE
                or PlaybackState.ACTION_SKIP_TO_NEXT
                or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                or PlaybackState.ACTION_STOP
                or PlaybackState.ACTION_SEEK_TO)

        val builder = PlaybackState.Builder()
            .setActions(actions)
            .setState(state, playback.getCurrentStreamingPosition(), 1.0f, SystemClock.elapsedRealtime())

        serviceCallback.onPlaybackStateChanged(builder.build())
    }

    override fun onFocusChanged(resumePlayback: Boolean) {
        if (resumePlayback) {
            handlePlay()
        } else {
            handlePause()
        }
    }
}
