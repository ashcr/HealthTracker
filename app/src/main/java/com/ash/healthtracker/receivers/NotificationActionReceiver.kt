package com.ash.healthtracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.ash.healthtracker.data.AppDatabase
import com.ash.healthtracker.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_MARK_TAKEN = "com.ash.healthtracker.ACTION_MARK_TAKEN"
        const val EXTRA_LOG_ID = "extra_log_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_TAKEN) return
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1)
        if (logId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val log = db.medicationDao().getById(logId)
                if (log != null) {
                    db.medicationDao().update(log.copy(takenTime = System.currentTimeMillis()))
                }
                NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIF_ID_MEDICATION)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
