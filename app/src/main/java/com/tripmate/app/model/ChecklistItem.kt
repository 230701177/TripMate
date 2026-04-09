package com.tripmate.app.model

data class ChecklistItem(
    val id: String,
    val title: String,
    var isCompleted: Boolean = false
)
