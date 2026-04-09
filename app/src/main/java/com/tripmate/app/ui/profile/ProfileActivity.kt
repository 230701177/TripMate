package com.tripmate.app.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.tripmate.app.databinding.ActivityProfileBinding
import com.tripmate.app.ui.auth.LoginActivity
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.UiFeedback

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindProfile()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            // Mock clear session and route to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun bindProfile() {
        val user = MockDataProvider.getCurrentUser()
        binding.tvProfileName.text = user.name
        binding.tvProfileEmail.text = user.email
    }

    private fun showEditProfileDialog() {
        val user = MockDataProvider.getCurrentUser()

        val nameInput = EditText(this).apply {
            hint = "Name"
            setText(user.name)
        }
        val emailInput = EditText(this).apply {
            hint = "Email"
            setText(user.email)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(nameInput)
            addView(emailInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                if (name.isBlank() || email.isBlank()) {
                    UiFeedback.showError(binding.root, "Name and email are required")
                    return@setPositiveButton
                }
                MockDataProvider.updateCurrentUser(name, email)
                bindProfile()
                UiFeedback.showInfo(binding.root, "Profile updated")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
