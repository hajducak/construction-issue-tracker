package com.hajducakmarek.fixit.models

data class ActivityLog(
    val id: String,
    val issueId: String,
    val userId: String,
    val activityType: ActivityType,
    val oldValue: String?,
    val newValue: String?,
    val createdAt: Long
)

enum class ActivityType {
    CREATED,
    STATUS_CHANGED,
    ASSIGNED,
    UNASSIGNED,
    COMMENT_ADDED,
    COMMENT_DELETED,
    PHOTO_ADDED,
    PHOTO_DELETED
}

data class ActivityLogWithUser(
    val activity: ActivityLog,
    val user: User
)