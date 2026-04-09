package com.tripmate.app.ui.expense

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.ExpenseAdapter
import com.tripmate.app.databinding.ActivityExpenseBinding
import com.tripmate.app.utils.AppState
import com.tripmate.app.utils.MockDataProvider
import com.tripmate.app.utils.UiFeedback
import java.util.Locale

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding
    private lateinit var adapter: ExpenseAdapter
    private var tripId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = AppState.resolveTripId(intent)

        binding.btnBack.setOnClickListener { finish() }
        binding.fabAddExpense.setOnClickListener { showAddExpenseDialog() }

        setupRecyclerView()
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(emptyList())

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun refreshData() {
        val expenses = MockDataProvider.getExpenses(tripId)
        adapter.updateData(expenses)
        val total = MockDataProvider.totalExpenses(tripId)
        binding.tvTotalExpense.text = "$${String.format(Locale.US, "%.2f", total)}"
    }

    private fun showAddExpenseDialog() {
        val titleInput = EditText(this).apply { hint = "Title" }
        val amountInput = EditText(this).apply { hint = "Amount" }
        val paidByInput = EditText(this).apply { hint = "Paid by" }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(titleInput)
            addView(amountInput)
            addView(paidByInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val amount = amountInput.text.toString().trim().toDoubleOrNull()
                val paidBy = paidByInput.text.toString().trim()

                if (title.isBlank() || amount == null || paidBy.isBlank()) {
                    UiFeedback.showError(binding.root, "Enter valid title, amount and paid by")
                    return@setPositiveButton
                }

                MockDataProvider.addExpense(tripId, title, amount, paidBy)
                refreshData()
                UiFeedback.showInfo(binding.root, "Expense added")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
