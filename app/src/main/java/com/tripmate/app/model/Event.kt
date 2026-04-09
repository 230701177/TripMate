package com.tripmate.app.model

import java.io.Serializable

data class Event(
    val id: String,
    val title: String,
    val time: String,
    val location: String,
    val description: String = ""
) : Serializable
