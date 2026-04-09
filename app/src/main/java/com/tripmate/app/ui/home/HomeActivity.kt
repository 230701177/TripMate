package com.tripmate.app.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.TripAdapter
import com.tripmate.app.databinding.ActivityHomeBinding
import com.tripmate.app.model.Trip
import com.tripmate.app.ui.collaboration.CollaborationActivity
import com.tripmate.app.ui.profile.ProfileActivity
import com.tripmate.app.ui.notification.NotificationActivity
import com.tripmate.app.ui.trip.TripDetailActivity
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.NavKeys
import com.tripmate.app.utils.UiFeedback

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate the RecyclerView on entry (fade‑in + upward motion)
        binding.rvTrips.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshTrips()
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(MockDataProvider.getTrips(), ::openTrip)

        binding.rvTrips.layoutManager = LinearLayoutManager(this)
        binding.rvTrips.adapter = adapter
    }

    private fun openTrip(selectedTrip: Trip) {
        AppState.selectedTripId = selectedTrip.id
        val intent = Intent(this, TripDetailActivity::class.java).apply {
            putExtra(NavKeys.EXTRA_TRIP_ID, selectedTrip.id)
            putExtra(NavKeys.EXTRA_TRIP_NAME, selectedTrip.title)
        }
        startActivity(intent)
    }

    private fun refreshTrips() {
        if (::adapter.isInitialized) {
            adapter.updateData(MockDataProvider.getTrips())
        }
    }

    private fun setupListeners() {
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.ivProfile.setOnLongClickListener {
            startActivity(Intent(this, CollaborationActivity::class.java))
            true
        }

        binding.fabNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // FAB press animation (scale down briefly)
        binding.fabAddTrip.setOnClickListener {
            binding.fabAddTrip.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.fabAddTrip.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction { showAddTripDialog() }
                        .start()
                }
                .start()
        }
    }

    private fun showAddTripDialog() {
        val destinationInput = EditText(this).apply { hint = "Destination" }
        val dateInput = EditText(this).apply { hint = "Date" }
        val budgetInput = EditText(this).apply { hint = "Budget" }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(destinationInput)
            addView(dateInput)
            addView(budgetInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Trip")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val destination = destinationInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val budget = budgetInput.text.toString().trim()

                if (destination.isBlank() || date.isBlank()) {
                    UiFeedback.showError(binding.root, "Destination and date are required")
                    return@setPositiveButton
                }

                MockDataProvider.addTrip(destination, date, budget)
                refreshTrips()
                UiFeedback.showInfo(binding.root, "Trip added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
