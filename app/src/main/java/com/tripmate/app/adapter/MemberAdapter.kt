package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemMemberBinding
import com.tripmate.app.model.Member

class MemberAdapter(
    members: List<Member>
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    private val items = members.toMutableList()

    inner class MemberViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: Member) {
            binding.tvMemberName.text = member.name
            binding.tvMemberRole.text = member.role
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newMembers: List<Member>) {
        items.clear()
        items.addAll(newMembers)
        notifyDataSetChanged()
    }
}
