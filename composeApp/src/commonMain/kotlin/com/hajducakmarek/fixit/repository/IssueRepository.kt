package com.hajducakmarek.fixit.repository

import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.database.FixItDatabase
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole

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

    suspend fun seedUsers() {
        // Check if users already exist
        val existingUsers = dbQuery.selectAllUsers().executeAsList()
        if (existingUsers.isEmpty()) {
            // Add default users
            dbQuery.insertUser(
                id = "user-1",
                name = "John Smith",
                role = "MANAGER"
            )
            dbQuery.insertUser(
                id = "user-2",
                name = "Mike Johnson",
                role = "WORKER"
            )
            dbQuery.insertUser(
                id = "user-3",
                name = "Sarah Williams",
                role = "WORKER"
            )
        }
    }

    suspend fun getAllUsers(): List<User> {
        return dbQuery.selectAllUsers().executeAsList().map { dbUser ->
            User(
                id = dbUser.id,
                name = dbUser.name,
                role = UserRole.valueOf(dbUser.role)
            )
        }
    }

    suspend fun getUserById(id: String): User? {
        val dbUser = dbQuery.selectUserById(id).executeAsOneOrNull() ?: return null
        return User(
            id = dbUser.id,
            name = dbUser.name,
            role = UserRole.valueOf(dbUser.role)
        )
    }

    suspend fun insertUser(user: User) {
        dbQuery.insertUser(
            id = user.id,
            name = user.name,
            role = user.role.name
        )
    }

    suspend fun getWorkers(): List<User> {
        return dbQuery.selectUsersByRole("WORKER").executeAsList().map { dbUser ->
            User(
                id = dbUser.id,
                name = dbUser.name,
                role = UserRole.valueOf(dbUser.role)
            )
        }
    }

    suspend fun updateIssueAssignment(issueId: String, workerId: String?) {
        dbQuery.updateIssueAssignment(
            assignedTo = workerId,
            id = issueId
        )
    }
}
