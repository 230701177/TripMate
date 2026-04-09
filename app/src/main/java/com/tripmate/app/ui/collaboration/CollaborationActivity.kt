package com.tripmate.app.ui.collaboration

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.MemberAdapter
import com.tripmate.app.databinding.ActivityCollaborationBinding
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.UiFeedback

class CollaborationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollaborationBinding
    private lateinit var adapter: MemberAdapter
    private var tripId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollaborationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = AppState.resolveTripId(intent)

        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnShare.setOnClickListener {
            showAddMemberDialog()
        }

        setupRecyclerView()
        binding.btnShare.text = "Add Member"
        updateInviteCode()
    }

    override fun onResume() {
        super.onResume()
        refreshMembers()
    }

    private fun setupRecyclerView() {
        adapter = MemberAdapter(emptyList())

        binding.rvMembers.layoutManager = LinearLayoutManager(this)
        binding.rvMembers.adapter = adapter
    }

    private fun refreshMembers() {
        adapter.updateData(MockDataProvider.getMembers(tripId))
    }

    private fun updateInviteCode() {
        val code = MockDataProvider.generateInviteCode(tripId.ifBlank { "00" })
        binding.tvInviteCodeValue.text = code
    }

    private fun showAddMemberDialog() {
        val input = EditText(this).apply { hint = "Member name" }

        AlertDialog.Builder(this)
            .setTitle("Add Member")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) {
                    UiFeedback.showError(binding.root, "Name is required")
                    return@setPositiveButton
                }
                MockDataProvider.addMember(tripId, name)
                refreshMembers()
                UiFeedback.showInfo(binding.root, "Member added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
