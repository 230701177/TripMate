package com.tripmate.app.ui.event

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.EventAdapter
import com.tripmate.app.databinding.ActivityEventBinding
import com.tripmate.app.model.Event
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.UiFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding
    private lateinit var adapter: EventAdapter
    private var tripId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = AppState.resolveTripId(intent)

        binding.btnBack.setOnClickListener { finish() }
        binding.fabAddEvent.setOnClickListener { showAddEventDialog() }

        adapter = EventAdapter(emptyList(), ::isUpcoming)
        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = adapter

        refreshEvents()
    }

    override fun onResume() {
        super.onResume()
        refreshEvents()
    }

    private fun refreshEvents() {
        adapter.updateData(MockDataProvider.getEvents(tripId))
    }

    private fun showAddEventDialog() {
        val titleInput = EditText(this).apply { hint = "Title" }
        val dateInput = EditText(this).apply { hint = "Date (e.g. Apr 01, 2026)" }
        val timeInput = EditText(this).apply { hint = "Time (e.g. 09:30 AM)" }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(titleInput)
            addView(dateInput)
            addView(timeInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Event")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val time = timeInput.text.toString().trim()

                if (title.isBlank() || date.isBlank() || time.isBlank()) {
                    UiFeedback.showError(binding.root, "All fields are required")
                    return@setPositiveButton
                }

                MockDataProvider.addEvent(tripId, title, date, time)
                refreshEvents()
                UiFeedback.showInfo(binding.root, "Event added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isUpcoming(event: Event): Boolean {
        val value = "${event.location} ${event.time}".trim()
        val patterns = listOf("MMM dd, yyyy hh:mm a", "dd/MM/yyyy hh:mm a")
        val now = Date()

        for (pattern in patterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                val parsed = format.parse(value)
                if (parsed != null) {
                    return parsed.after(now)
                }
            } catch (_: Exception) {
            }
        }
        return false
    }
}
