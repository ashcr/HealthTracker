package com.ash.healthtracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ash.healthtracker.data.AppDatabase
import com.ash.healthtracker.databinding.ActivityMigraineHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class MigraineHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMigraineHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMigraineHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(this)
        val adapter = MigraineHistoryAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = adapter

        db.migraineDao().getAll().observe(this) { episodes ->
            adapter.submitList(episodes)

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            val monthStart = cal.timeInMillis
            val thisMonthCount = episodes.count { it.startTime >= monthStart }

            binding.summaryText.text =
                "${episodes.size} total episodes logged • $thisMonthCount this month"
        }
    }
}
