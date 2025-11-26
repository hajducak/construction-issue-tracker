package com.hajducakmarek.fixit.models

data class Issue(
    val id: String,
    val photoPath: String,
    val description: String,
    val flatNumber: String,
    val status: IssueStatus,
    val createdBy: String,
    val assignedTo: String?,
    val createdAt: Long,
    val completedAt: Long? = null
)

enum class IssueStatus {
    OPEN,
    IN_PROGRESS,
    FIXED,
    VERIFIED
}