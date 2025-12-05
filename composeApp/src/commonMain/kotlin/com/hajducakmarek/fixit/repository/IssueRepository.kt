package com.hajducakmarek.fixit.repository

import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.database.FixItDatabase
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.models.Comment
import com.hajducakmarek.fixit.models.CommentWithUser

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

    // Comment operations
    suspend fun getCommentsByIssue(issueId: String): List<CommentWithUser> {
        return try {
            dbQuery.selectCommentsByIssue(issueId).executeAsList().map { commentData ->
                val comment = Comment(
                    id = commentData.id,
                    issueId = commentData.issueId,
                    userId = commentData.userId,
                    text = commentData.text,
                    createdAt = commentData.createdAt
                )
                val user = getUserById(commentData.userId) ?: User(
                    id = commentData.userId,
                    name = "Unknown User",
                    role = UserRole.WORKER
                )
                CommentWithUser(comment, user)
            }
        } catch (e: Exception) {
            throw Exception("Failed to load comments", e)
        }
    }

    suspend fun insertComment(comment: Comment) {
        try {
            dbQuery.insertComment(
                id = comment.id,
                issueId = comment.issueId,
                userId = comment.userId,
                text = comment.text,
                createdAt = comment.createdAt
            )
        } catch (e: Exception) {
            throw Exception("Failed to add comment", e)
        }
    }

    suspend fun deleteComment(commentId: String) {
        try {
            dbQuery.deleteComment(commentId)
        } catch (e: Exception) {
            throw Exception("Failed to delete comment", e)
        }
    }
}