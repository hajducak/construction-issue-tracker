package com.hajducakmarek.fixit.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val role: UserRole
)

@Serializable
enum class UserRole {
    MANAGER,
    WORKER
}