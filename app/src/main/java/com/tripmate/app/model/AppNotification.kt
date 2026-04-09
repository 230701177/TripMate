package com.tripmate.app.model

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean = false
)
