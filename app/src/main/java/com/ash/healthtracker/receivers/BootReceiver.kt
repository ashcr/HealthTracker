package com.ash.healthtracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ash.healthtracker.notifications.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Resume water reminders
            AlarmScheduler.scheduleWaterReminder(context)
            // Resume medication reminders starting from next expected slot
            val prefs = AlarmScheduler.getPrefs(context)
            val intervalHours = prefs.getInt("med_interval_hours", 12)
            val anchor = prefs.getLong("med_anchor_time", 0L)
            if (anchor > 0) {
                var next = anchor
                val now = System.currentTimeMillis()
                val intervalMillis = intervalHours * 60L * 60L * 1000L
                while (next < now) next += intervalMillis
                AlarmScheduler.scheduleMedicationReminder(context, next, intervalHours)
            }
        }
    }
}
