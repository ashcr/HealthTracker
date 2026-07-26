package com.ash.healthtracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.ash.healthtracker.notifications.AlarmScheduler
import com.ash.healthtracker.notifications.NotificationHelper
import com.ash.healthtracker.ui.MainActivity
import android.app.PendingIntent

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_tab", "water")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationHelper.baseBuilder(context, NotificationHelper.CHANNEL_WATER)
            .setContentTitle("Time to hydrate 💧")
            .setContentText("Log some water to hit your 2L goal today")
            .setContentIntent(contentPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationHelper.NOTIF_ID_WATER, notification)

        // schedule the next one
        AlarmScheduler.rescheduleNextWaterReminder(context)
    }
}
