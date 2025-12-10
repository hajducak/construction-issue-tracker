package com.hajducakmarek.fixit.models

data class DashboardStatistics(
    val totalIssues: Int,
    val openIssues: Int,
    val inProgressIssues: Int,
    val fixedIssues: Int,
    val verifiedIssues: Int,
    val totalWorkers: Int,
    val totalPhotos: Int,
    val totalComments: Int,
    val completionRate: Float
)

data class WorkerStatistics(
    val worker: User,
    val assignedIssues: Int,
    val completedIssues: Int
)

data class WorkerPersonalStatistics(
    val assignedToMe: Int,
    val completedByMe: Int,
    val openIssues: Int,
    val inProgressIssues: Int,
    val fixedIssues: Int,
    val completionRate: Float
)