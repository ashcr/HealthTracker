package com.ash.healthtracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ash.healthtracker.data.AppDatabase
import com.ash.healthtracker.data.MigraineEpisode
import com.ash.healthtracker.databinding.ActivityMigraineLogBinding
import kotlinx.coroutines.launch

class MigraineLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMigraineLogBinding
    private lateinit var db: AppDatabase
    private var ongoingEpisode: MigraineEpisode? = null

    private val triggerOptions = listOf(
        "Stress", "Lack of sleep", "Skipped meal", "Bright light",
        "Screen time", "Dehydration", "Weather change", "Loud noise", "Menstrual"
    )
    private val reliefOptions = listOf(
        "Rest/Dark room", "Sleep", "Painkiller", "Caffeine",
        "Hydration", "Cold compress", "Massage"
    )
    private val selectedTriggers = mutableSetOf<String>()
    private val selectedRelief = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMigraineLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)

        buildChips(binding.triggerChipGroup, triggerOptions, selectedTriggers)
        buildChips(binding.reliefChipGroup, reliefOptions, selectedRelief)

        binding.severitySeekBar.max = 9
        binding.severitySeekBar.progress = 4
        binding.severityValueText.text = "5"
        binding.severitySeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.severityValueText.text = (progress + 1).toString()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        lifecycleScope.launch {
            ongoingEpisode = db.migraineDao().getOngoing()
            updateUiForState()
        }

        binding.btnStartEpisode.setOnClickListener { startEpisode() }
        binding.btnEndEpisode.setOnClickListener { endEpisode() }
    }

    private fun updateUiForState() {
        val ongoing = ongoingEpisode
        if (ongoing != null) {
            binding.statusText.text = "Episode in progress since ${
                java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.US).format(java.util.Date(ongoing.startTime))
            }"
            binding.btnStartEpisode.isEnabled = false
            binding.btnEndEpisode.isEnabled = true
        } else {
            binding.statusText.text = "No active episode"
            binding.btnStartEpisode.isEnabled = true
            binding.btnEndEpisode.isEnabled = false
        }
    }

    private fun buildChips(group: com.google.android.material.chip.ChipGroup, options: List<String>, selectedSet: MutableSet<String>) {
        group.removeAllViews()
        for (option in options) {
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = option
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedSet.add(option) else selectedSet.remove(option)
                }
            }
            group.addView(chip)
        }
    }

    private fun startEpisode() {
        lifecycleScope.launch {
            val episode = MigraineEpisode(startTime = System.currentTimeMillis())
            val id = db.migraineDao().insert(episode)
            ongoingEpisode = episode.copy(id = id)
            updateUiForState()
        }
    }

    private fun endEpisode() {
        val ongoing = ongoingEpisode ?: return
        val severity = binding.severitySeekBar.progress + 1
        val notes = binding.notesInput.text.toString()

        lifecycleScope.launch {
            val updated = ongoing.copy(
                endTime = System.currentTimeMillis(),
                triggers = selectedTriggers.joinToString(","),
                reliefMethods = selectedRelief.joinToString(","),
                severity = severity,
                notes = notes
            )
            db.migraineDao().update(updated)
            ongoingEpisode = null
            finish()
        }
    }
}
