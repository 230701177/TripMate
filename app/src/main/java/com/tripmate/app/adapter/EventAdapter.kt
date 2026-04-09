package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.R
import com.tripmate.app.databinding.ItemEventBinding
import com.tripmate.app.model.Event

class EventAdapter(
    events: List<Event>,
    private val highlightPredicate: ((Event) -> Boolean)? = null
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val items = events.toMutableList()

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEventTime.text = event.time
            binding.tvEventTitle.text = event.title
            binding.tvEventLocation.text = event.location

            if (highlightPredicate?.invoke(event) == true) {
                binding.tvEventTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorAccent))
            } else {
                binding.tvEventTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorText))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newEvents: List<Event>) {
        items.clear()
        items.addAll(newEvents)
        notifyDataSetChanged()
    }
}
