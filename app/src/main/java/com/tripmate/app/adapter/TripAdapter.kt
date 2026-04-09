package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemTripBinding
import com.tripmate.app.model.Trip

class TripAdapter(
    trips: List<Trip>,
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private val items = trips.toMutableList()

    inner class TripViewHolder(private val binding: ItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip) {
            binding.tvTripTitle.text = trip.title
            binding.tvTripDestination.text = trip.destination
            binding.tvTripDates.text = "${trip.startDate} - ${trip.endDate}"
            // Budget is not part of the model; show placeholder
            binding.tvTripBudget?.text = "Budget: —"

            // Card click with slight scale animation
            binding.root.setOnClickListener { view ->
                view.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction { onTripClick(trip) }
                            .start()
                    }
                    .start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(items[position])
        // Staggered fade‑in + upward motion for each item
        holder.itemView.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((position * 50).toLong())
                .setDuration(300)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    fun updateData(newTrips: List<Trip>) {
        items.clear()
        items.addAll(newTrips)
        notifyDataSetChanged()
    }
}
