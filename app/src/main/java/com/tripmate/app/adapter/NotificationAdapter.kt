package com.tripmate.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmate.app.databinding.ItemNotificationBinding
import com.tripmate.app.model.AppNotification

class NotificationAdapter(
    notifications: List<AppNotification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val items = notifications.toMutableList()

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notif: AppNotification) {
            binding.tvNotifTitle.text = notif.title
            binding.tvNotifMessage.text = notif.message
            binding.tvNotifTime.text = notif.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newNotifications: List<AppNotification>) {
        items.clear()
        items.addAll(newNotifications)
        notifyDataSetChanged()
    }
}