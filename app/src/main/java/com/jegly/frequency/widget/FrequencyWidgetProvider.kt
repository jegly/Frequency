package com.jegly.frequency.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jegly.frequency.R
import com.jegly.frequency.TMS
import com.jegly.frequency.activities.MainActivity

class FrequencyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildRemoteViews(context, context.getString(R.string.app_name), "", isPlaying = false))
        }
    }

    companion object {
        /** Called by [TMS] whenever playback state or metadata changes, mirroring the notification. */
        fun update(context: Context, title: String, artist: String, isPlaying: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, FrequencyWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = buildRemoteViews(context, title, artist, isPlaying)
            for (id in ids) manager.updateAppWidget(id, views)
        }

        private fun buildRemoteViews(context: Context, title: String, artist: String, isPlaying: Boolean): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_frequency)
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_artist, artist)
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            fun serviceIntent(action: String) = PendingIntent.getService(
                context, action.hashCode(),
                Intent(context, TMS::class.java).setAction(action),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.widget_prev, serviceIntent(TMS.ACTION_PREV))
            views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                serviceIntent(if (isPlaying) TMS.ACTION_PAUSE else TMS.ACTION_PLAY)
            )
            views.setOnClickPendingIntent(R.id.widget_next, serviceIntent(TMS.ACTION_NEXT))

            val openAppIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_icon, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_title, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_artist, openAppIntent)

            return views
        }
    }
}
