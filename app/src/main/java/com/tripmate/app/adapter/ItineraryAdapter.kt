package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemItineraryBinding
import com.tripmate.app.model.ItineraryDay
import androidx.recyclerview.widget.LinearLayoutManager

class ItineraryAdapter(
    days: List<ItineraryDay>
) : RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    private val items = days.toMutableList()

        inner class ItineraryViewHolder(private val binding: ItemItineraryBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(day: ItineraryDay) {
            binding.tvDayTitle.text = day.dayTitle
            binding.tvDayDate.text = day.date
            
            // Nested RecyclerView for events
            binding.rvEvents.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvEvents.adapter = EventAdapter(day.events)

            // Expand/collapse animation for the events list
            val isExpanded = binding.root.tag as? Boolean ?: false
            binding.rvEvents.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { view ->
                val currently = view.tag as? Boolean ?: false
                view.tag = !currently
                binding.rvEvents.apply {
                    if (currently) {
                        animate().alpha(0f).setDuration(200).withEndAction { visibility = View.GONE }.start()
                    } else {
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(200).start()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val binding = ItemItineraryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItineraryViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newDays: List<ItineraryDay>) {
        items.clear()
        items.addAll(newDays)
        notifyDataSetChanged()
    }
}
