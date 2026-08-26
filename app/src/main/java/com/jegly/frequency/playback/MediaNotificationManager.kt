package com.jegly.frequency.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jegly.frequency.R
import com.jegly.frequency.TMS
import com.jegly.frequency.activities.MainActivity

class MediaNotificationManager(private val service: TMS) {

    private val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "tunes_playback_channel"
        private const val NOTIFICATION_ID = 412
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Playback",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Media Playback Controls"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun startNotification(session: MediaSession, isPlaying: Boolean) {
        service.startForeground(NOTIFICATION_ID, createNotification(session, isPlaying))
    }

    fun stopNotification() {
        // Since minSdk is 24, we can directly use the modern stopForeground
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(session: MediaSession, isPlaying: Boolean): Notification {
        val controller = session.controller
        val metadata = controller.metadata
        val description = metadata?.description

        val intent = Intent(service, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            service, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(android.support.v4.media.session.MediaSessionCompat.Token.fromToken(session.sessionToken))
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(description?.title ?: "Frequency")
            .setContentText(description?.subtitle ?: "Playing music")
            .setLargeIcon(description?.iconBitmap)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)

        // Previous
        builder.addAction(
            android.R.drawable.ic_media_previous, "Previous",
            PendingIntent.getService(service, 1, Intent(service, TMS::class.java).setAction(TMS.ACTION_PREV), PendingIntent.FLAG_IMMUTABLE)
        )

        // Play/Pause
        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseAction = if (isPlaying) TMS.ACTION_PAUSE else TMS.ACTION_PLAY
        builder.addAction(
            playPauseIcon, if (isPlaying) "Pause" else "Play",
            PendingIntent.getService(service, 2, Intent(service, TMS::class.java).setAction(playPauseAction), PendingIntent.FLAG_IMMUTABLE)
        )

        // Next
        builder.addAction(
            android.R.drawable.ic_media_next, "Next",
            PendingIntent.getService(service, 3, Intent(service, TMS::class.java).setAction(TMS.ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        )

        return builder.build()
    }
}
