package com.jegly.frequency

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.service.media.MediaBrowserService
import androidx.core.app.NotificationCompat
import com.jegly.frequency.playback.LocalPlayback
import com.jegly.frequency.playback.MediaNotificationManager
import com.jegly.frequency.playback.PlaybackManager
import com.jegly.frequency.singleton.TrackManager
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.widget.FrequencyWidgetProvider

class TMS : MediaBrowserService(), PlaybackManager.PlaybackServiceCallback {

    private var mMediaSession: MediaSession? = null
    private var mNotificationManager: MediaNotificationManager? = null
    private var mPlaybackManager: PlaybackManager? = null
    private var isServiceRunning = false
    private var mServiceThread: HandlerThread? = null
    private var mWorkerHandler: Handler? = null

    companion object {
        private const val TAG = "TMS"
        private const val CHANNEL_ID = "tunes_service_channel"
        private const val NOTIFICATION_ID = 412
        
        const val ACTION_PLAY = "com.jegly.frequency.ACTION_PLAY"
        const val ACTION_PAUSE = "com.jegly.frequency.ACTION_PAUSE"
        const val ACTION_STOP = "com.jegly.frequency.ACTION_STOP"
        const val ACTION_NEXT = "com.jegly.frequency.ACTION_NEXT"
        const val ACTION_PREV = "com.jegly.frequency.ACTION_PREV"
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // Biometric Lock implies the user wants playback/metadata hidden from other apps too,
        // so only our own process may bind while it's enabled (Auto/Bluetooth/Assistant still
        // work normally when the lock is off).
        if (AppSettings.isBiometricEnabled(applicationContext) && clientUid != Process.myUid()) {
            return null
        }
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowser.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onCreate() {
        super.onCreate()

        mMediaSession = MediaSession(applicationContext, TAG).apply {
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            isActive = true
        }

        sessionToken = mMediaSession?.sessionToken

        mServiceThread = HandlerThread("TMS-Worker").apply { start() }
        mWorkerHandler = Handler(mServiceThread!!.looper)
        
        val trackManager = TrackManager.instance
        val playback = LocalPlayback(this, trackManager, mWorkerHandler)
        mPlaybackManager = PlaybackManager(applicationContext, playback, trackManager, this)
        
        mMediaSession?.setCallback(mPlaybackManager?.sessionCallbacks, mWorkerHandler)
        mNotificationManager = MediaNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> mMediaSession?.controller?.transportControls?.play()
            ACTION_PAUSE -> mMediaSession?.controller?.transportControls?.pause()
            ACTION_STOP -> mMediaSession?.controller?.transportControls?.stop()
            ACTION_NEXT -> mMediaSession?.controller?.transportControls?.skipToNext()
            ACTION_PREV -> mMediaSession?.controller?.transportControls?.skipToPrevious()
        }

        if (!isServiceRunning && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createStartupChannel()
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Frequency")
                .setContentText("Music player active")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            startForeground(NOTIFICATION_ID, notification)
        }
        isServiceRunning = true
        return START_STICKY
    }

    private fun createStartupChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, "Frequency Service", NotificationManager.IMPORTANCE_LOW)
                nm.createNotificationChannel(channel)
            }
        }
    }

    override fun onPlaybackStart() {
        mMediaSession?.isActive = true
        mNotificationManager?.startNotification(mMediaSession!!, true)
        updateWidget(isPlaying = true)
    }

    override fun onPlaybackStopped() {
        mMediaSession?.isActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        updateWidget(isPlaying = false)
        stopSelf()
    }

    override fun onStartNotification() {
        mNotificationManager?.startNotification(mMediaSession!!, true)
        updateWidget(isPlaying = true)
    }

    override fun onStopNotification() {
        mNotificationManager?.stopNotification()
    }

    override fun onPlaybackStateChanged(newState: PlaybackState) {
        mMediaSession?.setPlaybackState(newState)
        val isPlaying = newState.state == PlaybackState.STATE_PLAYING
        mNotificationManager?.startNotification(mMediaSession!!, isPlaying)
        updateWidget(isPlaying)
    }

    private fun updateWidget(isPlaying: Boolean) {
        val description = mMediaSession?.controller?.metadata?.description
        FrequencyWidgetProvider.update(
            applicationContext,
            description?.title?.toString() ?: getString(R.string.app_name),
            description?.subtitle?.toString() ?: "",
            isPlaying
        )
    }

    override fun onMetaDataChanged(newMetaData: MediaMetadata) {
        mMediaSession?.setMetadata(newMetaData)
        mNotificationManager?.startNotification(mMediaSession!!, true)
        updateWidget(isPlaying = true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Stop playback when app is swiped away
        mPlaybackManager?.sessionCallbacks?.onStop()
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        mMediaSession?.run {
            isActive = false
            release()
        }
        mPlaybackManager?.sessionCallbacks?.onStop()
        mServiceThread?.quit()
        super.onDestroy()
    }
}
