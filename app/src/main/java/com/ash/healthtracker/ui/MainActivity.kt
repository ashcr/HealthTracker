package com.ash.healthtracker.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ash.healthtracker.R
import com.ash.healthtracker.data.AppDatabase
import com.ash.healthtracker.databinding.ActivityMainBinding
import com.ash.healthtracker.notifications.AlarmScheduler
import com.ash.healthtracker.notifications.NotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val notifPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* granted or not, continue */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        NotificationHelper.createChannels(this)
        requestNotificationPermissionIfNeeded()
        requestExactAlarmPermissionIfNeeded()

        setupMigraineSection()
        setupWaterSection()
        setupMedicationSection()

        ensureRemindersScheduled()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun ensureRemindersScheduled() {
        val prefs = AlarmScheduler.getPrefs(this)
        if (!prefs.contains("water_interval_min")) {
            AlarmScheduler.scheduleWaterReminder(this, intervalMinutes = 120)
        }
        // Medication reminder is scheduled explicitly by the user setting their first dose time
    }

    // ---------------- MIGRAINE ----------------
    private fun setupMigraineSection() {
        binding.btnLogMigraine.setOnClickListener {
            startActivity(Intent(this, MigraineLogActivity::class.java))
        }
        binding.btnMigraineHistory.setOnClickListener {
            startActivity(Intent(this, MigraineHistoryActivity::class.java))
        }
    }

    // ---------------- WATER ----------------
    private fun setupWaterSection() {
        val today = dayFormat.format(Date())
        db.waterDao().getTotalForDay(today).observe(this) { total ->
            val goalMl = 2000
            binding.waterProgressText.text = "${total ?: 0} / $goalMl ml"
            binding.waterProgressBar.max = goalMl
            binding.waterProgressBar.progress = (total ?: 0).coerceAtMost(goalMl)
        }

        binding.btnAdd250.setOnClickListener { addWater(250) }
        binding.btnAdd500.setOnClickListener { addWater(500) }
        binding.btnAdd1000.setOnClickListener { addWater(1000) }
    }

    private fun addWater(amountMl: Int) {
        val today = dayFormat.format(Date())
        lifecycleScope.launch {
            db.waterDao().insert(
                com.ash.healthtracker.data.WaterLog(
                    timestamp = System.currentTimeMillis(),
                    amountMl = amountMl,
                    dayKey = today
                )
            )
        }
    }

    // ---------------- MEDICATION ----------------
    private fun setupMedicationSection() {
        db.medicationDao().getRecent().observe(this) { logs ->
            val last = logs.firstOrNull()
            binding.medStatusText.text = if (last?.takenTime != null) {
                "Last dose taken: ${formatTime(last.takenTime!!)}"
            } else if (last != null) {
                "Dose due since ${formatTime(last.scheduledTime)} — not yet taken"
            } else {
                "No doses scheduled yet"
            }
        }

        binding.btnSetupMedication.setOnClickListener {
            // Schedule first dose starting now + 12h cycle going forward;
            // in practice user taps this right after taking a dose to set the anchor
            val now = System.currentTimeMillis()
            AlarmScheduler.scheduleMedicationReminder(this, now + 12 * 60 * 60 * 1000L, 12)
            binding.medStatusText.text = "Reminders set: every 12 hours starting now"
        }

        binding.btnMarkTakenNow.setOnClickListener {
            lifecycleScope.launch {
                val log = com.ash.healthtracker.data.MedicationLog(
                    scheduledTime = System.currentTimeMillis(),
                    takenTime = System.currentTimeMillis(),
                    medName = "Cirtel 40"
                )
                db.medicationDao().insert(log)
                AlarmScheduler.scheduleMedicationReminder(
                    this@MainActivity,
                    System.currentTimeMillis() + 12 * 60 * 60 * 1000L,
                    12
                )
            }
        }
    }

    private fun formatTime(millis: Long): String =
        SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(millis))
}
