package com.tripmate.app.utils

import android.content.Intent

object AppState {
    var selectedTripId: String? = null

    fun resolveTripId(intent: Intent): String {
        val fromIntent = intent.getStringExtra(NavKeys.EXTRA_TRIP_ID).orEmpty()
        if (fromIntent.isNotBlank()) {
            selectedTripId = fromIntent
            return fromIntent
        }

        val fromMemory = selectedTripId.orEmpty()
        if (fromMemory.isNotBlank()) {
            return fromMemory
        }

        val fallback = MockDataProvider.getTrips().firstOrNull()?.id.orEmpty()
        selectedTripId = fallback.takeIf { it.isNotBlank() }
        return fallback
    }
}
