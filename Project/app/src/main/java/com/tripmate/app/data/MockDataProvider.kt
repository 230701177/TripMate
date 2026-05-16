package com.tripmate.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tripmate.app.models.*
import com.tripmate.app.network.SupabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

object MockDataProvider {
    val trips = mutableStateListOf<Trip>()
    val completedTrips = mutableStateListOf<Trip>()
    val expenses = mutableStateListOf<Expense>()
    val tasks = mutableStateListOf<Task>()
    val events = mutableStateListOf<Event>()
    val members = mutableStateListOf<Member>()
    val notifications = mutableStateListOf<Notification>()
    val travelMemories = mutableStateListOf<TravelMemory>()

    var isDarkMode by mutableStateOf(true)
    var activeNotification by mutableStateOf<String?>(null)
    var isErrorNotification by mutableStateOf(false)

    var currentUser by mutableStateOf(
        UserProfile(
            id = "guest",
            name = "Guest Traveler",
            status = "Please Login",
            email = "",
            tripsCount = 0,
            countriesCount = 0,
            budgetSpent = "₹0"
        )
    )

    private val scope = CoroutineScope(Dispatchers.Main)

    // Mock Data Definitions
    private val mockTrips = listOf(
        Trip(
            id = "t1",
            title = "Goa Trip",
            destination = "Goa",
            date = "Oct 10-15, 2026",
            budget = 15000.0,
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "GOA123"
        ),
        Trip(
            id = "t2",
            title = "Munnar Hills Escape",
            destination = "Munnar",
            date = "Dec 1-5, 2026",
            budget = 22000.0,
            imageUrl = "https://images.unsplash.com/photo-1472396961693-142e6e269027?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "MUNNAR"
        ),
        Trip(
            id = "t3",
            title = "Chennai Weekend Trip",
            destination = "Chennai",
            date = "Jan 10-12, 2026",
            budget = 8000.0,
            imageUrl = "https://images.unsplash.com/photo-1529253355930-ddbe423a2ac7?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "CHENNAI"
        )
    )

    private val mockCompletedTrips = listOf(
        Trip(
            id = "c1",
            title = "Goa Beach Escape",
            destination = "Goa",
            date = "Completed: Sep 18-22, 2025",
            budget = 18000.0,
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "GOA-DONE"
        ),
        Trip(
            id = "c2",
            title = "Jaipur Royal Tour",
            destination = "Jaipur",
            date = "Completed: Feb 3-7, 2025",
            budget = 21000.0,
            imageUrl = "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "JAIPUR-DONE"
        ),
        Trip(
            id = "c3",
            title = "Chennai Heritage Walk",
            destination = "Chennai",
            date = "Completed: Jan 2-4, 2025",
            budget = 9000.0,
            imageUrl = "https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80",
            inviteCode = "CHENNAI-DONE"
        )
    )

    private val mockExpenses = listOf(
        Expense("e1", "t1", "Beach Shacks", 2500.0, "Rahul"),
        Expense("e2", "t1", "Scooter Rental", 1500.0, "Rahul"),
        Expense("e3", "t1", "Water Sports", 5000.0, "Me"),
        Expense("e4", "t2", "Hotel Stay", 12000.0, "Me"),
        Expense("e5", "t2", "Tea Garden Entry", 2000.0, "Amit")
    )

    private val mockTasks = listOf(
        Task("c1", "t1", "Pack Swimwear", true),
        Task("c2", "t1", "Sunscreen", false),
        Task("c3", "t1", "First Aid Kit", true),
        Task("c4", "t2", "Woolen Sweater", true),
        Task("c5", "t2", "Hiking Boots", false)
    )

    private val mockEvents = listOf(
        Event("v1", "t1", "Arrive at Calangute", "10 Oct", "11:00 AM"),
        Event("v2", "t1", "Sunset at Anjuna", "11 Oct", "06:30 PM"),
        Event("v3", "t1", "Scuba Diving", "12 Oct", "08:00 AM"),
        Event("v4", "t2", "Tea Plantation Walk", "1 Dec", "09:00 AM"),
        Event("v5", "t2", "Echo Point Visit", "2 Dec", "04:00 PM"),
        Event("ev1", "t1", "Beach visit", "Oct 11", "10:00 AM"),
        Event("ev2", "t1", "Hotel check-in", "Oct 10", "2:00 PM"),
        Event("ev3", "t1", "Local sightseeing", "Oct 12", "9:00 AM")
    )

    private val mockMembers = listOf(
        Member("m1", "t1", "User A", "usera@tripmate.com", "Admin"),
        Member("m2", "t1", "User B", "userb@tripmate.com", "Member"),
        Member("m3", "t1", "User C", "userc@tripmate.com", "Member")
    )

    private val mockNotifications = listOf(
        Notification("n1", "t1", "Trip created successfully", "trip", System.currentTimeMillis().toString()),
        Notification("n2", "t1", "Expense added", "expense", System.currentTimeMillis().toString()),
        Notification("n3", "t1", "Task completed", "task", System.currentTimeMillis().toString()),
        Notification("n4", "t1", "New member joined", "member", System.currentTimeMillis().toString())
    )

    private val mockMemories = listOf(
        TravelMemory("tm1", "u1", "t1", "Goa Trip", "Goa sunset photo", "Goa", "", "Oct 2024"),
        TravelMemory("tm2", "u1", "t2", "Manali Escape", "Manali mountain view", "Manali", "", "Dec 2024"),
        TravelMemory("tm3", "u1", "t3", "Chennai Visit", "Chennai street food trip", "Chennai", "", "Jan 2025")
    )

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            // Load Trips
            val remoteTrips = SupabaseRepository.getTrips()
            trips.clear()
            if (remoteTrips.isEmpty()) {
                trips.addAll(mockTrips)
            } else {
                trips.addAll(remoteTrips)
            }

            completedTrips.clear()
            completedTrips.addAll(mockCompletedTrips)

            // Load Expenses, Tasks, Events
            expenses.clear()
            expenses.addAll(mockExpenses)

            tasks.clear()
            tasks.addAll(mockTasks)

            events.clear()
            events.addAll(mockEvents)

            // Load Profile
            val currentUserId = SupabaseRepository.getCurrentUserId()
            if (currentUserId != null) {
                val remoteProfile = SupabaseRepository.getUserProfile(currentUserId)
                if (remoteProfile != null) {
                    currentUser = remoteProfile.copy(tripsCount = completedTrips.size)
                } else {
                    // Fallback or create profile logic
                    currentUser = UserProfile(
                        id = currentUserId,
                        name = "New User",
                        status = "Travel Enthusiast",
                        email = "user@tripmate.com",
                        tripsCount = completedTrips.size,
                        countriesCount = 0,
                        budgetSpent = "₹0",
                        profileImage = null
                    )
                }
            }

            // Load Memories
            val remoteMemories = SupabaseRepository.getTravelMemories()
            travelMemories.clear()
            if (remoteMemories.isEmpty()) {
                travelMemories.addAll(mockMemories)
            } else {
                travelMemories.addAll(remoteMemories)
            }

            // Load Notifications
            val remoteNotifications = SupabaseRepository.getNotifications()
            notifications.clear()
            if (remoteNotifications.isEmpty()) {
                notifications.addAll(mockNotifications)
            } else {
                notifications.addAll(remoteNotifications)
            }

            members.clear()
            members.addAll(mockMembers)
        }
    }

    fun loadTripDetails(tripId: String) {
        scope.launch {
            val remoteExpenses = SupabaseRepository.getExpenses(tripId)
            expenses.clear()
            if (remoteExpenses.isEmpty()) {
                val matchingExpenses = mockExpenses.filter { e -> e.tripId == tripId }
                expenses.addAll(matchingExpenses.ifEmpty { mockExpenses.map { e -> e.copy(id = e.id + "x", tripId = tripId) } })
            } else {
                expenses.addAll(remoteExpenses)
            }

            val remoteTasks = SupabaseRepository.getTasks(tripId)
            tasks.clear()
            if (remoteTasks.isEmpty()) {
                val matchingTasks = mockTasks.filter { t -> t.tripId == tripId }
                tasks.addAll(matchingTasks.ifEmpty { mockTasks.map { t -> t.copy(id = t.id + "x", tripId = tripId) } })
            } else {
                tasks.addAll(remoteTasks)
            }

            val remoteEvents = SupabaseRepository.getEvents(tripId)
            events.clear()
            if (remoteEvents.isEmpty()) {
                val matchingEvents = mockEvents.filter { v -> v.tripId == tripId }
                events.addAll(matchingEvents.ifEmpty { mockEvents.map { v -> v.copy(id = v.id + "x", tripId = tripId) } })
            } else {
                events.addAll(remoteEvents)
            }
        }
    }

    fun addTrip(trip: Trip) {
        trips.add(0, trip)
        scope.launch {
            SupabaseRepository.insertTrip(trip)
            addNotification("New trip to ${trip.destination} created!", "trip")
        }
    }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
        scope.launch {
            SupabaseRepository.insertExpense(expense)
            addNotification("Expense added: ${expense.title}", "expense")
        }
    }

    fun addTask(task: Task) {
        tasks.add(task)
        scope.launch {
            SupabaseRepository.insertTask(task)
            addNotification("Task added: ${task.name}", "task")
        }
    }

    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            scope.launch {
                SupabaseRepository.updateTask(task)
            }
        }
    }

    fun addEvent(event: Event) {
        events.add(event)
        scope.launch {
            SupabaseRepository.insertEvent(event)
            addNotification("Event added: ${event.title}", "event")
        }
    }

    fun addMember(member: Member) {
        members.add(member)
        addNotification("${member.name} joined the trip", "trip")
    }

    fun addNotification(message: String, type: String) {
        val notif = Notification(
            id = System.currentTimeMillis().toString(),
            message = message,
            type = type,
            timestamp = System.currentTimeMillis().toString()
        )
        notifications.add(0, notif)
        scope.launch {
            SupabaseRepository.insertNotification(notif)
        }
    }

    fun updateProfile(profile: UserProfile) {
        currentUser = profile
        scope.launch {
            SupabaseRepository.updateUserProfile(profile)
        }
    }

    fun addMemory(memory: TravelMemory) {
        travelMemories.add(0, memory)
        scope.launch {
            SupabaseRepository.insertMemory(memory)
        }
    }

    fun showMessage(message: String, isError: Boolean = false) {
        scope.launch {
            activeNotification = message
            isErrorNotification = isError
            delay(3000)
            if (activeNotification == message) {
                activeNotification = null
            }
        }
    }
}
