package com.ash.healthtracker.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.ash.healthtracker.data.AppDatabase
import com.ash.healthtracker.data.MedicationLog
import com.ash.healthtracker.notifications.AlarmScheduler
import com.ash.healthtracker.notifications.NotificationHelper
import com.ash.healthtracker.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduledTime = System.currentTimeMillis()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val logId = db.medicationDao().insert(
                    MedicationLog(scheduledTime = scheduledTime, medName = "Cirtel 40")
                )

                val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_MARK_TAKEN
                    putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
                }
                val takenPendingIntent = PendingIntent.getBroadcast(
                    context, logId.toInt(), takenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("open_tab", "medication")
                }
                val contentPendingIntent = PendingIntent.getActivity(
                    context, logId.toInt(), openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationHelper.baseBuilder(context, NotificationHelper.CHANNEL_MEDICATION)
                    .setContentTitle("Time for Cirtel 40")
                    .setContentText("Your 12-hourly dose is due now")
                    .setContentIntent(contentPendingIntent)
                    .addAction(0, "Mark as Taken", takenPendingIntent)
                    .setOngoing(true)
                    .build()

                NotificationManagerCompat.from(context)
                    .notify(NotificationHelper.NOTIF_ID_MEDICATION, notification)

                // schedule a "missed dose" check in 30 minutes, and the next dose in 12 hours
                AlarmScheduler.scheduleNextMedicationDose(context, scheduledTime)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
