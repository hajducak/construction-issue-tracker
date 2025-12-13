package com.hajducakmarek.fixit.models

data class Issue(
    val id: String,
    val description: String,
    val flatNumber: String,
    val status: IssueStatus,
    val createdBy: String,
    val assignedTo: String?,
    val createdAt: Long,
    val completedAt: Long?,
    var priority: IssuePriority,
    val dueDate: Long?
)

enum class IssueStatus {
    OPEN,
    IN_PROGRESS,
    FIXED,
    VERIFIED
}
enum class IssuePriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}