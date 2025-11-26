package com.hajducakmarek.fixit.models

data class User(
    val id: String,
    val name: String,
    val role: UserRole
)

enum class UserRole {
    MANAGER,
    WORKER
}