package com.ash.healthtracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ash.healthtracker.R

object NotificationHelper {
    const val CHANNEL_WATER = "water_reminders"
    const val CHANNEL_MEDICATION = "medication_reminders"

    const val NOTIF_ID_WATER = 1001
    const val NOTIF_ID_MEDICATION = 1002
    const val NOTIF_ID_MED_MISSED = 1003

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val waterChannel = NotificationChannel(
            CHANNEL_WATER, "Water Reminders", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders to drink water" }

        val medChannel = NotificationChannel(
            CHANNEL_MEDICATION, "Medication Reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Cirtel 40 dose reminders" }

        manager.createNotificationChannel(waterChannel)
        manager.createNotificationChannel(medChannel)
    }

    fun baseBuilder(context: Context, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
    }
}
