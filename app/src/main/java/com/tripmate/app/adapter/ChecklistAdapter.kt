package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemChecklistBinding
import com.tripmate.app.model.ChecklistItem

class ChecklistAdapter(
    items: List<ChecklistItem>,
    private val onItemChecked: (ChecklistItem, Boolean) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {

    private val data = items.toMutableList()

    inner class ChecklistViewHolder(private val binding: ItemChecklistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChecklistItem) {
            binding.cbChecklist.setOnCheckedChangeListener(null)
            binding.cbChecklist.text = item.title
            binding.cbChecklist.isChecked = item.isCompleted
            
            binding.cbChecklist.setOnCheckedChangeListener { _, isChecked ->
                onItemChecked(item, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val binding = ItemChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChecklistViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun updateData(newItems: List<ChecklistItem>) {
        data.clear()
        data.addAll(newItems)
        notifyDataSetChanged()
    }
}
