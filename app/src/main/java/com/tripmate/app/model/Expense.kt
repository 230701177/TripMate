package com.tripmate.app.model

import java.io.Serializable

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val paidBy: String,
    val date: String,
    val category: String = "General"
) : Serializable
