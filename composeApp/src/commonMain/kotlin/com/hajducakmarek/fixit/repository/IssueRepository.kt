package com.hajducakmarek.fixit.repository

import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.database.FixItDatabase
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole

class IssueRepository(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = FixItDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.fixItDatabaseQueries

    suspend fun getAllIssues(): List<Issue> {
        return try {
            dbQuery.selectAllIssues().executeAsList().map { issue ->
                Issue(
                    id = issue.id,
                    photoPath = issue.photoPath,
                    description = issue.description,
                    flatNumber = issue.flatNumber,
                    status = IssueStatus.valueOf(issue.status),
                    createdBy = issue.createdBy,
                    assignedTo = issue.assignedTo,
                    createdAt = issue.createdAt,
                    completedAt = issue.completedAt
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load issues from database", e)
        }
    }

    suspend fun getIssueById(id: String): Issue? {
        return try {
            dbQuery.selectIssueById(id).executeAsOneOrNull()?.let { issue ->
                Issue(
                    id = issue.id,
                    photoPath = issue.photoPath,
                    description = issue.description,
                    flatNumber = issue.flatNumber,
                    status = IssueStatus.valueOf(issue.status),
                    createdBy = issue.createdBy,
                    assignedTo = issue.assignedTo,
                    createdAt = issue.createdAt,
                    completedAt = issue.completedAt
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load issue details", e)
        }
    }

    suspend fun insertIssue(issue: Issue) {
        try {
            dbQuery.insertIssue(
                id = issue.id,
                photoPath = issue.photoPath,
                description = issue.description,
                flatNumber = issue.flatNumber,
                status = issue.status.name,
                createdBy = issue.createdBy,
                assignedTo = issue.assignedTo,
                createdAt = issue.createdAt,
                completedAt = issue.completedAt
            )
        } catch (e: Exception) {
            throw Exception("Failed to create issue", e)
        }
    }

    suspend fun updateIssueStatus(issueId: String, status: IssueStatus) {
        try {
            dbQuery.updateIssueStatus(
                status = status.name,
                id = issueId
            )
        } catch (e: Exception) {
            throw Exception("Failed to update issue status", e)
        }
    }

    suspend fun updateIssueAssignment(issueId: String, workerId: String?) {
        try {
            dbQuery.updateIssueAssignment(
                assignedTo = workerId,
                id = issueId
            )
        } catch (e: Exception) {
            throw Exception("Failed to assign worker", e)
        }
    }

    // User operations
    suspend fun seedUsers() {
        try {
            val existingUsers = dbQuery.selectAllUsers().executeAsList()
            if (existingUsers.isEmpty()) {
                dbQuery.insertUser("manager-1", "John Smith", UserRole.MANAGER.name)
                dbQuery.insertUser("worker-1", "Mike Johnson", UserRole.WORKER.name)
                dbQuery.insertUser("worker-2", "Sarah Williams", UserRole.WORKER.name)
            }
        } catch (e: Exception) {
            throw Exception("Failed to seed users", e)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            dbQuery.selectAllUsers().executeAsList().map { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load users", e)
        }
    }

    suspend fun getUserById(id: String): User? {
        return try {
            dbQuery.selectUserById(id).executeAsOneOrNull()?.let { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load user details", e)
        }
    }

    suspend fun insertUser(user: User) {
        try {
            dbQuery.insertUser(
                id = user.id,
                name = user.name,
                role = user.role.name
            )
        } catch (e: Exception) {
            throw Exception("Failed to add user", e)
        }
    }

    suspend fun getWorkers(): List<User> {
        return try {
            dbQuery.selectUsersByRole(UserRole.WORKER.name).executeAsList().map { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load workers", e)
        }
    }
}