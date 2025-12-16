package com.hajducakmarek.fixit.utils

import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.Photo
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.CommentWithUser
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect class PdfExporter {
    suspend fun exportIssueToPdf(
        issue: Issue,
        photos: List<Photo>,
        comments: List<CommentWithUser>,
        creator: User,
        assignedWorker: User?
    ): String

    suspend fun exportAllIssuesToPdf(
        issues: List<Issue>
    ): String
}

// Common helper functions
object PdfHelper {
    fun formatDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    fun getPriorityText(priority: com.hajducakmarek.fixit.models.IssuePriority): String {
        return when (priority) {
            com.hajducakmarek.fixit.models.IssuePriority.LOW -> "🟢 LOW"
            com.hajducakmarek.fixit.models.IssuePriority.MEDIUM -> "🟡 MEDIUM"
            com.hajducakmarek.fixit.models.IssuePriority.HIGH -> "🟠 HIGH"
            com.hajducakmarek.fixit.models.IssuePriority.URGENT -> "🔴 URGENT"
        }
    }

    fun getStatusText(status: com.hajducakmarek.fixit.models.IssueStatus): String {
        return status.name.replace("_", " ")
    }
}