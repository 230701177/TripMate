package com.tripmate.app.ui.notification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tripmate.app.adapter.NotificationAdapter
import com.tripmate.app.databinding.ActivityNotificationBinding
import com.tripmate.app.utils.MockDataProvider

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        refreshNotifications()
    }

    override fun onResume() {
        super.onResume()
        refreshNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList())

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun refreshNotifications() {
        adapter.updateData(MockDataProvider.getNotifications())
    }
}