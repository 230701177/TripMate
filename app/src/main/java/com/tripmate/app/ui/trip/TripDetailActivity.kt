package com.tripmate.app.ui.trip

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.ItineraryAdapter
import com.tripmate.app.databinding.ActivityTripDetailBinding
import com.tripmate.app.model.Trip
import com.tripmate.app.ui.collaboration.CollaborationActivity
import com.tripmate.app.ui.expense.ExpenseActivity
import com.tripmate.app.ui.checklist.ChecklistActivity
import com.tripmate.app.ui.event.EventActivity
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.NavKeys
import com.tripmate.app.utils.UiFeedback

class TripDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailBinding
    private lateinit var adapter: ItineraryAdapter
    private var trip: Trip? = null
    private var tripId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

            // Header fade‑in animation (alpha 0→1, slight upward motion)
            binding.headerLayout.apply {
                alpha = 0f
                translationY = -30f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        tripId = AppState.resolveTripId(intent)
        AppState.selectedTripId = tripId
        if (tripId.isBlank()) {
            @Suppress("DEPRECATION")
            trip = intent.getSerializableExtra(NavKeys.EXTRA_TRIP) as? Trip
            tripId = trip?.id.orEmpty()
        }

        if (trip == null && tripId.isNotBlank()) {
            trip = MockDataProvider.getTripById(tripId)
        }

        trip?.let {
            binding.tvTripTitleDetail.text = it.title
            binding.tvTripDatesDetail.text = "${it.startDate} - ${it.endDate}"
                // Budget placeholder (Trip model currently has no budget field)
                binding.tvBudget.text = "Budget: —"
        }

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        refreshItinerary()
    }

    private fun setupRecyclerView() {
        adapter = ItineraryAdapter(MockDataProvider.getItinerary(tripId))

        binding.rvItinerary.layoutManager = LinearLayoutManager(this)
        binding.rvItinerary.adapter = adapter
    }

    private fun refreshItinerary() {
        if (::adapter.isInitialized) {
            adapter.updateData(MockDataProvider.getItinerary(tripId))
        }
    }

    private fun setupButtons() {
        binding.btnEvent.setOnClickListener {
            startActivity(Intent(this, EventActivity::class.java).apply {
                putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
            })
        }

        binding.tvItineraryHeader.setOnClickListener {
            showAddActivityDialog()
        }

        binding.btnCollaboration.setOnClickListener {
            startActivity(Intent(this, CollaborationActivity::class.java).apply {
                putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
            })
        }

        binding.btnExpense.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java).apply {
                putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
            })
        }

        binding.btnChecklist.setOnClickListener {
            startActivity(Intent(this, ChecklistActivity::class.java).apply {
                putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
            })
        }

            // Apply scale animation to action buttons on click
            val buttonClickAnim: (android.view.View) -> Unit = { view ->
                view.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(100).start() }
                    .start()
            }
            binding.btnChecklist.setOnClickListener { view ->
                buttonClickAnim(view)
                startActivity(Intent(this, ChecklistActivity::class.java).apply {
                    putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
                })
            }
            binding.btnExpense.setOnClickListener { view ->
                buttonClickAnim(view)
                startActivity(Intent(this, ExpenseActivity::class.java).apply {
                    putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
                })
            }
            binding.btnEvent.setOnClickListener { view ->
                buttonClickAnim(view)
                startActivity(Intent(this, EventActivity::class.java).apply {
                    putExtra(NavKeys.EXTRA_TRIP_ID, tripId)
                })
            }
    }

    private fun showAddActivityDialog() {
        val titleInput = EditText(this).apply { hint = "Activity title" }
        val dateInput = EditText(this).apply { hint = "Date" }
        val timeInput = EditText(this).apply { hint = "Time" }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(titleInput)
            addView(dateInput)
            addView(timeInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Activity")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val time = timeInput.text.toString().trim()
                if (title.isBlank() || date.isBlank() || time.isBlank()) {
                    UiFeedback.showError(binding.root, "Fill all fields")
                    return@setPositiveButton
                }
                MockDataProvider.addEvent(tripId, title, date, time)
                refreshItinerary()
                UiFeedback.showInfo(binding.root, "Activity added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

            // FAB press animation
            binding.fabAddActivity.setOnClickListener {
                binding.fabAddActivity.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        binding.fabAddActivity.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                            showAddActivityDialog()
                        }.start()
                    }
                    .start()
            }
}
