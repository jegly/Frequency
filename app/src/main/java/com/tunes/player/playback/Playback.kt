package com.tunes.player.playback

import com.tunes.player.model.MusicModel

interface Playback {

    fun onPlay(md: MusicModel?, mediaHasChanged: Boolean)

    fun onPause()

    fun onSeekTo(position: Long)

    fun onStop(abandonAudioFocus: Boolean)

    fun getActiveMediaId(): Long

    fun isPlaying(): Boolean

    fun getCurrentStreamingPosition(): Long

    fun getPlaybackState(): Int

    fun setPlaybackSpeed(speed: Float)

    fun setCallback(callback: Callback)

    interface Callback {
        fun onPlaybackCompletion()

        fun onTrackChangedSeamlessly()

        fun onPlaybackStateChanged(state: Int)

        fun onFocusChanged(resumePlayback: Boolean)
    }
}
