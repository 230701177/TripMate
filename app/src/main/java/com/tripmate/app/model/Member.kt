package com.tripmate.app.model

data class Member(
    val id: String,
    val name: String,
    val role: String,
    val avatarUrl: String = ""
)
