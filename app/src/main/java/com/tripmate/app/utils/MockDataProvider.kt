package com.tripmate.app.utils

import com.tripmate.app.model.*
import java.util.Locale

object MockDataProvider {

    private var idCounter = 1000L
    private var currentUser = UserProfile("John Doe", "john.doe@example.com")

    private val trips = mutableListOf(
        Trip("1", "Summer in Paris", "Paris, France", "Aug 10, 2026", "Aug 20, 2026", "", 4),
        Trip("2", "Tokyo Adventure", "Tokyo, Japan", "Oct 05, 2026", "Oct 15, 2026", "", 2),
        Trip("3", "Weekend Getaway", "New York, USA", "Nov 12, 2026", "Nov 14, 2026", "", 3)
    )

    private val expensesByTrip = mutableMapOf(
        "1" to mutableListOf(
            Expense("e1", "Dinner at Eiffel", 150.0, "John Doe", "Aug 12, 2026", "Food"),
            Expense("e2", "Louvre Tickets", 40.0, "Jane Smith", "Aug 13, 2026", "Attraction")
        ),
        "2" to mutableListOf(
            Expense("e3", "Taxi to Hotel", 25.0, "John Doe", "Oct 05, 2026", "Transport")
        )
    )

    private val checklistByTrip = mutableMapOf(
        "1" to mutableListOf(
            ChecklistItem("c1", "Passports", true),
            ChecklistItem("c2", "Travel Insurance", true),
            ChecklistItem("c3", "Phone Chargers", false),
            ChecklistItem("c4", "Light Jackets", false)
        )
    )

    private val eventsByTrip = mutableMapOf(
        "1" to mutableListOf(
            Event("ev1", "Flight Arrival", "10:00 AM", "CDG Airport", "Arrive and take taxi to hotel"),
            Event("ev2", "Hotel Check-in", "02:00 PM", "Le Meurice", "Check into rooms and freshen up"),
            Event("ev3", "Eiffel Tower Visit", "06:00 PM", "Eiffel Tower", "Sunset view from the top")
        ),
        "2" to mutableListOf(
            Event("ev4", "Louvre Museum", "09:00 AM", "Louvre", "Morning art tour")
        )
    )

    private val membersByTrip = mutableMapOf(
        "1" to mutableListOf(
            Member("m1", "John Doe", "Organizer", ""),
            Member("m2", "Jane Smith", "Member", ""),
            Member("m3", "Alice Johnson", "Member", "")
        )
    )

    private val notifications = mutableListOf(
        AppNotification("n1", "New Expense Added", "Jane added Dinner for $50", "10 mins ago", false),
        AppNotification("n2", "Checklist Updated", "John completed Passports", "1 hour ago", true),
        AppNotification("n3", "Trip Reminder", "Paris trip starts in 2 days", "1 day ago", true)
    )

    fun getTrips(): List<Trip> = trips.toList()

    fun getTripById(tripId: String): Trip? = trips.firstOrNull { it.id == tripId }

    fun addTrip(destination: String, date: String, budget: String): Trip {
        val id = nextId()
        val title = "${destination.ifBlank { "New" }} Trip"
        val trip = Trip(
            id = id,
            title = title,
            destination = destination.ifBlank { "Unknown Destination" },
            startDate = date.ifBlank { "TBD" },
            endDate = date.ifBlank { "TBD" },
            imageUrl = "",
            memberCount = 1
        )
        trips.add(0, trip)
        if (budget.isNotBlank()) {
            addNotification("Trip Added", "${trip.title} created with budget $$budget")
        } else {
            addNotification("Trip Added", "${trip.title} added to your planner")
        }
        return trip
    }

    fun getExpenses(tripId: String): List<Expense> = expensesByTrip[tripId]?.toList() ?: emptyList()

    fun addExpense(tripId: String, title: String, amount: Double, paidBy: String): Expense {
        val expense = Expense(
            id = nextId(),
            title = title,
            amount = amount,
            paidBy = paidBy,
            date = "Today",
            category = "General"
        )
        val list = expensesByTrip.getOrPut(tripId) { mutableListOf() }
        list.add(0, expense)
        addNotification("New Expense", "$paidBy added $title for $${String.format(Locale.US, "%.2f", amount)}")
        return expense
    }

    fun totalExpenses(tripId: String): Double = getExpenses(tripId).sumOf { it.amount }

    fun getChecklistItems(tripId: String): List<ChecklistItem> {
        val items = checklistByTrip[tripId]?.toList() ?: emptyList()
        return items.sortedBy { it.isCompleted }
    }

    fun addChecklistItem(tripId: String, title: String): ChecklistItem {
        val item = ChecklistItem(nextId(), title, false)
        val list = checklistByTrip.getOrPut(tripId) { mutableListOf() }
        list.add(item)
        addNotification("Checklist Updated", "Added task: $title")
        return item
    }

    fun setChecklistCompleted(tripId: String, itemId: String, isCompleted: Boolean) {
        val list = checklistByTrip[tripId] ?: return
        list.firstOrNull { it.id == itemId }?.isCompleted = isCompleted
    }

    fun getEvents(tripId: String): List<Event> = eventsByTrip[tripId]?.toList() ?: emptyList()

    fun addEvent(tripId: String, title: String, date: String, time: String): Event {
        val event = Event(nextId(), title, time, date, "")
        val list = eventsByTrip.getOrPut(tripId) { mutableListOf() }
        list.add(event)
        addNotification("Event Added", "$title scheduled at $time")
        return event
    }

    fun getMembers(tripId: String): List<Member> = membersByTrip[tripId]?.toList() ?: emptyList()

    fun addMember(tripId: String, name: String): Member {
        val member = Member(nextId(), name, "Member", "")
        val list = membersByTrip.getOrPut(tripId) { mutableListOf() }
        list.add(member)
        return member
    }

    fun generateInviteCode(tripId: String): String {
        val suffix = tripId.takeLast(2).uppercase(Locale.US)
        return "TRIP$suffix${(1000..9999).random()}"
    }

    fun getCurrentUser(): UserProfile = currentUser.copy()

    fun updateCurrentUser(name: String, email: String) {
        currentUser = currentUser.copy(name = name, email = email)
    }

    fun getNotifications(): List<AppNotification> = notifications.toList()

    fun getItinerary(tripId: String): List<ItineraryDay> {
        val events = getEvents(tripId)
        if (events.isEmpty()) return emptyList()
        return events.groupBy { it.location }.entries.mapIndexed { index, entry ->
            ItineraryDay(
                id = "${tripId}_day_$index",
                dayTitle = "Day ${index + 1}",
                date = entry.key,
                events = entry.value
            )
        }
    }

    private fun addNotification(title: String, message: String) {
        notifications.add(0, AppNotification(nextId(), title, message, "just now", false))
    }

    private fun nextId(): String {
        idCounter += 1
        return idCounter.toString()
    }
}
