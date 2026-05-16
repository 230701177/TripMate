package com.tripmate.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Trip(
    val id: String,
    val title: String = "",
    val destination: String,
    @SerialName("start_date") val date: String,
    val budget: Double,
    @SerialName("user_id") val userId: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("invite_code") val inviteCode: String = ""
)

@Serializable
data class Expense(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val title: String,
    val amount: Double,
    @SerialName("created_at") val timestamp: String = "" // DB uses timestamp without time zone
)

@Serializable
data class Task(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("title") val name: String,
    val completed: Boolean = false
)

@Serializable
data class Event(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val title: String,
    @SerialName("event_date") val date: String,
    @SerialName("event_time") val time: String
)

@Serializable
data class Member(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val name: String,
    val email: String = "",
    val role: String = "Member"
)

@Serializable
data class Notification(
    val id: String = "",
    @SerialName("trip_id") val tripId: String = "",
    val message: String,
    val type: String,
    @SerialName("created_at") val timestamp: String = ""
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("avatar_url") val profileImage: String? = null,
    // These need to be added to the user_profiles table
    val status: String = "Traveler",
    @SerialName("trips_count") val tripsCount: Int = 0,
    @SerialName("countries_count") val countriesCount: Int = 0,
    @SerialName("budget_spent") val budgetSpent: String = "₹0"
)

@Serializable
data class TravelMemory(
    val id: String,
    @SerialName("user_id") val userId: String = "",
    @SerialName("trip_id") val tripId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("created_at") val date: String = ""
)
