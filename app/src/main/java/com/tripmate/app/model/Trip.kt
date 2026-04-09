package com.tripmate.app.model

import java.io.Serializable

data class Trip(
    val id: String,
    val title: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val imageUrl: String = "",
    val memberCount: Int = 1
) : Serializable
