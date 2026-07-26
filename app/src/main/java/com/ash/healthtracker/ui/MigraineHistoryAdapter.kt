package com.ash.healthtracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ash.healthtracker.data.MigraineEpisode
import com.ash.healthtracker.databinding.ItemMigraineEpisodeBinding
import java.text.SimpleDateFormat
import java.util.*

class MigraineHistoryAdapter : RecyclerView.Adapter<MigraineHistoryAdapter.ViewHolder>() {

    private var items: List<MigraineEpisode> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

    fun submitList(newItems: List<MigraineEpisode>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMigraineEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemMigraineEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: MigraineEpisode) {
            binding.dateText.text = dateFormat.format(Date(episode.startTime))
            val duration = episode.durationMinutes
            binding.durationText.text = if (duration != null) {
                val hours = duration / 60
                val mins = duration % 60
                "Duration: ${hours}h ${mins}m • Severity ${episode.severity}/10"
            } else {
                "Ongoing • Severity ${episode.severity}/10"
            }
            binding.triggersText.text = if (episode.triggers.isNotBlank())
                "Triggers: ${episode.triggers.replace(",", ", ")}" else "Triggers: —"
            binding.reliefText.text = if (episode.reliefMethods.isNotBlank())
                "Helped: ${episode.reliefMethods.replace(",", ", ")}" else "Helped: —"
        }
    }
}
