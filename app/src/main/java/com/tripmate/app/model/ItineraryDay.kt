package com.tripmate.app.model

data class ItineraryDay(
    val id: String,
    val dayTitle: String,
    val date: String,
    val events: List<Event> = emptyList()
)
