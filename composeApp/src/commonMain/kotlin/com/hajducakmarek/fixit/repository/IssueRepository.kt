package com.hajducakmarek.fixit.repository

import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.database.FixItDatabase
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus

open class IssueRepository(databaseDriverFactory: DatabaseDriverFactory) {
    // Create database instance
    private val database = FixItDatabase(databaseDriverFactory.createDriver())

    // Get SQLDelight's generated query methods
    private val dbQuery = database.fixItDatabaseQueries

    // Get all issues from database
    // suspend = async
    open suspend fun getAllIssues(): List<Issue> {
        return dbQuery.selectAllIssues().executeAsList().map { dbIssue ->
            Issue(
                id = dbIssue.id,
                photoPath = dbIssue.photoPath,
                description = dbIssue.description,
                flatNumber = dbIssue.flatNumber,
                status = IssueStatus.valueOf(dbIssue.status),
                createdBy = dbIssue.createdBy,
                assignedTo = dbIssue.assignedTo,
                createdAt = dbIssue.createdAt,
                completedAt = dbIssue.completedAt
            )
        }
    }

    // Save a new issue
    suspend fun insertIssue(issue: Issue) {
        dbQuery.insertIssue(
            id = issue.id,
            photoPath = issue.photoPath,
            description = issue.description,
            flatNumber = issue.flatNumber,
            status = issue.status.name,  // Enum to String
            createdBy = issue.createdBy,
            assignedTo = issue.assignedTo,
            createdAt = issue.createdAt,
            completedAt = issue.completedAt
        )
    }

    open suspend fun getIssueById(id: String): Issue? {
        val dbIssue = dbQuery.selectIssueById(id).executeAsOneOrNull() ?: return null

        return Issue(
            id = dbIssue.id,
            photoPath = dbIssue.photoPath,
            description = dbIssue.description,
            flatNumber = dbIssue.flatNumber,
            status = IssueStatus.valueOf(dbIssue.status),
            createdBy = dbIssue.createdBy,
            assignedTo = dbIssue.assignedTo,
            createdAt = dbIssue.createdAt,
            completedAt = dbIssue.completedAt
        )
    }

    suspend fun updateIssueStatus(issueId: String, status: IssueStatus) {
        dbQuery.updateIssueStatus(
            status = status.name,
            id = issueId
        )
    }
}
