package com.tripmate.app.ui.checklist

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.ChecklistAdapter
import com.tripmate.app.databinding.ActivityChecklistBinding
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.UiFeedback

class ChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistBinding
    private lateinit var adapter: ChecklistAdapter
    private var tripId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = AppState.resolveTripId(intent)

        binding.btnBack.setOnClickListener { finish() }
        binding.fabAddChecklist.setOnClickListener { showAddTaskDialog() }

        setupRecyclerView()
        refreshChecklist()
    }

    override fun onResume() {
        super.onResume()
        refreshChecklist()
    }

    private fun setupRecyclerView() {
        adapter = ChecklistAdapter(emptyList()) { item, isChecked ->
            MockDataProvider.setChecklistCompleted(tripId, item.id, isChecked)
            refreshChecklist()
        }

        binding.rvChecklist.layoutManager = LinearLayoutManager(this)
        binding.rvChecklist.adapter = adapter
    }

    private fun refreshChecklist() {
        adapter.updateData(MockDataProvider.getChecklistItems(tripId))
    }

    private fun showAddTaskDialog() {
        val input = EditText(this).apply { hint = "Task name" }

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isBlank()) {
                    UiFeedback.showError(binding.root, "Task cannot be empty")
                    return@setPositiveButton
                }
                MockDataProvider.addChecklistItem(tripId, title)
                refreshChecklist()
                UiFeedback.showInfo(binding.root, "Task added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
