package com.tripmate.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.tripmate.app.databinding.ActivitySignupBinding
import com.tripmate.app.ui.home.HomeActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate container on load
        animateContainerEntry()

        // Setup listeners
        setupClickListeners()
        setupInputFocus()
    }

    private fun animateContainerEntry() {
        binding.signupContainer.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            attemptSignup()
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun setupInputFocus() {
        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilName.apply {
                    animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(200)
                        .start()
                }
            } else {
                binding.tilName.apply {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilEmail.apply {
                    animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(200)
                        .start()
                }
            } else {
                binding.tilEmail.apply {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilPassword.apply {
                    animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(200)
                        .start()
                }
            } else {
                binding.tilPassword.apply {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }
    }

    private fun attemptSignup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Clear previous errors
        binding.tvError.visibility = View.GONE
        binding.tvError.text = ""

        // Validate inputs
        when {
            name.isEmpty() -> {
                showError("Full name is required")
                binding.tilName.requestFocus()
            }
            name.length < 2 -> {
                showError("Name must be at least 2 characters")
                binding.tilName.requestFocus()
            }
            email.isEmpty() -> {
                showError("Email address is required")
                binding.tilEmail.requestFocus()
            }
            !isValidEmail(email) -> {
                showError("Please enter a valid email address")
                binding.tilEmail.requestFocus()
            }
            password.isEmpty() -> {
                showError("Password is required")
                binding.tilPassword.requestFocus()
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                binding.tilPassword.requestFocus()
            }
            else -> {
                // Animation on button press
                binding.btnSignup.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        binding.btnSignup.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                        performSignup()
                    }
                    .start()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showError(message: String) {
        binding.tvError.apply {
            text = message
            visibility = View.VISIBLE
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        // Auto-hide after 4 seconds
        binding.tvError.postDelayed({
            if (binding.tvError.visibility == View.VISIBLE) {
                binding.tvError.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        binding.tvError.visibility = View.GONE
                    }
                    .start()
            }
        }, 4000)
    }

    private fun performSignup() {
        navigateToHome()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        finish()
    }
}
