package com.ash.healthtracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ash.healthtracker.receivers.MedicationReminderReceiver
import com.ash.healthtracker.receivers.WaterReminderReceiver
import java.util.Calendar

object AlarmScheduler {

    private const val WATER_REQUEST_CODE = 5001
    private const val MED_REQUEST_CODE = 5002

    private const val PREFS = "health_tracker_prefs"
    private const val KEY_WATER_INTERVAL_MIN = "water_interval_min"
    private const val KEY_MED_ANCHOR_TIME = "med_anchor_time" // epoch millis of first/most recent dose time-of-day anchor
    private const val KEY_MED_INTERVAL_HOURS = "med_interval_hours"

    fun getPrefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---------- WATER REMINDERS ----------
    // Repeats every N minutes during waking hours (default every 2 hours, 8am-10pm)
    fun scheduleWaterReminder(context: Context, intervalMinutes: Int = 120) {
        getPrefs(context).edit().putInt(KEY_WATER_INTERVAL_MIN, intervalMinutes).apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, WATER_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = nextWakingSlot(intervalMinutes)
        setExactOrInexact(alarmManager, triggerAt, pendingIntent)
    }

    fun rescheduleNextWaterReminder(context: Context) {
        val interval = getPrefs(context).getInt(KEY_WATER_INTERVAL_MIN, 120)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, WATER_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val next = Calendar.getInstance().apply { add(Calendar.MINUTE, interval) }
        var triggerAt = next.timeInMillis
        // Skip quiet hours (10pm - 8am)
        val hour = next.get(Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 8) {
            triggerAt = nextWakingSlot(interval)
        }
        setExactOrInexact(alarmManager, triggerAt, pendingIntent)
    }

    private fun nextWakingSlot(intervalMinutes: Int): Long {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        if (hour in 8..21) {
            return Calendar.getInstance().apply { add(Calendar.MINUTE, intervalMinutes) }.timeInMillis
        }
        // jump to 8am next available day
        val next8am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (hour >= 22) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next8am.timeInMillis
    }

    fun cancelWaterReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, WATER_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // ---------- MEDICATION REMINDERS (every 12 hours) ----------
    fun scheduleMedicationReminder(context: Context, firstDoseTimeMillis: Long, intervalHours: Int = 12) {
        getPrefs(context).edit()
            .putLong(KEY_MED_ANCHOR_TIME, firstDoseTimeMillis)
            .putInt(KEY_MED_INTERVAL_HOURS, intervalHours)
            .apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, MED_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrInexact(alarmManager, firstDoseTimeMillis, pendingIntent)
    }

    fun scheduleNextMedicationDose(context: Context, fromMillis: Long) {
        val intervalHours = getPrefs(context).getInt(KEY_MED_INTERVAL_HOURS, 12)
        val nextTime = fromMillis + intervalHours * 60L * 60L * 1000L

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, MED_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrInexact(alarmManager, nextTime, pendingIntent)
    }

    fun cancelMedicationReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, MED_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun setExactOrInexact(alarmManager: AlarmManager, triggerAt: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }
}
