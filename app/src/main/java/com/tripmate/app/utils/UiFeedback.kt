package com.tripmate.app.utils

import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object UiFeedback {
    fun showInfo(anchor: View, message: String) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showError(anchor: View, message: String) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showToast(anchor: View, message: String) {
        Toast.makeText(anchor.context, message, Toast.LENGTH_SHORT).show()
    }
}
