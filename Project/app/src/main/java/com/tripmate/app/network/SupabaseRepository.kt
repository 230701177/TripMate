package com.tripmate.app.network

import com.tripmate.app.models.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object SupabaseRepository {
    private const val TAG = "SupabaseRepository"

    // --- Authentication (Real Supabase Auth) ---
    suspend fun validateLogin(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        // --- MOCK OVERRIDE FOR DEVELOPMENT ---
        if (email == "admin@gmail.com" && password == "123456") {
            Log.d(TAG, "Using mock admin login")
            return@withContext Result.success("admin-user-id")
        }

        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Login failed: User not found")
            Log.d(TAG, "Login successful for $email, ID: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error during login", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(email: String, password: String, name: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Signup successful but session not created")
            
            // Create user profile in the public table
            val newProfile = UserProfile(
                id = userId,
                name = name,
                status = "New Traveler",
                email = email,
                tripsCount = 0,
                countriesCount = 0,
                budgetSpent = "₹0",
                profileImage = null
            )
            
            insertUserProfile(newProfile)
            Log.d(TAG, "User registered and profile created: $email")
            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error during registration", e)
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // --- Trips ---
    suspend fun getTrips(): List<Trip> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext emptyList()
            supabase.from("trips").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<Trip>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trips", e)
            emptyList()
        }
    }

    suspend fun insertTrip(trip: Trip) = withContext(Dispatchers.IO) {
        try {
            supabase.from("trips").insert(trip)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting trip", e)
        }
    }

    // --- Expenses ---
    suspend fun getExpenses(tripId: String): List<Expense> = withContext(Dispatchers.IO) {
        try {
            supabase.from("expenses").select {
                filter {
                    eq("trip_id", tripId)
                }
            }.decodeList<Expense>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching expenses", e)
            emptyList()
        }
    }

    suspend fun insertExpense(expense: Expense) = withContext(Dispatchers.IO) {
        try {
            supabase.from("expenses").insert(expense)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting expense", e)
        }
    }

    // --- Tasks (Checklist) ---
    suspend fun getTasks(tripId: String): List<Task> = withContext(Dispatchers.IO) {
        try {
            supabase.from("checklist").select {
                filter {
                    eq("trip_id", tripId)
                }
            }.decodeList<Task>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tasks", e)
            emptyList()
        }
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            supabase.from("checklist").insert(task)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting task", e)
        }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            supabase.from("checklist").update(task) {
                filter {
                    eq("id", task.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task", e)
        }
    }

    // --- Events (Itinerary) ---
    suspend fun getEvents(tripId: String): List<Event> = withContext(Dispatchers.IO) {
        try {
            supabase.from("events").select {
                filter {
                    eq("trip_id", tripId)
                }
            }.decodeList<Event>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events", e)
            emptyList()
        }
    }

    suspend fun insertEvent(event: Event) = withContext(Dispatchers.IO) {
        try {
            supabase.from("events").insert(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting event", e)
        }
    }

    // --- Notifications ---
    suspend fun getNotifications(): List<Notification> = withContext(Dispatchers.IO) {
        try {
            supabase.from("notifications").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Notification>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            emptyList()
        }
    }

    suspend fun insertNotification(notification: Notification) = withContext(Dispatchers.IO) {
        try {
            supabase.from("notifications").insert(notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting notification", e)
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        if (userId == "admin-user-id") {
            return@withContext UserProfile(
                id = "admin-user-id",
                name = "Admin Traveler",
                status = "Expert Explorer",
                email = "admin@gmail.com",
                tripsCount = 12,
                countriesCount = 5,
                budgetSpent = "₹45,000",
                profileImage = null
            )
        }

        try {
            supabase.from("user_profiles").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile", e)
            null
        }
    }

    suspend fun getUserProfileByEmail(email: String): UserProfile? = withContext(Dispatchers.IO) {
        try {
            supabase.from("user_profiles").select {
                filter {
                    eq("email", email)
                }
            }.decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile by email", e)
            null
        }
    }

    suspend fun insertUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            supabase.from("user_profiles").insert(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user profile", e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            supabase.from("user_profiles").update(profile) {
                filter {
                    eq("id", profile.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
        }
    }

    // --- Travel Memories ---
    suspend fun getTravelMemories(): List<TravelMemory> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext emptyList()
            supabase.from("travel_memories").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<TravelMemory>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching memories", e)
            emptyList()
        }
    }

    suspend fun insertMemory(memory: TravelMemory) = withContext(Dispatchers.IO) {
        try {
            supabase.from("travel_memories").insert(memory)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting memory", e)
        }
    }
}
