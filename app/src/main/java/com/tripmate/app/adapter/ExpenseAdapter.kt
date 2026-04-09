package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemExpenseBinding
import com.tripmate.app.model.Expense

class ExpenseAdapter(
    expenses: List<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val items = expenses.toMutableList()

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvExpenseTitle.text = expense.title
            binding.tvPaidBy.text = "Paid by ${expense.paidBy}"
            binding.tvAmount.text = "$${expense.amount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newExpenses: List<Expense>) {
        items.clear()
        items.addAll(newExpenses)
        notifyDataSetChanged()
    }
}
